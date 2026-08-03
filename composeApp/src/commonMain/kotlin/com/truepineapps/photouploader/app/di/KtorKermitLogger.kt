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
