package compiler

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe

class CodeTest : ExpectSpec({

    context("a make") {
        val op = OpConstant
        val operands = listOf(65534)
        val expect = listOf<UByte>(0x00u, 0xffu, 0xfeu)

        expect("should return correct instructions") {
            make(op, *operands.toIntArray()) shouldBe expect
        }
    }
})
