package ast

import token.Lexer
import token.Token
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
                    Token.Identifier("x", 0, 4) to Token.Number(5, 0, 8),
                    Token.Identifier("y", 1, 4) to Token.Number(10, 1, 8),
                    Token.Identifier("foobar", 2, 4) to Token.Number(838383, 2, 13),
                )

                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("should parse all statements") {
                    assertEquals(expected.size, program.statements.size, program.render())
                }

                expect("should not contain any errors") {
                    assertEquals(0, parser.errors.size, parser.errors())
                }


                expected.forEachIndexed { i, expect ->
                    expect("should parse let statement: $i") {
                        val statement = program.statements[i] as LetStatement
                        assertLiteral(expect.first, statement.name.token)
                        assertLiteral(expect.second.literal, statement.value.tokenLiteral())
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
                    Token.Identifier("x", 0, 4) to Token.Number(5, 0, 8),
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
                Token.Return(0, 0) to Token.Number(5, 0, 7),
                Token.Return(1, 0) to Token.Number(10, 1, 7),
                Token.Return(2, 0) to Token.Number(993322, 2, 7),
            )

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            expect("should parse all statements") {
                assertEquals(expected.size, program.statements.size, program.render())
            }

            expect("should not contain any errors") {
                assertEquals(0, parser.errors.size, parser.errors())
            }

            expected.forEachIndexed { i, expect ->
                expect("should parse return statement: $i") {
                    assertTrue(program.statements[i] is ReturnStatement)

                    val statement = program.statements[i] as ReturnStatement

                    assertLiteral(expect.first, statement.token)
                    assertLiteral(expect.second.literal, statement.value.tokenLiteral())
                }
            }
        }

        context("parse identifier expression") {
            val input = "foobar;"

            val expected = listOf(
                Token.Identifier("foobar", 0, 6),
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
                Token.Number(5, 0, 0),
            )

            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            expect("should parse all statements") {
                assertEquals(expected.size, program.statements.size, program.render())
            }

            expect("should not contain any errors") {
                assertEquals(0, parser.errors.size, parser.errors())
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
                Token.Bang(0, 0) to Token.Number(5, 0, 1),
                Token.Minus(1, 0) to Token.Number(15, 1, 1),
                Token.Bang(2, 0) to Token.True(2, 1),
                Token.Bang(3, 0) to Token.False(3, 1),
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
                        Token.Number(5, 0, 0),
                        Token.Plus(0, 2),
                        Token.Number(5, 0, 4),
                    ),
                    "5 - 5;" to listOf(
                        Token.Number(5, 0, 0),
                        Token.Minus(0, 2),
                        Token.Number(5, 0, 4),
                    ),
                    "5 * 5;" to listOf(
                        Token.Number(5, 0, 0),
                        Token.Asterisk(0, 2),
                        Token.Number(5, 0, 4),
                    ),
                    "5 / 5;" to listOf(
                        Token.Number(5, 0, 0),
                        Token.Slash(0, 2),
                        Token.Number(5, 0, 4),
                    ),
                    "5 > 5;" to listOf(
                        Token.Number(5, 0, 0),
                        Token.Gt(0, 2),
                        Token.Number(5, 0, 4),
                    ),
                    "5 < 5;" to listOf(
                        Token.Number(5, 0, 0),
                        Token.Lt(0, 2),
                        Token.Number(5, 0, 4),
                    ),
                    "5 == 5;" to listOf(
                        Token.Number(5, 0, 0),
                        Token.Eq(0, 2),
                        Token.Number(5, 0, 4),
                    ),
                    "5 != 5;" to listOf(
                        Token.Number(5, 0, 0),
                        Token.NotEq(0, 2),
                        Token.Number(5, 0, 4),
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
                        Token.Semicolon(0, 4), InfixExpression(
                            Token.Asterisk(0, 3),
                            PrefixExpression(
                                Token.Minus(0, 0),
                                Identifier.Id(Token.Identifier("a", 0, 1)),
                            ),
                            Identifier.Id(Token.Identifier("b", 0, 5)),
                        )
                    ),
                    "!-a;" to ExpressionStatement(
                        Token.Semicolon(0, 3), PrefixExpression(
                            Token.Bang(0, 0),
                            PrefixExpression(
                                Token.Minus(0, 1),
                                Identifier.Id(Token.Identifier("a", 0, 2)),
                            ),
                        )
                    ),
                    "a + b + c;" to ExpressionStatement(
                        Token.Semicolon(0, 8), InfixExpression(
                            Token.Plus(0, 3),
                            InfixExpression(
                                Token.Plus(0, 1),
                                Identifier.Id(Token.Identifier("a", 0, 0)),
                                Identifier.Id(Token.Identifier("b", 0, 2)),
                            ),
                            Identifier.Id(Token.Identifier("c", 0, 6)),
                        )
                    ),
                    "a + b - c;" to ExpressionStatement(
                        Token.Semicolon(0, 8), InfixExpression(
                            Token.Minus(0, 3),
                            InfixExpression(
                                Token.Plus(0, 1),
                                Identifier.Id(Token.Identifier("a", 0, 0)),
                                Identifier.Id(Token.Identifier("b", 0, 2)),
                            ),
                            Identifier.Id(Token.Identifier("c", 0, 6)),
                        )
                    ),
                    "a * b * c;" to ExpressionStatement(
                        Token.Semicolon(0, 8), InfixExpression(
                            Token.Asterisk(0, 3),
                            InfixExpression(
                                Token.Asterisk(0, 1),
                                Identifier.Id(Token.Identifier("a", 0, 0)),
                                Identifier.Id(Token.Identifier("b", 0, 2)),
                            ),
                            Identifier.Id(Token.Identifier("c", 0, 6)),
                        )
                    ),
                    "a * b / c;" to ExpressionStatement(
                        Token.Semicolon(0, 8), InfixExpression(
                            Token.Slash(0, 3),
                            InfixExpression(
                                Token.Asterisk(0, 1),
                                Identifier.Id(Token.Identifier("a", 0, 0)),
                                Identifier.Id(Token.Identifier("b", 0, 2)),
                            ),
                            Identifier.Id(Token.Identifier("c", 0, 6)),
                        )
                    ),
                    "a + b / c;" to ExpressionStatement(
                        Token.Semicolon(0, 8), InfixExpression(
                            Token.Plus(0, 3),
                            Identifier.Id(Token.Identifier("a", 0, 0)),
                            InfixExpression(
                                Token.Slash(0, 5),
                                Identifier.Id(Token.Identifier("b", 0, 4)),
                                Identifier.Id(Token.Identifier("c", 0, 6)),
                            ),
                        )
                    ),
                    "a + b * c + d / e - f;" to ExpressionStatement(
                        Token.Semicolon(0, 18), InfixExpression(
                            Token.Minus(0, 13),
                            InfixExpression(
                                Token.Plus(0, 3),
                                InfixExpression(
                                    Token.Plus(0, 1),
                                    Identifier.Id(Token.Identifier("a", 0, 0)),
                                    InfixExpression(
                                        Token.Asterisk(0, 5),
                                        Identifier.Id(Token.Identifier("b", 0, 4)),
                                        Identifier.Id(Token.Identifier("c", 0, 6)),
                                    ),
                                ),
                                InfixExpression(
                                    Token.Slash(0, 11),
                                    Identifier.Id(Token.Identifier("d", 0, 10)),
                                    Identifier.Id(Token.Identifier("e", 0, 12)),
                                ),
                            ),
                            Identifier.Id(Token.Identifier("f", 0, 16)),
                        )
                    ),
                    "3 + 4;" to ExpressionStatement(
                        Token.Semicolon(0, 4), InfixExpression(
                            Token.Plus(0, 2),
                            IntegerLiteral(Token.Number(3, 0, 0)),
                            IntegerLiteral(Token.Number(4, 0, 4)),
                        )
                    ),
                    "-5 * 5;" to ExpressionStatement(
                        Token.Semicolon(0, 5), InfixExpression(
                            Token.Asterisk(0, 3),
                            PrefixExpression(
                                Token.Minus(0, 0),
                                IntegerLiteral(Token.Number(5, 0, 1)),
                            ),
                            IntegerLiteral(Token.Number(5, 0, 5)),
                        )
                    ),
                    "3 > 5 == false;" to ExpressionStatement(
                        Token.Semicolon(0, 13), InfixExpression(
                            Token.Eq(0, 9),
                            InfixExpression(
                                Token.Gt(0, 3),
                                IntegerLiteral(Token.Number(3, 0, 0)),
                                IntegerLiteral(Token.Number(5, 0, 4)),
                            ),
                            BooleanLiteral(Token.False(0, 12)),
                        )
                    ),
                    "3 < 5 == true;" to ExpressionStatement(
                        Token.Semicolon(0, 12), InfixExpression(
                            Token.Eq(0, 8),
                            InfixExpression(
                                Token.Lt(0, 3),
                                IntegerLiteral(Token.Number(3, 0, 0)),
                                IntegerLiteral(Token.Number(5, 0, 4)),
                            ),
                            BooleanLiteral(Token.True(0, 11)),
                        )
                    ),
                    "true == true;" to ExpressionStatement(
                        Token.Semicolon(0, 12), InfixExpression(
                            Token.Eq(0, 6),
                            BooleanLiteral(Token.True(0, 0)),
                            BooleanLiteral(Token.True(0, 11)),
                        )
                    ),
                    "false == false;" to ExpressionStatement(
                        Token.Semicolon(0, 13), InfixExpression(
                            Token.Eq(0, 7),
                            BooleanLiteral(Token.False(0, 0)),
                            BooleanLiteral(Token.False(0, 12)),
                        )
                    ),
                    "true != false;" to ExpressionStatement(
                        Token.Semicolon(0, 12), InfixExpression(
                            Token.NotEq(0, 6),
                            BooleanLiteral(Token.True(0, 0)),
                            BooleanLiteral(Token.False(0, 11)),
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
                        Token.Semicolon(0, 13), InfixExpression(
                            Token.Plus(0, 3),
                            InfixExpression(
                                Token.Plus(0, 1),
                                IntegerLiteral(Token.Number(1, 0, 0)),
                                InfixExpression(
                                    Token.Plus(0, 5),
                                    IntegerLiteral(Token.Number(2, 0, 4)),
                                    IntegerLiteral(Token.Number(3, 0, 8)),
                                ),
                            ),
                            IntegerLiteral(Token.Number(4, 0, 12)),
                        )
                    ),
                    "(5 + 5) * 2;" to ExpressionStatement(
                        Token.Semicolon(0, 11), InfixExpression(
                            Token.Asterisk(0, 9),
                            InfixExpression(
                                Token.Plus(0, 1),
                                IntegerLiteral(Token.Number(5, 0, 1)),
                                IntegerLiteral(Token.Number(5, 0, 5)),
                            ),
                            IntegerLiteral(Token.Number(2, 0, 10)),
                        )
                    ),
                    "2 / (5 + 5);" to ExpressionStatement(
                        Token.Semicolon(0, 11), InfixExpression(
                            Token.Slash(0, 9),
                            IntegerLiteral(Token.Number(2, 0, 0)),
                            InfixExpression(
                                Token.Plus(0, 5),
                                IntegerLiteral(Token.Number(5, 0, 4)),
                                IntegerLiteral(Token.Number(5, 0, 8)),
                            ),
                        )
                    ),
                    "-(5 + 5);" to ExpressionStatement(
                        Token.Semicolon(0, 8), PrefixExpression(
                            Token.Minus(0, 0),
                            InfixExpression(
                                Token.Plus(0, 5),
                                IntegerLiteral(Token.Number(5, 0, 1)),
                                IntegerLiteral(Token.Number(5, 0, 5)),
                            ),
                        )
                    ),
                    "!(true == true);" to ExpressionStatement(
                        Token.Semicolon(0, 14), PrefixExpression(
                            Token.Bang(0, 0),
                            InfixExpression(
                                Token.Eq(0, 6),
                                BooleanLiteral(Token.True(0, 1)),
                                BooleanLiteral(Token.True(0, 11)),
                            ),
                        )
                    ),
                    "(a + k - (b + c)) * (d + f);" to ExpressionStatement(
                        Token.Semicolon(0, 26), InfixExpression(
                            Token.Asterisk(0, 24),
                            InfixExpression(
                                Token.Minus(0, 5),
                                InfixExpression(
                                    Token.Plus(0, 1),
                                    Identifier.Id(Token.Identifier("a", 0, 1)),
                                    Identifier.Id(Token.Identifier("k", 0, 5)),
                                ),
                                InfixExpression(
                                    Token.Plus(0, 13),
                                    Identifier.Id(Token.Identifier("b", 0, 9)),
                                    Identifier.Id(Token.Identifier("c", 0, 13)),
                                ),
                            ),
                            InfixExpression(
                                Token.Plus(0, 21),
                                Identifier.Id(Token.Identifier("d", 0, 17)),
                                Identifier.Id(Token.Identifier("f", 0, 21)),
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
                            Token.Semicolon(0, 4),
                            BooleanLiteral(Token.True(0, 0))
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
                            Token.Semicolon(0, 5),
                            BooleanLiteral(Token.False(0, 0))
                        ),
                        program.statements[0]
                    )
                }
            }
        }

        context("parse if expression") {
            val data = listOf(
                "if (x < y) { x };" to ExpressionStatement(
                    Token.Semicolon(0, 16),
                    IfExpression(
                        Token.If(0, 0),
                        InfixExpression(
                            Token.Lt(0, 6),
                            Identifier.Id(Token.Identifier("x", 0, 4)),
                            Identifier.Id(Token.Identifier("y", 0, 8)),
                        ),
                        BlockStatement(
                            Token.LBrace(0, 11),
                            listOf(
                                ExpressionStatement(
                                    Token.Identifier("x", 0, 13),
                                    Identifier.Id(Token.Identifier("x", 0, 13)),
                                )
                            ),
                        ),
                    ),
                ),
                "if (x < y) { x } else { y };" to ExpressionStatement(
                    Token.Semicolon(0, 27),
                    IfExpression(
                        Token.If(0, 0),
                        InfixExpression(
                            Token.Lt(0, 6),
                            Identifier.Id(Token.Identifier("x", 0, 4)),
                            Identifier.Id(Token.Identifier("y", 0, 8)),
                        ),
                        BlockStatement(
                            Token.LBrace(0, 11),
                            listOf(
                                ExpressionStatement(
                                    Token.Identifier("x", 0, 13),
                                    Identifier.Id(Token.Identifier("x", 0, 13)),
                                )
                            ),
                        ),
                        BlockStatement(
                            Token.LBrace(0, 20),
                            listOf(
                                ExpressionStatement(
                                    Token.Identifier("y", 0, 27),
                                    Identifier.Id(Token.Identifier("y", 0, 27)),
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

        context("parse function literal") {
            val data = listOf(
                "fn(x, y) { x + y; }" to FunctionLiteral(
                    Token.Function(0, 0),
                    listOf(
                        Identifier.Id(Token.Identifier("x", 0, 3)),
                        Identifier.Id(Token.Identifier("y", 0, 6)),
                    ),
                    BlockStatement(
                        Token.LBrace(0, 9),
                        listOf(
                            ExpressionStatement(
                                Token.Semicolon(0, 17),
                                InfixExpression(
                                    Token.Plus(0, 15),
                                    Identifier.Id(Token.Identifier("x", 0, 11)),
                                    Identifier.Id(Token.Identifier("y", 0, 14)),
                                ),
                            )
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
                        program.statements[0].let { it as ExpressionStatement }.expression,
                        "expected ${expected.render()} but got ${program.statements[0].render()}"
                    )
                }
            }
        }

        context("parse call expression") {
            val data = listOf(
                "add(1, 2 * 3, 4 + 5);" to CallExpression(
                    Token.LParen(0, 3),
                    Identifier.Id(Token.Identifier("add", 0, 0)),
                    listOf(
                        IntegerLiteral(Token.Number(1, 0, 4)),
                        InfixExpression(
                            Token.Asterisk(0, 10),
                            IntegerLiteral(Token.Number(2, 0, 8)),
                            IntegerLiteral(Token.Number(3, 0, 12)),
                        ),
                        InfixExpression(
                            Token.Plus(0, 16),
                            IntegerLiteral(Token.Number(4, 0, 14)),
                            IntegerLiteral(Token.Number(5, 0, 18)),
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
                        program.statements[0].let { it as ExpressionStatement }.expression,
                        "expected ${expected.render()} but got ${program.statements[0].render()}"
                    )
                }
            }
        }
    }
})