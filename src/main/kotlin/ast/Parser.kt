package ast

import Lexer
import Token
import TokenType


typealias prefixParseFn = () -> Expression
typealias infixParseFn = (Expression) -> Expression

data class Parser(private val lexer: Lexer) {
    private var currToken: Token = Token.fromString(0.toChar().toString(), 0, 0)
    private var peekToken: Token = Token.fromString(0.toChar().toString(), 0, 0)

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

    data class Error(
        val message: String,
        val line: Int,
        val position: Int,
    )

    init {
        nextToken()
        nextToken()

        prefixParseFns[TokenType.Identifier] = ::parseIdentifier
        prefixParseFns[TokenType.Number] = ::parseIntegerLiteral
        prefixParseFns[TokenType.Exclamation] = ::parsePrefixExpression
        prefixParseFns[TokenType.Minus] = ::parsePrefixExpression

        infixParseFns[TokenType.Plus] = ::parseInfixExpression
        infixParseFns[TokenType.Minus] = ::parseInfixExpression
        infixParseFns[TokenType.Multiply] = ::parseInfixExpression
        infixParseFns[TokenType.Divide] = ::parseInfixExpression
        infixParseFns[TokenType.Eq] = ::parseInfixExpression
        infixParseFns[TokenType.NotEq] = ::parseInfixExpression
        infixParseFns[TokenType.Lt] = ::parseInfixExpression
        infixParseFns[TokenType.Gt] = ::parseInfixExpression

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

    fun parseProgram(): Program {
        val statements = mutableListOf<Statement>()
        while (currToken.type !is TokenType.EOF) {
            statements += parseStatement()
            nextToken()
        }
        return Program(statements)
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
                expectPeek<TokenType.Assign>()
            }
        } else {
            Identifier(Token.illegal(currToken.line, currToken.position))
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
            errors += Error(
                "no prefix parse function for ${currToken.type}",
                currToken.line,
                currToken.position
            )
            return Nothing
        }

        var leftExp = prefix()

        while (peekToken.type !is TokenType.Semicolon && precedence < peekPrecedence()) {
            val infix = infixParseFns[peekToken.type]
            if (infix == null) {
                errors += Error(
                    "no infix parse function for ${peekToken.type}",
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

        return PrefixExpression(token, token.literal, parseExpression(Precedence.PREFIX))
    }

    private fun parseInfixExpression(left: Expression): Expression {
        val token = currToken
        val precedence = currPrecedence()
        nextToken()
        return InfixExpression(token, left, token.literal, parseExpression(precedence))
    }


    private inline fun <reified T : TokenType> expectPeek(): Boolean {
        return if (peekToken.type is T) {
            nextToken()
            true
        } else {
            errors += Error(
                "expected ${T::class.simpleName}, but got ${peekToken::class.simpleName}",
                peekToken.line,
                peekToken.position
            )
            false
        }
    }
}
