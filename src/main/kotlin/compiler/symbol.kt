package compiler

typealias SymbolScope = String

const val GlobalScope: SymbolScope = "GLOBAL"

data class Symbol(val name: String, val scope: SymbolScope, val index: Int)

data class SymbolTable(val store: MutableMap<String, Symbol> = mutableMapOf(), private var numDefinitions: Int = 0) {


    fun define(name: String): Symbol {
        val symbol = Symbol(name, GlobalScope, numDefinitions++)
        store[name] = symbol
        return symbol
    }

    fun resolve(name: String): Symbol? {
        return store[name]
    }
}