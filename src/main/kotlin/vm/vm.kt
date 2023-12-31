package vm

import compiler.Bytecode
import compiler.Instructions
import compiler.OpAdd
import compiler.OpCall
import compiler.OpConstant
import compiler.OpDiv
import compiler.OpEqual
import compiler.OpFalse
import compiler.OpGetGlobal
import compiler.OpGetLocal
import compiler.OpGreaterThan
import compiler.OpJump
import compiler.OpJumpNotTruthy
import compiler.OpMinus
import compiler.OpMul
import compiler.OpNot
import compiler.OpNotEqual
import compiler.OpNull
import compiler.OpPop
import compiler.OpReturn
import compiler.OpReturnValue
import compiler.OpSetGlobal
import compiler.OpSetLocal
import compiler.OpSub
import compiler.OpTrue
import compiler.Opcode
import compiler.readUint16
import compiler.readUint8
import `object`.BooleanObject
import `object`.CompiledFunctionObject
import `object`.IntegerObject
import `object`.NullObject
import `object`.Object
import `object`.StringObject

const val GlobalSize = 65536
const val MaxFrames = 1024
const val StackSize = 2048

data class Vm(
    private val frames: MutableList<Frame> = mutableListOf(),
    private var frameIndex: Int = 0,
    private val constants: MutableList<Object> = mutableListOf(),
    private val globals: MutableList<Object> = MutableList(GlobalSize) { NullObject },
) {
    constructor(bytecode: Bytecode) : this(
        frames = ArrayList<Frame>(MaxFrames)
            .also {
                it.add(
                    Frame(
                        fn = CompiledFunctionObject(bytecode.instructions, 0),
                        basePointer = 0
                    )
                )
            },
        frameIndex = 1,
        constants = bytecode.constants.toMutableList(),
    )

    private val stack: MutableList<Object> = MutableList(StackSize) { NullObject }
    private var sp: Int = 0

    fun run() {
        lateinit var ins: Instructions
        var op: Opcode

        while (currentFrame().ip < currentFrame().instructions.size) {

            ins = currentFrame().instructions
            op = ins[currentFrame().ip]

            when (op) {
                OpConstant -> {
                    val constIndex = readUint16(ins.subList(currentFrame().ip + 1, ins.size))
                    push(constants[constIndex])
                    currentFrame().ip += 2
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
                    val pos = readUint16(ins.subList(currentFrame().ip + 1, ins.size))
                    currentFrame().ip = pos - 1
                }

                OpJumpNotTruthy -> {
                    val pos = readUint16(ins.subList(currentFrame().ip + 1, ins.size))
                    currentFrame().ip += 2
                    val condition = pop()
                    if (toInvertedBooleanObject(condition).value) {
                        currentFrame().ip = pos - 1
                    }
                }

                OpSetGlobal -> {
                    val globalIndex = readUint16(ins.subList(currentFrame().ip + 1, ins.size))
                    currentFrame().ip += 2
                    globals[globalIndex] = pop()
                }

                OpGetGlobal -> {
                    val globalIndex = readUint16(ins.subList(currentFrame().ip + 1, ins.size))
                    currentFrame().ip += 2
                    push(globals[globalIndex])
                }

                OpSetLocal -> {
                    val localIndex = readUint8(ins.subList(currentFrame().ip + 1, ins.size))
                    currentFrame().ip += 1
                    stack[currentFrame().basePointer + localIndex] = pop()
                }

                OpGetLocal -> {
                    val localIndex = readUint8(ins.subList(currentFrame().ip + 1, ins.size))
                    currentFrame().ip += 1
                    push(stack[currentFrame().basePointer + localIndex])
                }

                OpNull -> {
                    push(NullObject)
                }

                OpCall -> {
                    val numArgs = readUint8(ins.subList(currentFrame().ip + 1, ins.size))
                    currentFrame().ip++
                    val fn = stack[sp - 1 - numArgs] as CompiledFunctionObject
                    val callFrame = Frame(
                        fn = fn,
                        ip = -1,
                        basePointer = sp - numArgs
                    )
                    pushFrame(callFrame)
                    sp = callFrame.basePointer + fn.numLocals
                }

                OpReturnValue -> {
                    val returnValue = pop()
                    val popFrame = popFrame()
                    sp = popFrame.basePointer - 1

                    push(returnValue)
                }

                OpReturn -> {
                    val popFrame = popFrame()
                    sp = popFrame.basePointer - 1

                    push(NullObject)
                }
            }
            currentFrame().ip++
        }
    }

    fun lastPoppedStackElem(): Object {
        return stack[sp]
    }

    private fun currentFrame(): Frame {
        return frames[frameIndex - 1]
    }

    private fun pushFrame(frame: Frame) {
        frames.add(frame)
        frameIndex++
    }

    private fun popFrame(): Frame {
        frameIndex--
        return frames.removeAt(frameIndex)
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
        stack[sp] = obj
        sp++
    }

    private fun pop(): Object {
        val obj = stack[sp - 1]
        sp--
        return obj
    }
}