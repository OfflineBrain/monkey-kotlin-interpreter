import io.kotest.core.spec.style.ExpectSpec
import kotlin.test.assertEquals

class LexerTest : ExpectSpec({
    context("a lexer") {

        context("valid input") {
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
                Token.Let(2, 9),
                Token.Identifier("five", 2, 13),
                Token.Assign(2, 18),
                Token.Number(5, 2, 20),
                Token.Semicolon(2, 21),
                Token.Let(3, 9),
                Token.Identifier("ten", 3, 13),
                Token.Assign(3, 17),
                Token.Number(10, 3, 19),
                Token.Semicolon(3, 21),
                Token.Let(4, 9),
                Token.Identifier("add", 4, 13),
                Token.Assign(4, 17),
                Token.Function(4, 19),
                Token.LParen(4, 21),
                Token.Identifier("x", 4, 22),
                Token.Comma(4, 23),
                Token.Identifier("y", 4, 25),
                Token.RParen(4, 26),
                Token.LBrace(4, 28),
                Token.Identifier("x", 5, 13),
                Token.Plus(5, 15),
                Token.Identifier("y", 5, 17),
                Token.Semicolon(5, 18),
                Token.RBrace(6, 9),
                Token.Semicolon(6, 10),
                Token.Let(7, 9),
                Token.Identifier("result", 7, 13),
                Token.Assign(7, 20),
                Token.Identifier("add", 7, 22),
                Token.LParen(7, 25),
                Token.Identifier("five", 7, 26),
                Token.Comma(7, 30),
                Token.Identifier("ten", 7, 32),
                Token.RParen(7, 35),
                Token.Semicolon(7, 36),
                Token.Exclamation(8, 9),
                Token.Minus(8, 10),
                Token.Divide(8, 11),
                Token.Multiply(8, 12),
                Token.Number(5, 8, 13),
                Token.Semicolon(8, 14),
                Token.Number(5, 9, 9),
                Token.Lt(9, 11),
                Token.Number(10, 9, 13),
                Token.Gt(9, 15),
                Token.Number(5, 9, 17),
                Token.Semicolon(9, 18),
                Token.If(11, 9),
                Token.LParen(11, 12),
                Token.Number(5, 11, 13),
                Token.Lt(11, 15),
                Token.Number(10, 11, 17),
                Token.RParen(11, 19),
                Token.LBrace(11, 21),
                Token.Return(12, 13),
                Token.True(12, 20),
                Token.Semicolon(12, 24),
                Token.RBrace(13, 9),
                Token.Else(13, 13),
                Token.LBrace(13, 18),
                Token.Return(14, 13),
                Token.False(14, 20),
                Token.Semicolon(14, 25),
                Token.RBrace(15, 9),
                Token.Number(10, 17, 9),
                Token.Eq(17, 12),
                Token.Number(10, 17, 14),
                Token.Semicolon(17, 16),
                Token.Number(10, 18, 9),
                Token.NotEq(18, 12),
                Token.Number(9, 18, 15),
                Token.Semicolon(18, 16),
                Token.EOF(19, 1)
            )
            val lexer = Lexer(input)

            expect.forEachIndexed { idx, token ->
                val tok = lexer.nextToken()

                expect("token[$idx] $token") {
                    assertEquals(token, tok, "token[$idx] [$tok] is not Assignual to expected token [$token]")
                }
            }
        }
    }
})