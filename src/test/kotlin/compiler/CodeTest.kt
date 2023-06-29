package compiler

import io.kotest.assertions.fail
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe

@DisplayName("Opcode")
class CodeTest : ExpectSpec({
    fun concatInstructions(vararg instructions: Instructions): Instructions {
        val out = mutableListOf<UByte>()
        for (instruction in instructions) {
            out.addAll(instruction)
        }
        return out
    }

    context("make instructions") {
        data class TestCase(
            val op: Opcode,
            val operands: List<Int>,
            val expect: Instructions,
        )

        val tests = listOf(
            TestCase(
                op = OpConstant,
                operands = listOf(65534),
                expect = mutableListOf(OpConstant, 0xffu, 0xfeu),
            ),
            TestCase(
                op = OpAdd,
                operands = emptyList(),
                expect = mutableListOf(OpAdd),
            )
        )

        tests.forEach { (op, operands, expect) ->
            expect("should return correct instructions Op[$op] Operands[$operands] Expect[$expect]") {
                make(op, *operands.toIntArray()) shouldBe expect
            }
        }
    }



    expect("Instructions.string() should be readable") {
        val instructions = concatInstructions(
            make(OpAdd),
            make(OpConstant, 2),
            make(OpConstant, 65535),
        )

        instructions.string() shouldBe "0000 OpAdd\n0001 OpConstant 2\n0004 OpConstant 65535\n"
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
            expect("should read correctly [$op, $operands]") {
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
})
