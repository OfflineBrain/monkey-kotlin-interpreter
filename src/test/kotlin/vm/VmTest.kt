package vm

import ast.Parser
import ast.Program
import compiler.Compiler
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import `object`.BooleanObject
import `object`.IntegerObject
import `object`.NullObject
import `object`.Object
import `object`.StringObject
import token.Lexer

@DisplayName("Virtual Machine")
class VmTest : ExpectSpec({

    fun parse(input: String): Program {
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        return parser.parseProgram()
    }

    fun extracted(input: String): Object {
        val program = parse(input)
        val compiler = Compiler()
        compiler.compile(program)
        val vm = Vm(compiler.bytecode())
        vm.run()
        return vm.lastPoppedStackElem()
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
            "-5" to -5,
            "-(-5)" to 5,
        )

        data.forEach {
            val (input, expected) = it
            expect("should return $expected for \"$input\"") {
                val stackTop = extracted(input)
                val result = stackTop as IntegerObject
                result.value shouldBe expected
            }
        }
    }

    context("a boolean expression") {
        val data = listOf(
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
            "1 <= 2" to true,
            "1 >= 2" to false,
            "1 <= 1" to true,
            "1 >= 1" to true,
            "1 <= 0" to false,
            "1 >= 0" to true,
            "true == true" to true,
            "false == false" to true,
            "true == false" to false,
            "true != false" to true,
            "!true" to false,
            "!false" to true,
            "!(if (false) { 5; })" to true,
        )

        data.forEach { (input, expected) ->
            expect("should return $expected for \"$input\"") {
                val stackTop = extracted(input)
                val result = stackTop as BooleanObject
                result.value shouldBe expected
            }
        }
    }

    context("a conditional") {
        context("integer return") {
            val data = listOf(
                "if (true) { 10 }" to 10,
                "if (true) { 10 } else { 20 }" to 10,
                "if (false) { 10 } else { 20 }" to 20,
                "if (1) { 10 }" to 10,
                "if (1 < 2) { 10 }" to 10,
                "if (1 < 2) { 10 } else { 20 }" to 10,
                "if (1 > 2) { 10 } else { 20 }" to 20,
                "if (1 >= 1) { 10 } else { 20 }" to 10,
                "if (1 <= 2) { 10 } else { 20 }" to 10,
                "if ((if (false) { 10 })) { 10 } else { 20 }" to 20,
            )

            data.forEach { (input, expected) ->
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

        context("null return") {
            val data = listOf(
                "if (1 > 2) { 10 }" to null,
                "if (false) { 10 }" to null,
            )

            data.forEach { (input, expected) ->
                expect("should return $expected for \"$input\"") {
                    val program = parse(input)
                    val compiler = Compiler()
                    compiler.compile(program)
                    val vm = Vm(compiler.bytecode())
                    vm.run()
                    val stackTop = vm.lastPoppedStackElem()

                    stackTop shouldBe NullObject
                }
            }
        }
    }

    context("string expression") {
        val data = listOf(
            "\"monkey\"" to "monkey",
            "\"mon\" + \"key\"" to "monkey",
            "\"mon\" + \"key\" + \"banana\"" to "monkeybanana",
        )

        data.forEach { (input, expected) ->
            expect("should return $expected for \"$input\"") {
                val stackTop = extracted(input)
                val result = stackTop as StringObject
                result.value shouldBe expected
            }
        }
    }

    context("global let statement") {
        val data = listOf(
            "let one = 1; one" to 1,
            "let one = 1; let two = 2; one + two" to 3,
            "let one = 1; let two = one + one; one + two" to 3,
        )

        data.forEach { (input, expected) ->
            val stackTop = extracted(input)
            val result = stackTop as IntegerObject

            expect("should return $expected for \"$input\"") {
                result.value shouldBe expected
            }
        }
    }

    context("a function") {

    }
})