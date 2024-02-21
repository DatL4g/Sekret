package dev.datlag.sekret

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

data object DeobfuscatorHelperGenerator {

    fun addMaxChunkLength(clazz: TypeSpec.Builder) = with(clazz) {
        val prop = PropertySpec.builder(
            name = "MAX_CHUNK_LENGTH",
            type = Int::class,
            KModifier.CONST
        ).initializer("%L", 0x1fff)
        addProperty(prop.build())
    }

    fun addValues(clazz: TypeSpec.Builder) = with(clazz) {
        addProperty(
            PropertySpec.builder(
                name = "values",
                type = List::class.parameterizedBy(String::class),
                KModifier.PRIVATE
            ).initializer("emptyList()").build()
        )
    }

    fun addGetCharAt(clazz: TypeSpec.Builder) = with(clazz) {
        addFunction(FunSpec.builder("getCharAt")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("charIndex", Int::class)
            .addParameter("chunks", Iterable::class.parameterizedBy(String::class))
            .addParameter("state", Long::class)
            .returns(Long::class)
            .addStatement("val nextState = Random.next(state)")
            .addStatement("val chunk = chunks.toList()[charIndex / MAX_CHUNK_LENGTH]")
            .addStatement("return nextState xor (chunk[charIndex %L MAX_CHUNK_LENGTH].code.toLong() shl %L)", "%", 32)
            .build()
        )
    }

    fun addGetString(clazz: TypeSpec.Builder) = with(clazz) {
        addFunction(FunSpec.builder("getString")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("id", Long::class)
            .addParameter("chunks", Iterable::class.parameterizedBy(String::class))
            .returns(String::class)
            .addStatement("var state = Random.seed(id and %L)", 0xffffffffL)
            .addStatement("state = Random.next(state)")
            .addStatement("")
            .addStatement("val low = (state ushr %L) and %L", 32, 0xffff)
            .addStatement("state = Random.next(state)")
            .addStatement("")
            .addStatement("val high = (state ushr %L) and %L", 16, 0xffff0000)
            .addStatement("val index = ((id ushr %L) xor low xor high).toInt()", 32)
            .addStatement("state = getCharAt(index, chunks, state)")
            .addStatement("")
            .addStatement("val length = ((state ushr %L) and %L).toInt()", 32, 0xffffL)
            .addStatement("val chars = CharArray(length)")
            .addStatement("")
            .beginControlFlow("for (i in 0..<length)")
            .addStatement("state = getCharAt(index + i + %L, chunks, state)", 1)
            .addStatement("chars[i] = Char(((state ushr %L) and %L).toUShort())", 32, 0xffffL)
            .endControlFlow()
            .addStatement("")
            .addStatement("return chars.concatToString()")
            .build()
        )
    }

    fun addGetStringCallable(clazz: TypeSpec.Builder) = with(clazz) {
        addFunction(FunSpec.builder("getString")
            .addParameter("id", Long::class)
            .returns(String::class)
            .addStatement("return getString(id, values)")
            .build()
        )
    }

    fun addRandom(clazz: TypeSpec.Builder) = with(clazz) {
        val spec = TypeSpec.objectBuilder("Random")
            .addFunction(FunSpec.builder("seed")
                .addParameter("x", Long::class)
                .returns(Long::class)
                .addStatement("val z = (x xor (x ushr %L)) * %L", 33, 0x62a9d9ed799705f5L)
                .addStatement("return ((z xor (z ushr %L)) * %L) ushr %L", 28, -0x34db2f5a3773ca4dL, 32)
                .build()
            )
            .addFunction(FunSpec.builder("next")
                .addParameter("state", Long::class)
                .returns(Long::class)
                .addStatement("var s0 = (state and %L).toShort()", 0xffff)
                .addStatement("var s1 = ((state ushr %L) and %L).toShort()", 16, 0xffff)
                .addStatement("var next = s0")
                .addStatement("next = (next + s1).toShort()")
                .addStatement("next = rotL(next, %L)", 9)
                .addStatement("next = (next + s0).toShort()")
                .addStatement("")
                .addStatement("s1 = (s1.toInt() xor s0.toInt()).toShort()")
                .addStatement("s0 = rotL(s0, %L)", 13)
                .addStatement("s0 = (s0.toInt() xor s1.toInt()).toShort()")
                .addStatement("s0 = (s0.toInt() xor (s1.toInt() shl %L)).toShort()", 5)
                .addStatement("s1 = rotL(s1, %L)", 10)
                .addStatement("")
                .addStatement("var result = next.toLong()")
                .addStatement("result = result shl %L", 16)
                .addStatement("result = result or s1.toLong()")
                .addStatement("result = result shl %L", 16)
                .addStatement("result = result or s0.toLong()")
                .addStatement("return result")
                .build()
            )
            .addFunction(FunSpec.builder("rotL")
                .addModifiers(KModifier.PRIVATE)
                .addParameter("x", Short::class)
                .addParameter("k", Int::class)
                .returns(Short::class)
                .addStatement("return ((x.toInt() shl k) or (x.toInt() ushr (%L - k))).toShort()", 32)
                .build()
            )

        addType(spec.build())
    }
}