package com.truepineapps.photouploader.ui

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import platform.Foundation.NSURL
import platform.UIKit.UIViewController

actual fun createTestKmpFile(path: String): KmpFile {
    val nsUrl = NSURL.fileURLWithPath(path)
    val result = KmpFile(nsUrl) 
    registerTestKmpFile(result, path)
    return result
}

actual fun createTestPlatformContext(): PlatformContext {
    val dummyController = UIViewController()
    // Suppress visibility check to access private constructor for testing
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    return object : PlatformContext(dummyController) {}
}
