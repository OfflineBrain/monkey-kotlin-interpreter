sealed class Token(
    val literal: kotlin.String,
    open val line: Int = 0,
    open val position: Int = 0,
) {
    fun infoString(): kotlin.String {
        return "[\"$literal\" at $line:$position]"
    }

    override fun toString(): kotlin.String {
        return literal
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Token) {
            literal == other.literal
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return literal.hashCode()
    }

    class Identifier(
        val value: kotlin.String,
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(value, line, position)

    class Number(
        val value: Int,
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(value.toString(), line, position)

    class String(
        val value: kotlin.String,
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(value, line, position)

    class Assign(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("=", line, position)

    class Plus(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("+", line, position)

    class Minus(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("-", line, position)

    class Multiply(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("*", line, position)

    class Divide(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("/", line, position)

    class LParen(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("(", line, position)

    class RParen(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(")", line, position)

    class LBrace(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("{", line, position)

    class RBrace(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("}", line, position)

    class Semicolon(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(";", line, position)

    class Colon(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(":", line, position)

    class Comma(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(",", line, position)

    class Dot(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(".", line, position)

    class Exclamation(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("!", line, position)

    class Question(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("?", line, position)

    class Quote(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("\"", line, position)

    class Lt(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("<", line, position)

    class Lte(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("<=", line, position)

    class Gt(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(">", line, position)

    class Gte(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(">=", line, position)

    class Eq(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("==", line, position)

    class NotEq(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("!=", line, position)

    class If(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("if", line, position)

    class Else(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("else", line, position)

    class For(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("for", line, position)

    class While(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("while", line, position)

    class Return(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("return", line, position)

    class Let(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("let", line, position)

    class Function(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("fn", line, position)

    class True(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("true", line, position)

    class False(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("false", line, position)

    class Null(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("null", line, position)

    class EOF(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(0.toChar().toString(), line, position)

    class Illegal(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token("ILLEGAL", line, position)
}