package token

class Lexer(private val input: String) {
    private var line = 0
    private var readLine = 0
    private var linePosition = 0
    private var readLinePosition = 0

    private var position = 0
    private var readPosition = 0
    private var ch = ' '


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
            '*' -> Token.Asterisk(line, linePosition)
            '/' -> Token.Slash(line, linePosition)
            '(' -> Token.LParen(line, linePosition)
            ')' -> Token.RParen(line, linePosition)
            '{' -> Token.LBrace(line, linePosition)
            '}' -> Token.RBrace(line, linePosition)
            ';' -> Token.Semicolon(line, linePosition)
            ':' -> Token.Colon(line, linePosition)
            ',' -> Token.Comma(line, linePosition)
            '.' -> Token.Dot(line, linePosition)
            '\\' -> Token.BackSlash(line, linePosition)
            '!' -> {
                if (peekChar() == '=') {
                    readChar()
                    Token.NotEq(line, linePosition - 1)
                } else {
                    Token.Bang(line, linePosition)
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

            '"' -> readString()

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
        ch = if (readPosition >= input.length || readPosition < 0) {
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
        return if (readPosition >= input.length || readPosition < 0) {
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
            Keywords.IF -> Token.If(line, position)
            Keywords.ELSE -> Token.Else(line, position)
            "for" -> Token.For(line, position)
            "while" -> Token.While(line, position)
            Keywords.RETURN -> Token.Return(line, position)
            Keywords.LET -> Token.Let(line, position)
            Keywords.FUNCTION -> Token.Function(line, position)
            Keywords.TRUE -> Token.True(line, position)
            Keywords.FALSE -> Token.False(line, position)
            else -> Token.Identifier(identifier, line, position)
        }
    }

    private fun readString(): Token {
        val line = line
        val linePosition = linePosition

        if (peekChar() == '"') {
            readChar()
            return Token.String("", line, linePosition)
        }
        val beginPosition = readPosition
        readChar()
        while (ch != '"') {
            if (ch == '\\') {
                readChar()
            }

            readChar()
        }
        val value = input.substring(beginPosition, position)
            .replace("\\\"", "\"")
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\\\", "\\")

        return Token.String(value, line, linePosition)
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

        return Token.Number(input.substring(beginPosition, position).toInt(), beingLine, beingLinePosition)
    }

    private fun isLetter(ch: Char): Boolean {
        return ch in 'a'..'z' || ch in 'A'..'Z' || ch == '_'
    }

    private fun isUnderscore(ch: Char) = ch == '_'
    private fun isDot(ch: Char) = ch == '.'

    private fun isDigit(ch: Char): Boolean {
        return ch in '0'..'9'
    }
}