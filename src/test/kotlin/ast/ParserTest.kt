package ast

import Lexer
import Token
import TokenType
import io.kotest.core.spec.style.ExpectSpec
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ParserTest : ExpectSpec({

    fun assertLiteral(expected: Token, actual: Token) {
        assertEquals(
            expected.literal, actual.literal,
            "expected: ${expected.literal}, actual: ${actual.literal}",
        )
    }

    fun assertLiteral(expected: String, actual: String) {
        assertEquals(
            expected, actual,
            "expected: $expected, actual: $actual",
        )
    }

    context("a parser") {

        context("parse let statement") {

            context("happy path") {
                val input = """
                let x = 5;
                let y = 10;
                let foobar = 838383;
                """.trimIndent()

                val expected = listOf(
                    Token(TokenType.Identifier, 0, 4, "x") to Token(TokenType.Number, 0, 8, "5"),
                    Token(TokenType.Identifier, 1, 4, "y") to Token(TokenType.Number, 1, 8, "10"),
                    Token(TokenType.Identifier, 2, 4, "foobar") to Token(TokenType.Number, 2, 13, "838383"),
                )

                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("should parse successfully") {
                    assertEquals(3, program.statements.size)
                    assertEquals(0, parser.errors.size)
                }


                expected.forEachIndexed { i, expect ->
                    expect("should parse let statement: $i") {
                        val statement = program.statements[i] as LetStatement
                        assertLiteral(expect.first, statement.name.token)
                    }
                }
            }

            context("input with missing tokens") {
                val input = """
                let x = 5;
                let = 10;
                let foobar 838383;
                """.trimIndent()

                val expected = listOf(
                    Token(TokenType.Identifier, 0, 4, "x") to Token(TokenType.Number, 0, 8, "5"),
                )

                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("contains parse errors") {
                    assertEquals(2, parser.errors.size)
                }

                expect("contains parsed statements") {
                    assertEquals(3, program.statements.size)
                }

                expected.forEachIndexed { i, expect ->
                    expect("should parse let statement: $i") {
                        val statement = program.statements[i] as LetStatement
                        assertLiteral(expect.first, statement.name.token)
                    }
                }
            }
        }

        context("parse return statement") {
            val input = """
            return 5;
            return 10;
            return 993322;
            """.trimIndent()

            val expected = listOf(
                Token(TokenType.Return, 0, 0),
                Token(TokenType.Return, 1, 0),
                Token(TokenType.Return, 2, 0),
            )

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            expect("should parse successfully") {
                assertEquals(3, program.statements.size)
                assertEquals(0, parser.errors.size)
            }

            expected.forEachIndexed { i, expect ->
                expect("should parse return statement: $i") {
                    assertTrue(program.statements[i] is ReturnStatement)

                    val statement = program.statements[i] as ReturnStatement

                    assertLiteral(expect, statement.token)
                }
            }
        }

        context("parse identifier expression") {
            val input = "foobar;"

            val expected = listOf(
                Token(TokenType.Identifier, 0, 6, "foobar"),
            )

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            expect("should parse successfully") {
                assertEquals(1, program.statements.size)
                assertEquals(0, parser.errors.size)
            }

            expected.forEachIndexed { i, token ->
                expect("should parse identifier expression: $i") {
                    assertTrue(program.statements[i] is ExpressionStatement)

                    val statement = program.statements[i] as ExpressionStatement

                    assertLiteral(
                        statement.expression.tokenLiteral(),
                        token.literal,
                    )
                }
            }
        }

        context("parse integer literal expression") {
            val input = "5;"

            val expected = listOf(
                Token(TokenType.Number, 0, 0, "5"),
            )

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            expect("should parse successfully") {
                assertEquals(1, program.statements.size)
                assertEquals(0, parser.errors.size)
            }

            expected.forEachIndexed { i, token ->
                expect("should parse integer literal expression: $i") {
                    assertTrue(program.statements[i] is ExpressionStatement)

                    val statement = program.statements[i] as ExpressionStatement

                    assertLiteral(
                        statement.expression.tokenLiteral(),
                        token.literal,
                    )
                }
            }
        }

        context("parse prefix expression") {
            val input = """
            !5;
            -15;
            !true;
            !false;
            """.trimIndent()

            val expected = listOf(
                Token(TokenType.Exclamation, 0, 0) to Token(TokenType.Number, 0, 1, "5"),
                Token(TokenType.Minus, 1, 0) to Token(TokenType.Number, 1, 1, "15"),
                Token(TokenType.Exclamation, 2, 0) to Token(TokenType.True, 2, 1),
                Token(TokenType.Exclamation, 3, 0) to Token(TokenType.False, 3, 1),
            )

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            expect("should parse successfully") {
                assertEquals(expected.size, program.statements.size)
                assertEquals(0, parser.errors.size)
            }

            expected.forEachIndexed { i, token ->
                expect("should parse prefix expression: $i") {
                    assertTrue(program.statements[i] is ExpressionStatement)

                    val statement = program.statements[i] as ExpressionStatement

                    assertLiteral(
                        statement.expression.tokenLiteral(),
                        token.first.literal,
                    )

                    assertLiteral(
                        (statement.expression as PrefixExpression).right.tokenLiteral(),
                        token.second.literal,
                    )
                }
            }
        }

        context("parse infix expression") {
            context("single operator") {
                val data = listOf(
                    "5 + 5;" to listOf(
                        Token(TokenType.Number, 0, 0, "5"),
                        Token(TokenType.Plus, 0, 2),
                        Token(TokenType.Number, 0, 4, "5"),
                    ),
                    "5 - 5;" to listOf(
                        Token(TokenType.Number, 0, 0, "5"),
                        Token(TokenType.Minus, 0, 2),
                        Token(TokenType.Number, 0, 4, "5"),
                    ),
                    "5 * 5;" to listOf(
                        Token(TokenType.Number, 0, 0, "5"),
                        Token(TokenType.Multiply, 0, 2),
                        Token(TokenType.Number, 0, 4, "5"),
                    ),
                    "5 / 5;" to listOf(
                        Token(TokenType.Number, 0, 0, "5"),
                        Token(TokenType.Divide, 0, 2),
                        Token(TokenType.Number, 0, 4, "5"),
                    ),
                    "5 > 5;" to listOf(
                        Token(TokenType.Number, 0, 0, "5"),
                        Token(TokenType.Gt, 0, 2),
                        Token(TokenType.Number, 0, 4, "5"),
                    ),
                    "5 < 5;" to listOf(
                        Token(TokenType.Number, 0, 0, "5"),
                        Token(TokenType.Lt, 0, 2),
                        Token(TokenType.Number, 0, 4, "5"),
                    ),
                    "5 == 5;" to listOf(
                        Token(TokenType.Number, 0, 0, "5"),
                        Token(TokenType.Eq, 0, 2),
                        Token(TokenType.Number, 0, 5, "5"),
                    ),
                    "5 != 5;" to listOf(
                        Token(TokenType.Number, 0, 0, "5"),
                        Token(TokenType.NotEq, 0, 2),
                        Token(TokenType.Number, 0, 5, "5"),
                    ),
                )
                data.forEach { (input, expected) ->
                    context("with input $input") {
                        val lexer = Lexer(input)
                        val parser = Parser(lexer)
                        val program = parser.parseProgram()

                        expect("should parse successfully") {
                            assertEquals(1, program.statements.size)
                            assertEquals(0, parser.errors.size)
                        }


                        context("should parse infix expression") {
                            val expression =
                                (program.statements[0] as ExpressionStatement).expression as InfixExpression

                            expect("left operand " + expected[0].literal) {
                                assertLiteral(expression.left.tokenLiteral(), expected[0].literal)
                            }
                            expect("operator " + expected[1].literal) {
                                assertLiteral(expression.operator, expected[1].literal)
                            }
                            expect("right operand " + expected[2].literal) {
                                assertLiteral(expression.right.tokenLiteral(), expected[2].literal)
                            }
                        }
                    }
                }
            }

            context("has precedence") {
                val data = listOf(
                    "-a * b;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 6), InfixExpression(
                            Token(TokenType.Multiply, 0, 3),
                            PrefixExpression(
                                Token(TokenType.Minus, 0, 0),
                                Identifier(Token(TokenType.Identifier, 0, 1, "a")),
                            ),
                            Identifier(Token(TokenType.Identifier, 0, 5, "b")),
                        )
                    ),
                    "!-a;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 3), PrefixExpression(
                            Token(TokenType.Exclamation, 0, 0),
                            PrefixExpression(
                                Token(TokenType.Minus, 0, 1),
                                Identifier(Token(TokenType.Identifier, 0, 2, "a")),
                            ),
                        )
                    ),
                    "a + b + c;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 8), InfixExpression(
                            Token(TokenType.Plus, 0, 3),
                            InfixExpression(
                                Token(TokenType.Plus, 0, 1),
                                Identifier(Token(TokenType.Identifier, 0, 0, "a")),
                                Identifier(Token(TokenType.Identifier, 0, 2, "b")),
                            ),
                            Identifier(Token(TokenType.Identifier, 0, 6, "c")),
                        )
                    ),
                    "a + b - c;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 8), InfixExpression(
                            Token(TokenType.Minus, 0, 3),
                            InfixExpression(
                                Token(TokenType.Plus, 0, 1),
                                Identifier(Token(TokenType.Identifier, 0, 0, "a")),
                                Identifier(Token(TokenType.Identifier, 0, 2, "b")),
                            ),
                            Identifier(Token(TokenType.Identifier, 0, 6, "c")),
                        )
                    ),
                    "a * b * c;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 8), InfixExpression(
                            Token(TokenType.Multiply, 0, 3),
                            InfixExpression(
                                Token(TokenType.Multiply, 0, 1),
                                Identifier(Token(TokenType.Identifier, 0, 0, "a")),
                                Identifier(Token(TokenType.Identifier, 0, 2, "b")),
                            ),
                            Identifier(Token(TokenType.Identifier, 0, 6, "c")),
                        )
                    ),
                    "a * b / c;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 8), InfixExpression(
                            Token(TokenType.Divide, 0, 3),
                            InfixExpression(
                                Token(TokenType.Multiply, 0, 1),
                                Identifier(Token(TokenType.Identifier, 0, 0, "a")),
                                Identifier(Token(TokenType.Identifier, 0, 2, "b")),
                            ),
                            Identifier(Token(TokenType.Identifier, 0, 6, "c")),
                        )
                    ),
                    "a + b / c;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 8), InfixExpression(
                            Token(TokenType.Plus, 0, 3),
                            Identifier(Token(TokenType.Identifier, 0, 0, "a")),
                            InfixExpression(
                                Token(TokenType.Divide, 0, 5),
                                Identifier(Token(TokenType.Identifier, 0, 4, "b")),
                                Identifier(Token(TokenType.Identifier, 0, 6, "c")),
                            ),
                        )
                    ),
                    "a + b * c + d / e - f;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 20), InfixExpression(
                            Token(TokenType.Minus, 0, 15),
                            InfixExpression(
                                Token(TokenType.Plus, 0, 3),
                                InfixExpression(
                                    Token(TokenType.Plus, 0, 1),
                                    Identifier(Token(TokenType.Identifier, 0, 0, "a")),
                                    InfixExpression(
                                        Token(TokenType.Multiply, 0, 5),
                                        Identifier(Token(TokenType.Identifier, 0, 4, "b")),
                                        Identifier(Token(TokenType.Identifier, 0, 6, "c")),
                                    ),
                                ),
                                InfixExpression(
                                    Token(TokenType.Divide, 0, 11),
                                    Identifier(Token(TokenType.Identifier, 0, 10, "d")),
                                    Identifier(Token(TokenType.Identifier, 0, 12, "e")),
                                ),
                            ),
                            Identifier(Token(TokenType.Identifier, 0, 18, "f")),
                        )
                    ),
                    "3 + 4;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 4), InfixExpression(
                            Token(TokenType.Plus, 0, 1),
                            IntegerLiteral(Token(TokenType.Number, 0, 0, "3")),
                            IntegerLiteral(Token(TokenType.Number, 0, 2, "4")),
                        )
                    ),
                    "-5 * 5;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 5), InfixExpression(
                            Token(TokenType.Multiply, 0, 2),
                            PrefixExpression(
                                Token(TokenType.Minus, 0, 0),
                                IntegerLiteral(Token(TokenType.Number, 0, 1, "5")),
                            ),
                            IntegerLiteral(Token(TokenType.Number, 0, 4, "5")),
                        )
                    ),
                    "3 > 5 == false;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 12), InfixExpression(
                            Token(TokenType.Eq, 0, 8),
                            InfixExpression(
                                Token(TokenType.Gt, 0, 2),
                                IntegerLiteral(Token(TokenType.Number, 0, 0, "3")),
                                IntegerLiteral(Token(TokenType.Number, 0, 4, "5")),
                            ),
                            BooleanLiteral(Token(TokenType.False, 0, 12, "false")),
                        )
                    ),
                    "3 < 5 == true;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 11), InfixExpression(
                            Token(TokenType.Eq, 0, 7),
                            InfixExpression(
                                Token(TokenType.Lt, 0, 2),
                                IntegerLiteral(Token(TokenType.Number, 0, 0, "3")),
                                IntegerLiteral(Token(TokenType.Number, 0, 4, "5")),
                            ),
                            BooleanLiteral(Token(TokenType.True, 0, 11, "true")),
                        )
                    ),
                    "true == true;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 11), InfixExpression(
                            Token(TokenType.Eq, 0, 5),
                            BooleanLiteral(Token(TokenType.True, 0, 0, "true")),
                            BooleanLiteral(Token(TokenType.True, 0, 10, "true")),
                        )
                    ),
                    "false == false;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 13), InfixExpression(
                            Token(TokenType.Eq, 0, 6),
                            BooleanLiteral(Token(TokenType.False, 0, 0, "false")),
                            BooleanLiteral(Token(TokenType.False, 0, 12, "false")),
                        )
                    ),
                    "true != false;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 12), InfixExpression(
                            Token(TokenType.NotEq, 0, 6),
                            BooleanLiteral(Token(TokenType.True, 0, 0, "true")),
                            BooleanLiteral(Token(TokenType.False, 0, 11, "false")),
                        )
                    ),
                )

                data.forEach { (input, expected) ->
                    val lexer = Lexer(input)
                    val parser = Parser(lexer)
                    val program = parser.parseProgram()


                    expect("parse $input") {
                        assertEquals(0, parser.errors.size, parser.errors())
                        assertEquals(expected, program.statements[0])
                    }
                }

                data.map { it.first }.joinToString("\n").let { input ->
                    val lexer = Lexer(input)
                    val parser = Parser(lexer)
                    val program = parser.parseProgram()

                    expect("no errors") {
                        assertEquals(0, parser.errors.size, parser.errors())
                    }

                    expect("parsed all statements") {
                        assertEquals(data.size, program.statements.size)
                    }

                    println(
                        program.render()
                    )
                }
            }

            context("grouped expressions") {
                val data = listOf(
                    "1 + (2 + 3) + 4;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 13), InfixExpression(
                            Token(TokenType.Plus, 0, 1),
                            InfixExpression(
                                Token(TokenType.Plus, 0, 5),
                                IntegerLiteral(Token(TokenType.Number, 0, 0, "1")),
                                InfixExpression(
                                    Token(TokenType.Plus, 0, 7),
                                    IntegerLiteral(Token(TokenType.Number, 0, 4, "2")),
                                    IntegerLiteral(Token(TokenType.Number, 0, 8, "3")),
                                ),
                            ),
                            IntegerLiteral(Token(TokenType.Number, 0, 12, "4")),
                        )
                    ),
                    "(5 + 5) * 2;" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 10), InfixExpression(
                            Token(TokenType.Multiply, 0, 9),
                            InfixExpression(
                                Token(TokenType.Plus, 0, 3),
                                IntegerLiteral(Token(TokenType.Number, 0, 1, "5")),
                                IntegerLiteral(Token(TokenType.Number, 0, 5, "5")),
                            ),
                            IntegerLiteral(Token(TokenType.Number, 0, 9, "2")),
                        )
                    ),
                    "2 / (5 + 5);" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 10), InfixExpression(
                            Token(TokenType.Divide, 0, 1),
                            IntegerLiteral(Token(TokenType.Number, 0, 0, "2")),
                            InfixExpression(
                                Token(TokenType.Plus, 0, 5),
                                IntegerLiteral(Token(TokenType.Number, 0, 3, "5")),
                                IntegerLiteral(Token(TokenType.Number, 0, 7, "5")),
                            ),
                        )
                    ),
                    "-(5 + 5);" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 6), PrefixExpression(
                            Token(TokenType.Minus, 0, 0),
                            InfixExpression(
                                Token(TokenType.Plus, 0, 3),
                                IntegerLiteral(Token(TokenType.Number, 0, 2, "5")),
                                IntegerLiteral(Token(TokenType.Number, 0, 6, "5")),
                            ),
                        )
                    ),
                    "!(true == true);" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 14), PrefixExpression(
                            Token(TokenType.Exclamation, 0, 0),
                            InfixExpression(
                                Token(TokenType.Eq, 0, 7),
                                BooleanLiteral(Token(TokenType.True, 0, 1, "true")),
                                BooleanLiteral(Token(TokenType.True, 0, 13, "true")),
                            ),
                        )
                    ),
                    "(a + k - (b + c)) * (d + f);" to ExpressionStatement(
                        Token(TokenType.Semicolon, 0, 25), InfixExpression(
                            Token(TokenType.Multiply, 0, 24),
                            InfixExpression(
                                Token(TokenType.Minus, 0, 5),
                                InfixExpression(
                                    Token(TokenType.Plus, 0, 3),
                                    Identifier(Token(TokenType.Identifier, 0, 1, "a")),
                                    Identifier(Token(TokenType.Identifier, 0, 5, "k")),
                                ),
                                InfixExpression(
                                    Token(TokenType.Plus, 0, 13),
                                    Identifier(Token(TokenType.Identifier, 0, 9, "b")),
                                    Identifier(Token(TokenType.Identifier, 0, 13, "c")),
                                ),
                            ),
                            InfixExpression(
                                Token(TokenType.Plus, 0, 23),
                                Identifier(Token(TokenType.Identifier, 0, 19, "d")),
                                Identifier(Token(TokenType.Identifier, 0, 23, "f")),
                            ),
                        )
                    ),
                )

                data.forEach { (input, expected) ->
                    val lexer = Lexer(input)
                    val parser = Parser(lexer)
                    val program = parser.parseProgram()

                    expect("parse $input") {
                        assertEquals(0, parser.errors.size, parser.errors())
                        assertEquals(
                            expected,
                            program.statements[0],
                            "expected ${expected.render()} but got ${program.statements[0].render()}"
                        )
                    }
                }
            }
        }

        context("parse boolean literal expression") {
            context("parse true") {
                val input = "true;"
                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("no errors") {
                    assertEquals(0, parser.errors.size)
                }

                expect("successfully parsed true") {
                    assertEquals(
                        ExpressionStatement(
                            Token(TokenType.Semicolon, 0, 4),
                            BooleanLiteral(Token(TokenType.True, 0, 0))
                        ),
                        program.statements[0]
                    )
                }
            }

            context("parse false") {
                val input = "false;"
                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("no errors") {
                    assertEquals(0, parser.errors.size)
                }

                expect("successfully parsed false") {
                    assertEquals(
                        ExpressionStatement(
                            Token(TokenType.Semicolon, 0, 5),
                            BooleanLiteral(Token(TokenType.False, 0, 0))
                        ),
                        program.statements[0]
                    )
                }
            }
        }

        context("parse if expression") {
            val data = listOf(
                "if (x < y) { x };" to ExpressionStatement(
                    Token(TokenType.Semicolon, 0, 15),
                    IfExpression(
                        Token(TokenType.If, 0, 0),
                        InfixExpression(
                            Token(TokenType.Lt, 0, 6),
                            Identifier(Token(TokenType.Identifier, 0, 4, "x")),
                            Identifier(Token(TokenType.Identifier, 0, 8, "y")),
                        ),
                        BlockStatement(
                            Token(TokenType.LBrace, 0, 11),
                            listOf(
                                ExpressionStatement(
                                    Token(TokenType.Identifier, 0, 13, "x"),
                                    Identifier(Token(TokenType.Identifier, 0, 13, "x")),
                                )
                            ),
                        ),
                    ),
                ),
                "if (x < y) { x } else { y };" to ExpressionStatement(
                    Token(TokenType.Semicolon, 0, 26),
                    IfExpression(
                        Token(TokenType.If, 0, 0),
                        InfixExpression(
                            Token(TokenType.Lt, 0, 6),
                            Identifier(Token(TokenType.Identifier, 0, 4, "x")),
                            Identifier(Token(TokenType.Identifier, 0, 8, "y")),
                        ),
                        BlockStatement(
                            Token(TokenType.LBrace, 0, 11),
                            listOf(
                                ExpressionStatement(
                                    Token(TokenType.Identifier, 0, 13, "x"),
                                    Identifier(Token(TokenType.Identifier, 0, 13, "x")),
                                )
                            ),
                        ),
                        BlockStatement(
                            Token(TokenType.LBrace, 0, 20),
                            listOf(
                                ExpressionStatement(
                                    Token(TokenType.Identifier, 0, 22, "y"),
                                    Identifier(Token(TokenType.Identifier, 0, 22, "y")),
                                )
                            ),
                        ),
                    ),
                ),
            )

            data.forEach { (input, expected) ->
                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("parse $input") {
                    assertEquals(0, parser.errors.size, parser.errors())
                    assertEquals(
                        expected,
                        program.statements[0],
                        "expected ${expected.render()} but got ${program.statements[0].render()}"
                    )
                }
            }
        }
    }
})