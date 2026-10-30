package com.truepineapps.photouploader.feature.uploader.data.repository

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.core.domain.state.DataLoadingState
import com.truepineapps.photouploader.core.io.PlatformFileSystem
import com.truepineapps.photouploader.ui.util.FakePlatformFileSystem
import com.truepineapps.photouploader.ui.util.createTestKmpFile
import com.truepineapps.photouploader.ui.util.createTestPlatformContext
import kotlinx.coroutines.flow.toList
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhotoDirectoryRepositoryImplTest {

    @Test
    fun `cancel keeps partial albums and explicit reload scans the whole directory`() = kotlinx.coroutines.test.runTest {
        val fileSystem = FakeFileSystem()
        fileSystem.createDirectories("/root/a".toPath())
        fileSystem.createDirectories("/root/b".toPath())
        fileSystem.write("/root/a/a.jpg".toPath()) { writeUtf8("") }
        fileSystem.write("/root/b/b.jpg".toPath()) { writeUtf8("") }

        val delegate = FakePlatformFileSystem(fileSystem)
        lateinit var repository: PhotoDirectoryRepositoryImpl
        var cancellationSent = false
        var listCalls = 0
        val platformFileSystem = object : PlatformFileSystem by delegate {
            override fun list(file: KmpFile, context: PlatformContext): List<KmpFile> {
                listCalls++
                val entries = delegate.list(file, context)
                if (!cancellationSent && delegate.getPath(file, context) == "/root/b") {
                    cancellationSent = true
                    repository.cancelScan()
                }
                return entries
            }
        }
        repository = PhotoDirectoryRepositoryImpl(platformFileSystem, Logger(StaticConfig()))
        repository.setPath(createTestKmpFile("/root"), createTestPlatformContext())

        val loadingStates = mutableListOf<DataLoadingState>()
        repository.loadingState.toList(loadingStates)

        assertEquals(listOf(DataLoadingState.Loading, DataLoadingState.Success), loadingStates)
        assertEquals(listOf("/root/a"), repository.albums.value.map { it.path.toString() })
        assertTrue(repository.isScanPartial.value)
        assertFalse(repository.isScanning.value)

        val listCallsAfterPartialScan = listCalls
        repository.loadingState.toList(mutableListOf())
        assertEquals(listCallsAfterPartialScan, listCalls, "Partial results should be cached")

        repository.prepareReload()
        repository.loadingState.toList(mutableListOf())

        assertEquals(
            listOf("/root/a", "/root/b"),
            repository.albums.value.map { it.path.toString() }
        )
        assertFalse(repository.isScanPartial.value)
    }

    @Test
    fun `changing path while scanning discards old results`() = kotlinx.coroutines.test.runTest {
        val fileSystem = FakeFileSystem()
        fileSystem.createDirectories("/root/old".toPath())
        fileSystem.createDirectories("/root/new".toPath())
        fileSystem.write("/root/old/old.jpg".toPath()) { writeUtf8("") }
        fileSystem.write("/root/new/new.jpg".toPath()) { writeUtf8("") }

        val delegate = FakePlatformFileSystem(fileSystem)
        val platformContext = createTestPlatformContext()
        lateinit var repository: PhotoDirectoryRepositoryImpl
        var pathChanged = false
        val platformFileSystem = object : PlatformFileSystem by delegate {
            override fun list(file: KmpFile, context: PlatformContext): List<KmpFile> {
                val entries = delegate.list(file, context)
                if (!pathChanged && delegate.getPath(file, context) == "/root/old") {
                    pathChanged = true
                    repository.setPath(createTestKmpFile("/root/new"), platformContext)
                }
                return entries
            }
        }
        repository = PhotoDirectoryRepositoryImpl(platformFileSystem, Logger(StaticConfig()))
        repository.setPath(createTestKmpFile("/root/old"), platformContext)

        val staleLoadingStates = mutableListOf<DataLoadingState>()
        repository.loadingState.toList(staleLoadingStates)

        assertEquals(listOf<DataLoadingState>(DataLoadingState.Loading), staleLoadingStates)
        assertTrue(repository.albums.value.isEmpty())
        assertFalse(repository.isScanPartial.value)
        assertFalse(repository.isScanning.value)

        repository.loadingState.toList(mutableListOf())

        assertEquals(listOf("/root/new"), repository.albums.value.map { it.path.toString() })
        assertFalse(repository.isScanPartial.value)
    }

    @Test
    fun `reload of same path invalidates the previous scan`() = kotlinx.coroutines.test.runTest {
        val fileSystem = FakeFileSystem()
        fileSystem.createDirectories("/root/a".toPath())
        fileSystem.write("/root/a/a.jpg".toPath()) { writeUtf8("") }

        val delegate = FakePlatformFileSystem(fileSystem)
        lateinit var repository: PhotoDirectoryRepositoryImpl
        var reloadRequested = false
        var listCalls = 0
        val platformFileSystem = object : PlatformFileSystem by delegate {
            override fun list(file: KmpFile, context: PlatformContext): List<KmpFile> {
                listCalls++
                val entries = delegate.list(file, context)
                if (!reloadRequested) {
                    reloadRequested = true
                    repository.prepareReload()
                }
                return entries
            }
        }
        repository = PhotoDirectoryRepositoryImpl(platformFileSystem, Logger(StaticConfig()))
        repository.setPath(createTestKmpFile("/root"), createTestPlatformContext())

        val staleLoadingStates = mutableListOf<DataLoadingState>()
        repository.loadingState.toList(staleLoadingStates)

        assertEquals(listOf<DataLoadingState>(DataLoadingState.Loading), staleLoadingStates)
        assertTrue(repository.albums.value.isEmpty())

        repository.loadingState.toList(mutableListOf())

        assertEquals(3, listCalls)
        assertEquals(listOf("/root/a"), repository.albums.value.map { it.path.toString() })
        assertFalse(repository.isScanPartial.value)
    }
}
