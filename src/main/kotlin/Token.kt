sealed class TokenType {
    // Identifiers + literals
    object Identifier : TokenType()
    object Number : TokenType()

    // Symbols
    object Assign : TokenType()
    object Plus : TokenType()
    object Minus : TokenType()
    object Multiply : TokenType()
    object Divide : TokenType()
    object LParen : TokenType()
    object RParen : TokenType()
    object LBrace : TokenType()
    object RBrace : TokenType()
    object Semicolon : TokenType()
    object Colon : TokenType()
    object Comma : TokenType()
    object Dot : TokenType()
    object Exclamation : TokenType()
    object Question : TokenType()
    object Quote : TokenType()
    object Lt : TokenType()
    object Lte : TokenType()
    object Gt : TokenType()
    object Gte : TokenType()
    object Eq : TokenType()
    object NotEq : TokenType()

    // Keywords
    object If : TokenType()
    object Else : TokenType()
    object For : TokenType()
    object While : TokenType()
    object Return : TokenType()
    object Let : TokenType()
    object Function : TokenType()
    object True : TokenType()
    object False : TokenType()

    // Special
    object EOF : TokenType()
    object Illegal : TokenType()
}

data class Token(val type: TokenType, val literal: String, val line: Int, val position: Int) {
    companion object {
        fun fromString(str: String, line: Int, position: Int): Token {
            return when (str) {
                "=" -> Token(TokenType.Assign, str, line, position)
                "+" -> Token(TokenType.Plus, str, line, position)
                "-" -> Token(TokenType.Minus, str, line, position)
                "*" -> Token(TokenType.Multiply, str, line, position)
                "/" -> Token(TokenType.Divide, str, line, position)
                "(" -> Token(TokenType.LParen, str, line, position)
                ")" -> Token(TokenType.RParen, str, line, position)
                "{" -> Token(TokenType.LBrace, str, line, position)
                "}" -> Token(TokenType.RBrace, str, line, position)
                ";" -> Token(TokenType.Semicolon, str, line, position)
                ":" -> Token(TokenType.Colon, str, line, position)
                "," -> Token(TokenType.Comma, str, line, position)
                "." -> Token(TokenType.Dot, str, line, position)
                "!" -> Token(TokenType.Exclamation, str, line, position)
                "?" -> Token(TokenType.Question, str, line, position)
                "\"" -> Token(TokenType.Quote, str, line, position)
                "<" -> Token(TokenType.Lt, str, line, position)
                "<=" -> Token(TokenType.Lte, str, line, position)
                ">" -> Token(TokenType.Gt, str, line, position)
                ">=" -> Token(TokenType.Gte, str, line, position)
                "==" -> Token(TokenType.Eq, str, line, position)
                "!=" -> Token(TokenType.NotEq, str, line, position)
                "if" -> Token(TokenType.If, str, line, position)
                "else" -> Token(TokenType.Else, str, line, position)
                "for" -> Token(TokenType.For, str, line, position)
                "while" -> Token(TokenType.While, str, line, position)
                "return" -> Token(TokenType.Return, str, line, position)
                "let" -> Token(TokenType.Let, str, line, position)
                "fn" -> Token(TokenType.Function, str, line, position)
                "true" -> Token(TokenType.True, str, line, position)
                "false" -> Token(TokenType.False, str, line, position)
                0.toChar().toString() -> Token(TokenType.EOF, str, line, position)
                else -> Token(TokenType.Identifier, str, line, position)
            }
        }

        fun number(str: String, line: Int, position: Int): Token {
            return Token(TokenType.Number, str, line, position)
        }

        fun illegal(line: Int, position: Int): Token {
            return Token(TokenType.Illegal, "", line, position)
        }
    }
}