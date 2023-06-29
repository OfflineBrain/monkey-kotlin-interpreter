package vm

import compiler.Bytecode
import compiler.Instructions
import compiler.OpAdd
import compiler.OpConstant
import compiler.OpDiv
import compiler.OpFalse
import compiler.OpMul
import compiler.OpPop
import compiler.OpSub
import compiler.OpTrue
import compiler.readUint16
import eval.BooleanObject
import eval.IntegerObject
import eval.Object

data class Vm(
    val instructions: Instructions = mutableListOf(),
    val constants: MutableList<Object> = mutableListOf()
) {
    constructor(bytecode: Bytecode) : this(bytecode.instructions, bytecode.constants.toMutableList())

    private val stack: MutableList<Object> = ArrayList(2048)
    private var sp = 0

    fun run() {
        var ip = 0
        while (ip < instructions.size) {
            val op = instructions[ip]
            when (op) {
                OpConstant -> {
                    val constIndex = readUint16(instructions.subList(ip + 1, instructions.size))
                    push(constants[constIndex])
                    ip += 2
                }

                OpPop -> {
                    pop()
                }

                OpAdd -> {
                    val right = pop()
                    val left = pop()
                    val result = (left as IntegerObject).value + (right as IntegerObject).value
                    push(IntegerObject(result))
                }

                OpSub -> {
                    val right = pop()
                    val left = pop()
                    val result = (left as IntegerObject).value - (right as IntegerObject).value
                    push(IntegerObject(result))
                }

                OpMul -> {
                    val right = pop()
                    val left = pop()
                    val result = (left as IntegerObject).value * (right as IntegerObject).value
                    push(IntegerObject(result))
                }

                OpDiv -> {
                    val right = pop()
                    val left = pop()
                    val result = (left as IntegerObject).value / (right as IntegerObject).value
                    push(IntegerObject(result))
                }

                OpTrue -> {
                    push(BooleanObject.True)
                }

                OpFalse -> {
                    push(BooleanObject.False)
                }
            }

            ip++
        }
    }

    fun stackTop(): Object {
        return stack[sp - 1]
    }

    fun push(obj: Object) {
        stack.add(sp, obj)
        sp++
    }

    fun pop(): Object {
        val obj = stack[sp - 1]
        sp--
        return obj
    }

    fun lastPoppedStackElem(): Object {
        return stack[sp]
    }
}