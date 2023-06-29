package compiler

import ast.Parser
import ast.Program
import eval.IntegerObject
import eval.Object
import io.kotest.assertions.fail
import io.kotest.assertions.withClue
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import token.Lexer

@DisplayName("Compiler")
class CompilerTest : ExpectSpec({

    context("a compiler") {

        fun parse(input: String): Program {
            val lexer = Lexer(input)
            val parser = Parser(lexer)
            return parser.parseProgram()
        }

        fun concatInstructions(vararg instructions: Instructions): Instructions {
            val out = mutableListOf<UByte>()
            for (instruction in instructions) {
                out.addAll(instruction)
            }
            return out
        }

        context("an instructions string") {
            val instructions = concatInstructions(
                make(OpAdd),
                make(OpConstant, 2),
                make(OpConstant, 65535),
            )

            expect("should be readable") {
                instructions.string() shouldBe "0000 OpAdd\n0001 OpConstant 2\n0004 OpConstant 65535\n"
            }
        }

        context("read operands") {
            data class TestCase(
                val op: Opcode,
                val operands: List<Int>,
                val bytesRead: Int,
            )

            val tests = listOf(
                TestCase(
                    op = OpConstant,
                    operands = listOf(65535),
                    bytesRead = 2,
                ),
            )

            tests.forEach { (op, operands, bytesRead) ->
                expect("should read operands correctly [$op, $operands]") {
                    val definition = lookupDefinition(op) ?: fail("definition not found: $op")
                    val instructions = concatInstructions(make(op, *operands.toIntArray()))
                    val (def, read) = readOperands(
                        definition,
                        instructions.subList(1, instructions.size)
                    )
                    def shouldBe operands
                    read shouldBe bytesRead
                }
            }
        }

        context("integer arithmetic") {

            data class TestCase(
                val input: String,
                val expectedConstants: List<Object>,
                val expectedInstructions: List<Instructions>
            )

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
                expect("should compile correctly [$input]") {
                    val program = parse(input)
                    val compiler = Compiler()
                    compiler.compile(program)

                    val bytecode = compiler.bytecode()
                    bytecode.constants shouldBe expectedConstants
                    bytecode.instructions shouldBe concatInstructions(*expectedInstructions.toTypedArray())
                }
            }

        }

        context("boolean expressions") {
            data class TestCase(
                val input: String,
                val expectedConstants: List<Object>,
                val expectedInstructions: List<Instructions>
            )

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
                expect("should compile correctly [$input]") {
                    val program = parse(input)
                    val compiler = Compiler()
                    compiler.compile(program)

                    val bytecode = compiler.bytecode()
                    bytecode.constants shouldBe expectedConstants
                    bytecode.instructions shouldBe concatInstructions(*expectedInstructions.toTypedArray())
                }
            }
        }

        context("conditionals") {
            data class TestCase(
                val input: String,
                val expectedConstants: List<Object>,
                val expectedInstructions: List<Instructions>
            )

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
                    val program = parse(input)
                    val compiler = Compiler()
                    compiler.compile(program)

                    val bytecode = compiler.bytecode()
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
