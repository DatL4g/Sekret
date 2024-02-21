package dev.datlag.sekret

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

class SekretProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return SekretProcessor(environment)
    }
}

class SekretProcessor(private val env: SymbolProcessorEnvironment, ) : SymbolProcessor {

    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true

        val codeGenerator = env.codeGenerator

        val className = ClassName(
            "dev.datlag.sekret",
            "DeobfuscatorHelper"
        )
        var classSpec = TypeSpec.objectBuilder(className)
        classSpec = DeobfuscatorHelperGenerator.addMaxChunkLength(classSpec)
        classSpec = DeobfuscatorHelperGenerator.addValues(classSpec)
        classSpec = DeobfuscatorHelperGenerator.addGetCharAt(classSpec)
        classSpec = DeobfuscatorHelperGenerator.addGetString(classSpec)
        classSpec = DeobfuscatorHelperGenerator.addGetStringCallable(classSpec)
        classSpec = DeobfuscatorHelperGenerator.addRandom(classSpec)

        if (codeGenerator.generatedFile.isEmpty()) {
            FileSpec.builder(className)
                .addType(classSpec.build())
                .build()
                .writeTo(
                    codeGenerator = codeGenerator,
                    aggregating = true
                )
        }

        return emptyList()
    }
}