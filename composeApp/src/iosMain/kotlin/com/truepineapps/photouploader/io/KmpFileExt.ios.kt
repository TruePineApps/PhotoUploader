package com.truepineapps.photouploader.io

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.io.isDirectory
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSURL

actual fun KmpFile.list(context: PlatformContext): List<KmpFile> {
    val pathString = getPath(context) ?: return emptyList()

    // 1. List files using Okio's FileSystem
    val childPaths = try {
        FileSystem.SYSTEM.list(pathString.toPath())
    } catch (e: Exception) {
        println("Exception while listing files: ${e::class.simpleName} - ${e.message}")
        return emptyList()
    }

    // Convert okio.Path -> String -> NSURL -> KmpFile
    return childPaths.map { okioPath ->
        val nsUrl = NSURL.fileURLWithPath(okioPath.toString())
        KmpFile(nsUrl)
    }
}

actual fun KmpFile.getAbsolutePath(context: PlatformContext): String? = getPath(context)

actual fun KmpFile.isDir(context: PlatformContext): Boolean = isDirectory(context)

actual fun KmpFile.getDisplayName(context: PlatformContext): String = getName(context) ?: "Unknown"
