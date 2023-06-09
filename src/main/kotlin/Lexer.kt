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
                    Token.Eq(line, linePosition - 1)
                } else {
                    Token.Assign(line, linePosition)
                }
            }

            '+' -> Token.Plus(line, linePosition)
            '-' -> Token.Minus(line, linePosition)
            '*' -> Token.Multiply(line, linePosition)
            '/' -> Token.Divide(line, linePosition)
            '(' -> Token.LParen(line, linePosition)
            ')' -> Token.RParen(line, linePosition)
            '{' -> Token.LBrace(line, linePosition)
            '}' -> Token.RBrace(line, linePosition)
            ';' -> Token.Semicolon(line, linePosition)
            ':' -> Token.Colon(line, linePosition)
            ',' -> Token.Comma(line, linePosition)
            '.' -> Token.Dot(line, linePosition)
            '!' -> {
                if (peekChar() == '=') {
                    readChar()
                    Token.NotEq(line, linePosition - 1)
                } else {
                    Token.Exclamation(line, linePosition)
                }
            }

            '<' -> {
                if (peekChar() == '=') {
                    readChar()
                    Token.Lte(line, linePosition - 1)
                } else {
                    Token.Lt(line, linePosition)
                }
            }

            '>' -> {
                if (peekChar() == '=') {
                    readChar()
                    Token.Gte(line, linePosition - 1)
                } else {
                    Token.Gt(line, linePosition)
                }
            }

            '?' -> Token.Question(line, linePosition)
            '"' -> Token.Quote(line, linePosition)
            0.toChar() -> Token.EOF(line, linePosition)
            else -> {
                when {
                    isLetter(ch) || isUnderscore(ch) -> return readWord()
                    isDigit(ch) -> return readNumber()
                    else -> Token.Illegal(line, linePosition)
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
        val beingLine = line
        val beingLinePosition = linePosition
        return when (val identifier = readIdentifier()) {
            "let" -> Token.Let(beingLine, beingLinePosition)
            "fn" -> Token.Function(beingLine, beingLinePosition)
            "true" -> Token.True(beingLine, beingLinePosition)
            "false" -> Token.False(beingLine, beingLinePosition)
            "if" -> Token.If(beingLine, beingLinePosition)
            "else" -> Token.Else(beingLine, beingLinePosition)
            "return" -> Token.Return(beingLine, beingLinePosition)
            "for" -> Token.For(beingLine, beingLinePosition)
            "while" -> Token.While(beingLine, beingLinePosition)

            else -> Token.Identifier(identifier, beingLine, beingLinePosition)
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
            return Token.Illegal(beingLine, beingLinePosition)
        }

        val int = input.substring(beginPosition, position).replace("_", "").toInt()
        return Token.Integer(int, beingLine, beingLinePosition)
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