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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

object DesktopThemeMonitor {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val themeState: StateFlow<DesktopThemeState> = run {
        val osName = System.getProperty("os.name")?.lowercase(Locale.ROOT).orEmpty()
        when {
            osName.contains("win") -> {
                WindowsHighContrastDetector.monitorHighContrast()
            }
            osName.contains("linux") -> {
                LinuxHighContrastDetector.monitorHighContrast().map { isHighContrast ->
                    DesktopThemeState(
                        contrastLevel = if (isHighContrast) ContrastLevel.High else ContrastLevel.Standard,
                        isHighContrastDark = null
                    )
                }
            }
            else -> {
                kotlinx.coroutines.flow.flowOf(DesktopThemeState())
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = getInitialState()
        )
    }

    private fun getInitialState(): DesktopThemeState {
        val osName = System.getProperty("os.name")?.lowercase(Locale.ROOT).orEmpty()
        return when {
            osName.contains("win") -> {
                WindowsHighContrastDetector.calculateCurrentThemeState()
            }
            osName.contains("linux") -> {
                val isHighContrast = LinuxHighContrastDetector.isHighContrastEnabled() ?: false
                DesktopThemeState(
                    contrastLevel = if (isHighContrast) ContrastLevel.High else ContrastLevel.Standard,
                    isHighContrastDark = null
                )
            }
            else -> DesktopThemeState()
        }
    }
}
