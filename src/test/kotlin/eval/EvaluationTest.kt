package eval

import token.Lexer
import token.Token
import ast.BooleanLiteral
import ast.Parser
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.ExpectSpec
import kotlin.test.assertEquals

@DisplayName("Evaluation")
class EvaluationTest : ExpectSpec({

    val env = Environment()
    beforeTest { env.clear() }

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
                    val evaluated = eval(program, env)
                    assert(evaluated is IntegerObject)
                    assert((evaluated as IntegerObject).value == expected)
                }
            }
        }

        context("of a boolean expression") {
            val data = listOf(
                Token.True() to true,
                Token.False() to false
            )

            data.forEach { (token, expected) ->
                expect("to return a boolean [$expected] object") {
                    val evaluated = eval(BooleanLiteral(token), env)
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
                    val evaluated = eval(program, env)
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
                    val evaluated = eval(program, env)
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
                    val evaluated = eval(program, env)
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
                    val evaluated = eval(program, env)
                    if (expected == null) {
                        assert(evaluated is NullObject)
                    } else {
                        assert(evaluated is IntegerObject)
                        assert((evaluated as IntegerObject).value == expected)
                    }
                }
            }
        }

        context("of a return statement") {
            val data = listOf(
                "return 10;" to 10,
                "return 10; 9;" to 10,
                "return 2 * 5; 9;" to 10,
                "9; return 2 * 5; 9;" to 10,
                """
                    if (10 > 1) {
                        if (10 > 1) {
                            return 10;
                        }
                        return 1;
                    }
                """ to 10
            )

            data.forEach { (input, expected) ->
                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("[$input] to return an integer [$expected] object") {
                    val evaluated = eval(program, env)
                    assert(evaluated is IntegerObject)
                    assertEquals(expected, (evaluated as IntegerObject).value, program.render())
                }
            }
        }

        context("error handling") {
            val data = listOf(
                "5 + true;" to ErrorObject.TypeMismatch(INTEGER, BOOLEAN),
                "5 + true; 5;" to ErrorObject.TypeMismatch(INTEGER, BOOLEAN),
                "-true" to ErrorObject.UnknownOperator("-", left = null, right = BOOLEAN),
                "true + false;" to ErrorObject.UnknownOperator("+", left = BOOLEAN, right = BOOLEAN),
                "5; true + false; 5" to ErrorObject.UnknownOperator("+", left = BOOLEAN, right = BOOLEAN),
                "if (10 > 1) { true + false; }" to ErrorObject.UnknownOperator("+", left = BOOLEAN, right = BOOLEAN),
                """
                    if (10 > 1) {
                        if (10 > 1) {
                            return true + false;
                        }
                        return 1;
                    }
                """ to ErrorObject.UnknownOperator("+", left = BOOLEAN, right = BOOLEAN),
                "foobar" to ErrorObject.UnknownIdentifier("foobar"),
            )

            data.forEach { (input, expected) ->
                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("[$input] to return an error object") {
                    val evaluated = eval(program, env)
                    assert(evaluated is ErrorObject)
                    assertEquals(expected.message, (evaluated as ErrorObject).message, program.render())
                }
            }
        }

        context("of a let statement") {
            val data = listOf(
                "let a = 5; a;" to 5,
                "let a = 5 * 5; a;" to 25,
                "let a = 5; let b = a; b;" to 5,
                "let a = 5; let b = a; let c = a + b + 5; c;" to 15
            )

            data.forEach { (input, expected) ->
                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("[$input] to return an integer [$expected] object") {
                    val evaluated = eval(program, env)
                    assert(evaluated is IntegerObject)
                    assertEquals(expected, (evaluated as IntegerObject).value, program.render())
                }
            }
        }

        context("of a function") {
            val data = listOf(
                "fn(x) { x + 2; };" to "(x + 2);",
                "fn(x) { x; };" to "x;",
                "fn(x, y) { x + y; };" to "(x + y);",
                "fn(x,y,z) { x + y + z; };" to "((x + y) + z);",
            )

            data.forEach { (input, expected) ->
                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("[$input] to return a function [$expected] object") {
                    val evaluated = eval(program, env)
                    assert(evaluated is FunctionObject)
                    assertEquals("{\n$expected\n}", (evaluated as FunctionObject).body.render(), program.render())
                }
            }
        }

        context("of a function application") {
            val data = listOf(
                "let identity = fn(x) { x; }; identity(5);" to 5,
                "let identity = fn(x) { return x; }; identity(5);" to 5,
                "let double = fn(x) { x * 2; }; double(5);" to 10,
                "let add = fn(x, y) { x + y; }; add(5, 5);" to 10,
                "let add = fn(x, y) { x + y; }; add(5 + 5, add(5, 5));" to 20,
                "fn(x) { x; }(5)" to 5,
            )

            data.forEach { (input, expected) ->
                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("[$input] to return an integer [$expected] object") {
                    val evaluated = eval(program, env)
                    assert(evaluated is IntegerObject)
                    assertEquals(expected, (evaluated as IntegerObject).value, program.render())
                }
            }
        }

        context("of a closure") {
            val data = listOf(
                """
                    let newAdder = fn(x) {
                        fn(y) { x + y };
                    };
                    let addTwo = newAdder(2);
                    addTwo(2);
                """ to 4,
                """
                    let newAdder = fn(x) {
                        fn(y) { x + y };
                    };
                    let addTwo = newAdder(2);
                    let addThree = newAdder(3);
                    addTwo(2) + addThree(3);
                """ to 10,
                """
                    let newAdder = fn(x) {
                        let newAdder = fn(y) { x + y };
                        newAdder;
                    };
                    let addTwo = newAdder(2);
                    let addThree = newAdder(3);
                    addTwo(2) + addThree(3);
                """ to 10,
                """
                    let newAdderOuter = fn(x) {
                        fn(y) {
                            let x = x;
                            let y = y;
                            x + y;
                        };
                    };
                    let newAdderInner = newAdderOuter(2);
                    newAdderInner(2);
                """ to 4,
                """
                    let a = 1;
                    let newAdderOuter = fn(x) {
                        fn(y) {
                            let x = x;
                            let y = y;
                            x + y + a;
                        };
                    };
                    let newAdderInner = newAdderOuter(2);
                    newAdderInner(2);
                """ to 5,
                """
                    let newAdderOuter = fn(x) {
                        let a = x;
                        fn(y) {
                            let b = y;
                            fn(z) {
                                let c = z;
                                a + b + c;
                            };
                        };
                    };
                    let newAdderInner = newAdderOuter(2);
                    let adder = newAdderInner(2);
                    adder(2);
                """ to 6,
            )

            data.forEach { (input, expected) ->
                val lexer = Lexer(input)
                val parser = Parser(lexer)
                val program = parser.parseProgram()

                expect("[$input] to return an integer [$expected] object") {
                    val evaluated = eval(program, env)
                    assert(evaluated is IntegerObject)
                    assertEquals(expected, (evaluated as IntegerObject).value, program.render())
                }
            }
        }

        context("of string operations") {
            context("concatenation") {
                val data = listOf(
                    """"a" + "b"""" to "ab",
                    """"a" + "b" + "c"""" to "abc",
                    """"a" + 1""" to "a1",
                    """"a" + 1 + "b" + 2""" to "a1b2",
                )

                data.forEach { (input, expected) ->
                    val lexer = Lexer(input)
                    val parser = Parser(lexer)
                    val program = parser.parseProgram()

                    expect("[$input] to return a string [$expected] object") {
                        val evaluated = eval(program, env)
                        assert(evaluated is StringObject)
                        assertEquals(expected, (evaluated as StringObject).value, program.render())
                    }
                }
            }

            context("comparison") {
                val data = listOf(
                    """"a" == "a"""" to true,
                    """"a" == "b"""" to false,
                    """"a" != "a"""" to false,
                    """"a" != "b"""" to true,
                )

                data.forEach { (input, expected) ->
                    val lexer = Lexer(input)
                    val parser = Parser(lexer)
                    val program = parser.parseProgram()

                    expect("[$input] to return a boolean [$expected] object") {
                        val evaluated = eval(program, env)
                        assert(evaluated is BooleanObject)
                        assertEquals(expected, (evaluated as BooleanObject).value, program.render())
                    }
                }
            }
        }
    }
})