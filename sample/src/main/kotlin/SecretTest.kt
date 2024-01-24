import dev.datlag.sekret.Obfuscate
import dev.datlag.sekret.Secret

@Obfuscate
data class SecretTest(
    @Secret val kString: String,
    @Secret val jString: java.lang.String,
    @Secret val kCharSequence: CharSequence = "kotlin-char-sequence",
    @Secret val jCharSequence: java.lang.CharSequence = "java-char-sequence" as java.lang.CharSequence,
    @Secret val kBuilder: StringBuilder = StringBuilder("kotlin-string-builder"),
    @Secret val jBuilder: java.lang.StringBuilder = StringBuilder("java-string-builder") as java.lang.StringBuilder,
    @Secret val kAppendable: Appendable = kBuilder,
    @Secret val jAppendable: java.lang.Appendable = jBuilder,
    @Secret val buffer: StringBuffer = StringBuffer("java-string-buffer"),
    @Secret val nullable: String? = null
)