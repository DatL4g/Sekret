package dev.datlag.sekret

@Target(AnnotationTarget.FILE, AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.RUNTIME)
annotation class Obfuscate
