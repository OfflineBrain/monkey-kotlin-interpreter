package compiler

import ast.Parser
import ast.Program
import io.kotest.assertions.withClue
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.core.spec.style.scopes.ExpectSpecContainerScope
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

    suspend fun ExpectSpecContainerScope.verifyTestCase(
        input: String,
        expectedConstants: List<Object>,
        expectedInstructions: List<Instructions>
    ) {
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

    context("scopes") {
        val compiler = Compiler()
        val globalSt = compiler.symbolTable

        compiler.emit(OpMul)

        context("entering a scope") {
            compiler.enterScope()

            expect("index should be incremented") {
                compiler.scopeIndex shouldBe 1
            }

            expect("should push a new symbol table") {
                compiler.symbolTable.outer shouldBe globalSt
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

            expect("should pop the symbol table") {
                compiler.symbolTable shouldBe globalSt
            }

            expect("should not modify the global symbol table incorrectly") {
                compiler.symbolTable.outer shouldBe null
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
            verifyTestCase(input, expectedConstants, expectedInstructions)
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
            verifyTestCase(input, expectedConstants, expectedInstructions)
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
            verifyTestCase(input, expectedConstants, expectedInstructions)
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
            verifyTestCase(input, expectedConstants, expectedInstructions)
        }
    }

    context("compile let statements") {
        context("global") {
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
                TestCase(
                    input = """
                    let num = 55;
                    fn() { num }
                """.trimIndent(),
                    expectedConstants = listOf(55).map { IntegerObject(it) } + listOf(
                        CompiledFunctionObject(
                            instructions = concatInstructions(
                                make(OpGetGlobal, 0x00),
                                make(OpReturnValue),
                            ),
                        )
                    ),
                    expectedInstructions = listOf(
                        make(OpConstant, 0x00),
                        make(OpSetGlobal, 0x00),
                        make(OpConstant, 0x01),
                        make(OpPop),
                    ),
                )
            )


            tests.forEach { (input, expectedConstants, expectedInstructions) ->
                verifyTestCase(input, expectedConstants, expectedInstructions)
            }
        }

        context("local") {
            val tests = listOf(
                TestCase(
                    input = """
                    fn() {
                        let one = 1;
                        one;
                    }
                """.trimIndent(),
                    expectedConstants = listOf(1).map { IntegerObject(it) } + listOf(
                        CompiledFunctionObject(
                            instructions = concatInstructions(
                                make(OpConstant, 0x00),
                                make(OpSetLocal, 0x00),
                                make(OpGetLocal, 0x00),
                                make(OpReturnValue),
                            ),
                            1,
                        )
                    ),
                    expectedInstructions = listOf(
                        make(OpConstant, 0x01),
                        make(OpPop),
                    ),
                ),
                TestCase(
                    input = """
                    fn() {
                        let one = 1;
                        let two = 2;
                        one + two;
                    }
                """.trimIndent(),
                    expectedConstants = listOf(1, 2).map { IntegerObject(it) } + listOf(
                        CompiledFunctionObject(
                            instructions = concatInstructions(
                                make(OpConstant, 0x00),
                                make(OpSetLocal, 0x00),
                                make(OpConstant, 0x01),
                                make(OpSetLocal, 0x01),
                                make(OpGetLocal, 0x00),
                                make(OpGetLocal, 0x01),
                                make(OpAdd),
                                make(OpReturnValue),
                            ),
                            2,
                        )
                    ),
                    expectedInstructions = listOf(
                        make(OpConstant, 0x02),
                        make(OpPop),
                    ),
                ),
                TestCase(
                    input = """
                    fn() {
                        let one = 1;
                        let two = one;
                        one + two;
                    }
                """.trimIndent(),
                    expectedConstants = listOf(1).map { IntegerObject(it) } + listOf(
                        CompiledFunctionObject(
                            instructions = concatInstructions(
                                make(OpConstant, 0x00),
                                make(OpSetLocal, 0x00),
                                make(OpGetLocal, 0x00),
                                make(OpSetLocal, 0x01),
                                make(OpGetLocal, 0x00),
                                make(OpGetLocal, 0x01),
                                make(OpAdd),
                                make(OpReturnValue),
                            ),
                            2,
                        )
                    ),
                    expectedInstructions = listOf(
                        make(OpConstant, 0x01),
                        make(OpPop),
                    ),
                ),
            )

            tests.forEach { (input, expectedConstants, expectedInstructions) ->
                verifyTestCase(input, expectedConstants, expectedInstructions)
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
                verifyTestCase(input, expectedConstants, expectedInstructions)
            }
        }

        context("without arguments and return value") {
            val tests = listOf(

                TestCase(
                    input = "fn() { 5 + 10 }",
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
                TestCase(
                    input = "fn() { 1; 2 }",
                    expectedConstants = listOf(1, 2).map { IntegerObject(it) } + listOf(
                        CompiledFunctionObject(
                            instructions = concatInstructions(
                                make(OpConstant, 0x00),
                                make(OpPop),
                                make(OpConstant, 0x01),
                                make(OpReturnValue),
                            ),
                        )
                    ),
                    expectedInstructions = listOf(
                        make(OpConstant, 0x02),
                        make(OpPop),
                    ),
                ),
                TestCase(
                    input = "fn() { }",
                    expectedConstants = listOf(
                        CompiledFunctionObject(
                            instructions = concatInstructions(
                                make(OpReturn),
                            ),
                        )
                    ),
                    expectedInstructions = listOf(
                        make(OpConstant, 0x00),
                        make(OpPop),
                    ),
                ),
            )

            tests.forEach { (input, expectedConstants, expectedInstructions) ->
                verifyTestCase(input, expectedConstants, expectedInstructions)
            }
        }

        context("call") {
            val tests = listOf(
                TestCase(
                    input = "fn() { 24 }();",
                    expectedConstants = listOf(24).map { IntegerObject(it) } + listOf(
                        CompiledFunctionObject(
                            instructions = concatInstructions(
                                make(OpConstant, 0x00),
                                make(OpReturnValue),
                            ),
                        )
                    ),
                    expectedInstructions = listOf(
                        make(OpConstant, 0x01),
                        make(OpCall),
                        make(OpPop),
                    ),
                ),
                TestCase(
                    input = """
                        let noArg = fn() { 24 };
                        noArg();
                    """.trimIndent(),
                    expectedConstants = listOf(24).map { IntegerObject(it) } + listOf(
                        CompiledFunctionObject(
                            instructions = concatInstructions(
                                make(OpConstant, 0x00),
                                make(OpReturnValue),
                            ),
                        )
                    ),
                    expectedInstructions = listOf(
                        make(OpConstant, 0x01),
                        make(OpSetGlobal, 0x00),
                        make(OpGetGlobal, 0x00),
                        make(OpCall),
                        make(OpPop),
                    ),
                ),
            )

            tests.forEach { (input, expectedConstants, expectedInstructions) ->
                verifyTestCase(input, expectedConstants, expectedInstructions)
            }
        }
    }
})


