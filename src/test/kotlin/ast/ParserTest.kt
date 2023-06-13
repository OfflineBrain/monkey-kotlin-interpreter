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
    }
}