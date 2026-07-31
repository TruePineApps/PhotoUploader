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
