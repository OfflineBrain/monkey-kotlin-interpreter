package compiler

typealias Instructions = MutableList<UByte>
typealias Opcode = UByte

const val OpConstant: Opcode = 0x00u
const val OpAdd: Opcode = 0x01u
const val OpSub: Opcode = 0x02u
const val OpMul: Opcode = 0x03u
const val OpDiv: Opcode = 0x04u
const val OpPop: Opcode = 0x05u
const val OpTrue: Opcode = 0x06u
const val OpFalse: Opcode = 0x07u
const val OpEqual: Opcode = 0x08u
const val OpNotEqual: Opcode = 0x09u
const val OpGreaterThan: Opcode = 0x0au
const val OpNot: Opcode = 0x0bu


data class Definition(val name: String, val operandWidths: List<Int>)

val definitions = mapOf(
    OpConstant to Definition("OpConstant", listOf(2)),
    OpAdd to Definition("OpAdd", listOf()),
    OpSub to Definition("OpSub", listOf()),
    OpMul to Definition("OpMul", listOf()),
    OpDiv to Definition("OpDiv", listOf()),
    OpPop to Definition("OpPop", listOf()),
    OpTrue to Definition("OpTrue", listOf()),
    OpFalse to Definition("OpFalse", listOf()),
    OpEqual to Definition("OpEqual", listOf()),
    OpNotEqual to Definition("OpNotEqual", listOf()),
    OpGreaterThan to Definition("OpGreaterThan", listOf()),
    OpNot to Definition("OpNot", listOf()),
)

fun lookupDefinition(opcode: Opcode): Definition? {
    return definitions[opcode]
}

fun Instructions.string(): String {
    val out = StringBuilder()

    var i = 0
    while (i < size) {
        val def = lookupDefinition(this[i])
        if (def == null) {
            out.append("ERROR: ${this[i]}")
            continue
        }

        val (operands, read) = readOperands(def, this.subList(i + 1, size))
        out.append("${i.toString().padStart(4, '0')} ${def.name}")
        for (operand in operands) {
            out.append(" $operand")
        }
        out.append("\n")
        i += 1 + read

    }
    return out.toString()
}

fun readOperands(def: Definition, instructions: Instructions): Pair<List<Int>, Int> {
    val operands = mutableListOf<Int>()
    var offset = 0

    def.operandWidths.forEach { width ->
        when (width) {
            2 -> {
                operands.add(readUint16(instructions.subList(offset, offset + width)))
            }
        }

        offset += width
    }

    return operands to offset
}

fun readUint16(ins: Instructions): Int {
    return ((ins[0] and 0xFFu).toInt() shl 8) xor (ins[1] and 0xFFu).toInt()
}

fun make(op: Opcode, vararg operands: Int): Instructions {
    val definition = lookupDefinition(op) ?: return mutableListOf()

    val instructionLen = 1 + definition.operandWidths.sum()
    val instructions = ArrayList<UByte>(instructionLen)

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

