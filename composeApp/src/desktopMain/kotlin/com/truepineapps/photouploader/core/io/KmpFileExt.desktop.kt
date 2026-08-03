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
import com.mohamedrejeb.calf.io.toKmpFile
import okio.Sink
import okio.Source
import okio.sink
import okio.source
import java.io.File

actual fun getApplicationHomeDirectory(context: PlatformContext): String {
    val dir = File(System.getProperty("user.home"), ".photouploader")
    if (!dir.exists()) dir.mkdirs()
    return dir.absolutePath
}

actual fun KmpFile.list(context: PlatformContext): List<KmpFile> = file.listFiles()?.map { KmpFile(it) } ?: emptyList()

actual fun KmpFile.getAbsolutePath(context: PlatformContext): String? = file.absolutePath

actual fun KmpFile.isDir(context: PlatformContext): Boolean = file.isDirectory

actual fun KmpFile.getDisplayName(context: PlatformContext): String = file.name

actual fun KmpFile.source(context: PlatformContext): Source = file.source()

actual fun KmpFile.sink(context: PlatformContext): Sink = file.sink()

actual fun KmpFile.writeText(context: PlatformContext, text: String) {
    file.parentFile?.mkdirs()
    file.writeText(text)
}

actual fun KmpFile.readText(context: PlatformContext): String = file.readText()

actual fun String.toKmpFile(): KmpFile = File(this).toKmpFile()
