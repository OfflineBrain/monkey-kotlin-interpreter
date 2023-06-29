@file:Suppress("NO_TAIL_CALLS_FOUND", "NON_TAIL_RECURSIVE_CALL")

package compiler

import ast.BlockStatement
import ast.BooleanLiteral
import ast.CallExpression
import ast.ExpressionStatement
import ast.FunctionLiteral
import ast.Identifier
import ast.IfExpression
import ast.InfixExpression
import ast.IntegerLiteral
import ast.LetStatement
import ast.Node
import ast.Nothing
import ast.PrefixExpression
import ast.Program
import ast.ReturnStatement
import ast.StringLiteral
import eval.IntegerObject
import eval.Object
import token.Symbols

data class Compiler(
    val instructions: Instructions = mutableListOf(),
    val constants: MutableList<Object> = mutableListOf()
) {
    tailrec fun compile(node: Node) {
        when (node) {
            is Program -> {
                node.statements.forEach { compile(it) }
            }

            is BooleanLiteral -> {
                if (node.value) {
                    emit(OpTrue)
                } else {
                    emit(OpFalse)
                }
            }

            is CallExpression -> TODO()
            is FunctionLiteral -> TODO()
            is Identifier.Id -> TODO()
            is Identifier.Invalid -> TODO()
            is IfExpression -> TODO()
            is InfixExpression -> {

                when (node.operator) {
                    Symbols.LT, Symbols.GTE -> {
                        compile(node.right)
                        compile(node.left)
                    }

                    else -> {
                        compile(node.left)
                        compile(node.right)
                    }
                }

                when (node.operator) {
                    Symbols.PLUS -> emit(OpAdd)
                    Symbols.MINUS -> emit(OpSub)
                    Symbols.ASTERISK -> emit(OpMul)
                    Symbols.SLASH -> emit(OpDiv)
                    Symbols.EQ -> emit(OpEqual)
                    Symbols.NOT_EQ -> emit(OpNotEqual)
                    Symbols.GT, Symbols.LT -> emit(OpGreaterThan)
                    Symbols.GTE, Symbols.LTE -> {
                        emit(OpGreaterThan)
                        emit(OpNot)
                    }

                    else -> TODO()
                }
            }

            is IntegerLiteral -> {
                val integer = IntegerObject(node.value)
                emit(OpConstant, addConstant(integer))
            }

            is PrefixExpression -> TODO()
            is StringLiteral -> TODO()
            is BlockStatement -> TODO()
            is ExpressionStatement -> {
                compile(node.expression)
                emit(OpPop)
            }

            is LetStatement -> TODO()
            is ReturnStatement -> TODO()
            Nothing -> TODO()
        }
    }

    fun addConstant(obj: Object): Int {
        constants += obj
        return constants.size - 1
    }

    fun emit(op: Opcode, vararg operands: Int): Int {
        val instruction = make(op, *operands)
        return addInstruction(instruction)
    }

    fun addInstruction(instruction: Instructions): Int {
        instructions += instruction
        return instructions.size - 1
    }

    fun bytecode() = Bytecode(instructions, constants)
}

data class Bytecode(val instructions: Instructions, val constants: List<Object>)
