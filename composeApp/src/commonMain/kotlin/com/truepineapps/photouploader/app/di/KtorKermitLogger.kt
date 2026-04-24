package com.truepineapps.photouploader.app.di

import co.touchlab.kermit.Logger
import io.ktor.client.plugins.logging.Logger as KtorLogger

/**
 * An adapter to bridge Ktor's logging system with Kermit.
 * This allows network logs to be processed by Kermit, ensuring consistent formatting
 * and output (e.g., to Logcat on Android or console on Desktop).
 */
class KtorKermitLogger(private val kermitLogger: Logger) : KtorLogger {
    override fun log(message: String) {
        // Ktor's logs are usually at the INFO level. Use Debug or Verbose if you prefer.
        kermitLogger.i { message }
    }
}
