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
const val OpMinus: Opcode = 0x0cu
const val OpJump: Opcode = 0x0du
const val OpJumpNotTruthy: Opcode = 0x0eu
const val OpNull: Opcode = 0x0fu
const val OpSetGlobal: Opcode = 0x10u
const val OpGetGlobal: Opcode = 0x11u
const val OpCall: Opcode = 0x12u
const val OpReturnValue: Opcode = 0x13u
const val OpReturn: Opcode = 0x14u
const val OpGetLocal: Opcode = 0x15u
const val OpSetLocal: Opcode = 0x16u

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
    OpMinus to Definition("OpMinus", listOf()),
    OpJump to Definition("OpJump", listOf(2)),
    OpJumpNotTruthy to Definition("OpJumpNotTruthy", listOf(2)),
    OpNull to Definition("OpNull", listOf()),
    OpSetGlobal to Definition("OpSetGlobal", listOf(2)),
    OpGetGlobal to Definition("OpGetGlobal", listOf(2)),
    OpCall to Definition("OpCall", listOf(1)),
    OpReturnValue to Definition("OpReturnValue", listOf()),
    OpReturn to Definition("OpReturn", listOf()),
    OpGetLocal to Definition("OpGetLocal", listOf(1)),
    OpSetLocal to Definition("OpSetLocal", listOf(1)),
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
            1 -> {
                operands.add(instructions[offset].toInt())
            }

            2 -> {
                operands.add(readUint16(instructions.subList(offset, offset + width)))
            }
        }

        offset += width
    }

    return operands to offset
}


fun readUint8(ins: Instructions): Int {
    return ins[0].toInt()
}

fun readUint16(ins: Instructions): Int {
    return ((ins[0] and 0xFFu).toInt() shl 8) xor (ins[1] and 0xFFu).toInt()
}

fun make(op: Opcode, vararg operands: Int): Instructions {
    val definition = lookupDefinition(op) ?: return mutableListOf()

    val instructionLen = 1 + definition.operandWidths.sum()
    val instructions = MutableList<UByte>(instructionLen) { 0u }

    instructions[0] = op
    var offset = 1
    for ((i, operand) in operands.withIndex()) {
        val width = definition.operandWidths[i]
        when (width) {
            1 -> instructions[offset] = operand.toUByte()
            2 -> {
                instructions[offset] = (operand shr 8).toUByte()
                instructions[offset + 1] = operand.toUByte()
            }
        }
        offset += width
    }

    return instructions
}

