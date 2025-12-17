package com.truepineapps.photouploader.io

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import okio.Source
import okio.source


actual fun KmpFile.list(context: PlatformContext): List<KmpFile> = file.listFiles()?.map { KmpFile(it) } ?: emptyList()

actual fun KmpFile.getAbsolutePath(context: PlatformContext): String? = file.absolutePath

actual fun KmpFile.isDir(context: PlatformContext): Boolean = file.isDirectory

actual fun KmpFile.getDisplayName(context: PlatformContext): String = file.name

actual fun KmpFile.source(context: PlatformContext): Source = file.source()
