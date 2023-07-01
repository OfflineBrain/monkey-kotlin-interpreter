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
import `object`.CompiledFunctionObject
import `object`.IntegerObject
import `object`.Object
import `object`.StringObject
import token.Symbols

data class EmittedInstruction(
    val op: Opcode,
    val position: Int
)

sealed class CompileError {
    object None : CompileError()
    class Union(val errors: MutableList<CompileError> = mutableListOf()) : CompileError()
    data class InvalidInfixOperator(val operator: String) : CompileError()
    data class InvalidIdentifier(val literal: String) : CompileError()
    data class InvalidPrefixOperator(val operator: String) : CompileError()
    object LeaveGlobalScope : CompileError()
    object RemoveFromEmptyScope : CompileError()
    object Nothing : CompileError()

    infix operator fun plus(other: CompileError): CompileError {
        return when (this) {
            is None -> other
            is Union -> {
                when (other) {
                    is None -> this
                    is Union -> {
                        errors.addAll(other.errors)
                        this
                    }

                    else -> {
                        errors.add(other)
                        this
                    }
                }
            }

            else -> {
                when (other) {
                    is None -> this
                    is Union -> {
                        other.errors.add(this)
                        other
                    }

                    else -> {
                        Union(mutableListOf(this, other))
                    }
                }
            }
        }
    }
}

data class Compiler(
    val constants: MutableList<Object> = mutableListOf(),
    var symbolTable: SymbolTable = SymbolTable(),
    val scopes: MutableList<CompilationScope> = mutableListOf(CompilationScope())
) {
    var scopeIndex = 0
    var error: CompileError = CompileError.None

    fun bytecode() = Bytecode(currentInstructions(), constants)

    tailrec fun compile(node: Node): CompileError {

        when (node) {
            is Program -> {
                node.statements.forEach { error += compile(it) }
            }

            is BooleanLiteral -> {
                if (node.value) {
                    emit(OpTrue)
                } else {
                    emit(OpFalse)
                }
            }

            is CallExpression -> {
                error += compile(node.function)

                node.arguments.forEach { error += compile(it) }

                emit(OpCall, node.arguments.size)
            }

            is FunctionLiteral -> {
                enterScope()

                node.parameters.forEach { symbolTable.define(it.token.literal) }

                error += compile(node.body)

                if (isLastInstructionPop()) {
                    replaceLastPopWithReturn()
                }
                if (!lastInstructionIs(OpReturnValue)) {
                    emit(OpReturn)
                }

                val numLocals = symbolTable.numDefinitions
                val instructions = leaveScope()
                val compiledFunction = CompiledFunctionObject(instructions, numLocals)
                emit(OpConstant, addConstant(compiledFunction))
            }

            is Identifier.Id -> {
                val symbol = symbolTable.resolve(node.token.literal)
                if (symbol != null) {
                    if (symbol.scope == GlobalScope) {
                        emit(OpGetGlobal, symbol.index)
                    } else {
                        emit(OpGetLocal, symbol.index)
                    }
                }
            }

            is Identifier.Invalid -> return CompileError.InvalidIdentifier(node.token.literal)
            is IfExpression -> {
                error += compile(node.condition)
                val jump = emit(OpJumpNotTruthy, 0)

                error += compile(node.consequence)
                if (isLastInstructionPop()) {
                    removeLastPop()
                }

                val alternativeJump = emit(OpJump, 0)
                val afterConsequencePosition = currentInstructions().size
                changeOperand(jump, afterConsequencePosition)

                if (node.alternative == null) {
                    emit(OpNull)
                } else {
                    error += compile(node.alternative)
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
                        error += compile(node.right)
                        error += compile(node.left)
                    }

                    else -> {
                        error += compile(node.left)
                        error += compile(node.right)
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

                    else -> return CompileError.InvalidInfixOperator(node.operator)
                }
            }

            is IntegerLiteral -> {
                val integer = IntegerObject(node.value)
                emit(OpConstant, addConstant(integer))
            }

            is PrefixExpression -> {
                error += compile(node.right)
                when (node.operator) {
                    Symbols.BANG -> emit(OpNot)
                    Symbols.MINUS -> emit(OpMinus)
                    else -> CompileError.InvalidPrefixOperator(node.operator)
                }
            }

            is StringLiteral -> {
                emit(OpConstant, addConstant(StringObject(node.value)))
            }

            is BlockStatement -> {
                for (statement in node.statements) {
                    error += compile(statement)
                }
            }

            is ExpressionStatement -> {
                error += compile(node.expression)
                emit(OpPop)
            }

            is LetStatement -> {
                error += compile(node.value)
                val symbol = symbolTable.define(node.name.token.literal)
                if (symbol.scope == GlobalScope) {
                    emit(OpSetGlobal, symbol.index)
                } else {
                    emit(OpSetLocal, symbol.index)
                }
            }

            is ReturnStatement -> {
                error += compile(node.value)
                emit(OpReturnValue)
            }

            Nothing -> error += CompileError.Nothing
        }
        return error
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

        symbolTable = SymbolTable(symbolTable)
    }

    fun leaveScope(): Instructions {
        val scope = scopes.removeAt(scopeIndex)

        if (scopeIndex == 0) {
            error += CompileError.LeaveGlobalScope
            return mutableListOf()
        }

        scopeIndex--

        symbolTable = symbolTable.outer ?: run {
            error += CompileError.LeaveGlobalScope
            return mutableListOf()
        }

        return scope.instructions
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

    private fun lastInstructionIs(op: Opcode): Boolean {
        if (currentInstructions().isEmpty()) {
            return false
        }
        return currentScope().lastInstruction?.op == op
    }

    private fun isLastInstructionPop(): Boolean {
        return lastInstructionIs(OpPop)
    }

    private fun removeLastPop() {
        val scope = currentScope()
        val lastInstructionPosition = scope.lastInstruction?.position ?: run {
            error += CompileError.RemoveFromEmptyScope
            return
        }
        while (scope.instructions.size > lastInstructionPosition) {
            scope.instructions.removeLast()
        }
        scope.lastInstruction = scope.previousInstruction
    }

    private fun replaceLastPopWithReturn() {
        val scope = currentScope()
        val lastPopPosition = scope.lastInstruction?.position ?: run {
            error += CompileError.RemoveFromEmptyScope
            return
        }
        replaceInstruction(lastPopPosition, make(OpReturnValue))
        scope.lastInstruction = EmittedInstruction(OpReturnValue, lastPopPosition)
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
)
