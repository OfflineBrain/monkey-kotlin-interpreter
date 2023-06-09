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
            Token.Let(0, 0),
            Token.Identifier("five", 0, 5),
            Token.Assign(0, 10),
            Token.Integer(5, 0, 12),
            Token.Semicolon(0, 13),
            Token.Let(1, 0),
            Token.Identifier("ten", 1, 4),
            Token.Assign(1, 8),
            Token.Integer(10, 1, 10),
            Token.Semicolon(1, 12),
            Token.Let(2, 0),
            Token.Identifier("add", 2, 4),
            Token.Assign(2, 8),
            Token.Function(2, 10),
            Token.LParen(2, 12),
            Token.Identifier("x", 2, 13),
            Token.Comma(2, 14),
            Token.Identifier("y", 2, 16),
            Token.RParen(2, 17),
            Token.LBrace(2, 19),
            Token.Identifier("x", 3, 4),
            Token.Plus(3, 6),
            Token.Identifier("y", 3, 8),
            Token.Semicolon(3, 9),
            Token.RBrace(4, 0),
            Token.Semicolon(4, 1),
            Token.Let(5, 0),
            Token.Identifier("result", 5, 4),
            Token.Assign(5, 11),
            Token.Identifier("add", 5, 13),
            Token.LParen(5, 16),
            Token.Identifier("five", 5, 17),
            Token.Comma(5, 21),
            Token.Identifier("ten", 5, 23),
            Token.RParen(5, 26),
            Token.Semicolon(5, 27),
            Token.Exclamation(6, 0),
            Token.Minus(6, 1),
            Token.Divide(6, 2),
            Token.Multiply(6, 3),
            Token.Integer(5, 6, 4),
            Token.Semicolon(6, 5),
            Token.Integer(5, 7, 0),
            Token.Lt(7, 2),
            Token.Integer(10, 7, 4),
            Token.Gt(7, 7),
            Token.Integer(5, 7, 9),
            Token.Semicolon(7, 10),
            Token.If(9, 0),
            Token.LParen(9, 3),
            Token.Integer(5, 9, 4),
            Token.Lt(9, 6),
            Token.Integer(10, 9, 8),
            Token.RParen(9, 10),
            Token.LBrace(9, 12),
            Token.Return(10, 4),
            Token.True(10, 11),
            Token.Semicolon(10, 15),
            Token.RBrace(11, 0),
            Token.Else(11, 2),
            Token.LBrace(11, 7),
            Token.Return(12, 4),
            Token.False(12, 11),
            Token.Semicolon(12, 16),
            Token.RBrace(13, 0),
            Token.Integer(10, 15, 0),
            Token.Eq(15, 3),
            Token.Integer(10, 15, 6),
            Token.Semicolon(15, 8),
            Token.Integer(10, 16, 0),
            Token.NotEq(16, 3),
            Token.Integer(9, 16, 6),
            Token.Semicolon(16, 7),
            Token.EOF(16, 8)
        )

        val lexer = Lexer(input)

        expect.forEachIndexed { idx, token ->
            val tok = lexer.nextToken()
            assertEquals(token, tok, "token[$idx] [$tok] is not equal to expected token [$token]")
        }
    }
}