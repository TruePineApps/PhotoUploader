package com.truepineapps.photouploader.ui

import android.content.Context
import android.net.Uri
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import org.mockito.Mockito

actual fun createTestKmpFile(path: String): KmpFile {
    // KmpFile is a typealias for Uri on Android.
    // We use Mockito to mock the abstract Uri class.
    val mockUri = Mockito.mock(Uri::class.java)
    
    // Configure the mock to return the path when toString() is called.
    // This allows FakePlatformFileSystem to retrieve the path.
    Mockito.`when`(mockUri.toString()).thenReturn(path)
    val result = KmpFile(mockUri)
    registerTestKmpFile(result, path)
    return result
}

actual fun createTestPlatformContext(): PlatformContext {
    // PlatformContext is a typealias for Context on Android.
    // We use Mockito to mock the abstract Context class.
    return Mockito.mock(Context::class.java)
}
