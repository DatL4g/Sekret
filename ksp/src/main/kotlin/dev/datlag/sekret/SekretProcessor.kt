package dev.datlag.sekret

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSType

class SekretProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return SekretProcessor(environment)
    }
}

class SekretProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {

    private var invoked: Boolean = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val codeGenerator: CodeGenerator = env.codeGenerator
        if (invoked) {
            return emptyList()
        }
        invoked = true

        val classDataList = getAnnotatedFunctions(resolver)
        classDataList.forEach {
            println(it.toString())
        }

        return emptyList()
    }

    private fun getAnnotatedFunctions(resolver: Resolver): List<KSAnnotated> {
        val obfuscateAnnotated = resolver.getSymbolsWithAnnotation(Obfuscate::class.java.name).toList()

        return obfuscateAnnotated
    }

}