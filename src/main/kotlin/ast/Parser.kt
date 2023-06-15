package ast

import Lexer
import Token
import TokenType


typealias PrefixParseFn = () -> Expression
typealias InfixParseFn = (Expression) -> Expression

data class Parser(private val lexer: Lexer) {
    private var currToken: Token = Token(TokenType.Illegal, 0, 0)
    private var peekToken: Token = Token(TokenType.Illegal, 0, 0)

    private val _errors = mutableListOf<Error>()
    val errors: List<Error>
        get() = _errors

    private val prefixParseFns = mutableMapOf<TokenType, PrefixParseFn>()
    private val infixParseFns = mutableMapOf<TokenType, InfixParseFn>()

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
        TokenType.Eq to Precedence.EQUALS,
        TokenType.NotEq to Precedence.EQUALS,
        TokenType.Lt to Precedence.LESSGREATER,
        TokenType.Gt to Precedence.LESSGREATER,
        TokenType.Plus to Precedence.SUM,
        TokenType.Minus to Precedence.SUM,
        TokenType.Divide to Precedence.PRODUCT,
        TokenType.Multiply to Precedence.PRODUCT,
        TokenType.LParen to Precedence.CALL,
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

        prefixParseFns[TokenType.Identifier] = ::parseIdentifier
        prefixParseFns[TokenType.Number] = ::parseIntegerLiteral
        prefixParseFns[TokenType.True] = ::parseBooleanLiteral
        prefixParseFns[TokenType.False] = ::parseBooleanLiteral
        prefixParseFns[TokenType.Exclamation] = ::parsePrefixExpression
        prefixParseFns[TokenType.Minus] = ::parsePrefixExpression
        prefixParseFns[TokenType.LParen] = ::parseGroupedExpression
        prefixParseFns[TokenType.If] = ::parseIfExpression
        prefixParseFns[TokenType.Function] = ::parseFunctionLiteral

        infixParseFns[TokenType.Plus] = ::parseInfixExpression
        infixParseFns[TokenType.Minus] = ::parseInfixExpression
        infixParseFns[TokenType.Multiply] = ::parseInfixExpression
        infixParseFns[TokenType.Divide] = ::parseInfixExpression
        infixParseFns[TokenType.Eq] = ::parseInfixExpression
        infixParseFns[TokenType.NotEq] = ::parseInfixExpression
        infixParseFns[TokenType.Lt] = ::parseInfixExpression
        infixParseFns[TokenType.Gt] = ::parseInfixExpression
        infixParseFns[TokenType.LParen] = ::parseCallExpression
    }

    fun parseProgram(): Program {
        val statements = mutableListOf<Statement>()
        while (currToken.type !is TokenType.EOF) {
            statements += parseStatement()
            nextToken()
        }
        return Program(statements)
    }

    fun errors(): String {
        return errors.joinToString("\n") { "${it.message} at [${it.line}:${it.position}]" }
    }

    private fun peekPrecedence(): Precedence {
        return precedences[peekToken.type] ?: Precedence.LOWEST
    }

    private fun currPrecedence(): Precedence {
        return precedences[currToken.type] ?: Precedence.LOWEST
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

    private fun nextToken() {
        currToken = peekToken
        peekToken = lexer.nextToken()
    }

    private fun parseStatement(): Statement {
        return when (currToken.type) {
            is TokenType.Let -> parseLetStatement()
            is TokenType.Return -> parseReturnStatement()
            else -> parseExpressionStatement()
        }
    }

    private fun parseLetStatement(): LetStatement {
        val letToken = currToken

        val name = if (expectPeek<TokenType.Identifier>()) {
            Identifier.Id(currToken)
        } else {
            Identifier.Invalid(currToken)
        }

        expectPeek(TokenType.Assign)
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
        if (peekToken.type is TokenType.Semicolon) {
            nextToken()
        }

        return ExpressionStatement(currToken, expression)
    }

    private fun parseBlockStatement(): BlockStatement {
        val token = currToken

        val statements = mutableListOf<Statement>()

        nextToken()

        while (currToken.type !is TokenType.RBrace && currToken.type !is TokenType.EOF) {
            statements += parseStatement()
            nextToken()
        }

        return BlockStatement(token, statements)
    }

    private fun parseExpression(precedence: Precedence): Expression {
        val prefix = prefixParseFns[currToken.type]
        if (prefix == null) {
            _errors += Error.ParseError(
                "no PREFIX parse function for ${currToken.type}",
                currToken.line,
                currToken.position
            )
            return Nothing
        }

        var leftExp = prefix()

        while (peekToken.type !is TokenType.Semicolon && precedence < peekPrecedence()) {
            val infix = infixParseFns[peekToken.type]
            if (infix == null) {
                _errors += Error.ParseError(
                    "no INFIX parse function for ${peekToken.type}",
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
        expectPeek(TokenType.RParen)

        return exp
    }

    private fun parseIfExpression(): Expression {
        val token = currToken

        !expectPeek(TokenType.LParen)

        nextToken()

        val condition = parseExpression(Precedence.LOWEST)

        expectPeek(TokenType.RParen)

        expectPeek(TokenType.LBrace)

        val consequence = parseBlockStatement()

        if (peekToken.type is TokenType.Else) {
            nextToken()

            !expectPeek(TokenType.LBrace)

            val alternative = parseBlockStatement()

            return IfExpression(token, condition, consequence, alternative)
        }

        return IfExpression(token, condition, consequence, null)
    }

    private fun parseFunctionLiteral(): Expression {
        val token = currToken

        expectPeek(TokenType.LParen)

        val parameters = parseFunctionParameters()

        expectPeek(TokenType.LBrace)

        val body = parseBlockStatement()

        return FunctionLiteral(token, parameters, body)
    }

    private fun parseFunctionParameters(): List<Identifier> {
        val identifiers = mutableListOf<Identifier>()

        if (peekToken.type is TokenType.RParen) {
            nextToken()
            return identifiers
        }

        nextToken()

        identifiers += parseIdentifier()

        while (peekToken.type is TokenType.Comma) {
            nextToken()
            nextToken()
            identifiers += parseIdentifier()
        }

        !expectPeek(TokenType.RParen)

        return identifiers
    }

    private fun parseCallExpression(function: Expression): Expression {
        val token = currToken
        val arguments = parseCallArguments()
        return CallExpression(token, function, arguments)
    }

    private fun parseCallArguments(): List<Expression> {
        val arguments = mutableListOf<Expression>()

        if (peekToken.type is TokenType.RParen) {
            nextToken()
            return arguments
        }

        nextToken()

        arguments += parseExpression(Precedence.LOWEST)

        while (peekToken.type is TokenType.Comma) {
            nextToken()
            nextToken()
            arguments += parseExpression(Precedence.LOWEST)
        }

        !expectPeek(TokenType.RParen)

        return arguments
    }

    private inline fun <reified T : TokenType> expectPeek(type: T? = null): Boolean {
        return if (peekToken.type is T) {
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
