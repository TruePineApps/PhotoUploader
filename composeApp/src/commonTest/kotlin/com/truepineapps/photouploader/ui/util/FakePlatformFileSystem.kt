package com.truepineapps.photouploader.ui.util

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.core.io.PlatformFileSystem
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source

expect fun createTestKmpFile(path: String): KmpFile
expect fun createTestPlatformContext(): PlatformContext


private val testKmpFileRegistry = mutableMapOf<KmpFile, String>()

fun registerTestKmpFile(file: KmpFile, path: String) {
    testKmpFileRegistry[file] = path
}

class FakePlatformFileSystem(private val fileSystem: FileSystem) : PlatformFileSystem {
    // Registry to map KmpFile instances to their paths in the test environment
    override fun list(file: KmpFile, context: PlatformContext): List<KmpFile> {
        val pathString = getPath(file, context)
        val path = pathString.toPath()
        if (!fileSystem.exists(path) || !fileSystem.metadata(path).isDirectory) {
            return emptyList()
        }
        return fileSystem.list(path).map { 
            val p = it.toString()
            val kmp = createTestKmpFile(p)
            // Register children so subsequent calls work
            registerTestKmpFile(kmp, p)
            kmp
        }
    }

    override fun isDir(file: KmpFile, context: PlatformContext): Boolean {
        return isDirectory(file, context)
    }

    override fun isDirectory(file: KmpFile, context: PlatformContext): Boolean {
        val pathString = getPath(file, context)
        val path = pathString.toPath()
        return fileSystem.exists(path) && fileSystem.metadata(path).isDirectory
    }

    override fun getDisplayName(file: KmpFile, context: PlatformContext): String {
        val pathString = getPath(file, context)
        val path = pathString.toPath()
        return path.name
    }

    override fun getPath(file: KmpFile, context: PlatformContext): String {
        return testKmpFileRegistry[file] ?: file.toString()
    }

    override fun getName(file: KmpFile, context: PlatformContext): String {
        val pathString = getPath(file, context)
        val path = pathString.toPath()
        return path.name
    }

    override fun source(file: KmpFile, context: PlatformContext): Source {
        val pathString = getPath(file, context)
        return fileSystem.source(pathString.toPath())
    }

    override fun sink(file: KmpFile, context: PlatformContext): Sink {
        val pathString = getPath(file, context)
        return fileSystem.sink(pathString.toPath())
    }

}
