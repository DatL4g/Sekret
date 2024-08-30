package dev.datlag.sekret

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.toLogger

data class Logger(
    val debug: Boolean,
    val messageCollector: MessageCollector
) {
    fun log(message: String) {
        messageCollector.report(CompilerMessageSeverity.INFO, message)
        messageCollector.toLogger().log(message)
    }

    fun warn(message: String) {
        messageCollector.report(CompilerMessageSeverity.WARNING, message)
        messageCollector.toLogger().warning(message)
    }
}