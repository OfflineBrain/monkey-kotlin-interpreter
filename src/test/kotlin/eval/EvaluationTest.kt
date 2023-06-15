package eval

import Token
import TokenType
import ast.BooleanLiteral
import ast.IntegerLiteral
import io.kotest.core.spec.style.ExpectSpec

class EvaluationTest : ExpectSpec({
    context("an evaluation") {
        context("of an integer expression") {
            val data = listOf(
                IntegerLiteral(Token(type = TokenType.Number, literal = "5")) to 5,
                IntegerLiteral(Token(type = TokenType.Number, literal = "10")) to 10
            )

            data.forEach { (expression, expected) ->
                expect("to return an integer [$expected] object") {
                    val evaluated = eval(expression)
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
    }
})