package vm

import `object`.CompiledFunctionObject

data class Frame(
    val fn: CompiledFunctionObject,
    var ip: Int,
) {
    val instructions
        get() = fn.instructions


}