package com.truepineapps.photouploader.core.util

import kotlinx.coroutines.delay

class ServiceUtil(val logMessage: String, val maxAttempts: Int = 5, val baseDelay: Long = 2000) {

    private var attempts = 0

    /** Delay by exponential backoff
     * @param maxAttempts Maximum number of attempts
     * @param baseDelay Base delay in milliseconds
     * @return True if the delay was applied, false if the maximum number of attempts was reached
     */
    suspend fun exponentialBackoffDelay(): Boolean {
        return if (attempts < maxAttempts) {
            attempts++

            val delayMillis = calculateBackoffDelay(attempts) // Exponential backoff
            println("$logMessage: Retrying in ${delayMillis / 1000}s (attempt $attempts/$maxAttempts)")
            delay(delayMillis)
            true
        } else {
            println("$logMessage: Max attempts reached. Stopping.")
            false
        }
    }

    /** Calculate exponential backoff delay in milliseconds */
    private fun calculateBackoffDelay(attempt: Int): Long {
        val exponentialDelay = baseDelay * (1 shl (attempt - 1)) // 2s, 4s, 8s, 16s...
        // Add randomness to prevent multiple clients from reconnecting at exactly the same time
        val jitter = (0..1000).random()
        return exponentialDelay + jitter
    }

}