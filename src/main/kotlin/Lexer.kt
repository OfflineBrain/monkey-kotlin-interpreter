class Lexer(val input: String) {
    var line = 0
    var readLine = 0
    var linePosition = 0
    var readLinePosition = 0

    var position = 0
    var readPosition = 0
    var ch = ' '


    init {
        readChar()
    }

    fun nextToken(): Token {
        skipWhitespace()
        return when (ch) {
            '=' -> {
                if (peekChar() == '=') {
                    readChar()
                    Token(TokenType.Eq, line, linePosition - 1)
                } else {
                    Token(TokenType.Assign, line, linePosition)
                }
            }

            '+' -> Token(TokenType.Plus, line, linePosition)
            '-' -> Token(TokenType.Minus, line, linePosition)
            '*' -> Token(TokenType.Multiply, line, linePosition)
            '/' -> Token(TokenType.Divide, line, linePosition)
            '(' -> Token(TokenType.LParen, line, linePosition)
            ')' -> Token(TokenType.RParen, line, linePosition)
            '{' -> Token(TokenType.LBrace, line, linePosition)
            '}' -> Token(TokenType.RBrace, line, linePosition)
            ';' -> Token(TokenType.Semicolon, line, linePosition)
            ':' -> Token(TokenType.Colon, line, linePosition)
            ',' -> Token(TokenType.Comma, line, linePosition)
            '.' -> Token(TokenType.Dot, line, linePosition)
            '!' -> {
                if (peekChar() == '=') {
                    readChar()
                    Token(TokenType.NotEq, line, linePosition - 1)
                } else {
                    Token(TokenType.Exclamation, line, linePosition)
                }
            }

            '<' -> {
                if (peekChar() == '=') {
                    readChar()
                    Token(TokenType.Lte, line, linePosition - 1)
                } else {
                    Token(TokenType.Lt, line, linePosition)
                }
            }

            '>' -> {
                if (peekChar() == '=') {
                    readChar()
                    Token(TokenType.Gte, line, linePosition - 1)
                } else {
                    Token(TokenType.Gt, line, linePosition)
                }
            }

            '?' -> Token(TokenType.Question, line, linePosition)
            '"' -> Token(TokenType.Quote, line, linePosition)
            0.toChar() -> Token(TokenType.EOF, line, linePosition)
            else -> {
                when {
                    isLetter(ch) || isUnderscore(ch) -> return readWord()
                    isDigit(ch) -> return readNumber()
                    else -> Token(TokenType.Illegal, line, linePosition)
                }
            }
        }.also {
            readChar()
        }

    }

    private fun readChar() {
        ch = if (readPosition >= input.length) {
            0.toChar()
        } else {
            input[readPosition]
        }
        position = readPosition
        readPosition += 1

        line = readLine
        linePosition = readLinePosition
        if (ch == '\n') {
            readLine += 1
            readLinePosition = 0
        } else {
            readLinePosition += 1
        }
    }

    private fun peekChar(): Char {
        return if (readPosition >= input.length) {
            0.toChar()
        } else {
            input[readPosition]
        }
    }

    private fun skipWhitespace() {
        while (ch.isWhitespace() or (ch.category == CharCategory.LINE_SEPARATOR)) {
            readChar()
        }
    }

    private fun readIdentifier(): String {
        val beginPosition = position
        while (isLetter(ch) || isUnderscore(ch)) {
            readChar()
        }
        return input.substring(beginPosition, position)
    }

    private fun readWord(): Token {
        val line = line
        val position = linePosition
        return when (val identifier = readIdentifier()) {
            "if" -> Token(TokenType.If, line, position)
            "else" -> Token(TokenType.Else, line, position)
            "for" -> Token(TokenType.For, line, position)
            "while" -> Token(TokenType.While, line, position)
            "return" -> Token(TokenType.Return, line, position)
            "let" -> Token(TokenType.Let, line, position)
            "fn" -> Token(TokenType.Function, line, position)
            "true" -> Token(TokenType.True, line, position)
            "false" -> Token(TokenType.False, line, position)
            else -> Token(TokenType.Identifier, line, position, identifier)
        }
    }

    private fun readNumber(): Token {
        val beginPosition = position
        val beingLine = line
        val beingLinePosition = linePosition

        while (isDigit(ch) || isUnderscore(ch)) {
            readChar()
        }

        if (isLetter(ch)) {
            return Token(TokenType.Illegal, beingLine, beingLinePosition)
        }

        return Token(TokenType.Number, beingLine, beingLinePosition, input.substring(beginPosition, position))
    }

    private fun isLetter(ch: Char): Boolean {
        return ch in 'a'..'z' || ch in 'A'..'Z'
    }

    private fun isUnderscore(ch: Char) = ch == '_'
    private fun isDot(ch: Char) = ch == '.'

    private fun isDigit(ch: Char): Boolean {
        return ch in '0'..'9'
    }
}