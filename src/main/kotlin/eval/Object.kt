package eval

typealias ObjectType = String

const val INTEGER_OBJ = "INTEGER"
const val BOOLEAN_OBJ = "BOOLEAN"
const val NULL_OBJ = "NULL"

interface Object {
    fun type(): ObjectType
    fun inspect(): String
}

data class IntegerObject(val value: Int) : Object {
    override fun type(): ObjectType {
        return INTEGER_OBJ
    }

    override fun inspect(): String {
        return value.toString()
    }
}

sealed class BooleanObject(val value: Boolean) : Object {
    override fun type(): ObjectType {
        return BOOLEAN_OBJ
    }

    override fun inspect(): String {
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
        return NULL_OBJ
    }

    override fun inspect(): String {
        return "null"
    }
}