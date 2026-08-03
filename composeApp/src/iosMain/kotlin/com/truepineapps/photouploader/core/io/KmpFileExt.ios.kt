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

package com.truepineapps.photouploader.core.io

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.io.isDirectory
import com.mohamedrejeb.calf.io.toKmpFile
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source
import okio.buffer
import okio.use
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL

actual fun getApplicationHomeDirectory(context: PlatformContext): String {
    val paths = NSFileManager.defaultManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
    return (paths.first() as NSURL).path ?: ""
}

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

actual fun KmpFile.source(context: PlatformContext): Source {
    val pathString = getPath(context) ?: ""
    return FileSystem.SYSTEM.source(pathString.toPath())
}

actual fun KmpFile.sink(context: PlatformContext): Sink {
    val pathString = getPath(context) ?: throw Exception("Path not found")
    return FileSystem.SYSTEM.sink(pathString.toPath())
}

actual fun KmpFile.writeText(context: PlatformContext, text: String) {
    val pathString = getPath(context) ?: return
    FileSystem.SYSTEM.sink(pathString.toPath()).buffer().use {
        it.writeUtf8(text)
    }
}

actual fun KmpFile.readText(context: PlatformContext): String {
    val pathString = getPath(context) ?: return ""
    return FileSystem.SYSTEM.source(pathString.toPath()).buffer().use {
        it.readUtf8()
    }
}

actual fun String.toKmpFile(): KmpFile  = NSURL.fileURLWithPath(this).toKmpFile()