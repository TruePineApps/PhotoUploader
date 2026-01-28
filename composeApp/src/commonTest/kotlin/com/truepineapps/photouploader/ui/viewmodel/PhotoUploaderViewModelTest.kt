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
import com.truepineapps.photouploader.log.TimestampMessageFormatter
import com.truepineapps.photouploader.ui.util.FakePlatformFileSystem
import com.truepineapps.photouploader.ui.util.GoogleAuthServiceTestStub
import com.truepineapps.photouploader.ui.util.createTestKmpFile
import com.truepineapps.photouploader.ui.util.createTestPlatformContext
import com.truepineapps.photouploader.ui.screen.uploader.PhotoUploaderViewModel
import com.truepineapps.photouploader.ui.screen.uploader.uistate.toPhotoUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.FileSystem
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoUploaderViewModelTest : KoinTest {
    private lateinit var fileSystem: FakeFileSystem

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fileSystem = FakeFileSystem()
        // Ensure any previous Koin instance is killed before this test starts
        stopKoin()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    private fun startTestKoin(
        serviceStub: GoogleAuthService,
    ) {
        startKoin {
            modules(
                viewModelModule(),
                module {
                    single<FileSystem> { fileSystem }
                    single { PhotoDirectoryRepository(FakePlatformFileSystem(fileSystem)) }
                    single<GoogleAuthService> { serviceStub }
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

    @Test
    fun testRestoreSignInSuccess() = runTest {
        // 1. Prepare the stub
        val successStub = GoogleAuthServiceTestStub(restoreToken = "restored_token")
        startTestKoin(successStub)

        // 2. Inject the ViewModel via Koin.
        // Accessing the property triggers creation, which runs the init block.
        val viewModel: PhotoUploaderViewModel by inject()

        // Start collecting the state flow to ensure it updates
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        // Force creation by accessing property and check initial state
        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated, "Initially should not be authenticated")

        // 3. Run pending coroutines (from the init block)
        testDispatcher.scheduler.advanceUntilIdle()

        // 4. Verify state changed to true
        assertTrue(
            viewModel.uiState.value.isAuthenticated,
            "Should be authenticated after successful restore"
        )
    }

    @Test
    fun testSignInSuccess() = runTest {
        val successStub = GoogleAuthServiceTestStub(signInToken = "valid_token")
        startTestKoin(successStub)

        val viewModel: PhotoUploaderViewModel by inject()

        // Start collecting the state flow to ensure it updates
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        // Run initial auth check (which will fail/do nothing since restoreToken is null)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(
            viewModel.uiState.value.isAuthenticated,
            "Initially should not be authenticated"
        )

        // 3. Trigger Sign In
        viewModel.signIn()
        testDispatcher.scheduler.advanceUntilIdle()

        // 4. Verify state changed to true
        assertTrue(
            viewModel.uiState.value.isAuthenticated,
            "Should be authenticated after successful sign-in"
        )
    }

    @Test
    fun testSignInFailure() = runTest {
        val failureStub = GoogleAuthServiceTestStub(signInToken = null)
        startTestKoin(failureStub)

        val viewModel: PhotoUploaderViewModel by inject()

        // Start collecting the state flow to ensure it updates
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }

        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(
            viewModel.uiState.value.isAuthenticated,
            "Initially should not be authenticated"
        )

        // 3. Trigger Sign In
        viewModel.signIn()
        testDispatcher.scheduler.advanceUntilIdle()

        // 4. Verify state remains false
        assertFalse(
            viewModel.uiState.value.isAuthenticated,
            "Should NOT be authenticated if signIn returns null"
        )
    }

    @Test
    fun testUpdateCoverPhoto() = runTest {
        // 1. Start Koin
        val successStub = GoogleAuthServiceTestStub()
        startTestKoin(successStub)

        // 2. Prepare Fake Data
        val albumName = "album"
        val albumPath = "/photos/$albumName"
        val photoName1 = "photo1.jpg"
        val photoName2 = "photo2.jpg"
        val photoPath1 = "$albumPath/$photoName1"
        val photoPath2 = "$albumPath/$photoName2"
        val photoFile1 = createTestKmpFile(photoPath1)
        val photoFile2 = createTestKmpFile(photoPath2)
        val photo1 = Photo(
            kmpFile = photoFile1,
            path = photoPath1.toPath(),
            name = photoName1,
        )
        val photo2 = Photo(
            kmpFile = photoFile2,
            path = photoPath2.toPath(),
            name = photoName2,
        )
        val albumFile = createTestKmpFile(albumPath)
        val album = Album(
            id = albumName,
            kmpFile = albumFile,
            path = albumPath.toPath(),
            name = "Test Album",
            group = "Test",
            photos = listOf(photo1, photo2),
        )

        val photoRepo: PhotoDirectoryRepository by inject()
        val viewModel: PhotoUploaderViewModel by inject()
        viewModel.uiState.test {
            assertEquals(emptyList(), awaitItem().albumUiStates)

            photoRepo.updateAlbums(listOf(album))
            awaitItem() // consume populated


            // 3. Trigger the update cover action
            viewModel.updateCoverPhoto(albumName, photo2.toPhotoUiState())

            // 4. Verify the state
            val updatedAlbum = awaitItem().albumUiStates.first()

            assertEquals(
                photo2.toPhotoUiState(),
                updatedAlbum.coverPhotoUiState,
                "Album coverPhoto should be updated to the photo2 object"
            )

            val updatedPhoto1 = updatedAlbum.photoUiStates.find { it.path == photo1.path }!!
            val updatedPhoto2 = updatedAlbum.photoUiStates.find { it.path == photo2.path }!!

            assertFalse(
                updatedPhoto1.isCoverPhoto,
                "Old cover photo (photo1) should have isCoverPhoto set to false"
            )
            assertTrue(
                updatedPhoto2.isCoverPhoto,
                "New cover photo (photo2) should have isCoverPhoto set to true"
            )
        }
    }

    @Test
    fun testUpload_GlobalErrorOnSignInFailure() = runTest {
        val errorStub = GoogleAuthServiceTestStub(signInShouldFail = true)
        startTestKoin(errorStub)

        val photo1 = Photo(
            kmpFile = createTestKmpFile(path = "/p1"),
            path = "/p1".toPath(),
            name = "p1.jpg"
        )
        val album1 = Album(
            id = "a1",
            kmpFile = createTestKmpFile("/a1"),
            path = "/a1".toPath(),
            name = "A1",
            group = "G1",
            photos = listOf(photo1),
        )

        val photoRepo: PhotoDirectoryRepository by inject()
        photoRepo.updateAlbums(listOf(album1))

        val viewModel: PhotoUploaderViewModel by inject()
        viewModel.platformContext = createTestPlatformContext()
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uploadPhotos()?.join()
        testDispatcher.scheduler.advanceUntilIdle()

        val globalErrorMessage = viewModel.uiState.value.globalErrorMessage
        assertNotNull(
            globalErrorMessage,
            "Global error should be set on sign-in failure"
        )
        assertTrue(
            globalErrorMessage.toString().contains("Sign-in failed"),
            "Error message should indicate sign-in failure, but was: $globalErrorMessage"
        )
    }

}
