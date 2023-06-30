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
import `object`.IntegerObject
import `object`.Object
import `object`.StringObject
import token.Symbols

data class EmittedInstruction(
    val op: Opcode,
    val position: Int
)

data class Compiler(
    val constants: MutableList<Object> = mutableListOf(),
    val symbolTable: SymbolTable = SymbolTable(),
    val scopes: MutableList<CompilationScope> = mutableListOf(CompilationScope())
) {
    var scopeIndex = 0

    fun bytecode() = Bytecode(currentInstructions(), constants)

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
            is Identifier.Id -> {
                val symbol = symbolTable.resolve(node.token.literal)
                if (symbol != null) {
                    emit(OpGetGlobal, symbol.index)
                }
            }

            is Identifier.Invalid -> TODO()
            is IfExpression -> {
                compile(node.condition)
                val jump = emit(OpJumpNotTruthy, 0)

                compile(node.consequence)
                if (isLastInstructionPop()) {
                    removeLastPop()
                }

                val alternativeJump = emit(OpJump, 0)
                val afterConsequencePosition = currentInstructions().size
                changeOperand(jump, afterConsequencePosition)

                if (node.alternative == null) {
                    emit(OpNull)
                } else {
                    compile(node.alternative)
                    if (isLastInstructionPop()) {
                        removeLastPop()
                    }
                }

                val afterAlternativePosition = currentInstructions().size
                changeOperand(alternativeJump, afterAlternativePosition)
            }

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

            is PrefixExpression -> {
                compile(node.right)
                when (node.operator) {
                    Symbols.BANG -> emit(OpNot)
                    Symbols.MINUS -> emit(OpMinus)
                    else -> TODO()
                }
            }

            is StringLiteral -> {
                emit(OpConstant, addConstant(StringObject(node.value)))
            }

            is BlockStatement -> {
                for (statement in node.statements) {
                    compile(statement)
                }
            }

            is ExpressionStatement -> {
                compile(node.expression)
                emit(OpPop)
            }

            is LetStatement -> {
                compile(node.value)
                val symbol = symbolTable.define(node.name.token.literal)
                emit(OpSetGlobal, symbol.index)
            }

            is ReturnStatement -> TODO()
            Nothing -> TODO()
        }
    }

    fun emit(op: Opcode, vararg operands: Int): Int {
        val instruction = make(op, *operands)
        val position = addInstruction(instruction)

        setLastInstruction(op, position)

        return position
    }

    fun enterScope() {
        val scope = CompilationScope()
        scopes += scope
        scopeIndex++
    }

    fun leaveScope() {
        val scope = scopes.removeAt(scopeIndex)
        scopeIndex--

        val lastInstruction = scope.lastInstruction
        if (lastInstruction?.op == OpPop) {
            removeLastPop()
            scope.lastInstruction = scope.previousInstruction
        }
    }

    private fun addConstant(obj: Object): Int {
        constants += obj
        return constants.size - 1
    }

    private fun addInstruction(instruction: Instructions): Int {
        val position = currentInstructions().size
        currentInstructions() += instruction

        return position
    }

    private fun setLastInstruction(op: Opcode, position: Int) {
        val scope = currentScope()
        scope.previousInstruction = scope.lastInstruction
        scope.lastInstruction = EmittedInstruction(op, position)
    }

    private fun isLastInstructionPop(): Boolean {
        return currentScope().lastInstruction?.op == OpPop
    }

    private fun removeLastPop() {
        val scope = currentScope()
        while (scope.instructions.size > scope.lastInstruction!!.position) {
            scope.instructions.removeLast()
        }
        scope.lastInstruction = scope.previousInstruction
    }

    private fun changeOperand(opPosition: Int, operand: Int) {
        val op = currentInstructions()[opPosition]
        val newInstruction = make(op, operand)

        replaceInstruction(opPosition, newInstruction)
    }

    private fun replaceInstruction(position: Int, newInstruction: Instructions) {
        val scope = currentScope()
        newInstruction.forEachIndexed { index, instruction ->
            scope.instructions[position + index] = instruction
        }
    }

    private fun currentInstructions() = currentScope().instructions

    private fun currentScope() = scopes[scopeIndex]
}

data class Bytecode(val instructions: Instructions, val constants: List<Object>)

data class CompilationScope(
    val instructions: Instructions = mutableListOf(),
    var lastInstruction: EmittedInstruction? = null,
    var previousInstruction: EmittedInstruction? = null,
//    val symbolTable: SymbolTable = SymbolTable(),
)
