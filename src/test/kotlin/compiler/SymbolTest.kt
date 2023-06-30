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

        val global = SymbolTable()

        context("global") {
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


            with(global) {
                tests.forEach { (name, expect) ->
                    define(name)

                    expect("should return $expect for \"$name\"") {
                        store[name] shouldBe expect
                    }
                }
            }
        }

        val firstLocal = SymbolTable(global)
        context("first local") {
            val tests = listOf(
                TestCase(
                    name = "c",
                    expect = Symbol("c", LocalScope, 0),
                ),
                TestCase(
                    name = "d",
                    expect = Symbol("d", LocalScope, 1),
                ),
            )

            with(firstLocal) {
                tests.forEach { (name, expect) ->
                    define(name)

                    expect("should return $expect for \"$name\"") {
                        store[name] shouldBe expect
                    }
                }
            }
        }

        val secondLocal = SymbolTable(firstLocal)
        context("second local") {
            val tests = listOf(
                TestCase(
                    name = "e",
                    expect = Symbol("e", LocalScope, 0),
                ),
                TestCase(
                    name = "f",
                    expect = Symbol("f", LocalScope, 1),
                ),
            )

            with(secondLocal) {
                tests.forEach { (name, expect) ->
                    define(name)

                    expect("should return $expect for \"$name\"") {
                        store[name] shouldBe expect
                    }
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

    context("resolve local") {
        val global = SymbolTable().apply {
            define("a")
            define("b")
        }

        val firstLocal = SymbolTable(global).apply {
            define("c")
            define("d")
        }

        context("first local") {
            val data = listOf(
                firstLocal to listOf(
                    Symbol("a", GlobalScope, 0),
                    Symbol("b", GlobalScope, 1),
                    Symbol("c", LocalScope, 0),
                    Symbol("d", LocalScope, 1)
                ),
            )

            data.forEach { (symbolTable, expected) ->
                expected.forEach { symbol ->
                    expect("should return $symbol for \"${symbol.name}\"") {
                        symbolTable.resolve(symbol.name) shouldBe symbol
                    }
                }
            }
        }

        val secondLocal = SymbolTable(firstLocal).apply {
            define("e")
            define("f")
        }

        context("second local") {
            val data = listOf(
                secondLocal to listOf(
                    Symbol("a", GlobalScope, 0),
                    Symbol("b", GlobalScope, 1),
                    Symbol("e", LocalScope, 0),
                    Symbol("f", LocalScope, 1)
                ),
            )
            data.forEach { (symbolTable, expected) ->
                expected.forEach { symbol ->
                    expect("should return $symbol for \"${symbol.name}\"") {
                        symbolTable.resolve(symbol.name) shouldBe symbol
                    }
                }
            }
        }
    }
})
