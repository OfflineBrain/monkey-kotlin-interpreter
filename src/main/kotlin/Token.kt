sealed class Token(val literal: String, val line: Int, val position: Int) {
    class Identifier(val value: String, line: Int, position: Int) : Token(value, line, position)
    class Integer(val value: Int, line: Int, position: Int) : Token(value.toString(), line, position)
    class Long(val value: kotlin.Long, line: Int, position: Int) : Token(value.toString(), line, position)
    class Float(val value: kotlin.Float, line: Int, position: Int) : Token(value.toString(), line, position)
    class Double(val value: kotlin.Double, line: Int, position: Int) : Token(value.toString(), line, position)

    // Symbols
    class Assign(line: Int, position: Int) : Token("=", line, position)
    class Plus(line: Int, position: Int) : Token("+", line, position)
    class Minus(line: Int, position: Int) : Token("-", line, position)
    class Multiply(line: Int, position: Int) : Token("*", line, position)
    class Divide(line: Int, position: Int) : Token("/", line, position)
    class LParen(line: Int, position: Int) : Token("(", line, position)
    class RParen(line: Int, position: Int) : Token(")", line, position)
    class LBrace(line: Int, position: Int) : Token("{", line, position)
    class RBrace(line: Int, position: Int) : Token("}", line, position)
    class Semicolon(line: Int, position: Int) : Token(";", line, position)
    class Colon(line: Int, position: Int) : Token(":", line, position)
    class Comma(line: Int, position: Int) : Token(",", line, position)
    class Dot(line: Int, position: Int) : Token(".", line, position)
    class Exclamation(line: Int, position: Int) : Token("!", line, position)
    class Question(line: Int, position: Int) : Token("?", line, position)
    class Quote(line: Int, position: Int) : Token("\"", line, position)
    class Lt(line: Int, position: Int) : Token("<", line, position)
    class Lte(line: Int, position: Int) : Token("<=", line, position)
    class Gt(line: Int, position: Int) : Token(">", line, position)
    class Gte(line: Int, position: Int) : Token(">=", line, position)
    class Eq(line: Int, position: Int) : Token("==", line, position)
    class NotEq(line: Int, position: Int) : Token("!=", line, position)

    // Keywords
    class If(line: Int, position: Int) : Token("if", line, position)
    class Else(line: Int, position: Int) : Token("else", line, position)
    class For(line: Int, position: Int) : Token("for", line, position)
    class While(line: Int, position: Int) : Token("while", line, position)
    class Return(line: Int, position: Int) : Token("return", line, position)
    class Let(line: Int, position: Int) : Token("let", line, position)
    class Function(line: Int, position: Int) : Token("fn", line, position)
    class True(line: Int, position: Int) : Token("true", line, position)
    class False(line: Int, position: Int) : Token("false", line, position)


    // Special
    class EOF(line: Int, position: Int) : Token(0.toChar().toString(), line, position)
    class Illegal(line: Int, position: Int) : Token("ILLEGAL", line, position)

    override fun toString(): String {
        return "Token($literal, $line, $position)"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Token) return false
        return this::class == other::class && this.literal == other.literal && this.line == other.line && this.position == other.position
    }

    override fun hashCode(): Int {
        return this::class.hashCode() + this.literal.hashCode()
    }
}