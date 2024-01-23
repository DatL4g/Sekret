import dev.datlag.sekret.NativeLoader
import dev.datlag.sekret.sample.Sekret
import java.io.File

fun main() {
    println("Hello World!")

    System.getProperty("compose.application.resources.dir")?.let { File(it) }?.listFiles()?.forEach {
        println(it.name)
    }
    val loaded = NativeLoader.loadLibrary("sekret", System.getProperty("compose.application.resources.dir")?.let { File(it) })
    println("Loaded sekret: $loaded")

    if (loaded) {
        val secretValue = Sekret.testKey("password12345")
        println("Decoded secret: $secretValue")
    }
    val testing = SecretTest("kotlin-string", java.lang.String("java-string"))
    println(testing.toString())
    println(testing.kString)
    println(testing.jString)
}