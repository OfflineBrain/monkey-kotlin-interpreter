package eval

import Lexer
import Token
import TokenType
import ast.BooleanLiteral
import ast.Parser
import io.kotest.core.spec.style.ExpectSpec

class EvaluationTest : ExpectSpec({
    context("an evaluation") {
        context("of an integer expression") {
            val data = listOf(
                "5" to 5,
                "10" to 10,
                "-5" to -5,
                "-10" to -10,
            )


            data.forEach { (expression, expected) ->
                val lexer = Lexer(expression)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("to return an integer [$expected] object") {
                    val evaluated = eval(program)
                    assert(evaluated is IntegerObject)
                    assert((evaluated as IntegerObject).value == expected)
                }
            }
        }

        context("of a boolean expression") {
            val data = listOf(
                Token(type = TokenType.True, literal = "true") to true,
                Token(type = TokenType.False, literal = "false") to false
            )

            data.forEach { (token, expected) ->
                expect("to return a boolean [$expected] object") {
                    val evaluated = eval(BooleanLiteral(token))
                    assert(evaluated is BooleanObject)
                    assert((evaluated as BooleanObject).value == expected)
                }
            }
        }

        context("of an exclamation") {
            val data = listOf(
                "!true" to false,
                "!false" to true,
                "!5" to false,
                "!!true" to true,
                "!!false" to false,
                "!!5" to true
            )

            data.forEach { (input, expected) ->
                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("[$input] to return a boolean [$expected] object") {
                    val evaluated = eval(program)
                    assert(evaluated is BooleanObject)
                    assert((evaluated as BooleanObject).value == expected)
                }
            }
        }

        context("of an infix expression") {
            val intExpressions = listOf(
                "5 + 5 + 5 + 5 - 10" to 10,
                "2 * 2 * 2 * 2 * 2" to 32,
                "-50 + 100 + -50" to 0,
                "5 * 2 + 10" to 20,
                "5 + 2 * 10" to 25,
                "20 + 2 * -10" to 0,
                "50 / 2 * 2 + 10" to 60,
                "2 * (5 + 10)" to 30,
                "3 * 3 * 3 + 10" to 37,
                "3 * (3 * 3) + 10" to 37,
                "(5 + 10 * 2 + 15 / 3) * 2 + -10" to 50
            )

            intExpressions.forEach { (input, expected) ->
                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("[$input] to return an integer [$expected] object") {
                    val evaluated = eval(program)
                    assert(evaluated is IntegerObject)
                    assert((evaluated as IntegerObject).value == expected)
                }
            }

            val boolExpressions = listOf(
                "true" to true,
                "false" to false,
                "1 < 2" to true,
                "1 > 2" to false,
                "1 < 1" to false,
                "1 > 1" to false,
                "1 == 1" to true,
                "1 != 1" to false,
                "1 == 2" to false,
                "1 != 2" to true,
                "true == true" to true,
                "false == false" to true,
                "true == false" to false,
                "true != false" to true,
                "false != true" to true,
                "(1 < 2) == true" to true,
                "(1 < 2) == false" to false,
                "(1 > 2) == true" to false,
                "(1 > 2) == false" to true
            )

            boolExpressions.forEach { (input, expected) ->
                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("[$input] to return a boolean [$expected] object") {
                    val evaluated = eval(program)
                    assert(evaluated is BooleanObject)
                    assert((evaluated as BooleanObject).value == expected)
                }
            }
        }

        context("of an if expression") {
            val data = listOf(
                "if (true) { 10 }" to 10,
                "if (false) { 10 }" to null,
                "if (1) { 10 }" to 10,
                "if (1 < 2) { 10 }" to 10,
                "if (1 > 2) { 10 }" to null,
                "if (1 > 2) { 10 } else { 20 }" to 20,
                "if (1 < 2) { 10 } else { 20 }" to 10
            )

            data.forEach { (input, expected) ->
                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("[$input] to return an integer [$expected] object") {
                    val evaluated = eval(program)
                    if (expected == null) {
                        assert(evaluated is NullObject)
                    } else {
                        assert(evaluated is IntegerObject)
                        assert((evaluated as IntegerObject).value == expected)
                    }
                }
            }
        }
    }
})