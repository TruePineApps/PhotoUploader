package com.truepineapps.photouploader.ui

import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.core.PlatformContext
import java.io.File
import org.mockito.Mockito

actual fun createTestKmpFile(path: String): KmpFile {
    val file = KmpFile(File(path))
    registerTestKmpFile(file, path)
    return file
}

actual fun createTestPlatformContext(): PlatformContext {
    return Mockito.mock(PlatformContext::class.java)
}
