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

import java.util.concurrent.TimeUnit
import java.util.Locale
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Detects whether the OS-level "High Contrast" accessibility setting is enabled.
 *
 * Only performs real detection on Linux (Ubuntu/GNOME). On Windows and macOS
 * (or any non-Linux JVM), this always returns false immediately without
 * running any external process — so it can never produce a false positive
 * on those platforms.
 *
 * Place this in your `jvmMain` / `desktopMain` source set.
 */
object LinuxHighContrastDetector : KoinComponent {

    private val log: Logger by inject()

    /**
     * A gsettings key to check, along with how to interpret its raw string
     * value as "high contrast is on".
     */
    private data class GsettingsQuery(
        val schema: String,
        val key: String,
        val matches: (String) -> Boolean
    )

    // Priority order matters: the first query that returns *any* value wins,
    // and its own `matches` function decides true/false.
    private val queries = listOf(
        // Modern GNOME (42+)
        GsettingsQuery("org.gnome.desktop.interface", "high-contrast") {
            it.equals("true", ignoreCase = true)
        },
        // Older GNOME
        GsettingsQuery("org.gnome.desktop.a11y.interface", "high-contrast") {
            it.equals("true", ignoreCase = true)
        },
        // Fallback: some setups swap the GTK theme itself rather than (or in
        // addition to) flipping a boolean key.
        GsettingsQuery("org.gnome.desktop.interface", "gtk-theme") {
            it.contains("highcontrast", ignoreCase = true)
        }
    )

    /**
     * @Returns true if high contrast mode appears to be enabled, null if called on another platform than Linux.
     * Safe to call from any platform; only does work on Linux.
     */
    fun isHighContrastEnabled(): Boolean? {
        if (!isLinux()) return false

        return try {
            val (query, value) = runGsettingsBatch(queries) ?: return null
            query.matches(value)
        } catch (e: Exception) {
            // Any failure (gsettings missing, headless, no D-Bus session, etc.)
            // must never be reported as "high contrast enabled".
            log.e("Failed to detect Linux high contrast mode", e)
            false
        }
    }

    /**
     * Monitors the high contrast setting on Linux (GNOME).
     * Emits the current value immediately and then any subsequent changes.
     */
    fun monitorHighContrast(): Flow<Boolean> = callbackFlow {
        if (!isLinux()) {
            trySend(false)
            close()
            return@callbackFlow
        }

        val monitorProcess = try {
            // Monitor the unique schemas defined in `queries` for changes
            val schemas = queries.map { it.schema }.distinct()
            val monitorCommand = schemas.joinToString(" & ") { "gsettings monitor $it" }
            ProcessBuilder("sh", "-c", monitorCommand).start()
        } catch (e: Exception) {
            log.e("Failed to start gsettings monitor", e)
            trySend(isHighContrastEnabled() ?: false)
            close()
            return@callbackFlow
        }

        val reader = monitorProcess.inputStream.bufferedReader()
        
        // Initial state emission inside the flow
        trySend(isHighContrastEnabled() ?: false)

        // Monitor loop
        try {
            while (true) {
                val line = reader.readLine() ?: break
                // If any of the monitored schemas change, we re-evaluate the full state
                if (line.isNotEmpty()) {
                    trySend(isHighContrastEnabled() ?: false)
                }
            }
        } catch (e: Exception) {
            log.e("Error in gsettings monitor loop", e)
        } finally {
            monitorProcess.destroy()
            close()
        }

        awaitClose {
            monitorProcess.destroy()
        }
    }.flowOn(Dispatchers.IO)

    internal fun isLinux(): Boolean {
        val osName = System.getProperty("os.name")?.lowercase(Locale.ROOT).orEmpty()
        // Deliberately narrow: only "linux" matches. This avoids any chance
        // of matching "windows" or "mac os x" strings.
        return osName.contains("linux")
    }

    /**
     * Runs all queries in a single `sh` process (one gsettings call per
     * query, chained together in one script) instead of spawning a separate
     * process per key. This keeps the worst-case wait to one timeout window
     * instead of one timeout per query.
     *
     * Returns the (query, rawValue) pair for the earliest-listed query that
     * produced a successful, non-blank result, or null if none did.
     */
    private fun runGsettingsBatch(queries: List<GsettingsQuery>): Pair<GsettingsQuery, String>? {
        if (queries.isEmpty()) return null

        // Wrap each call with markers so we can split the combined stdout
        // back into per-query chunks, each tagged with its exit code.
        //
        // IMPORTANT: we capture each gsettings call into a variable first and
        // then explicitly echo it, so exactly one line is always emitted for
        // the value - even when gsettings fails and would otherwise print
        // nothing at all. Without this, a failed call collapses to zero
        // output lines, which breaks the fixed-line-count assumption the
        // parser below relies on and can cause it to read into the *next*
        // query's block.
        val script = buildString {
            queries.forEachIndexed { i, q ->
                append($$"__v$$i=$(gsettings get '$${q.schema}' '$${q.key}' 2>/dev/null); ")
                append($$"__ec$$i=$?; ")
                append("echo '@@@$i@@@'; ")
                append($$"echo \"$__v$$i\"; ")
                append($$"echo \"@@@EXIT:$$i:$__ec$$i@@@\"; ")
            }
        }

        val process = try {
            ProcessBuilder("sh", "-c", script)
                .redirectErrorStream(false)
                .start()
        } catch (e: Exception) {
            // e.g. /bin/sh not available (shouldn't happen on Linux, but be safe)
            log.e("Error retrieving high-contrast settings", e)
            return null
        }

        // Read all combined output. Each query's stdout is at most one short
        // line, so this can't deadlock on pipe buffers here.
        val output = process.inputStream.bufferedReader().readText()

        val finished = process.waitFor(2, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            return null
        }

        // Matches: @@@<index>@@@ \n <value (possibly empty)> \n @@@EXIT:<index>:<code>@@@
        // The \1 backreference requires the exit marker's index to match the
        // opening marker's index for this same match, so a block can never
        // be resolved using a neighboring block's exit code - even if the
        // line-count guarantee above were ever violated for some other
        // reason, this ensures we'd fail to match (and thus skip) rather
        // than silently attribute the wrong result.
        val pattern = Regex("""@@@(\d+)@@@\r?\n(.*?)\r?\n@@@EXIT:\1:(\d+)@@@""", RegexOption.DOT_MATCHES_ALL)

        val resultsByIndex: Map<Int, Pair<Int, String>> = pattern.findAll(output).associate { m ->
            val idx = m.groupValues[1].toInt()
            val exitCode = m.groupValues[3].toIntOrNull() ?: -1
            // gsettings wraps string values in single quotes, e.g. 'Yaru-dark'
            val rawValue = m.groupValues[2].trim().removeSurrounding("'")
            idx to (exitCode to rawValue)
        }

        // Return the first (highest-priority) query that actually succeeded.
        queries.forEachIndexed { i, q ->
            val (exitCode, value) = resultsByIndex[i] ?: return@forEachIndexed
            if (exitCode == 0 && value.isNotBlank()) {
                return q to value
            }
        }

        return null
    }
}