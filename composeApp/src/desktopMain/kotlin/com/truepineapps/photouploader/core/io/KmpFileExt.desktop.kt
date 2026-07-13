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
