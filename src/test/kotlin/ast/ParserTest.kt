package ast

import Lexer
import Token
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {
    class LetStatementTest {
        @Test
        fun `test success`() {
            val input = """
            let x = 5;
            let y = 10;
            let foobar = 838383;
            """.trimIndent()

            val expected = listOf(
                Token.fromString("x", 0, 4) to Token.number("5", 0, 8),
                Token.fromString("y", 1, 4) to Token.number("10", 1, 8),
                Token.fromString("foobar", 2, 4) to Token.number("838383", 2, 13),
            )

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            assertEquals(3, program.statements.size)
            assertEquals(0, parser.errors.size)
            expected.forEachIndexed { i, expect ->
                assertEquals(
                    program.statements[i].tokenLiteral(),
                    "let",
                    "program.statements[$i].tokenLiteral() is not 'let'"
                )
                val statement = program.statements[i] as LetStatement
                assertEquals(
                    expect.first.literal,
                    statement.name.tokenLiteral(),
                    "program.statements[$i].name.token is not ${expect.first}"
                )
            }
        }

        @Test
        fun `test errors`() {
            val input = """
            let x = 5;
            let = 10;
            let foobar 838383;
            """.trimIndent()

            val expected = listOf(
                Token.fromString("x", 0, 4) to Token.number("5", 0, 8),
            )

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            assertEquals(3, program.statements.size)
            assertEquals(2, parser.errors.size)
            expected.forEachIndexed { i, expect ->
                assertEquals(
                    program.statements[i].tokenLiteral(),
                    "let",
                    "program.statements[$i].tokenLiteral() is not 'let'"
                )
                val statement = program.statements[i] as LetStatement
                assertEquals(
                    expect.first.literal,
                    statement.name.tokenLiteral(),
                    "program.statements[$i].name.token is not ${expect.first}"
                )
            }
        }
    }

    class ReturnStatementTest {
        @Test
        fun `test success`() {
            val input = """
            return 5;
            return 10;
            return 993322;
            """.trimIndent()

            val expected = listOf(
                Token.number("5", 0, 8),
                Token.number("10", 1, 8),
                Token.number("993322", 2, 8),
            )

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            assertEquals(3, program.statements.size)
            assertEquals(0, parser.errors.size)
            expected.forEachIndexed { i, expect ->
                assertEquals(
                    program.statements[i].tokenLiteral(),
                    "return",
                    "program.statements[$i].tokenLiteral() is not 'return'"
                )
            }
        }
    }

    class IdentifierExpressionTest {
        @Test
        fun `test success`() {
            val input = "foobar;"

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            assertEquals(1, program.statements.size)
            assertEquals(0, parser.errors.size)
            val statement = program.statements[0] as ExpressionStatement
            assertEquals(
                statement.expression.tokenLiteral(),
                "foobar",
                "statement.expression.tokenLiteral() is not 'foobar'"
            )
        }
    }

    class IntegerLiteralExpressionTest {
        @Test
        fun `test success`() {
            val input = "5;"

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            assertEquals(1, program.statements.size)
            assertEquals(0, parser.errors.size)
            val statement = program.statements[0] as ExpressionStatement
            val expected = statement.expression as IntegerLiteral
            assertEquals(
                expected.tokenLiteral(),
                "5",
                "statement.expression.tokenLiteral() is not '5'"
            )

            assertEquals(
                expected.value,
                5,
                "statement.expression.value is not 5"
            )
        }
    }

    class PrefixExpressionTest {
        @Test
        fun `test success`() {
            val input = """
            !5;
            -15;
            """.trimIndent()

            val expected = listOf(
                Token.fromString("!", 0, 1) to Token.number("5", 0, 2),
                Token.fromString("-", 1, 1) to Token.number("15", 1, 2),
            )

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            assertEquals(2, program.statements.size)
            assertEquals(0, parser.errors.size)
            expected.forEachIndexed { i, expect ->
                val statement = program.statements[i] as ExpressionStatement
                val expression = statement.expression as PrefixExpression
                assertEquals(
                    expression.operator,
                    expect.first.literal,
                    "expression.operator is not ${expect.first.literal}"
                )
                assertEquals(
                    expression.right.tokenLiteral(),
                    expect.second.literal,
                    "expression.right.tokenLiteral() is not ${expect.second.literal}"
                )
            }
        }

    }

    class InfixExpressionTest {
        @Test
        fun `test success`() {
            val input = """
            5 + 5;
            5 - 5;
            5 * 5;
            5 / 5;
            5 > 5;
            5 < 5;
            5 == 5;
            5 != 5;
            """.trimIndent()

            val expected = listOf(
                listOf(Token.number("5", 0, 2), Token.fromString("+", 0, 3), Token.number("5", 0, 4)),
                listOf(Token.number("5", 1, 2), Token.fromString("-", 1, 3), Token.number("5", 1, 4)),
                listOf(Token.number("5", 2, 2), Token.fromString("*", 2, 3), Token.number("5", 2, 4)),
                listOf(Token.number("5", 3, 2), Token.fromString("/", 3, 3), Token.number("5", 3, 4)),
                listOf(Token.number("5", 4, 2), Token.fromString(">", 4, 3), Token.number("5", 4, 4)),
                listOf(Token.number("5", 5, 2), Token.fromString("<", 5, 3), Token.number("5", 5, 4)),
                listOf(Token.number("5", 6, 2), Token.fromString("==", 6, 3), Token.number("5", 6, 5)),
                listOf(Token.number("5", 7, 2), Token.fromString("!=", 7, 3), Token.number("5", 7, 5)),
            )

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            assertEquals(8, program.statements.size)
            assertEquals(0, parser.errors.size)
            expected.forEachIndexed { i, expect ->
                val statement = program.statements[i] as ExpressionStatement
                val expression = statement.expression as InfixExpression
                assertEquals(
                    expression.left.tokenLiteral(),
                    expect[0].literal,
                    "expression.left.tokenLiteral() is not ${expect[0].literal}"
                )
                assertEquals(
                    expression.operator,
                    expect[1].literal,
                    "expression.operator is not ${expect[1].literal}"
                )
                assertEquals(
                    expression.right.tokenLiteral(),
                    expect[2].literal,
                    "expression.right.tokenLiteral() is not ${expect[2].literal}"
                )
            }
        }

        @Test
        fun `test operator precedence`() {
            val input = """
-a * b;
!-a;
a + b + c;
a + b - c;
a * b * c;
a * b / c;
a + b / c;
a + b * c + d / e - f;
3 + 4;
-5 * 5;

3 > 5 == false;
3 < 5 == true;

true == true;
false == false;
true != false;
            """.trimIndent()

            val expected = listOf(
                ExpressionStatement(
                    Token.fromString(";", 0, 6), InfixExpression(
                        Token.fromString("*", 0, 3),
                        PrefixExpression(
                            Token.fromString("-", 0, 0),
                            Identifier(Token.fromString("a", 0, 1)),
                        ),
                        Identifier(Token.fromString("b", 0, 5)),
                    )
                ),
                ExpressionStatement(
                    Token.fromString(";", 1, 3), PrefixExpression(
                        Token.fromString("!", 1, 0),
                        PrefixExpression(
                            Token.fromString("-", 1, 1),
                            Identifier(Token.fromString("a", 1, 2)),
                        ),
                    )
                ),
                ExpressionStatement(
                    Token.fromString(";", 2, 9), InfixExpression(
                        Token.fromString("+", 2, 6),
                        InfixExpression(
                            Token.fromString("+", 2, 2),
                            Identifier(Token.fromString("a", 2, 0)),
                            Identifier(Token.fromString("b", 2, 4)),
                        ),
                        Identifier(Token.fromString("c", 2, 8)),
                    )
                ),
                ExpressionStatement(
                    Token.fromString(";", 3, 9), InfixExpression(
                        Token.fromString("-", 3, 6),
                        InfixExpression(
                            Token.fromString("+", 3, 2),
                            Identifier(Token.fromString("a", 3, 0)),
                            Identifier(Token.fromString("b", 3, 4)),
                        ),
                        Identifier(Token.fromString("c", 3, 8)),
                    )
                ),
                ExpressionStatement(
                    Token.fromString(";", 4, 9), InfixExpression(
                        Token.fromString("*", 4, 6),
                        InfixExpression(
                            Token.fromString("*", 4, 2),
                            Identifier(Token.fromString("a", 4, 0)),
                            Identifier(Token.fromString("b", 4, 4)),
                        ),
                        Identifier(Token.fromString("c", 4, 8)),
                    )
                ),
                ExpressionStatement(
                    Token.fromString(";", 5, 9), InfixExpression(
                        Token.fromString("/", 5, 6),
                        InfixExpression(
                            Token.fromString("*", 5, 2),
                            Identifier(Token.fromString("a", 5, 0)),
                            Identifier(Token.fromString("b", 5, 4)),
                        ),
                        Identifier(Token.fromString("c", 5, 8)),
                    )
                ),
                ExpressionStatement(
                    Token.fromString(";", 6, 9), InfixExpression(
                        Token.fromString("+", 6, 2),
                        Identifier(Token.fromString("a", 6, 0)),
                        InfixExpression(
                            Token.fromString("/", 6, 6),
                            Identifier(Token.fromString("b", 6, 4)),
                            Identifier(Token.fromString("c", 6, 8)),
                        ),
                    )
                ),
                //a + b * c + d / e - f;
                ExpressionStatement(
                    Token.fromString(";", 7, 21), InfixExpression(
                        Token.fromString("-", 7, 18),
                        InfixExpression(
                            Token.fromString("+", 7, 10),
                            InfixExpression(
                                Token.fromString("+", 7, 2),
                                Identifier(Token.fromString("a", 7, 0)),
                                InfixExpression(
                                    Token.fromString("*", 7, 6),
                                    Identifier(Token.fromString("b", 7, 4)),
                                    Identifier(Token.fromString("c", 7, 8)),
                                ),
                            ),
                            InfixExpression(
                                Token.fromString("/", 7, 14),
                                Identifier(Token.fromString("d", 7, 12)),
                                Identifier(Token.fromString("e", 7, 16)),
                            )
                        ),
                        Identifier(Token.fromString("f", 7, 20)),
                    )
                ),
                ExpressionStatement(
                    Token.fromString(";", 8, 5), InfixExpression(
                        Token.fromString("+", 8, 2),
                        IntegerLiteral(Token.number("3", 8, 0)),
                        IntegerLiteral(Token.number("4", 8, 4)),
                    )
                ),
                ExpressionStatement(
                    Token.fromString(";", 9, 6), InfixExpression(
                        Token.fromString("*", 9, 3),
                        PrefixExpression(
                            Token.fromString("-", 9, 0),
                            IntegerLiteral(Token.number("5", 9, 1)),
                        ),
                        IntegerLiteral(Token.number("5", 9, 5)),
                    )
                ),
                ExpressionStatement(
                    Token.fromString(";", 11, 14), InfixExpression(
                        Token.fromString("==", 11, 6),
                        InfixExpression(
                            Token.fromString(">", 11, 2),
                            IntegerLiteral(Token.number("3", 11, 0)),
                            IntegerLiteral(Token.number("5", 11, 4)),
                        ),
                        BooleanLiteral(Token.fromString("false", 11, 9)),
                    )
                ),
                ExpressionStatement(
                    Token.fromString(";", 12, 13), InfixExpression(
                        Token.fromString("==", 12, 6),
                        InfixExpression(
                            Token.fromString("<", 12, 2),
                            IntegerLiteral(Token.number("3", 12, 0)),
                            IntegerLiteral(Token.number("5", 12, 4)),
                        ),
                        BooleanLiteral(Token.fromString("true", 12, 9)),
                    )
                ),
                ExpressionStatement(
                    Token.fromString(";", 14, 12), InfixExpression (
                        Token.fromString("==", 14, 5),
                        BooleanLiteral(Token.fromString("true", 14, 0)),
                        BooleanLiteral(Token.fromString("true", 14, 8)),
                    )
                ),
                ExpressionStatement(
                    Token.fromString(";", 15, 14), InfixExpression(
                        Token.fromString("==", 15, 6),
                        BooleanLiteral(Token.fromString("false", 15, 0)),
                        BooleanLiteral(Token.fromString("false", 15, 9)),
                    )
                ),
                ExpressionStatement(
                    Token.fromString(";", 16, 13), InfixExpression(
                        Token.fromString("!=", 16, 5),
                        BooleanLiteral(Token.fromString("true", 16, 0)),
                        BooleanLiteral(Token.fromString("false", 16, 8)),
                    )
                ),
            )

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            assertEquals(expected.size, program.statements.size)
            assertEquals(0, parser.errors.size)

            expected.forEachIndexed { index, statement ->


                assertEquals(statement, program.statements[index], "program.statements[$index] is not $statement")
            }
        }
    }

    class TestBooleanLiteral {
        @Test
        fun `test boolean literal`() {
            val input = """
                    true;
                    false;
                    """.trimIndent()

            val expected = listOf(
                ExpressionStatement(Token.fromString(";", 0, 4), BooleanLiteral(Token.fromString("true", 0, 0))),
                ExpressionStatement(Token.fromString(";", 1, 5), BooleanLiteral(Token.fromString("false", 1, 0))),
            )

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            assertEquals(2, program.statements.size)
            assertEquals(0, parser.errors.size)

            expected.forEachIndexed { index, statement ->


                assertEquals(statement, program.statements[index], "program.statements[$index] is not $statement")
            }
        }
    }

}