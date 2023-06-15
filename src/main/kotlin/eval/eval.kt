@file:Suppress("NON_TAIL_RECURSIVE_CALL")

package eval

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
import ast.Nothing
import ast.PrefixExpression
import ast.Program
import ast.ReturnStatement
import ast.Statement

tailrec fun eval(node: ast.Node): Object {

    return when (node) {
        is Program -> evalProgram(node.statements)
        //literals
        is BooleanLiteral -> BooleanObject.from(node.value)
        is IntegerLiteral -> IntegerObject(node.value)
        is Identifier.Id -> TODO()
        is Identifier.Invalid -> TODO()
        is FunctionLiteral -> TODO()
        //expressions
        is IfExpression -> evalIfExpression(
            eval(node.condition),
            eval(node.consequence),
            node.alternative?.let { eval(it) } ?: NullObject
        )

        is InfixExpression -> {
            val left = eval(node.left)
            if (left is ErrorObject) return left

            val right = eval(node.right)
            if (right is ErrorObject) return right

            evalInfixExpression(node.operator, left, right)
        }

        is PrefixExpression -> {
            val right = eval(node.right)
            if (right is ErrorObject) right else evalPrefixExpression(node.operator, right)
        }

        is CallExpression -> TODO()
        //statements
        is BlockStatement -> evalBlockStatement(node.statements)
        is ExpressionStatement -> eval(node.expression)
        is LetStatement -> TODO()
        is ReturnStatement -> {
            val value = eval(node.value)
            if (value is ErrorObject) value else ReturnValueObject(value)
        }

        Nothing -> TODO()
    }
}


private fun evalProgram(statements: List<Statement>): Object {
    var result: Object = NullObject

    for (stmt in statements) {
        result = when (val tmp = eval(stmt)) {
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

private fun evalBlockStatement(statements: List<Statement>): Object {
    var result: Object = NullObject

    for (stmt in statements) {
        result = when (val tmp = eval(stmt)) {
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
        "!" -> evalExclamationPrefixOperatorExpression(right)
        "-" -> evalMinusPrefixOperatorExpression(right)
        else -> ErrorObject.UnknownOperator(operator, right = right.type())
    }
}

private fun evalInfixExpression(operator: String, left: Object, right: Object): Object {
    when {
        left is IntegerObject && right is IntegerObject -> {
            return when (operator) {
                "+" -> IntegerObject(left.value + right.value)
                "-" -> IntegerObject(left.value - right.value)
                "*" -> IntegerObject(left.value * right.value)
                "/" -> IntegerObject(left.value / right.value)
                "<" -> BooleanObject.from(left.value < right.value)
                ">" -> BooleanObject.from(left.value > right.value)
                "==" -> BooleanObject.from(left.value == right.value)
                "!=" -> BooleanObject.from(left.value != right.value)
                else -> ErrorObject.UnknownOperator(operator, left = left.type(), right = right.type())
            }
        }

        left is BooleanObject && right is BooleanObject -> {
            return when (operator) {
                "==" -> BooleanObject.from(left.value == right.value)
                "!=" -> BooleanObject.from(left.value != right.value)
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