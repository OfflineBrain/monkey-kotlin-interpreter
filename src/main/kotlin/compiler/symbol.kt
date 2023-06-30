package compiler

typealias SymbolScope = String

const val GlobalScope: SymbolScope = "GLOBAL"
const val LocalScope: SymbolScope = "LOCAL"

data class Symbol(val name: String, val scope: SymbolScope, val index: Int)

data class SymbolTable(
    val outer: SymbolTable? = null,
    val store: MutableMap<String, Symbol> = mutableMapOf(),
    var numDefinitions: Int = 0,
) {

    constructor(outer: SymbolTable) : this(outer, mutableMapOf(), 0)

    fun define(name: String): Symbol {
        val scope = if (outer == null) GlobalScope else LocalScope

        val symbol = Symbol(name, scope, numDefinitions++)
        store[name] = symbol
        return symbol
    }

    fun resolve(name: String): Symbol? {
        return store[name] ?: outer?.resolve(name)
    }
}