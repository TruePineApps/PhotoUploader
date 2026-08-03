/*
 * Copyright (c) 2026 True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.truepineapps.photouploader.core.util

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class ServiceUtil(val logMessage: String, val maxAttempts: Int = 5, val baseDelay: Long = 2000) {

    private var attempts = 0

    /** Delay by exponential backoff
     * @return True if the delay was applied, false if the maximum number of attempts was reached
     */
    suspend fun exponentialBackoffDelay(): Boolean {
        return if (attempts < maxAttempts) {
            attempts++

            val delayMillis = calculateBackoffDelay(attempts) // Exponential backoff
            println("$logMessage: Retrying in ${delayMillis / 1000}s (attempt $attempts/$maxAttempts)")
            delay(delayMillis.milliseconds)
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