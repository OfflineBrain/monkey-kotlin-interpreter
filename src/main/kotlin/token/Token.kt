package token

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
    ) : Token(Symbols.ASSIGN, line, position)

    class Plus(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.PLUS, line, position)

    class Minus(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.MINUS, line, position)

    class Asterisk(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.ASTERISK, line, position)

    class Slash(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.SLASH, line, position)

    class LParen(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.LPAREN, line, position)

    class RParen(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.RPAREN, line, position)

    class LBrace(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.LBRACE, line, position)

    class RBrace(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.RBRACE, line, position)

    class Semicolon(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.SEMICOLON, line, position)

    class Colon(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.COLON, line, position)

    class Comma(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.COMMA, line, position)

    class Dot(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.DOT, line, position)

    class Bang(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.BANG, line, position)

    class Question(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.QUESTION, line, position)

    class Quote(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.QUOTE, line, position)

    class Lt(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.LT, line, position)

    class Lte(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.LTE, line, position)

    class Gt(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.GT, line, position)

    class Gte(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.GTE, line, position)

    class Eq(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.EQ, line, position)

    class NotEq(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Symbols.NOT_EQ, line, position)

    class If(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Keywords.IF, line, position)

    class Else(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Keywords.ELSE, line, position)

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
    ) : Token(Keywords.RETURN, line, position)

    class Let(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Keywords.LET, line, position)

    class Function(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Keywords.FUNCTION, line, position)

    class True(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Keywords.TRUE, line, position)

    class False(
        override val line: Int = 0,
        override val position: Int = 0,
    ) : Token(Keywords.FALSE, line, position)

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