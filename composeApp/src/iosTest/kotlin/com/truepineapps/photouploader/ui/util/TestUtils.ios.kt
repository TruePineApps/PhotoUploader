package com.truepineapps.photouploader.ui.util

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import platform.Foundation.NSURL

actual fun createTestKmpFile(path: String): KmpFile {
    val nsUrl = NSURL.fileURLWithPath(path)
    val result = KmpFile(nsUrl)
    registerTestKmpFile(result, path)
    return result
}

actual fun createTestPlatformContext(): PlatformContext {
    // On iOS, Mockito is not supported, so we need to create some sort of PlatformContext object.
    // Due to actual PlatformContext having a private constructor, INSTANCE is the only way to get
    // a testable instance without major refactoring. We now on PlatformContext.INSTANCE
    // providing non-crashing default behavior for all methods called by the ViewModel/KmpFile
    // extensions. If iOS tests fail at runtime due to PlatformContext calls, a full refactoring of
    // PlatformContext to an interface will be required.
    return PlatformContext.INSTANCE
}
