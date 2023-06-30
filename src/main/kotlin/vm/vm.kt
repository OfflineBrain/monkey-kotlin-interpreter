package vm

import compiler.Bytecode
import compiler.Instructions
import compiler.OpAdd
import compiler.OpConstant
import compiler.OpDiv
import compiler.OpEqual
import compiler.OpFalse
import compiler.OpGetGlobal
import compiler.OpGreaterThan
import compiler.OpJump
import compiler.OpJumpNotTruthy
import compiler.OpMinus
import compiler.OpMul
import compiler.OpNot
import compiler.OpNotEqual
import compiler.OpNull
import compiler.OpPop
import compiler.OpSetGlobal
import compiler.OpSub
import compiler.OpTrue
import compiler.Opcode
import compiler.readUint16
import `object`.BooleanObject
import `object`.IntegerObject
import `object`.NullObject
import `object`.Object
import `object`.StringObject

const val GlobalSize = 65536

data class Vm(
    val instructions: Instructions = mutableListOf(),
    val constants: MutableList<Object> = mutableListOf(),
    val globals: MutableList<Object> = MutableList(GlobalSize) { NullObject },
) {
    constructor(bytecode: Bytecode) : this(bytecode.instructions, bytecode.constants.toMutableList())

    private val stack: MutableList<Object> = MutableList(2048) { NullObject }
    private var sp = 0

    fun run() {
        var ip = 0
        while (ip < instructions.size) {
            when (val op = instructions[ip]) {
                OpConstant -> {
                    val constIndex = readUint16(instructions.subList(ip + 1, instructions.size))
                    push(constants[constIndex])
                    ip += 2
                }

                OpPop -> {
                    pop()
                }

                OpAdd, OpSub, OpMul, OpDiv, OpEqual, OpNotEqual, OpGreaterThan -> {
                    val right = pop()
                    val left = pop()
                    executeBinaryOperation(op, left, right)
                }


                OpTrue -> {
                    push(BooleanObject.True)
                }

                OpFalse -> {
                    push(BooleanObject.False)
                }

                OpNot -> {
                    val operand = pop()
                    push(toInvertedBooleanObject(operand))
                }

                OpMinus -> {
                    val operand = pop()
                    push(IntegerObject(-(operand as IntegerObject).value))
                }

                OpJump -> {
                    val pos = readUint16(instructions.subList(ip + 1, instructions.size))
                    ip = pos - 1
                }

                OpJumpNotTruthy -> {
                    val pos = readUint16(instructions.subList(ip + 1, instructions.size))
                    ip += 2
                    val condition = pop()
                    if (toInvertedBooleanObject(condition).value) {
                        ip = pos - 1
                    }
                }

                OpSetGlobal -> {
                    val globalIndex = readUint16(instructions.subList(ip + 1, instructions.size))
                    ip += 2
                    globals[globalIndex] = pop()
                }

                OpGetGlobal -> {
                    val globalIndex = readUint16(instructions.subList(ip + 1, instructions.size))
                    ip += 2
                    push(globals[globalIndex])
                }

                OpNull -> {
                    push(NullObject)
                }
            }

            ip++
        }
    }

    fun lastPoppedStackElem(): Object {
        return stack[sp]
    }

    private fun executeBinaryOperation(op: Opcode, left: Object, right: Object) {

        if (left is IntegerObject && right is IntegerObject) {
            executeBinaryIntegerOperation(op, left, right)
            return
        }

        when (op) {
            OpAdd -> {
                push(StringObject(left.toString() + right.toString()))
            }

            OpEqual -> {
                push(BooleanObject.from(left == right))
            }

            OpNotEqual -> {
                push(BooleanObject.from(left != right))
            }
        }
    }

    private fun executeBinaryIntegerOperation(op: Opcode, left: IntegerObject, right: IntegerObject) {
        val leftValue = left.value
        val rightValue = right.value

        when (op) {
            OpAdd -> {
                push(IntegerObject(leftValue + rightValue))
            }

            OpSub -> {
                push(IntegerObject(leftValue - rightValue))
            }

            OpMul -> {
                push(IntegerObject(leftValue * rightValue))
            }

            OpDiv -> {
                push(IntegerObject(leftValue / rightValue))
            }

            OpEqual -> {
                push(BooleanObject.from(leftValue == rightValue))
            }

            OpNotEqual -> {
                push(BooleanObject.from(leftValue != rightValue))
            }

            OpGreaterThan -> {
                push(BooleanObject.from(leftValue > rightValue))
            }
        }
    }

    private fun toInvertedBooleanObject(operand: Object) = when (operand) {
        BooleanObject.True -> BooleanObject.False
        BooleanObject.False -> BooleanObject.True
        NullObject -> BooleanObject.True
        else -> BooleanObject.False
    }

    private fun push(obj: Object) {
        stack.add(sp, obj)
        sp++
    }

    private fun pop(): Object {
        val obj = stack[sp - 1]
        sp--
        return obj
    }
}