package com.truepineapps.photouploader.core.io

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import okio.Source

interface PlatformFileSystem {
    fun list(file: KmpFile, context: PlatformContext): List<KmpFile>
    fun isDir(file: KmpFile, context: PlatformContext): Boolean
    fun isDirectory(file: KmpFile, context: PlatformContext): Boolean
    fun getDisplayName(file: KmpFile, context: PlatformContext): String
    fun getPath(file: KmpFile, context: PlatformContext): String?
    fun getName(file: KmpFile, context: PlatformContext): String?
    fun source(file: KmpFile, context: PlatformContext): Source
}
