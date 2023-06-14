package ast

import Token

interface Expression : Node

object Nothing : Expression {
    override fun tokenLiteral(): String {
        return ""
    }

    override fun render() = ""
}


data class Identifier(val token: Token) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun render(): String {
        return token.literal
    }
}

data class IntegerLiteral(val token: Token) : Expression {
    val value: Int = token.literal.toInt()

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun render(): String {
        return token.literal
    }
}

data class BooleanLiteral(val token: Token) : Expression {
    val value: Boolean = token.literal.toBoolean()

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun render(): String {
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

    override fun render(): String {
        return "($operator${right.render()})"
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

    override fun render(): String {
        return "(${left.render()} $operator ${right.render()})"
    }
}