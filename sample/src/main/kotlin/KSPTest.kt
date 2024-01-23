import dev.datlag.sekret.Obfuscate
import dev.datlag.sekret.Secret

@Obfuscate
data class KSPTest<T>(
    @Secret val data: java.lang.String,
    @Secret val secret: String,
    val testing: Int = 1337,
    val subClass: Sub = Sub("sub-data"),
    @Secret val charSeq: CharSequence = "abcdefg",
    @Secret val javaCharSeq: java.lang.CharSequence = secret as java.lang.CharSequence,
    @Secret val builder: StringBuilder = StringBuilder("string-builder"),
    @Secret val appendable: Appendable = builder
) {
    val other: String = "test"
}

data class Sub(
    val subData: String
)