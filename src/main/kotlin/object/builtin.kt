package `object`

val len = BuiltInFunctionObject(
    fn = { args: Array<Object> ->
        if (args.size != 1) {
            return@BuiltInFunctionObject ErrorObject.ArgumentNumberMismatch(1, args.size)
        }
        when (val arg = args[0]) {
            is StringObject -> IntegerObject(arg.value.length)
//            is ArrayObject -> IntegerObject(arg.elements.size)
            else -> ErrorObject.ArgumentMismatch(STRING, arg.type())
        }
    }
)

val builtins = mapOf<String, Object>(
    "len" to len,
)
