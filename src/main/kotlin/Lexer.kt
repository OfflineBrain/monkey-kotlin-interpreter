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
                    Token.fromString("==", line, linePosition - 1)
                } else {
                    Token.fromString("=", line, linePosition)
                }
            }

            '+' -> Token.fromString("+", line, linePosition)
            '-' -> Token.fromString("-", line, linePosition)
            '*' -> Token.fromString("*", line, linePosition)
            '/' -> Token.fromString("/", line, linePosition)
            '(' -> Token.fromString("(", line, linePosition)
            ')' -> Token.fromString(")", line, linePosition)
            '{' -> Token.fromString("{", line, linePosition)
            '}' -> Token.fromString("}", line, linePosition)
            ';' -> Token.fromString(";", line, linePosition)
            ':' -> Token.fromString("", line, linePosition)
            ',' -> Token.fromString(",", line, linePosition)
            '.' -> Token.fromString(".", line, linePosition)
            '!' -> {
                if (peekChar() == '=') {
                    readChar()
                    Token.fromString("!=", line, linePosition - 1)
                } else {
                    Token.fromString("!", line, linePosition)
                }
            }

            '<' -> {
                if (peekChar() == '=') {
                    readChar()
                    Token.fromString("<=", line, linePosition - 1)
                } else {
                    Token.fromString("<", line, linePosition)
                }
            }

            '>' -> {
                if (peekChar() == '=') {
                    readChar()
                    Token.fromString(">=", line, linePosition - 1)
                } else {
                    Token.fromString(">", line, linePosition)
                }
            }

            '?' -> Token.fromString("?", line, linePosition)
            '"' -> Token.fromString("\"", line, linePosition)
            0.toChar() -> Token.fromString(0.toChar().toString(), line, linePosition)
            else -> {
                when {
                    isLetter(ch) || isUnderscore(ch) -> return readWord()
                    isDigit(ch) -> return readNumber()
                    else -> Token.illegal(line, linePosition)
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
        return Token.fromString(readIdentifier(), beingLine, beingLinePosition)
    }

    private fun readNumber(): Token {
        val beginPosition = position
        val beingLine = line
        val beingLinePosition = linePosition

        while (isDigit(ch) || isUnderscore(ch)) {
            readChar()
        }

        if (isLetter(ch)) {
            return Token.illegal(beingLine, beingLinePosition)
        }

        return Token.number(input.substring(beginPosition, position), beingLine, beingLinePosition)
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