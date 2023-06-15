import ast.Parser
import eval.eval
import java.io.BufferedReader
import java.io.PrintWriter

class Repl(private val reader: BufferedReader, private val writer: PrintWriter) {
    fun start() {
        val env = eval.Environment()

        while (true) {
            writer.print(">> ")
            writer.flush()
            val line = reader.readLine() ?: break
            val lexer = Lexer(line)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            if (parser.errors.isNotEmpty()) {
                writer.println(parser.errors())
            } else {
                writer.println(eval(program, env).render())
            }

            writer.flush()
        }
    }
}