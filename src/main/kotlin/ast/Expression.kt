package ast

import token.Token

sealed interface Expression : Node

object Nothing : Expression {
    override fun tokenLiteral(): String {
        return ""
    }

    override fun render() = ""
}


sealed interface Identifier : Expression {
    val token: Token

    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun render(): String {
        return token.literal
    }

    data class Id(override val token: Token) : Identifier
    data class Invalid(override val token: Token) : Identifier
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

data class IfExpression(
    val token: Token,
    val condition: Expression,
    val consequence: Statement,
    val alternative: Statement? = null
) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun render(): String {
        val alternative = if (alternative != null) {
            " else ${alternative.render()}"
        } else {
            ""
        }
        return """if ${condition.render()}
            | ${consequence.render()}$alternative""".trimMargin()
    }
}

data class FunctionLiteral(
    val token: Token,
    val parameters: List<Identifier>,
    val body: Statement
) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun render(): String {
        return "${token.literal}(${parameters.joinToString(", ") { it.render() }}) ${body.render()}"
    }
}

data class CallExpression(
    val token: Token,
    val function: Expression,
    val arguments: List<Expression>
) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun render(): String {
        return "${function.render()}(${arguments.joinToString(", ") { it.render() }})"
    }
}