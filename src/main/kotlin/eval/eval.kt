@file:Suppress("NON_TAIL_RECURSIVE_CALL")

package eval

import ast.BlockStatement
import ast.BooleanLiteral
import ast.CallExpression
import ast.Expression
import ast.ExpressionStatement
import ast.FunctionLiteral
import ast.Identifier
import ast.IfExpression
import ast.InfixExpression
import ast.IntegerLiteral
import ast.LetStatement
import ast.Nothing
import ast.PrefixExpression
import ast.Program
import ast.ReturnStatement
import ast.Statement
import token.Symbols

tailrec fun eval(node: ast.Node, env: Environment): Object {

    return when (node) {
        is Program -> evalProgram(node.statements, env)
        //literals
        is BooleanLiteral -> BooleanObject.from(node.value)
        is IntegerLiteral -> IntegerObject(node.value)
        is Identifier.Id -> evalIdentifier(node, env)
        is FunctionLiteral -> FunctionObject(node.parameters, node.body, env)
        //expressions
        is IfExpression -> evalIfExpression(
            eval(node.condition, env),
            eval(node.consequence, env),
            node.alternative?.let { eval(it, env) } ?: NullObject
        )

        is InfixExpression -> {
            val left = eval(node.left, env)
            if (left is ErrorObject) return left

            val right = eval(node.right, env)
            if (right is ErrorObject) return right

            evalInfixExpression(node.operator, left, right)
        }

        is PrefixExpression -> {
            val right = eval(node.right, env)
            if (right is ErrorObject) right else evalPrefixExpression(node.operator, right)
        }

        is CallExpression -> {
            val function = eval(node.function, env)
            if (function is ErrorObject) return function

            val args = evalExpressions(node.arguments, env)
            if (args.size == 1 && args[0] is ErrorObject) return args[0]

            applyFunction(function, args)
        }
        //statements
        is BlockStatement -> evalBlockStatement(node.statements, env)
        is ExpressionStatement -> eval(node.expression, env)
        is LetStatement -> {
            val value = eval(node.value, env)
            if (value is ErrorObject) return value

            env[node.name.tokenLiteral()] = value
            value
        }

        is ReturnStatement -> {
            val value = eval(node.value, env)
            if (value is ErrorObject) value else ReturnValueObject(value)
        }

        is Identifier.Invalid -> throw UnsupportedOperationException("Invalid identifier")
        Nothing -> throw UnsupportedOperationException("Invalid identifier")
        else -> throw UnsupportedOperationException("Unknown node type: ${node.javaClass}")
    }
}


private fun evalProgram(statements: List<Statement>, env: Environment): Object {
    var result: Object = NullObject

    for (stmt in statements) {
        result = when (val tmp = eval(stmt, env)) {
            is ReturnValueObject -> {
                return tmp.value
            }

            is ErrorObject -> {
                return tmp
            }

            else -> tmp
        }
    }

    return result
}

private fun evalBlockStatement(statements: List<Statement>, env: Environment): Object {
    var result: Object = NullObject

    for (stmt in statements) {
        result = when (val tmp = eval(stmt, env)) {
            is ReturnValueObject -> {
                return tmp
            }

            is ErrorObject -> {
                return tmp
            }

            else -> tmp
        }
    }

    return result
}

private fun evalIdentifier(node: Identifier.Id, env: Environment): Object {
    return env[node.tokenLiteral()] ?: ErrorObject.UnknownIdentifier(node.tokenLiteral())
}

private fun evalExpressions(expressions: List<Expression>, env: Environment): List<Object> {
    val result = mutableListOf<Object>()

    for (expr in expressions) {
        val evaluated = eval(expr, env)
        if (evaluated is ErrorObject) return listOf(evaluated)
        result.add(evaluated)
    }

    return result
}

private fun evalPrefixExpression(operator: String, right: Object): Object {
    fun evalExclamationPrefixOperatorExpression(right: Object): Object {
        return when (right) {
            is BooleanObject -> BooleanObject.from(!right.value)
            is NullObject -> BooleanObject.True
            else -> BooleanObject.False
        }
    }

    fun evalMinusPrefixOperatorExpression(right: Object): Object {
        return when (right) {
            is IntegerObject -> IntegerObject(-right.value)
            else -> ErrorObject.UnknownOperator(operator, right = right.type())
        }
    }

    return when (operator) {
        Symbols.BANG -> evalExclamationPrefixOperatorExpression(right)
        Symbols.MINUS -> evalMinusPrefixOperatorExpression(right)
        else -> ErrorObject.UnknownOperator(operator, right = right.type())
    }
}

private fun evalInfixExpression(operator: String, left: Object, right: Object): Object {
    when {
        left is IntegerObject && right is IntegerObject -> {
            return when (operator) {
                Symbols.PLUS -> IntegerObject(left.value + right.value)
                Symbols.MINUS -> IntegerObject(left.value - right.value)
                Symbols.ASTERISK -> IntegerObject(left.value * right.value)
                Symbols.SLASH -> IntegerObject(left.value / right.value)
                Symbols.LT -> BooleanObject.from(left.value < right.value)
                Symbols.LTE -> BooleanObject.from(left.value <= right.value)
                Symbols.GT -> BooleanObject.from(left.value > right.value)
                Symbols.GTE -> BooleanObject.from(left.value >= right.value)
                Symbols.EQ -> BooleanObject.from(left.value == right.value)
                Symbols.NOT_EQ -> BooleanObject.from(left.value != right.value)
                else -> ErrorObject.UnknownOperator(operator, left = left.type(), right = right.type())
            }
        }

        left is BooleanObject && right is BooleanObject -> {
            return when (operator) {
                Symbols.EQ -> BooleanObject.from(left.value == right.value)
                Symbols.NOT_EQ -> BooleanObject.from(left.value != right.value)
                else -> ErrorObject.UnknownOperator(operator, left = left.type(), right = right.type())
            }
        }

        else -> return ErrorObject.TypeMismatch(left.type(), right.type())
    }
}

private fun evalIfExpression(condition: Object, consequence: Object, alternative: Object): Object {
    return when (condition) {
        is ErrorObject -> condition

        is BooleanObject.True -> consequence
        is BooleanObject.False -> alternative

        IntegerObject.ZERO -> alternative
        is IntegerObject -> consequence

        is NullObject -> alternative
        else -> NullObject
    }
}

private fun applyFunction(fn: Object, args: List<Object>): Object {

    fun extendFunctionEnv(fn: FunctionObject, args: List<Object>): Environment {
        val env = Environment(outer = fn.env)

        for ((index, param) in fn.parameters.withIndex()) {
            env[param.tokenLiteral()] = args[index]
        }

        return env
    }

    fun unwrapReturnValue(obj: Object) = when (obj) {
        is ReturnValueObject -> obj.value
        else -> obj
    }


    return when (fn) {
        is FunctionObject -> {
            val extendedEnv = extendFunctionEnv(fn, args)
            val evaluated = eval(fn.body, extendedEnv)
            unwrapReturnValue(evaluated)
        }

        else -> ErrorObject.NotAFunction(fn.type())
    }
}

