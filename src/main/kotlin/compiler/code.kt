package compiler

typealias Instructions = ArrayList<UByte>
typealias Opcode = UByte

const val OpConstant: Opcode = 0x00u

data class Definition(val name: String, val operandWidths: List<Int>)

val definitions = mapOf(
    OpConstant to Definition("OpConstant", listOf(2)),
)

fun lookupDefinition(opcode: Opcode): Definition? {
    return definitions[opcode]
}

fun make(op: Opcode, vararg operands: Int): Instructions {
    val definition = lookupDefinition(op) ?: return Instructions(0)

    val instructionLen = 1 + definition.operandWidths.sum()
    val instructions = Instructions(instructionLen)

    instructions.add(op)
    for ((i, operand) in operands.withIndex()) {
        when (definition.operandWidths[i]) {
            2 -> {
                instructions.add((operand shr 8).toUByte())
                instructions.add(operand.toUByte())
            }
        }
    }

    return instructions
}