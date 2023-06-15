import ast.Parser
import java.io.BufferedReader
import java.io.PrintWriter

class Repl(private val reader: BufferedReader, private val writer: PrintWriter) {
    fun start() {
        while (true) {
            writer.print(">> ")
            writer.flush()
            val line = reader.readLine() ?: break
            val lexer = Lexer(line)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            if (parser.errors.isNotEmpty()) {
                writer.println(parser.errors())
                writer.println("\n\n")
            }

            writer.println(program.render())
            writer.flush()
        }
    }
}