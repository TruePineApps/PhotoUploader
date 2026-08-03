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

import com.sun.jna.platform.win32.Advapi32Util.registryGetStringValue
import com.sun.jna.platform.win32.WinReg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.awt.SystemColor

object WindowsHighContrastDetector {

    private const val COLORS_REGISTRY_PATH = "Control Panel\\Colors"
    private const val ACCESSIBILITY_REGISTRY_PATH = "Control Panel\\Accessibility\\HighContrast"

    /**
     * Determines if High Contrast is currently active by reading the registry directly.
     * This avoids race conditions with the AWT toolkit's property cache.
     * The 'Flags' value in the accessibility key is odd if High Contrast is ON.
     */
    fun isHighContrastActive(): Boolean = try {
        val flags = registryGetStringValue(
            WinReg.HKEY_CURRENT_USER,
            ACCESSIBILITY_REGISTRY_PATH,
            "Flags",
        ).toIntOrNull() ?: 0
        (flags % 2) != 0
    } catch (_: Exception) {
        false
    }

    /**
     * Determines if the current High Contrast theme is Dark or Light by checking the luminance
     * of the system's "Window" background color from the registry.
     */
    private fun isCurrentThemeLuminanceDark(): Boolean = try {
        val windowColorStr = registryGetStringValue(
            WinReg.HKEY_CURRENT_USER,
            COLORS_REGISTRY_PATH,
            "Window"
        )
        val parts = windowColorStr.split(" ")
        if (parts.size == 3) {
            val luminance = getLuminance(
                red = parts[0].toInt(),
                green = parts[1].toInt(),
                blue = parts[2].toInt()
            )
            luminance < 0.5
        } else {
            false
        }
    } catch (_: Exception) {
        val windowColor = SystemColor.window
        val luminance = getLuminance(windowColor.red, windowColor.green, windowColor.blue)
        luminance < 0.5
    }

    private fun getLuminance(red: Int, green: Int, blue: Int): Double =
        ((0.2126 * red) + (0.7152 * green) + (0.0722 * blue)) / 255.0

    /**
     * Calculates the current theme state based on registry settings.
     */
    fun calculateCurrentThemeState(): DesktopThemeState {
        val isOn = isHighContrastActive()
        val contrastLevel = if (isOn) ContrastLevel.High else ContrastLevel.Standard
        val isDark = if (isOn) isCurrentThemeLuminanceDark() else null
        return DesktopThemeState(contrastLevel, isDark)
    }

    /**
     * Monitors High Contrast changes on Windows.
     * It listens to system color and accessibility flag changes in the registry.
     * This is the most reliable way to catch theme shifts immediately.
     */
    fun monitorHighContrast(): Flow<DesktopThemeState> = callbackFlow {
        val onUpdate = {
            trySend(calculateCurrentThemeState())
            // startRegistryMonitor expects a Unit return value, not the return value of trySend
            Unit
        }

        val colorsThread =
            startRegistryMonitor(COLORS_REGISTRY_PATH, "WindowsThemeMonitor-Colors", onUpdate)
        val accessibilityThread = startRegistryMonitor(
            ACCESSIBILITY_REGISTRY_PATH,
            "WindowsThemeMonitor-Accessibility",
            onUpdate
        )

        // Initial state
        onUpdate()

        awaitClose {
            colorsThread.interrupt()
            accessibilityThread.interrupt()
        }
    }.flowOn(Dispatchers.IO)

}