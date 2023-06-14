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
                Token(TokenType.Let, 0, 0),
                Token(TokenType.Identifier, 0, 5, "five"),
                Token(TokenType.Assign, 0, 10),
                Token(TokenType.Number, 0, 12, "5"),
                Token(TokenType.Semicolon, 0, 13),
                Token(TokenType.Let, 1, 0),
                Token(TokenType.Identifier, 1, 4, "ten"),
                Token(TokenType.Assign, 1, 8),
                Token(TokenType.Number, 1, 10, "10"),
                Token(TokenType.Semicolon, 1, 12),
                Token(TokenType.Let, 2, 0),
                Token(TokenType.Identifier, 2, 4, "add"),
                Token(TokenType.Assign, 2, 8),
                Token(TokenType.Function, 2, 10),
                Token(TokenType.LParen, 2, 12),
                Token(TokenType.Identifier, 2, 13, "x"),
                Token(TokenType.Comma, 2, 14),
                Token(TokenType.Identifier, 2, 16, "y"),
                Token(TokenType.RParen, 2, 17),
                Token(TokenType.LBrace, 2, 19),
                Token(TokenType.Identifier, 3, 4, "x"),
                Token(TokenType.Plus, 3, 6),
                Token(TokenType.Identifier, 3, 8, "y"),
                Token(TokenType.Semicolon, 3, 9),
                Token(TokenType.RBrace, 4, 0),
                Token(TokenType.Semicolon, 4, 1),
                Token(TokenType.Let, 5, 0),
                Token(TokenType.Identifier, 5, 4, "result"),
                Token(TokenType.Assign, 5, 11),
                Token(TokenType.Identifier, 5, 13, "add"),
                Token(TokenType.LParen, 5, 16),
                Token(TokenType.Identifier, 5, 17, "five"),
                Token(TokenType.Comma, 5, 21),
                Token(TokenType.Identifier, 5, 23, "ten"),
                Token(TokenType.RParen, 5, 26),
                Token(TokenType.Semicolon, 5, 27),
                Token(TokenType.Exclamation, 6, 0),
                Token(TokenType.Minus, 6, 1),
                Token(TokenType.Divide, 6, 2),
                Token(TokenType.Multiply, 6, 3),
                Token(TokenType.Number, 6, 4, "5"),
                Token(TokenType.Semicolon, 6, 5),
                Token(TokenType.Number, 7, 0, "5"),
                Token(TokenType.Lt, 7, 2),
                Token(TokenType.Number, 7, 4, "10"),
                Token(TokenType.Gt, 7, 7),
                Token(TokenType.Number, 7, 9, "5"),
                Token(TokenType.Semicolon, 7, 10),
                Token(TokenType.If, 9, 0),
                Token(TokenType.LParen, 9, 3),
                Token(TokenType.Number, 9, 4, "5"),
                Token(TokenType.Lt, 9, 6),
                Token(TokenType.Number, 9, 8, "10"),
                Token(TokenType.RParen, 9, 10),
                Token(TokenType.LBrace, 9, 12),
                Token(TokenType.Return, 10, 4),
                Token(TokenType.True, 10, 11),
                Token(TokenType.Semicolon, 10, 15),
                Token(TokenType.RBrace, 11, 0),
                Token(TokenType.Else, 11, 2),
                Token(TokenType.LBrace, 11, 7),
                Token(TokenType.Return, 12, 4),
                Token(TokenType.False, 12, 11),
                Token(TokenType.Semicolon, 12, 16),
                Token(TokenType.RBrace, 13, 0),
                Token(TokenType.Number, 15, 0, "10"),
                Token(TokenType.Eq, 15, 3),
                Token(TokenType.Number, 15, 6, "10"),
                Token(TokenType.Semicolon, 15, 8),
                Token(TokenType.Number, 16, 0, "10"),
                Token(TokenType.NotEq, 16, 3),
                Token(TokenType.Number, 16, 6, "9"),
                Token(TokenType.Semicolon, 16, 7),
                Token(TokenType.EOF, 16, 8)
            )

            val lexer = Lexer(input)

            expect.forEachIndexed { idx, token ->
                val tok = lexer.nextToken()

                expect("token[$idx] ${token.render()}") {
                    assertEquals(token, tok, "token[$idx] [$tok] is not Assignual to expected token [$token]")
                }
            }
        }
    }
})