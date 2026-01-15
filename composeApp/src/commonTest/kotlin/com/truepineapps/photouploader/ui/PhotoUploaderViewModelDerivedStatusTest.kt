package com.truepineapps.photouploader.ui

import app.cash.turbine.test
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.auth.GoogleAuthService
import com.truepineapps.photouploader.data.PhotoDirectoryRepository
import com.truepineapps.photouploader.di.viewModelModule
import com.truepineapps.photouploader.io.PlatformFileSystem
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.model.UploadStatus
import com.truepineapps.photouploader.ui.screen.uploader.PhotoUploaderViewModel
import com.truepineapps.photouploader.util.UiTextString
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoUploaderViewModelDerivedStatusTest : KoinTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        stopKoin()
        startKoin {
            modules(
                viewModelModule(),
                module {
                    single<PlatformFileSystem> { FakePlatformFileSystem(FakeFileSystem()) }
                    single { PhotoDirectoryRepository(get()) }
                    single<GoogleAuthService> { GoogleAuthServiceTestStub() }
                    single { Logger.withTag("Test") }
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
    fun `album status becomes Error if a photo upload fails`() = runTest {
        val viewModel: PhotoUploaderViewModel by inject()
        val repository: PhotoDirectoryRepository by inject()
        val photo1 = createDummyPhoto("/p1", UploadStatus.Success)
        val photo2 = createDummyPhoto("/p2", UploadStatus.None)

        viewModel.uiState.test {
            assertEquals(emptyList(), awaitItem().albums)

            repository.updateAlbums(listOf(createDummyAlbum("a1", listOf(photo1, photo2))))
            awaitItem() // Consume populated state

            viewModel.updatePhotoStatus("a1", "/p2".toPath(), UploadStatus.Error(UiTextString("fail")))
            val updatedAlbum = awaitItem().albums.first()

            assertPhotoStatusIs<UploadStatus.Success>(updatedAlbum, "/p1")
            assertPhotoStatusIs<UploadStatus.Error>(updatedAlbum, "/p2")
            assertAlbumStatusIs<UploadStatus.Error>(updatedAlbum)
        }
    }

    @Test
    fun `album status becomes UploadingError if a photo fails while another is uploading`() = runTest {
        val viewModel: PhotoUploaderViewModel by inject()
        val repository: PhotoDirectoryRepository by inject()
        val photo1 = createDummyPhoto("/p1", UploadStatus.Uploading)
        val photo2 = createDummyPhoto("/p2", UploadStatus.None)

        viewModel.uiState.test {
            assertEquals(emptyList(), awaitItem().albums)

            repository.updateAlbums(listOf(createDummyAlbum("a1", listOf(photo1, photo2))))
            awaitItem() // Consume populated state

            viewModel.updatePhotoStatus("a1", "/p2".toPath(), UploadStatus.Error(UiTextString("fail")))
            val updatedAlbum = awaitItem().albums.first()

            assertPhotoStatusIs<UploadStatus.Uploading>(updatedAlbum, "/p1")
            assertPhotoStatusIs<UploadStatus.Error>(updatedAlbum, "/p2")
            assertAlbumStatusIs<UploadStatus.UploadingError>(updatedAlbum)
        }
    }

    @Test
    fun `album status becomes Success only when all enabled photos are Success`() = runTest {
        val viewModel: PhotoUploaderViewModel by inject()
        val repository: PhotoDirectoryRepository by inject()
        val photo1 = createDummyPhoto("/p1", UploadStatus.Success)
        val photo2 = createDummyPhoto("/p2", UploadStatus.None)
        val photo3 = createDummyPhoto("/p3", UploadStatus.Success, isEnabled = false)

        viewModel.uiState.test {
            assertEquals(emptyList(), awaitItem().albums)

            repository.updateAlbums(listOf(createDummyAlbum("a1", listOf(photo1, photo2, photo3))))
            awaitItem() // consume populated

            viewModel.updatePhotoStatus("a1", "/p2".toPath(), UploadStatus.Success)
            val updatedAlbum = awaitItem().albums.first()

            assertPhotoStatusIs<UploadStatus.Success>(updatedAlbum, "/p1")
            assertPhotoStatusIs<UploadStatus.Success>(updatedAlbum, "/p2")
            assertPhotoStatusIs<UploadStatus.Success>(updatedAlbum, "/p3")
            assertAlbumStatusIs<UploadStatus.Success>(updatedAlbum)
        }
    }

    // region Helper Methods
    private inline fun <reified T : UploadStatus> assertAlbumStatusIs(album: Album) {
        assertTrue(album.uploadStatus is T,
            "Album status should be ${T::class.simpleName}, but was ${album.uploadStatus::class.simpleName}"
        )
    }

    private inline fun <reified T : UploadStatus> assertPhotoStatusIs(album: Album, photoPath: String) {
        val photo = album.photos.find { it.path == photoPath.toPath() }!!
        assertTrue(photo.uploadStatus is T,
            "Photo '${photo.name}' status should be ${T::class.simpleName}, but was ${photo.uploadStatus::class.simpleName}"
        )
    }

    private fun createDummyPhoto(path: String, status: UploadStatus, isEnabled: Boolean = true) = Photo(
        kmpFile = createTestKmpFile(path),
        path = path.toPath(),
        name = path.substringAfterLast('/'),
        uploadStatus = status,
        isEnabled = isEnabled
    )

    private fun createDummyAlbum(id: String, photos: List<Photo>) = Album(
        id = id,
        kmpFile = createTestKmpFile("/$id"),
        path = "/$id".toPath(),
        name = "Test Album",
        group = "Test Group",
        photos = photos,
        coverPhoto = photos.first()
    )
    // endregion
}
