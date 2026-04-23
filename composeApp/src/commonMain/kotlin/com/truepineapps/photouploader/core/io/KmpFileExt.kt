package com.truepineapps.photouploader.core.io

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import okio.Source

expect fun KmpFile.list(context: PlatformContext): List<KmpFile>
expect fun KmpFile.getAbsolutePath(context: PlatformContext): String?
expect fun KmpFile.isDir(context: PlatformContext): Boolean
expect fun KmpFile.getDisplayName(context: PlatformContext): String
expect fun KmpFile.source(context: PlatformContext): Source
