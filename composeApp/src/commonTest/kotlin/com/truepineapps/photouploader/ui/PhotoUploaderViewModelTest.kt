package com.truepineapps.photouploader.ui

import com.truepineapps.photouploader.auth.GoogleAuthService
import com.truepineapps.photouploader.data.PhotoDirectoryRepository
import com.truepineapps.photouploader.di.viewModelModule
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.ui.screen.uploader.PhotoUploaderViewModel
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
            isCoverPhoto = true
        )
        val photo2 = Photo(
            kmpFile = photoFile2,
            path = photoPath2.toPath(),
            name = photoName2,
            isCoverPhoto = false
        )
        val albumFile = createTestKmpFile(albumPath)
        val album = Album(
            id = albumName,
            kmpFile = albumFile,
            path = albumPath.toPath(),
            name = "Test Album",
            group = "Test",
            photos = listOf(photo1, photo2),
            coverPhoto = photo1.kmpFile,
            coverDescription = photo1.name
        )

        val photoRepo: PhotoDirectoryRepository by inject()
        photoRepo.updateAlbums(listOf(album))

        val viewModel: PhotoUploaderViewModel by inject()

        // 3. Trigger the update cover action
        viewModel.updateCoverPhoto(albumName, photo2)

        // 4. Verify the state
        val updatedAlbum = photoRepo.albums.value.first()

        assertEquals(
            photo2.kmpFile,
            updatedAlbum.coverPhoto,
            "Album coverPhoto should be updated to photo2's kmpFile"
        )
        assertEquals(
            photo2.getDisplayName(),
            updatedAlbum.coverDescription,
            "Album coverDescription should be updated to photo2's name"
        )

        val updatedPhoto1 = updatedAlbum.photos.find { it.path == photo1.path }!!
        val updatedPhoto2 = updatedAlbum.photos.find { it.path == photo2.path }!!

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
