package vm

import ast.Parser
import ast.Program
import compiler.Compiler
import eval.BooleanObject
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
                "4 / 3" to 1,
                "50 / 2 * 2 + 10 - 5" to 55,
                "5 * (2 + 10)" to 60,
                "2 * 2 * 2 * 2" to 16,
                "5 * 2 + 10" to 20,
                "5 + 5 + 5 + 5 * 2" to 25,
            )

            data.forEach {
                val (input, expected) = it
                expect("should return $expected for \"$input\"") {
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

        context("a boolean expression") {
            val data = listOf(
                "true" to true,
                "false" to false,
            )

            data.forEach { (input, expected) ->
                expect("should return $expected for \"$input\"") {
                    val program = parse(input)
                    val compiler = Compiler()
                    compiler.compile(program)
                    val vm = Vm(compiler.bytecode())
                    vm.run()
                    val stackTop = vm.lastPoppedStackElem()
                    val result = stackTop as BooleanObject
                    result.value shouldBe expected
                }
            }
        }
    }
})