package com.truepineapps.photouploader.io

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile

interface PlatformFileSystem {
    fun list(file: KmpFile, context: PlatformContext): List<KmpFile>
    fun isDir(file: KmpFile, context: PlatformContext): Boolean
    fun isDirectory(file: KmpFile, context: PlatformContext): Boolean
    fun getDisplayName(file: KmpFile, context: PlatformContext): String
    fun getPath(file: KmpFile, context: PlatformContext): String?
    fun getName(file: KmpFile, context: PlatformContext): String?
    suspend fun read(file: KmpFile, context: PlatformContext): ByteArray
}

