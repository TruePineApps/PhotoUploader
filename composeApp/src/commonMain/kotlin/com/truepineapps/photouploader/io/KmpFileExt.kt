package com.truepineapps.photouploader.io

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile

expect fun KmpFile.list(context: PlatformContext): List<KmpFile>
expect fun KmpFile.getAbsolutePath(context: PlatformContext): String?
expect fun KmpFile.isDir(context: PlatformContext): Boolean
expect fun KmpFile.getDisplayName(context: PlatformContext): String
