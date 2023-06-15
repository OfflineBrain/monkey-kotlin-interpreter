package eval

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

data class IntegerObject(val value: Int) : Object {

    override fun type(): ObjectType {
        return INTEGER
    }

    override fun render(): String {
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

    object True : BooleanObject(true)
    object False : BooleanObject(false)

    companion object {
        fun from(value: Boolean): BooleanObject {
            return if (value) True else False
        }
    }
}


object NullObject : Object {
    override fun type(): ObjectType {
        return NULL
    }

    override fun render(): String {
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
}