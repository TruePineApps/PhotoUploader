package com.truepineapps.photouploader.ui.viewmodel

import app.cash.turbine.test
import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.loggerConfigInit
import com.truepineapps.photouploader.feature.auth.GoogleAuthService
import com.truepineapps.photouploader.feature.uploader.domain.model.Album
import com.truepineapps.photouploader.feature.uploader.domain.model.Photo
import com.truepineapps.photouploader.feature.uploader.data.repository.PhotoDirectoryRepository
import com.truepineapps.photouploader.app.di.viewModelModule
import com.truepineapps.photouploader.core.io.PlatformFileSystem
import com.truepineapps.photouploader.core.log.TimestampMessageFormatter
import com.truepineapps.photouploader.ui.util.FakePlatformFileSystem
import com.truepineapps.photouploader.ui.util.GoogleAuthServiceTestStub
import com.truepineapps.photouploader.ui.util.createTestKmpFile
import com.truepineapps.photouploader.feature.uploader.viewmodel.PhotoUploaderViewModel
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.AlbumUiState
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UploadStatus
import com.truepineapps.photouploader.core.util.UiTextString
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
    fun `album status becomes Waiting at the start`() = runTest {
        testDerivedAlbumStatusForThreePhotos(
            photoStatus1 = UploadStatus.Waiting,
            photoStatus2 = UploadStatus.None,
            photoStatus3 = UploadStatus.None,
            expectedAlbumStatus = UploadStatus.Waiting
        )
    }

    @Test
    fun `album status becomes Uploading if a photo starts uploading`() = runTest {
        testDerivedAlbumStatusForThreePhotos(
            photoStatus1 = UploadStatus.Uploading,
            photoStatus2 = UploadStatus.Waiting,
            photoStatus3 = UploadStatus.Waiting,
            expectedAlbumStatus = UploadStatus.Uploading
        )
    }

    @Test
    fun `album status remains Uploading when the second photo starts uploading`() = runTest {
        testDerivedAlbumStatusForThreePhotos(
            photoStatus1 = UploadStatus.Success,
            photoStatus2 = UploadStatus.Uploading,
            photoStatus3 = UploadStatus.Waiting,
            expectedAlbumStatus = UploadStatus.Uploading
        )
    }

    @Test
    fun `album status becomes UploadingError if a photo fails while another is uploading`() = runTest {
        val message = UiTextString("fail")
        testDerivedAlbumStatusForThreePhotos(
            photoStatus1 = UploadStatus.Success,
            photoStatus2 = UploadStatus.Uploading,
            photoStatus3 = UploadStatus.Error(message),
            expectedAlbumStatus = UploadStatus.UploadingError(message)
        )
    }

    @Test
    fun `album status becomes Error if a photo upload failed`() = runTest {
        val message = UiTextString("fail")
        testDerivedAlbumStatusForThreePhotos(
            photoStatus1 = UploadStatus.Success,
            photoStatus2 = UploadStatus.Error(message),
            photoStatus3 = UploadStatus.Success,
            expectedAlbumStatus = UploadStatus.Error(message)
        )
    }

    @Test
    fun `album status becomes Success only when all enabled photos are Success`() = runTest {
        testDerivedAlbumStatusForThreePhotos(
            photoStatus1 = UploadStatus.Success,
            photoStatus2 = UploadStatus.Success,
            photoStatus3 = UploadStatus.Success,
            expectedAlbumStatus = UploadStatus.Success
        )
    }

    private fun testDerivedAlbumStatusForThreePhotos(
        photoStatus1: UploadStatus,
        photoStatus2: UploadStatus,
        photoStatus3: UploadStatus,
        expectedAlbumStatus: UploadStatus,
    ) = runTest {
        val viewModel: PhotoUploaderViewModel by inject()
        val repository: PhotoDirectoryRepository by inject()
        val photo1 = createDummyPhoto("/p1")
        val photo2 = createDummyPhoto("/p2")
        val photo3 = createDummyPhoto("/p3")

        viewModel.uiState.test {
            assertEquals(emptyList(), awaitItem().albumUiStates)

            // Adding the albums to the repository fills the view model state
            repository.updateAlbums(listOf(createDummyAlbum("a1", listOf(photo1, photo2, photo3))))
            // Set all upload states to Waiting to mimic an upload
            viewModel.setWaitingStatus(awaitItem().albumUiStates)
            awaitItem()

            viewModel.updatePhotoStatus("a1", "/p1".toPath(), photoStatus1)
            viewModel.updatePhotoStatus("a1", "/p2".toPath(), photoStatus2)
            viewModel.updatePhotoStatus("a1", "/p3".toPath(), photoStatus3)
            val updatedAlbum = awaitItem().albumUiStates.first()

            assertPhotoStatus(updatedAlbum, "/p1", photoStatus1)
            assertPhotoStatus(updatedAlbum, "/p2", photoStatus2)
            assertPhotoStatus(updatedAlbum, "/p3", photoStatus3)
            assertAlbumStatus(updatedAlbum, expectedAlbumStatus)
        }
    }

    // region Helper Methods
    private inline fun <reified T : UploadStatus> assertAlbumStatus(albumUiState: AlbumUiState, expectedStatus: T) {
        assertEquals(expectedStatus::class, albumUiState.uploadStatus::class,
            "Album status should be $expectedStatus, but was ${albumUiState.uploadStatus::class.simpleName}"
        )
    }

    private inline fun <reified T : UploadStatus> assertPhotoStatus(albumUiState: AlbumUiState, photoPath: String, expectedStatus: T) {
        val photo = albumUiState.photoUiStates.find { it.path == photoPath.toPath() }!!
        assertEquals(expectedStatus, photo.uploadStatus,
            "Photo '${photo.name}' status should be $expectedStatus, but was ${photo.uploadStatus::class.simpleName}"
        )
    }

    private fun createDummyPhoto(path: String) = Photo(
                kmpFile = createTestKmpFile(path),
                path = path.toPath(),
                name = path.substringAfterLast('/'),
            )

    private fun createDummyAlbum(id: String, photos: List<Photo>) = Album(
        id = id,
        kmpFile = createTestKmpFile("/$id"),
        path = "/$id".toPath(),
        name = "Test Album",
        group = "Test Group",
        photos = photos,
    )
    // endregion
}
