package ast

import token.Lexer
import token.Token
import kotlin.reflect.KClass


typealias PrefixParseFn = () -> Expression
typealias InfixParseFn = (Expression) -> Expression

data class Parser(private val lexer: Lexer) {
    private var currToken: Token = Token.EOF(0, 0)
    private var peekToken: Token = Token.EOF(0, 0)

    private val _errors = mutableListOf<Error>()
    val errors: List<Error>
        get() = _errors

    private val prefixParseFns = mutableMapOf<KClass<out Token>, PrefixParseFn>()
    private val infixParseFns = mutableMapOf<KClass<out Token>, InfixParseFn>()

    enum class Precedence(val value: Int) {
        LOWEST(0),
        EQUALS(1),
        LESSGREATER(2),
        SUM(3),
        PRODUCT(4),
        PREFIX(5),
        CALL(6),
    }

    private val precedences = mapOf(
        Token.Eq::class to Precedence.EQUALS,
        Token.NotEq::class to Precedence.EQUALS,
        Token.Lt::class to Precedence.LESSGREATER,
        Token.Gt::class to Precedence.LESSGREATER,
        Token.Plus::class to Precedence.SUM,
        Token.Minus::class to Precedence.SUM,
        Token.Slash::class to Precedence.PRODUCT,
        Token.Asterisk::class to Precedence.PRODUCT,
        Token.LParen::class to Precedence.CALL,
    )

    sealed interface Error {
        val message: String
        val line: Int
        val position: Int

        data class UnexpectedToken(
            override val message: String,
            override val line: Int,
            override val position: Int
        ) : Error

        data class ParseError(
            override val message: String,
            override val line: Int,
            override val position: Int
        ) : Error
    }


    init {
        nextToken()
        nextToken()

        prefixParseFns[Token.Identifier::class] = ::parseIdentifier
        prefixParseFns[Token.Number::class] = ::parseIntegerLiteral
        prefixParseFns[Token.True::class] = ::parseBooleanLiteral
        prefixParseFns[Token.False::class] = ::parseBooleanLiteral
        prefixParseFns[Token.Bang::class] = ::parsePrefixExpression
        prefixParseFns[Token.Minus::class] = ::parsePrefixExpression
        prefixParseFns[Token.LParen::class] = ::parseGroupedExpression
        prefixParseFns[Token.If::class] = ::parseIfExpression
        prefixParseFns[Token.Function::class] = ::parseFunctionLiteral
        prefixParseFns[Token.String::class] = ::parseStringLiteral

        infixParseFns[Token.Plus::class] = ::parseInfixExpression
        infixParseFns[Token.Minus::class] = ::parseInfixExpression
        infixParseFns[Token.Asterisk::class] = ::parseInfixExpression
        infixParseFns[Token.Slash::class] = ::parseInfixExpression
        infixParseFns[Token.Eq::class] = ::parseInfixExpression
        infixParseFns[Token.NotEq::class] = ::parseInfixExpression
        infixParseFns[Token.Lt::class] = ::parseInfixExpression
        infixParseFns[Token.Lte::class] = ::parseInfixExpression
        infixParseFns[Token.Gt::class] = ::parseInfixExpression
        infixParseFns[Token.Gte::class] = ::parseInfixExpression
        infixParseFns[Token.LParen::class] = ::parseCallExpression
    }

    fun parseProgram(): Program {
        val statements = mutableListOf<Statement>()
        while (currToken !is Token.EOF) {
            statements += parseStatement()
            nextToken()
        }
        return Program(statements)
    }

    fun errors(): String {
        return errors.joinToString("\n") { "${it.message} at [${it.line}:${it.position}]" }
    }

    private fun peekPrecedence(): Precedence {
        return precedences[peekToken::class] ?: Precedence.LOWEST
    }

    private fun currPrecedence(): Precedence {
        return precedences[currToken::class] ?: Precedence.LOWEST
    }

    private fun parseIdentifier(): Identifier {
        return Identifier.Id(currToken)
    }

    private fun parseIntegerLiteral(): IntegerLiteral {
        return IntegerLiteral(currToken)
    }

    private fun parseBooleanLiteral(): BooleanLiteral {
        return BooleanLiteral(currToken)
    }

    private fun parseStringLiteral(): StringLiteral {
        return StringLiteral(currToken)
    }

    private fun nextToken() {
        currToken = peekToken
        peekToken = lexer.nextToken()
    }

    private fun parseStatement(): Statement {
        return when (currToken) {
            is Token.Let -> parseLetStatement()
            is Token.Return -> parseReturnStatement()
            is Token.LBrace -> parseBlockStatement()
            else -> parseExpressionStatement()
        }
    }

