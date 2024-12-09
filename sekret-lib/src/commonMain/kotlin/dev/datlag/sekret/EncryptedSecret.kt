package dev.datlag.sekret

class EncryptedSecret private constructor(
    private val data: IntArray
) {
    fun decrypt(key: String): String {
        return SekretHelper.getNativeValue(data, key)
    }

    companion object {
        operator fun invoke(data: IntArray?): EncryptedSecret? {
            return if (data == null || data.isEmpty()) {
                null
            } else {
                EncryptedSecret(data)
            }
        }
    }
}