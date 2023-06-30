package `object`

import ast.Identifier
import ast.Statement
import compiler.Instructions
import compiler.string
import eval.Environment

typealias ObjectType = String

interface Object {
    fun type(): ObjectType
    fun render(): String
}

const val INTEGER = "INTEGER"
const val BOOLEAN = "BOOLEAN"
const val NULL = "NULL"
const val RETURN_VALUE = "RETURN_VALUE"
const val ERROR = "ERROR"
const val FUNCTION = "FUNCTION"
const val COMPILED_FUNCTION = "COMPILED_FUNCTION"
const val STRING = "STRING"

data class IntegerObject(val value: Int) : Object {

    override fun type(): ObjectType {
        return INTEGER
    }

    override fun render(): String {
        return value.toString()
    }

    override fun toString(): String {
        return value.toString()
    }

    companion object {
        val ZERO = IntegerObject(0)
    }
}

sealed class BooleanObject(val value: Boolean) : Object {

    override fun type(): ObjectType {
        return BOOLEAN
    }

    override fun render(): String {
        return value.toString()
    }

    override fun toString(): String {
        return value.toString()
    }

    object True : BooleanObject(true)
    object False : BooleanObject(false)

    companion object {
        fun from(value: Boolean): BooleanObject {
            return if (value) True else False
        }
    }
}

data class StringObject(val value: String) : Object {
    override fun type(): ObjectType {
        return STRING
    }

    override fun render(): String {
        return "\"$value\""
    }

    override fun toString(): String {
        return value
    }

}


object NullObject : Object {
    override fun type(): ObjectType {
        return NULL
    }

    override fun render(): String {
        return "null"
    }

    override fun toString(): String {
        return "null"
    }

}


data class ReturnValueObject(val value: Object) : Object {
    override fun type(): ObjectType {
        return RETURN_VALUE
    }

    override fun render(): String {
        return value.render()
    }
}

sealed class ErrorObject(val message: String) : Object {
    override fun type(): ObjectType {
        return ERROR
    }

    override fun render(): String {
        return "ERROR: $message"
    }

    class TypeMismatch(expected: ObjectType, actual: ObjectType) :
        ErrorObject("type mismatch: expected=$expected, actual=$actual")

    class UnknownOperator(operator: String, left: ObjectType? = null, right: ObjectType) :
        ErrorObject("unknown operator: ${left ?: ""} $operator $right")

    class UnknownIdentifier(identifier: String) : ErrorObject("unknown identifier: $identifier")

    class NotAFunction(type: String) : ErrorObject("not a function: $type")
}

data class FunctionObject(
    val parameters: List<Identifier>,
    val body: Statement,
    val env: Environment
) : Object {
    override fun type(): ObjectType {
        return FUNCTION
    }

    override fun render(): String {
        return "fn(${parameters.joinToString(", ")}) ${body.render()}"
    }
}

data class CompiledFunctionObject(
    val instructions: Instructions,
) : Object {
    override fun type(): ObjectType {
        return COMPILED_FUNCTION
    }

    override fun render(): String {
        return instructions.string()
    }
}