package com.truepineapps.photouploader.core.io

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import okio.Sink
import okio.Source

expect fun getApplicationHomeDirectory(context: PlatformContext): String
expect fun KmpFile.list(context: PlatformContext): List<KmpFile>
expect fun KmpFile.getAbsolutePath(context: PlatformContext): String?
expect fun KmpFile.isDir(context: PlatformContext): Boolean
expect fun KmpFile.getDisplayName(context: PlatformContext): String
expect fun KmpFile.source(context: PlatformContext): Source
expect fun KmpFile.sink(context: PlatformContext): Sink
expect fun KmpFile.writeText(context: PlatformContext, text: String)
expect fun KmpFile.readText(context: PlatformContext): String
expect fun String.toKmpFile(): KmpFile
