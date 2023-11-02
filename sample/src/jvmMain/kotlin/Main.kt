import dev.datlag.sekret.sample.Sekret

fun main() {
    println("Hello World")
    val sekretClass = Sekret()
    sekretClass.sampleKey("dev.datlag.sekret").let(::println)
}