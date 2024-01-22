package dev.datlag.sekret

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.util.IrMessageLogger

data class Logger(
    val debug: Boolean,
    val messageCollector: MessageCollector,
    val messageLogger: IrMessageLogger
) {
    fun log(message: String) {
        messageCollector.report(CompilerMessageSeverity.INFO, message)
        messageLogger.report(IrMessageLogger.Severity.INFO, message, null)
    }

    fun warn(message: String) {
        messageCollector.report(CompilerMessageSeverity.WARNING, message)
        messageLogger.report(IrMessageLogger.Severity.WARNING, message, null)
    }
}