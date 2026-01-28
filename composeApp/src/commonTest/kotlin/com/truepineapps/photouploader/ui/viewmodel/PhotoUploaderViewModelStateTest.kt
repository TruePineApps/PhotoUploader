package com.truepineapps.photouploader.ui.viewmodel

import app.cash.turbine.test
import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.loggerConfigInit
import com.truepineapps.photouploader.auth.GoogleAuthService
import com.truepineapps.photouploader.data.Album
import com.truepineapps.photouploader.data.Photo
import com.truepineapps.photouploader.data.PhotoDirectoryRepository
import com.truepineapps.photouploader.di.viewModelModule
import com.truepineapps.photouploader.io.PlatformFileSystem
import com.truepineapps.photouploader.log.TimestampMessageFormatter
import com.truepineapps.photouploader.ui.util.FakePlatformFileSystem
import com.truepineapps.photouploader.ui.util.GoogleAuthServiceTestStub
import com.truepineapps.photouploader.ui.util.createTestKmpFile
import com.truepineapps.photouploader.ui.screen.uploader.PhotoUploaderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoUploaderViewModelStateTest : KoinTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Ensure clean Koin instance for each test
        stopKoin()
        startKoin {
            modules(
                viewModelModule(),
                module {
                    single<PlatformFileSystem> { FakePlatformFileSystem(FakeFileSystem()) }
                    single { PhotoDirectoryRepository(get()) }
                    single<GoogleAuthService> { GoogleAuthServiceTestStub() }
                    single {
                        Logger(
                            config = loggerConfigInit(CommonWriter(TimestampMessageFormatter)),
                            tag = "Test"
                        )
                    }

                }
            )
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `toggleAlbum correctly updates isEnabled state of the target album`() = runTest {
        val viewModel: PhotoUploaderViewModel by inject()
        val repository: PhotoDirectoryRepository by inject()
        val initialAlbums = listOf(
            createDummyAlbum(id = "album1"),
            createDummyAlbum(id = "album2")
        )

        viewModel.uiState.test {
            // 1. Assert initial state
            assertEquals(emptyList(), awaitItem().albumUiStates)

            // 2. Trigger state change
            repository.updateAlbums(initialAlbums)

            // 3. Assert populated state and toggle once
            var state = awaitItem()
            assertTrue(state.albumUiStates.find { it.id == "album1" }!!.isEnabled)

            viewModel.toggleAlbum("album1")
            state = awaitItem()
            assertFalse(state.albumUiStates.find { it.id == "album1" }!!.isEnabled)
            assertTrue(state.albumUiStates.find { it.id == "album2" }!!.isEnabled) // Should not change

            // 4. Toggle back
            viewModel.toggleAlbum("album1")
            state = awaitItem()
            assertTrue(state.albumUiStates.find { it.id == "album1" }!!.isEnabled)
        }
    }

    @Test
    fun `toggleAlbums correctly updates isEnabled state for a group`() = runTest {
        val viewModel: PhotoUploaderViewModel by inject()
        val repository: PhotoDirectoryRepository by inject()
        val albums = listOf(
            createDummyAlbum(id = "album1"),
            createDummyAlbum(id = "album2"),
            createDummyAlbum(id = "album3")
        )

        viewModel.uiState.test {
            assertEquals(emptyList(), awaitItem().albumUiStates)

            repository.updateAlbums(albums)
            val populatedState = awaitItem()
            assertEquals(3, populatedState.albumUiStates.size)

            viewModel.toggleAlbums(
                listOf(populatedState.albumUiStates[0], populatedState.albumUiStates[2]),
                isEnabled = false
            )

            val finalState = awaitItem()
            assertFalse(finalState.albumUiStates.find { it.id == "album1" }!!.isEnabled)
            assertTrue(finalState.albumUiStates.find { it.id == "album2" }!!.isEnabled) // Unchanged
            assertFalse(finalState.albumUiStates.find { it.id == "album3" }!!.isEnabled)
        }
    }

    @Test
    fun `togglePhoto correctly updates isEnabled state of the target photo`() = runTest {
        val viewModel: PhotoUploaderViewModel by inject()
        val repository: PhotoDirectoryRepository by inject()
        val photo1 = createDummyPhoto(path = "/photo1")
        val photo2 = createDummyPhoto(path = "/photo2")
        val album = createDummyAlbum(id = "album1", photos = listOf(photo1, photo2))

        viewModel.uiState.test {
            assertEquals(emptyList(), awaitItem().albumUiStates)

            repository.updateAlbums(listOf(album))
            awaitItem() // Consume populated state

            viewModel.togglePhoto("album1", "/photo2".toPath())

            val updatedAlbum = awaitItem().albumUiStates.first()
            assertFalse(updatedAlbum.photoUiStates.find { it.path == "/photo2".toPath() }!!.isEnabled)
            assertTrue(updatedAlbum.photoUiStates.find { it.path == "/photo1".toPath() }!!.isEnabled)
        }
    }

    @Test
    fun `renameAlbum correctly updates the name of the target album`() = runTest {
        val viewModel: PhotoUploaderViewModel by inject()
        val repository: PhotoDirectoryRepository by inject()
        val album = createDummyAlbum(id = "album1", name = "Old Name")

        viewModel.uiState.test {
            assertEquals(emptyList(), awaitItem().albumUiStates)

            repository.updateAlbums(listOf(album))
            awaitItem() // Consume populated state

            viewModel.renameAlbum("album1", "New Album Name")

            val updatedAlbum = awaitItem().albumUiStates.first()
            assertEquals("New Album Name", updatedAlbum.name)
        }
    }

    // Helper functions to create test data
    private fun createDummyPhoto(path: String) = Photo(
        kmpFile = createTestKmpFile(path),
        path = path.toPath(),
        name = "photo.jpg",
    )

    private fun createDummyAlbum(
        id: String,
        name: String = "Test Album",
        photos: List<Photo> = listOf(createDummyPhoto(path = "/$id/p1")),
    ) = Album(
        id = id,
        kmpFile = createTestKmpFile("/$id"),
        path = "/$id".toPath(),
        name = name,
        group = "Test Group",
        photos = photos,
    )
}
