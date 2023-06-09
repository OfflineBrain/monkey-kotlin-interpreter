import java.io.PrintWriter

fun main(args: Array<String>) {
    println("Welcome to the Monkey programming language!")
    println("Feel free to type in commands")

    Repl(System.`in`.bufferedReader(), PrintWriter(System.out)).start()
}