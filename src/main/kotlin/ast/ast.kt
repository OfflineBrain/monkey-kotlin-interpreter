package ast

import Token

sealed interface Node {
    fun tokenLiteral(): String

    fun render(): String
}

sealed interface Statement : Node

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
    val lastToken: Token,
    val expression: Expression
) : Statement {
    override fun tokenLiteral(): String {
        return lastToken.literal
    }

    override fun render(): String {
        return "${expression.render()};"
    }
}

data class BlockStatement(
    val token: Token,
    val statements: List<Statement>
) : Statement {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun render(): String {
        return "{\n${statements.joinToString(separator = "\n") { it.render() }}\n}"
    }
}

