package `object`

import java.io.File

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

val readLine = BuiltInFunctionObject(
    fn = { args: Array<Object> ->
        if (args.size !in 0..1) {
            return@BuiltInFunctionObject ErrorObject.ArgumentNumberMismatch(1, args.size)
        }

        if (args.isEmpty()) {
            val input = readlnOrNull()
            return@BuiltInFunctionObject if (input == null) NullObject else StringObject(input)
        }

        when (val arg = args[0]) {
            is StringObject -> {
                val input = File(arg.value).useLines { it.firstOrNull() }
                if (input == null) NullObject else StringObject(input)
            }

            else -> ErrorObject.ArgumentMismatch(STRING, arg.type())
        }
    }
)

val readFile = BuiltInFunctionObject(
    fn = { args: Array<Object> ->
        if (args.size != 1) {
            return@BuiltInFunctionObject ErrorObject.ArgumentNumberMismatch(1, args.size)
        }

        when (val arg = args[0]) {
            is StringObject -> {
                val input = File(arg.value).useLines { it.joinToString("\n") }
                StringObject(input)
            }

            else -> ErrorObject.ArgumentMismatch(STRING, arg.type())
        }
    }
)

val builtins = mapOf<String, Object>(
    "len" to len,
    "read_line" to readLine,
    "read_file" to readFile
)
