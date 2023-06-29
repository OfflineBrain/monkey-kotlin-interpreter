package vm

import ast.Parser
import ast.Program
import compiler.Compiler
import eval.IntegerObject
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import token.Lexer

class VmTest : ExpectSpec({
    context("a vm") {

        fun parse(input: String): Program {
            val lexer = Lexer(input)
            val parser = Parser(lexer)
            return parser.parseProgram()
        }

        context("an integer arithmetic") {
            val data = listOf(
                "1" to 1,
                "2" to 2,
                "1 + 2" to 3,
                "3 - 3" to 0,
                "2 - 4" to -2,
                "2 * 3" to 6,
                "4 / 2" to 2,
            )

            data.forEach {
                val (input, expected) = it
                expect("should return $expected for $input") {
                    val program = parse(input)
                    val compiler = Compiler()
                    compiler.compile(program)
                    val vm = Vm(compiler.bytecode())
                    vm.run()
                    val stackTop = vm.lastPoppedStackElem()
                    val result = stackTop as IntegerObject
                    result.value shouldBe expected
                }
            }
        }
    }
})