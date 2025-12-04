package com.truepineapps.photouploader

object DesktopTypeUtil {
    val current: DesktopType
        get() {
            val system = System.getProperty("os.name").lowercase()
            return when {
                system.contains("win") ->
                    DesktopType.Windows

                system.contains("nix") || system.contains("nux") || system.contains("aix") ->
                    DesktopType.Linux

                system.contains("mac") ->
                    DesktopType.MacOS

                else ->
                    DesktopType.Linux
            }
        }
}

enum class DesktopType {
    Linux,
    MacOS,
    Windows
}