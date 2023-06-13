import java.io.BufferedReader
import java.io.PrintWriter

class Repl(private val reader: BufferedReader, private val writer: PrintWriter) {
    fun start() {
        while (true) {
            writer.print(">> ")
            writer.flush()
            val line = reader.readLine() ?: break
            val lexer = Lexer(line)
            var token = lexer.nextToken()
            while (token.type !is TokenType.EOF) {
                writer.println(token)
                token = lexer.nextToken()
            }
            writer.flush()
        }
    }
}