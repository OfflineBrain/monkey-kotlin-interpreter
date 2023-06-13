import kotlin.test.Test
import kotlin.test.assertEquals

class LexerTest {
    @Test
    fun test() {
        val input = """
        let  five = 5;
        let ten = 10;
        let add = fn(x, y) {
            x + y;
        };
        let result = add(five, ten);
        !-/*5;
        5 < 10 > 5;
        
        if (5 < 10) {
            return true;
        } else {
            return false;
        }
        
        10 == 10;
        10 != 9;
        """.trimIndent()

        val expect = listOf(
            Token.fromString("let", 0, 0),
            Token.fromString("five", 0, 5),
            Token.fromString("=", 0, 10),
            Token.number("5", 0, 12),
            Token.fromString(";", 0, 13),
            Token.fromString("let", 1, 0),
            Token.fromString("ten", 1, 4),
            Token.fromString("=", 1, 8),
            Token.number("10", 1, 10),
            Token.fromString(";", 1, 12),
            Token.fromString("let", 2, 0),
            Token.fromString("add", 2, 4),
            Token.fromString("=", 2, 8),
            Token.fromString("fn", 2, 10),
            Token.fromString("(", 2, 12),
            Token.fromString("x", 2, 13),
            Token.fromString(",", 2, 14),
            Token.fromString("y", 2, 16),
            Token.fromString(")", 2, 17),
            Token.fromString("{", 2, 19),
            Token.fromString("x", 3, 4),
            Token.fromString("+", 3, 6),
            Token.fromString("y", 3, 8),
            Token.fromString(";", 3, 9),
            Token.fromString("}", 4, 0),
            Token.fromString(";", 4, 1),
            Token.fromString("let", 5, 0),
            Token.fromString("result", 5, 4),
            Token.fromString("=", 5, 11),
            Token.fromString("add", 5, 13),
            Token.fromString("(", 5, 16),
            Token.fromString("five", 5, 17),
            Token.fromString(",", 5, 21),
            Token.fromString("ten", 5, 23),
            Token.fromString(")", 5, 26),
            Token.fromString(";", 5, 27),
            Token.fromString("!", 6, 0),
            Token.fromString("-", 6, 1),
            Token.fromString("/", 6, 2),
            Token.fromString("*", 6, 3),
            Token.number("5", 6, 4),
            Token.fromString(";", 6, 5),
            Token.number("5", 7, 0),
            Token.fromString("<", 7, 2),
            Token.number("10", 7, 4),
            Token.fromString(">", 7, 7),
            Token.number("5", 7, 9),
            Token.fromString(";", 7, 10),
            Token.fromString("if", 9, 0),
            Token.fromString("(", 9, 3),
            Token.number("5", 9, 4),
            Token.fromString("<", 9, 6),
            Token.number("10", 9, 8),
            Token.fromString(")", 9, 10),
            Token.fromString("{", 9, 12),
            Token.fromString("return", 10, 4),
            Token.fromString("true", 10, 11),
            Token.fromString(";", 10, 15),
            Token.fromString("}", 11, 0),
            Token.fromString("else", 11, 2),
            Token.fromString("{", 11, 7),
            Token.fromString("return", 12, 4),
            Token.fromString("false", 12, 11),
            Token.fromString(";", 12, 16),
            Token.fromString("}", 13, 0),
            Token.number("10", 15, 0),
            Token.fromString("==", 15, 3),
            Token.number("10", 15, 6),
            Token.fromString(";", 15, 8),
            Token.number("10", 16, 0),
            Token.fromString("!=", 16, 3),
            Token.number("9", 16, 6),
            Token.fromString(";", 16, 7),
            Token.fromString(0.toChar().toString(), 16, 8)
        )

        val lexer = Lexer(input)

        expect.forEachIndexed { idx, token ->
            val tok = lexer.nextToken()
            assertEquals(token, tok, "token[$idx] [$tok] is not equal to expected token [$token]")
        }
    }
}