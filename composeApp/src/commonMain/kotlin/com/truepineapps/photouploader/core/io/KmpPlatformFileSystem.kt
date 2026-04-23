package com.truepineapps.photouploader.core.io

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.io.isDirectory
import com.truepineapps.photouploader.core.io.getDisplayName
import com.truepineapps.photouploader.core.io.isDir
import com.truepineapps.photouploader.core.io.list
import com.truepineapps.photouploader.core.io.source
import okio.Source

class KmpPlatformFileSystem : com.truepineapps.photouploader.core.io.PlatformFileSystem {
    override fun list(file: KmpFile, context: PlatformContext): List<KmpFile> =
            file.list(context)

    override fun isDir(file: KmpFile, context: PlatformContext): Boolean =
            file.isDir(context)

    override fun isDirectory(file: KmpFile, context: PlatformContext): Boolean =
            file.isDirectory(context)

    override fun getDisplayName(file: KmpFile, context: PlatformContext): String =
            file.getDisplayName(context)

    override fun getPath(file: KmpFile, context: PlatformContext): String? =
            file.getPath(context)

    override fun getName(file: KmpFile, context: PlatformContext): String? =
            file.getName(context)

    override fun source(file: KmpFile, context: PlatformContext): Source =
            file.source(context)
}
