package ast

import Token

sealed interface Node {
    fun tokenLiteral(): String

    fun render(): String
}

interface Statement : Node


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

    override fun render(): String {
        return statements.joinToString(separator = "\n") { it.render() }
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

    override fun render(): String {
        return "${tokenLiteral()} ${name.render()} = ${value.render()};"
    }
}

data class ReturnStatement(
    val token: Token,
    val value: Expression
) : Statement {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun render(): String {
        return "${tokenLiteral()} ${value.render()};"
    }
}

data class ExpressionStatement(
    val token: Token,
    val expression: Expression
) : Statement {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun render(): String {
        return "${expression.render()};"
    }
}

