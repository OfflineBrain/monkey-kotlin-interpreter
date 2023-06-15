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

fun eval(node: ast.Node): Object {

    return when (node) {
        is BooleanLiteral -> BooleanObject(node.value)
        is CallExpression -> TODO()
        is FunctionLiteral -> TODO()
        is Identifier.Id -> TODO()
        is Identifier.Invalid -> TODO()
        is IfExpression -> TODO()
        is InfixExpression -> TODO()
        is IntegerLiteral -> IntegerObject(node.value)
        Nothing -> TODO()
        is PrefixExpression -> TODO()
        is Program -> TODO()
        is BlockStatement -> TODO()
        is ExpressionStatement -> TODO()
        is LetStatement -> TODO()
        is ReturnStatement -> TODO()
    }
}