package dev.datlag.sekret

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

data class Logger(val debug: Boolean, val messageCollector: MessageCollector) {
    fun log(message: String) {
        messageCollector.report(CompilerMessageSeverity.INFO, message)
    }
}