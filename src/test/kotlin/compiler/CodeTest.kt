package compiler

import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe

@DisplayName("Opcode")
class CodeTest : ExpectSpec({

    context("a make") {
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
            expect("should return correct instructions Op[$op] Operands[$operands] Expect[${expect.string()}]") {
                make(op, *operands.toIntArray()) shouldBe expect
            }
        }
    }
})
