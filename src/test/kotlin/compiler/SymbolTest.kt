package compiler

import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe

@DisplayName("Symbol")
class SymbolTest : ExpectSpec({

    context("define") {
        data class TestCase(
            val name: String,
            val expect: Symbol,
        )

        val tests = listOf(
            TestCase(
                name = "a",
                expect = Symbol("a", GlobalScope, 0),
            ),
            TestCase(
                name = "b",
                expect = Symbol("b", GlobalScope, 1),
            ),
        )

        with(SymbolTable()) {
            tests.forEach { (name, expect) ->
                define(name)

                expect("should return $expect for \"$name\"") {
                    store[name] shouldBe expect
                }
            }
        }
    }

    context("resolve global") {
        data class TestCase(
            val name: String,
            val expect: Symbol?,
        )

        val tests = listOf(
            TestCase(
                name = "a",
                expect = Symbol("a", GlobalScope, 0),
            ),
            TestCase(
                name = "b",
                expect = Symbol("b", GlobalScope, 1),
            ),
        )

        with(SymbolTable()) {
            tests.forEach { (name, expect) ->
                define(name)

                expect("should return $expect for \"$name\"") {
                    resolve(name) shouldBe expect
                }
            }
        }
    }
})
