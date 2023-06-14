sealed class TokenType(val literal: String? = null) {
    // Identifiers + literals
    object Identifier : TokenType()
    object Number : TokenType()

    // Symbols
    object Assign : TokenType("=")
    object Plus : TokenType("+")
    object Minus : TokenType("-")
    object Multiply : TokenType("*")
    object Divide : TokenType("/")
    object LParen : TokenType("(")
    object RParen : TokenType(")")
    object LBrace : TokenType("{")
    object RBrace : TokenType("}")
    object Semicolon : TokenType(";")
    object Colon : TokenType(":")
    object Comma : TokenType(",")
    object Dot : TokenType(".")
    object Exclamation : TokenType("!")
    object Question : TokenType("?")
    object Quote : TokenType("\"")
    object Lt : TokenType("<")
    object Lte : TokenType("<=")
    object Gt : TokenType(">")
    object Gte : TokenType(">=")
    object Eq : TokenType("==")
    object NotEq : TokenType("!=")

    // Keywords
    object If : TokenType("if")
    object Else : TokenType("else")
    object For : TokenType("for")
    object While : TokenType("while")
    object Return : TokenType("return")
    object Let : TokenType("let")
    object Function : TokenType("fn")
    object True : TokenType("true")
    object False : TokenType("false")

    // Special
    object EOF : TokenType(0.toChar().toString())
    object Illegal : TokenType("Illegal")

    override fun toString(): String {
        return this::class.simpleName ?: ""
    }
}

data class Token(
    val type: TokenType,
    val line: Int = 0,
    val position: Int = 0,
    val literal: String = type.literal ?: throw Exception("No literal for token type $type")
) {
    fun render(): String {
        return "[\"$literal\" at $line:$position]"
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Token) {
            literal == other.literal
        } else {
            false
        }
    }
}