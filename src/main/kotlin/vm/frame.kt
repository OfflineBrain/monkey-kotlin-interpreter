package vm

import `object`.CompiledFunctionObject

data class Frame(
    val fn: CompiledFunctionObject,
    var ip: Int = 0,
    val basePointer: Int = 0,
) {
    val instructions
        get() = fn.instructions


}