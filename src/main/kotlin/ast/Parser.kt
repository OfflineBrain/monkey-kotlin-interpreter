package ast

import Lexer
import Token
import TokenType


typealias prefixParseFn = () -> Expression
typealias infixParseFn = (Expression) -> Expression

data class Parser(private val lexer: Lexer) {
    private var currToken: Token = Token(TokenType.EOF, 0, 0)
    private var peekToken: Token = Token(TokenType.EOF, 0, 0)

    val errors = mutableListOf<Error>()

    private val prefixParseFns = mutableMapOf<TokenType, prefixParseFn>()
    private val infixParseFns = mutableMapOf<TokenType, infixParseFn>()

    private val precedences = mapOf(
        TokenType.Eq to Precedence.EQUALS,
        TokenType.NotEq to Precedence.EQUALS,
        TokenType.Lt to Precedence.LESSGREATER,
        TokenType.Gt to Precedence.LESSGREATER,
        TokenType.Plus to Precedence.SUM,
        TokenType.Minus to Precedence.SUM,
        TokenType.Divide to Precedence.PRODUCT,
        TokenType.Multiply to Precedence.PRODUCT,
//        TokenType.LParen to Precedence.CALL,
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

        infixParseFns[TokenType.Plus] = ::parseInfixExpression
        infixParseFns[TokenType.Minus] = ::parseInfixExpression
        infixParseFns[TokenType.Multiply] = ::parseInfixExpression
        infixParseFns[TokenType.Divide] = ::parseInfixExpression
        infixParseFns[TokenType.Eq] = ::parseInfixExpression
        infixParseFns[TokenType.NotEq] = ::parseInfixExpression
        infixParseFns[TokenType.Lt] = ::parseInfixExpression
        infixParseFns[TokenType.Gt] = ::parseInfixExpression

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
        return errors.joinToString("\n") { it.message }
    }

    private fun peekPrecedence(): Precedence {
        return precedences[peekToken.type] ?: Precedence.LOWEST
    }

    private fun currPrecedence(): Precedence {
        return precedences[currToken.type] ?: Precedence.LOWEST
    }

    private fun parseIdentifier(): Identifier {
        return Identifier(currToken)
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
            Identifier(currToken).also {
                expectPeek(TokenType.Assign)
            }
        } else {
            Identifier(Token(TokenType.Illegal, currToken.line, currToken.position))
        }


        while (currToken.type !is TokenType.Semicolon) {
            nextToken()
        }

        return LetStatement(letToken, name, Nothing)
    }

    private fun parseReturnStatement(): ReturnStatement {
        val returnToken = currToken

        while (currToken.type !is TokenType.Semicolon) {
            nextToken()
        }

        return ReturnStatement(returnToken, Nothing)
    }

    private fun parseExpressionStatement(): ExpressionStatement {
        val expression = parseExpression(Precedence.LOWEST)
        if (peekToken.type is TokenType.Semicolon) {
            nextToken()
        }

        return ExpressionStatement(currToken, expression)
    }

    private fun parseExpression(precedence: Precedence): Expression {
        val prefix = prefixParseFns[currToken.type]
        if (prefix == null) {
            errors += Error.ParseError(
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
                errors += Error.ParseError(
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


    private inline fun <reified T : TokenType> expectPeek(type: T? = null): Boolean {
        return if (peekToken.type is T) {
            nextToken()
            true
        } else {
            errors += Error.UnexpectedToken(
                "expected '${type?.literal ?: T::class.simpleName}', but got '${peekToken.literal}'",
                peekToken.line,
                peekToken.position
            )
            false
        }
    }
}
