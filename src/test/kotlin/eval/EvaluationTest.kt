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
    }
})