    private fun parseLetStatement(): LetStatement {
        val letToken = currToken

        val name = if (expectPeek<Token.Identifier>()) {
            Identifier.Id(currToken)
        } else {
            Identifier.Invalid(currToken)
        }

        expectPeek<Token.Assign>()
        nextToken()

        val expression = parseExpressionStatement()

        return LetStatement(letToken, name, expression.expression)
    }

    private fun parseReturnStatement(): ReturnStatement {
        val returnToken = currToken
        nextToken()

        val expression = parseExpressionStatement()

        return ReturnStatement(returnToken, expression.expression)
    }

    private fun parseExpressionStatement(): ExpressionStatement {
        val expression = parseExpression(Precedence.LOWEST)
        if (peekToken is Token.Semicolon) {
            nextToken()
        }

        return ExpressionStatement(currToken, expression)
    }

    private fun parseBlockStatement(): BlockStatement {
        val token = currToken

        val statements = mutableListOf<Statement>()

        nextToken()

        while (currToken !is Token.RBrace && currToken !is Token.EOF) {
            statements += parseStatement()
            nextToken()
        }

        return BlockStatement(token, statements)
    }

    private fun parseExpression(precedence: Precedence): Expression {
        val prefix = prefixParseFns[currToken::class]
        if (prefix == null) {
            _errors += Error.ParseError(
                "no PREFIX parse function for ${currToken}",
                currToken.line,
                currToken.position
            )
            return Nothing
        }

        var leftExp = prefix()

        while (peekToken !is Token.Semicolon && precedence < peekPrecedence()) {
            val infix = infixParseFns[peekToken::class]
            if (infix == null) {
                _errors += Error.ParseError(
                    "no INFIX parse function for ${peekToken}",
                    peekToken.line,
                    peekToken.position
                )
                return leftExp
            }

            nextToken()

            leftExp = infix(leftExp)
        }

        return leftExp
    }

    private fun parsePrefixExpression(): PrefixExpression {
        val token = currToken

        nextToken()

        return PrefixExpression(token, parseExpression(Precedence.PREFIX))
    }

    private fun parseInfixExpression(left: Expression): Expression {
        val token = currToken
        val precedence = currPrecedence()
        nextToken()
        return InfixExpression(token, left, parseExpression(precedence))
    }

    private fun parseGroupedExpression(): Expression {
        nextToken()
        val exp = parseExpression(Precedence.LOWEST)
        expectPeek<Token.RParen>()

        return exp
    }

    private fun parseIfExpression(): Expression {
        val token = currToken

        expectPeek<Token.LParen>()

        nextToken()

        val condition = parseExpression(Precedence.LOWEST)

        expectPeek<Token.RParen>()

        expectPeek<Token.LBrace>()

        val consequence = parseStatement()

        if (peekToken is Token.Else) {
            nextToken()

            !expectPeek<Token.LBrace>()

            val alternative = parseStatement()

            return IfExpression(token, condition, consequence, alternative)
        }

        return IfExpression(token, condition, consequence, null)
    }

    private fun parseFunctionLiteral(): Expression {
        val token = currToken

        expectPeek<Token.LParen>()

        val parameters = parseFunctionParameters()

        expectPeek<Token.LBrace>()

        val body = parseStatement()

        return FunctionLiteral(token, parameters, body)
    }

    private fun parseFunctionParameters(): List<Identifier> {
        val identifiers = mutableListOf<Identifier>()

        if (peekToken is Token.RParen) {
            nextToken()
            return identifiers
        }

        nextToken()

        identifiers += parseIdentifier()

        while (peekToken is Token.Comma) {
            nextToken()
            nextToken()
            identifiers += parseIdentifier()
        }

        !expectPeek<Token.RParen>()

        return identifiers
    }

    private fun parseCallExpression(function: Expression): Expression {
        val token = currToken
        val arguments = parseCallArguments()
        return CallExpression(token, function, arguments)
    }

    private fun parseCallArguments(): List<Expression> {
        val arguments = mutableListOf<Expression>()

        if (peekToken is Token.RParen) {
            nextToken()
            return arguments
        }

        nextToken()

        arguments += parseExpression(Precedence.LOWEST)

        while (peekToken is Token.Comma) {
            nextToken()
            nextToken()
            arguments += parseExpression(Precedence.LOWEST)
        }

        !expectPeek<Token.RParen>()

        return arguments
    }

    private inline fun <reified T : Token> expectPeek(type: T? = null): Boolean {
        return if (peekToken is T) {
            nextToken()
            true
        } else {
            _errors += Error.UnexpectedToken(
                "expected '${type?.literal ?: T::class.simpleName}', but got '${peekToken.literal}'",
                peekToken.line,
                peekToken.position
            )
            false
        }
    }
}
