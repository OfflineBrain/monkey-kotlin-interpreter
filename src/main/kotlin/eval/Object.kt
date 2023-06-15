package eval

typealias ObjectType = String

interface Object {
    fun render(): String
}

data class IntegerObject(val value: Int) : Object {
    override fun render(): String {
        return value.toString()
    }

    companion object {
        val ZERO = IntegerObject(0)
    }
}

sealed class BooleanObject(val value: Boolean) : Object {
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
    override fun render(): String {
        return "null"
    }
}

data class ReturnValueObject(val value: Object) : Object {
    override fun render(): String {
        return value.render()
    }
}