package ast

import Token

sealed interface Node {
    fun tokenLiteral(): String
}

interface Statement : Node

interface Expression : Node

object Nothing : Expression {
    override fun tokenLiteral(): String {
        return ""
    }
}

enum class Precedence(val value: Int) {
    LOWEST(0),
    EQUALS(1),
    LESSGREATER(2),
    SUM(3),
    PRODUCT(4),
    PREFIX(5),
    CALL(6),
}

data class Program(val statements: List<Statement>) : Node {
    override fun tokenLiteral(): String {
        return if (statements.isNotEmpty()) {
            statements[0].tokenLiteral()
        } else {
            ""
        }
    }

    override fun toString(): String {
        return statements.joinToString(separator = "\n") { it.toString() }
    }
}

data class Identifier(val token: Token) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }
}

data class IntegerLiteral(val token: Token) : Expression {
    val value: Int = token.literal.toInt()

    override fun tokenLiteral(): String {
        return token.literal
    }
}

data class BooleanLiteral(val token: Token) : Expression {
    val value: Boolean = token.literal.toBoolean()

    override fun tokenLiteral(): String {
        return token.literal
    }
}

data class PrefixExpression(
    val token: Token,
    val right: Expression
) : Expression {
    val operator: String
        get() = token.literal

    override fun tokenLiteral(): String {
        return token.literal
    }
}

data class InfixExpression(
    val token: Token,
    val left: Expression,
    val right: Expression
) : Expression {
    val operator: String
        get() = token.literal

    override fun tokenLiteral(): String {
        return token.literal
    }
}

data class LetStatement(
    val token: Token,
    val name: Identifier,
    val value: Expression
) : Statement {
    override fun tokenLiteral(): String {
        return token.literal
    }
}

data class ReturnStatement(
    val token: Token,
    val value: Expression
) : Statement {
    override fun tokenLiteral(): String {
        return token.literal
    }
}

data class ExpressionStatement(
    val token: Token,
    val expression: Expression
) : Statement {
    override fun tokenLiteral(): String {
        return token.literal
    }
}

