package compiler

import ast.Parser
import ast.Program
import io.kotest.assertions.withClue
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import `object`.CompiledFunctionObject
import `object`.IntegerObject
import `object`.Object
import `object`.StringObject
import token.Lexer

@DisplayName("Compiler")
class CompilerTest : ExpectSpec({

    data class TestCase(
        val input: String,
        val expectedConstants: List<Object>,
        val expectedInstructions: List<Instructions>
    )


    fun parse(input: String): Program {
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        return parser.parseProgram()
    }

    fun bytecode(input: String): Bytecode {
        val program = parse(input)
        val compiler = Compiler()
        compiler.compile(program)
        return compiler.bytecode()
    }

    fun concatInstructions(vararg instructions: Instructions): Instructions {
        val out = mutableListOf<UByte>()
        for (instruction in instructions) {
            out.addAll(instruction)
        }
        return out
    }

    context("scopes") {
        val compiler = Compiler()
        compiler.emit(OpMul)

        context("entering a scope") {
            compiler.enterScope()

            expect("index should be incremented") {
                compiler.scopeIndex shouldBe 1
            }
        }

        context("emitting an instruction in the new scope") {
            compiler.emit(OpSub)
            expect("should emit an instruction in the current scope [scope : ${compiler.scopeIndex}]") {
                compiler.scopes[compiler.scopeIndex].instructions shouldBe listOf(OpSub)
            }
        }

        context("leaving a scope") {
            compiler.leaveScope()
            expect("index should be decremented") {
                compiler.scopeIndex shouldBe 0
            }
        }

        context("emitting an instruction in the previous scope") {
            compiler.emit(OpAdd)
            expect("should emit an instruction in the current scope [scope : ${compiler.scopeIndex}]") {
                compiler.scopes[compiler.scopeIndex].instructions shouldBe listOf(OpMul, OpAdd)
            }
        }
    }

    context("compile integer arithmetic") {


        val tests = listOf(
            TestCase(
                input = "1 + 2",
                expectedConstants = listOf(1, 2).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpConstant, 0x01),
                    make(OpAdd),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "1 - 2",
                expectedConstants = listOf(1, 2).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpConstant, 0x01),
                    make(OpSub),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "1 * 2",
                expectedConstants = listOf(1, 2).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpConstant, 0x01),
                    make(OpMul),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "1 / 2",
                expectedConstants = listOf(1, 2).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpConstant, 0x01),
                    make(OpDiv),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "1; 2",
                expectedConstants = listOf(1, 2).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpPop),
                    make(OpConstant, 0x01),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "-1",
                expectedConstants = listOf(1).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpMinus),
                    make(OpPop),
                ),
            ),
        )

        tests.forEach { (input, expectedConstants, expectedInstructions) ->
            context("input: \"$input\"") {
                val bytecode = bytecode(input)

                expect("should compile constants") {
                    bytecode.constants shouldBe expectedConstants
                }

                expect("should compile instructions") {
                    val instructions = concatInstructions(*expectedInstructions.toTypedArray())

                    withClue({ " Expected: \n${instructions.string()} \n Actual\n${bytecode.instructions.string()}" }) {
                        bytecode.instructions shouldBe instructions
                    }
                }
            }
        }

    }

    context("compile boolean expressions") {


        val tests = listOf(
            TestCase(
                input = "true",
                expectedConstants = emptyList(),
                expectedInstructions = listOf(
                    make(OpTrue),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "false",
                expectedConstants = emptyList(),
                expectedInstructions = listOf(
                    make(OpFalse),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "1 > 2",
                expectedConstants = listOf(1, 2).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpConstant, 0x01),
                    make(OpGreaterThan),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "1 < 2",
                expectedConstants = listOf(2, 1).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpConstant, 0x01),
                    make(OpGreaterThan),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "1 == 2",
                expectedConstants = listOf(1, 2).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpConstant, 0x01),
                    make(OpEqual),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "1 != 2",
                expectedConstants = listOf(1, 2).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpConstant, 0x01),
                    make(OpNotEqual),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "true == false",
                expectedConstants = emptyList(),
                expectedInstructions = listOf(
                    make(OpTrue),
                    make(OpFalse),
                    make(OpEqual),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "true != false",
                expectedConstants = emptyList(),
                expectedInstructions = listOf(
                    make(OpTrue),
                    make(OpFalse),
                    make(OpNotEqual),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "1 >= 2",
                expectedConstants = listOf(2, 1).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpConstant, 0x01),
                    make(OpGreaterThan),
                    make(OpNot),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "1 <= 2",
                expectedConstants = listOf(1, 2).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpConstant, 0x01),
                    make(OpGreaterThan),
                    make(OpNot),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "!true",
                expectedConstants = emptyList(),
                expectedInstructions = listOf(
                    make(OpTrue),
                    make(OpNot),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "!false",
                expectedConstants = emptyList(),
                expectedInstructions = listOf(
                    make(OpFalse),
                    make(OpNot),
                    make(OpPop),
                ),
            ),
        )

        tests.forEach { (input, expectedConstants, expectedInstructions) ->
            context("input: \"$input\"") {
                val bytecode = bytecode(input)

                expect("should compile constants") {
                    bytecode.constants shouldBe expectedConstants
                }

                expect("should compile instructions") {
                    val instructions = concatInstructions(*expectedInstructions.toTypedArray())

                    withClue({ " Expected: \n${instructions.string()} \n Actual\n${bytecode.instructions.string()}" }) {
                        bytecode.instructions shouldBe instructions
                    }
                }
            }
        }
    }

    context("compile conditionals") {


        val tests = listOf(
            TestCase(
                input = "if (true) { 10 }; 3333;",
                expectedConstants = listOf(10, 3333).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpTrue),
                    make(OpJumpNotTruthy, 0x000a),
                    make(OpConstant, 0x00),
                    make(OpJump, 0x000b),
                    make(OpNull),
                    make(OpPop),
                    make(OpConstant, 0x01),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "if (true) { 10 } else { 20 }; 3333;",
                expectedConstants = listOf(10, 20, 3333).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpTrue),
                    make(OpJumpNotTruthy, 0x000a),
                    make(OpConstant, 0x00),
                    make(OpJump, 0x000d),
                    make(OpConstant, 0x01),
                    make(OpPop),
                    make(OpConstant, 0x02),
                    make(OpPop),
                ),
            ),
        )


        tests.forEach { (input, expectedConstants, expectedInstructions) ->
            context("input: \"$input\"") {
                val bytecode = bytecode(input)

                expect("should compile constants") {
                    bytecode.constants shouldBe expectedConstants
                }

                expect("should compile instructions") {
                    val instructions = concatInstructions(*expectedInstructions.toTypedArray())

                    withClue({ " Expected: \n${instructions.string()} \n Actual\n${bytecode.instructions.string()}" }) {
                        bytecode.instructions shouldBe instructions
                    }
                }
            }
        }
    }

    context("compile string expressions") {
        val tests = listOf(
            TestCase(
                input = "\"monkey\"",
                expectedConstants = listOf("monkey").map { StringObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = "\"mon\" + \"key\"",
                expectedConstants = listOf("mon", "key").map { StringObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpConstant, 0x01),
                    make(OpAdd),
                    make(OpPop),
                ),
            )
        )

        tests.forEach { (input, expectedConstants, expectedInstructions) ->
            context("input: \"$input\"") {
                val bytecode = bytecode(input)

                expect("should compile constants") {
                    bytecode.constants shouldBe expectedConstants
                }

                expect("should compile instructions") {
                    val instructions = concatInstructions(*expectedInstructions.toTypedArray())

                    withClue({ " Expected: \n${instructions.string()} \n Actual\n${bytecode.instructions.string()}" }) {
                        bytecode.instructions shouldBe instructions
                    }
                }
            }
        }
    }

    context("compile global let statements") {


        val tests = listOf(
            TestCase(
                input = """
                    let one = 1;
                    let two = 2;
                """.trimIndent(),
                expectedConstants = listOf(1, 2).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpSetGlobal, 0x00),
                    make(OpConstant, 0x01),
                    make(OpSetGlobal, 0x01),
                ),
            ),
            TestCase(
                input = """
                    let one = 1;
                    one;
                """.trimIndent(),
                expectedConstants = listOf(1).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpSetGlobal, 0x00),
                    make(OpGetGlobal, 0x00),
                    make(OpPop),
                ),
            ),
            TestCase(
                input = """
                    let one = 1;
                    let two = one;
                    two;
                """.trimIndent(),
                expectedConstants = listOf(1).map { IntegerObject(it) },
                expectedInstructions = listOf(
                    make(OpConstant, 0x00),
                    make(OpSetGlobal, 0x00),
                    make(OpGetGlobal, 0x00),
                    make(OpSetGlobal, 0x01),
                    make(OpGetGlobal, 0x01),
                    make(OpPop),
                ),
            ),
        )

        tests.forEach { (input, expectedConstants, expectedInstructions) ->
            context("input: \"$input\"") {
                val bytecode = bytecode(input)

                expect("should compile constants") {
                    bytecode.constants shouldBe expectedConstants
                }

                expect("should compile instructions") {
                    val instructions = concatInstructions(*expectedInstructions.toTypedArray())

                    withClue({ " Expected: \n${instructions.string()} \n Actual\n${bytecode.instructions.string()}" }) {
                        bytecode.instructions shouldBe instructions
                    }
                }
            }
        }
    }

    context("compile functions") {

        context("without arguments") {
            val tests = listOf(
                TestCase(
                    input = "fn() { return 5 + 10 }",
                    expectedConstants = listOf(5, 10).map { IntegerObject(it) } + listOf(
                        CompiledFunctionObject(
                            instructions = concatInstructions(
                                make(OpConstant, 0x00),
                                make(OpConstant, 0x01),
                                make(OpAdd),
                                make(OpReturnValue),
                            ),
                        )
                    ),
                    expectedInstructions = listOf(
                        make(OpConstant, 0x02),
                        make(OpPop),
                    ),
                ),
            )

            tests.forEach { (input, expectedConstants, expectedInstructions) ->
                context("input: \"$input\"") {
                    val bytecode = bytecode(input)

                    expect("should compile constants") {
                        bytecode.constants shouldBe expectedConstants
                    }

                    expect("should compile instructions") {
                        val instructions = concatInstructions(*expectedInstructions.toTypedArray())

                        withClue({ " Expected: \n${instructions.string()} \n Actual\n${bytecode.instructions.string()}" }) {
                            bytecode.instructions shouldBe instructions
                        }
                    }
                }
            }
        }
    }
})


