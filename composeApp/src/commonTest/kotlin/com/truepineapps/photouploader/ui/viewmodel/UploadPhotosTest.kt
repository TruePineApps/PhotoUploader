package com.truepineapps.photouploader.ui.viewmodel

import app.cash.turbine.test
import com.truepineapps.photouploader.core.util.UiTextResource
import com.truepineapps.photouploader.feature.uploader.data.dto.AlbumResponse
import com.truepineapps.photouploader.feature.uploader.data.dto.BatchCreateMediaItemsRequest
import com.truepineapps.photouploader.feature.uploader.data.dto.BatchCreateMediaItemsResponse
import com.truepineapps.photouploader.feature.uploader.data.dto.GooglePhotosErrorContent
import com.truepineapps.photouploader.feature.uploader.data.dto.GooglePhotosErrorResponse
import com.truepineapps.photouploader.feature.uploader.data.dto.MediaItem
import com.truepineapps.photouploader.feature.uploader.data.dto.MediaItemResult
import com.truepineapps.photouploader.feature.uploader.data.dto.StatusInfo
import com.truepineapps.photouploader.feature.uploader.domain.model.Album
import com.truepineapps.photouploader.feature.uploader.domain.model.Photo
import com.truepineapps.photouploader.feature.uploader.domain.repository.PhotoDirectoryRepository
import com.truepineapps.photouploader.feature.uploader.viewmodel.PhotoUploaderViewModel
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UiState
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UploadStatus
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.error_add_media_items_failed_with_message
import com.truepineapps.photouploader.resources.error_add_to_album_failed
import com.truepineapps.photouploader.resources.error_album_creation_failed_with_message
import com.truepineapps.photouploader.resources.error_one_or_more_photos_failed
import com.truepineapps.photouploader.resources.error_upload_failed_with_message
import com.truepineapps.photouploader.ui.util.createTestKmpFile
import com.truepineapps.photouploader.ui.util.createTestPlatformContext
import com.truepineapps.photouploader.ui.util.startTestKoin
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.Path
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.koin.core.context.stopKoin
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
class UploadPhotosTest : KoinTest {

    private lateinit var fileSystem: FakeFileSystem
    private val rootPath = ROOT_PATH.toPath()
    private val testDispatcher = StandardTestDispatcher()
    private val json = Json { ignoreUnknownKeys = true }


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


    // --- Tests ---

    @Test
    fun `uploadPhotos does nothing when root does not exist`() = runTest {
        val mockEngine = createMockEngine(mutableListOf())
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)
        val viewModel: PhotoUploaderViewModel by inject()
        val platformContext = createTestPlatformContext()

        backgroundScope.launch { viewModel.loadingState.collect() }
        backgroundScope.launch { viewModel.uiState.collect() }

        // In test KmpFile is just holding the path string
        viewModel.updatePath(
            kmpFile = createTestKmpFile(ROOT_PATH), platformContext = platformContext
        )
        advanceUntilIdle() // Wait for scan

        // When the root path doesn't exist, the repository returns an empty album list.
        // Consequently, uploadPhotos returns null and performs no actions.
        val job = viewModel.uploadPhotos(platformContext)
        assertEquals(null, job)
    }

    @Test
    fun `uploadPhotos does nothing when root is not a directory`() = runTest {
        val mockEngine = createMockEngine(mutableListOf())
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)
        // Manually create root as file
        ensureDirectory(rootPath.parent!!)
        fileSystem.write(rootPath) { writeUtf8("not a directory") }

        val viewModel: PhotoUploaderViewModel by inject()
        val platformContext = createTestPlatformContext()

        backgroundScope.launch { viewModel.loadingState.collect() }
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.updatePath(
            kmpFile = createTestKmpFile(ROOT_PATH), platformContext = platformContext
        )
        advanceUntilIdle() // Wait for scan

        // When the root path is not a directory, the repository returns an empty album list.
        // UploadPhotos returns null.
        val job = viewModel.uploadPhotos(platformContext)
        assertEquals(null, job)
    }

    @Test
    fun `uploadPhotos successfully uploads photos`() = runTest {
        createTestFiles(
            "2023/Holiday/photo1.jpg", "2023/Holiday/photo2.png"
        )

        val requests = mutableListOf<HttpRequestData>()
        val mockEngine = createMockEngine(requests)
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)

        val viewModel: PhotoUploaderViewModel by inject()
        val platformContext = createTestPlatformContext()
        backgroundScope.launch { viewModel.loadingState.collect() }
        backgroundScope.launch { viewModel.uiState.collect() }
        viewModel.updatePath(
            kmpFile = createTestKmpFile(ROOT_PATH), platformContext = platformContext
        )
        advanceUntilIdle() // Wait for scan
        viewModel.uploadPhotos(platformContext)?.join()
        advanceUntilIdle()

        // Verify requests
        // 1 create album, 2 uploads, 1 batch create, 1 patch for cover
        assertEquals(5, requests.size)
        requests.assertAlbumCreated("2023 - Holiday")
        requests.assertPhotoUploaded("photo1.jpg")
        requests.assertPhotoUploaded("photo2.png")
        requests.assertAlbumCoverPatched()

        // Make sure the state is success and the items are disabled to prevent accidental redo
        val uiState = viewModel.uiState.value
        assertFalse(uiState.albumUiStates.isEmpty(), "Album state exists")
        val album = uiState.albumUiStates.first()
        assertEquals(UploadStatus.Success, album.uploadStatus, "Album status mismatch")
        assertFalse(album.isEnabled, "Album enabled state mismatch")

        assertEquals(2, album.photoUiStates.size, "Expected 2 photo UI states")
        val photo1 = album.photoUiStates.first { it.name == "photo1.jpg" }
        val photo2 = album.photoUiStates.first { it.name == "photo2.png" }

        assertEquals(
            UploadStatus.Success, photo1.uploadStatus, "Photo 1 status mismatch"
        )
        assertEquals(
            UploadStatus.Success, photo2.uploadStatus, "Photo 2 status mismatch"
        )
        assertFalse(photo1.isEnabled, "Photo 1 enabled state mismatch")
        assertFalse(photo2.isEnabled, "Photo 2 enabled state mismatch")
    }

    @Test
    fun `uploadPhotos successfully uploads from multiple years and topics`() = runTest {
        createTestFiles(
            "2023/Holiday/photo.jpg",
            "2023/Work/photo.jpg",
            "2024/Holiday/photo.jpg",
            "2024/Work/photo.jpg"
        )

        val requests = mutableListOf<HttpRequestData>()
        val mockEngine = createMockEngine(requests)
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)

        uploadPhotos()

        // 4 albums * (1 create + 1 upload + 1 batch + 1 patch) = 16
        assertEquals(16, requests.size)

        val years = listOf("2023", "2024")
        val topics = listOf("Holiday", "Work")
        years.forEach { year ->
            topics.forEach { topic ->
                requests.assertAlbumCreated("$year - $topic")
            }
        }
    }

    @Test
    fun `uploadPhotos skips topic if album creation fails`() = runTest {
        createTestFiles("2023/Holiday/photo1.jpg")

        val requests = mutableListOf<HttpRequestData>()
        val mockEngine = createMockEngine(requestLog = requests, shouldFailAlbumCreation = true)
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)

        uploadPhotos()

        // Verify only album creation was attempted
        assertEquals(1, requests.size)
        assertTrue(requests[0].url.toString().endsWith(ENDPOINT_ALBUMS))
    }

    @Test
    fun `uploadPhotos skips photo if upload fails`() = runTest {
        testSkipPhotoOnFailure(false)
    }

    @Test
    fun `uploadPhotos skips photo if upload fails with retry`() = runTest {
        testSkipPhotoOnFailure(true)
    }

    private suspend fun TestScope.testSkipPhotoOnFailure(shouldRetryFailure: Boolean) {
        createTestFiles(
            "2023/Holiday/photo1.jpg", "2023/Holiday/photo2.jpg"
        )

        val requests = mutableListOf<HttpRequestData>()
        val mockEngine = createMockEngine(
            requestLog = requests,
            shouldFailUploadForFile = "photo2.jpg", // Fail upload for photo2
            shouldRetryFailure = shouldRetryFailure
        )
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)

        uploadPhotos()

        // Verify requests: 1 album, 2 uploads, 1 batch (only containing 1 photo), 1 patch
        // plus 5 retries if requested
        val expectedRequestCount = if (shouldRetryFailure) 10 else 5
        assertEquals(expectedRequestCount, requests.size)

        // Check that batch_create was called, implying at least one photo succeeded
        assertTrue(requests.any { it.url.toString().endsWith(ENDPOINT_BATCH_CREATE) })

        // Verify photo1 succeeded
        requests.assertPhotoUploaded("photo1.jpg")
    }

    @Test
    fun `uploadPhotos batches media creation requests`() = runTest {
        // Setup FileSystem with 55 photos
        // Use loop for creating many files
        val files = (0 until 55).map { "2023/Holiday/photo_$it.jpg" }.toTypedArray()
        createTestFiles(*files)

        val requests = mutableListOf<HttpRequestData>()
        val mockEngine = createMockEngine(requests)
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)

        uploadPhotos()

        // 1 create album + 55 uploads + 2 batch create + 1 patch = 59 requests
        assertEquals(59, requests.size)
        val batchRequests = requests.filter { it.url.toString().endsWith(ENDPOINT_BATCH_CREATE) }
        assertEquals(2, batchRequests.size) // 55 photos -> 50 + 5 -> 2 batches
    }

    @Test
    fun `uploadPhotos skips album creation if topic directory is empty`() = runTest {
        // Create directory structure without photos
        ensureDirectory(rootPath / "2023" / "Holiday")

        val requests = mutableListOf<HttpRequestData>()
        val mockEngine = createMockEngine(requests)
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)

        uploadPhotos()

        // Verify no requests were made
        assertTrue(requests.isEmpty(), "No requests should be made for empty topic directory")
    }

    @Test
    fun `uploadPhotos successfully uploads nested structure`() = runTest {
        createTestFiles(
            "2024/Our family.jpg",
            "2024/Holiday France/Paris at night.png",
            "2024/Holiday France/Flower garden/Tree with blossom.webp"
        )

        val requests = mutableListOf<HttpRequestData>()
        val mockEngine = createMockEngine(requests)
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)

        uploadPhotos()

        // Expectations: 3 Albums * (1 create + 1 upload + 1 batch + 1 patch) = 12 requests
        assertEquals(12, requests.size)

        requests.assertAlbumCreated("2024")
        requests.assertAlbumCreated("2024 - Holiday France")
        requests.assertAlbumCreated("2024 - Holiday France - Flower garden")

        requests.assertPhotoUploaded("Our family.jpg")
        requests.assertPhotoUploaded("Paris at night.png")
        requests.assertPhotoUploaded("Tree with blossom.webp")
    }

    @Test
    fun `uploadPhotos respects disabled albums and photos`() = runTest {
        createTestFiles(
            "2023/EnabledAlbum/photo1.jpg",
            "2023/EnabledAlbum/photo2.jpg",
            "2023/DisabledAlbum/photo3.jpg"
        )

        val requests = mutableListOf<HttpRequestData>()
        val mockEngine = createMockEngine(requests)
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)
        val viewModel: PhotoUploaderViewModel by inject()
        val platformContext = createTestPlatformContext()

        backgroundScope.launch { viewModel.loadingState.collect() }
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.updatePath(
            kmpFile = createTestKmpFile(ROOT_PATH), platformContext = platformContext
        )
        advanceUntilIdle()

        // Disable one album
        val disabledAlbum = viewModel.uiState.value.getAlbumContaining("DisabledAlbum")
        viewModel.toggleAlbum(disabledAlbum.id)

        // Disable one photo in the enabled album
        val enabledAlbum = viewModel.uiState.value.getAlbumContaining("EnabledAlbum")
        val photoToDisable = enabledAlbum.photoUiStates.find { it.name == "photo2.jpg" }!!
        viewModel.togglePhoto(enabledAlbum.id, photoToDisable.path)

        advanceUntilIdle() // Wait for UI state to update

        viewModel.uploadPhotos(platformContext)?.join()
        advanceUntilIdle()

        // Verify requests:
        // 1 album created (EnabledAlbum)
        // 1 upload (photo1.jpg)
        // 1 batch create
        // 1 patch for cover
        assertEquals(4, requests.size)

        requests.assertAlbumCreated("2023 - EnabledAlbum")
        requests.assertPhotoUploaded("photo1.jpg")

        // Ensure disabled items were NOT processed
        requests.assertAlbumNotCreated("DisabledAlbum")
        requests.assertPhotoNotUploaded("photo2.jpg")
        requests.assertPhotoNotUploaded("photo3.jpg")
    }

    @Test
    fun `uploadPhotos respects renamed albums`() = runTest {
        createTestFiles("2023/OriginalName/photo.jpg")

        val requests = mutableListOf<HttpRequestData>()
        val mockEngine = createMockEngine(requests)
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)
        val viewModel: PhotoUploaderViewModel by inject()
        val platformContext = createTestPlatformContext()

        backgroundScope.launch { viewModel.loadingState.collect() }
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.updatePath(
            kmpFile = createTestKmpFile(ROOT_PATH), platformContext = platformContext
        )
        advanceUntilIdle()

        // Rename album
        val album = viewModel.uiState.value.albumUiStates.first()
        viewModel.renameAlbum(album.id, "Renamed Album Title")

        advanceUntilIdle() // Wait for UI state to update

        viewModel.uploadPhotos(platformContext)?.join()
        advanceUntilIdle()

        requests.assertAlbumCreated("Renamed Album Title")
    }

    @Test
    fun `upload status is correctly updated during upload`() = runTest {
        // Delay network requests to allow the test to observe the status updates
        val requests = mutableListOf<HttpRequestData>()
        val mockEngine = createMockEngine(requestLog = requests)
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)

        val photo1Data = createDummyPhotoData("/p1.jpg")
        val photo2Data = createDummyPhotoData("/p2.jpg")
        val albumData = createDummyAlbumData(
            path = "a1",
            name = "A1",
            group = "G1",
            photos = listOf(photo1Data, photo2Data),
        )

        val photoRepo: PhotoDirectoryRepository by inject()
        photoRepo.updateAlbums(listOf(albumData)) // Directly emit albums to the repository

        val viewModel: PhotoUploaderViewModel by inject()
        val platformContext = createTestPlatformContext() // Essential for KmpFile operations

        viewModel.uiState.test {
            // 1. Await initial ViewModel state (empty albumUiStates)
            val uiState = awaitItem()
            assertTrue(uiState.albumUiStates.isEmpty(), "Initial state should be empty")

            // 2. Await state after repository emits `albumData` and ViewModel maps it.
            // All items should be in their default `UploadStatus.None` state.
            assertStatus(
                uiState = awaitItem(),
                testName = "After data loaded & mapped",
                expectedAlbumStatus = UploadStatus.None,
                expectedPhoto1Status = UploadStatus.None,
                expectedPhoto2Status = UploadStatus.None,
                expectedPhoto2Enabled = true
            )

            // Disable photo2 in the UI state directly
            viewModel.togglePhoto("a1", "/p2.jpg".toPath())

            // 3. Await state after `togglePhoto` action. Only photo2Enabled changes.
            assertStatus(
                uiState = awaitItem(),
                testName = "After photo 2 toggled off",
                expectedAlbumStatus = UploadStatus.None, // Album status remains None
                expectedPhoto1Status = UploadStatus.None,
                expectedPhoto2Status = UploadStatus.None,
                expectedPhoto2Enabled = false // Photo2 should now be disabled
            )

            // Perform upload - this launches the upload coroutine
            val uploadJob = viewModel.uploadPhotos(platformContext)

            // 4. Await state after `uploadPhotos` sets initial 'Waiting' status for enabled,
            // items, immediately followed by setting the album to 'Uploading'
            var uploadState = awaitItem()
            var albumState = uploadState.albumUiStates.first()
            if (albumState.uploadStatus == UploadStatus.Waiting) {
                assertStatus(
                    uiState = uploadState,
                    testName = "After initial 'Waiting' status set for enabled items",
                    expectedAlbumStatus = UploadStatus.Waiting, // Album goes to Waiting
                    expectedPhoto1Status = UploadStatus.Waiting, // Photo1 goes to Waiting
                    expectedPhoto2Status = UploadStatus.None, // Photo2 remains None (disabled)
                    expectedPhoto2Enabled = false
                )
                uploadState = awaitItem()
                albumState = uploadState.albumUiStates.first()
            }

            // 5. Await state after album creation/verification, album status becomes 'Uploading'.
            val photo1State = albumState.photoUiStates.first { it.name == "p1.jpg" }
            if (photo1State.uploadStatus == UploadStatus.Waiting) {
                assertStatus(
                    uiState = uploadState,
                    testName = "After album itself starts 'Uploading'",
                    expectedAlbumStatus = UploadStatus.Uploading, // Album goes to Uploading
                    expectedPhoto1Status = UploadStatus.Waiting, // Photo1 still Waiting
                    expectedPhoto2Status = UploadStatus.None,
                    expectedPhoto2Enabled = false
                )
                uploadState = awaitItem()
            }

            // 6. Await state after the first photo (`p1.jpg`) *starts* uploading.
            assertStatus(
                uiState = uploadState,
                testName = "After photo 1 starts 'Uploading'",
                expectedAlbumStatus = UploadStatus.Uploading,
                expectedPhoto1Status = UploadStatus.Uploading, // Photo1 goes to Uploading
                expectedPhoto2Status = UploadStatus.None,
                expectedPhoto2Enabled = false
            )

            // 7. Await state after the first photo (`p1.jpg`) *successfully uploaded bytes* (before batch create).
            // This is an internal step where photo status changes to Success, but album might still be Uploading.
            assertStatus(
                uiState = awaitItem(),
                testName = "After photo 1 successfully uploaded bytes",
                expectedAlbumStatus = UploadStatus.Uploading, // Album still Uploading
                expectedPhoto1Status = UploadStatus.Success, // Photo1 goes to Success
                expectedPhoto2Status = UploadStatus.None,
                expectedPhoto2Enabled = false
            )

            // Let the entire upload job complete and advance time until idle
            uploadJob?.join()
            advanceUntilIdle() // Ensure all remaining coroutines and cleanup are done

            // 8. Await the final state after all enabled photos are processed (batch create, set cover).
            assertStatus(
                uiState = awaitItem(),
                testName = "Final state after successful upload",
                expectedAlbumStatus = UploadStatus.Success, // Album should now be Success
                expectedPhoto1Status = UploadStatus.Success, // Photo1 confirms Success (potentially already was)
                expectedPhoto2Status = UploadStatus.None,
                expectedPhoto2Enabled = false
            )

            expectNoEvents() // Ensure no further unexpected emissions
        }
    }

    // Fixed values for `upload status is correctly updated during upload`
    private fun assertStatus(
        testName: String, // A descriptive name for the test step
        uiState: UiState,
        expectedAlbumStatus: UploadStatus,
        expectedPhoto1Status: UploadStatus,
        expectedPhoto2Status: UploadStatus,
        expectedPhoto2Enabled: Boolean,
    ) {
        assertEquals(1, uiState.albumUiStates.size, "$testName - Expected 1 album UI state")

        val album = uiState.albumUiStates.first()
        assertEquals(expectedAlbumStatus, album.uploadStatus, "$testName - Album status mismatch")

        assertEquals(2, album.photoUiStates.size, "$testName - Expected 2 photo UI states")
        val photo1 = album.photoUiStates.first { it.name == "p1.jpg" }
        val photo2 = album.photoUiStates.first { it.name == "p2.jpg" }

        assertEquals(
            expectedPhoto1Status, photo1.uploadStatus, "$testName - Photo 1 status mismatch"
        )
        assertEquals(
            expectedPhoto2Status, photo2.uploadStatus, "$testName - Photo 2 status mismatch"
        )
        assertEquals(
            expectedPhoto2Enabled, photo2.isEnabled, "$testName - Photo 2 enabled state mismatch"
        )
    }

    @Test
    fun `album and photo status is error when album creation fails`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        // Fail album creation
        val mockEngine = createMockEngine(requestLog = requests, shouldFailAlbumCreation = true)
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)

        val photo1 = Photo(createTestKmpFile("/p1"), "/p1".toPath(), "photo1.jpg")
        val album1 = Album(
            "a1",
            createTestKmpFile("/a1"),
            "/a1".toPath(),
            "A1",
            "G1",
            listOf(photo1),
        )
        val photoRepo: PhotoDirectoryRepository by inject()
        photoRepo.updateAlbums(listOf(album1))

        val viewModel: PhotoUploaderViewModel by inject()
        val platformContext = createTestPlatformContext()
        // Activate flow
        backgroundScope.launch { viewModel.uiState.collect() }
        // Explicitly await the state synchronization
        viewModel.uiState.first { it.albumUiStates.isNotEmpty() }

        viewModel.uploadPhotos(platformContext)?.join()
        advanceUntilIdle()

        val album = viewModel.uiState.value.albumUiStates.first()
        val photo = album.photoUiStates.first()

        val albumStatus = album.uploadStatus
        assertTrue(
            albumStatus is UploadStatus.Error,
            "Album status: expected = Error, actual is $albumStatus"
        )
        assertEquals(
            UploadStatus.Error(
                UiTextResource(
                    Res.string.error_album_creation_failed_with_message, "400 Bad Request"
                )
            ).message.toString(), albumStatus.message.toString()
        )
        assertEquals(UploadStatus.Waiting, photo.uploadStatus)
    }

    @Test
    fun `photo and album status is error when photo upload fails`() = runTest {
        val failUploadForFile = "photo1.jpg"
        val requests = mutableListOf<HttpRequestData>()
        val mockEngine =
            createMockEngine(requestLog = requests, shouldFailUploadForFile = failUploadForFile)
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)

        val photo1 = Photo(createTestKmpFile("/p1"), "/p1".toPath(), failUploadForFile)
        val album1 = Album(
            "a1",
            createTestKmpFile("/a1"),
            "/a1".toPath(),
            "A1",
            "G1",
            listOf(photo1),
        )
        val photoRepo: PhotoDirectoryRepository by inject()
        photoRepo.updateAlbums(listOf(album1))

        val viewModel: PhotoUploaderViewModel by inject()
        val platformContext = createTestPlatformContext()
        // Activate flow
        backgroundScope.launch { viewModel.uiState.collect() }
        // Explicitly await the state synchronization
        viewModel.uiState.first { it.albumUiStates.isNotEmpty() }

        viewModel.uploadPhotos(platformContext)?.join()
        advanceUntilIdle()

        val album = viewModel.uiState.value.albumUiStates.first()
        val photo = album.photoUiStates.first()
        val photoStatus = photo.uploadStatus
        assertTrue(photoStatus is UploadStatus.Error, "Photo status: expected Error, actual is $photoStatus")
        val expectedPhotoErrorStatus = UploadStatus.Error(
            UiTextResource(
                Res.string.error_upload_failed_with_message,
                "Simulated upload failure for $failUploadForFile (406 Not Acceptable)"
            )
        )
        assertEquals(
            expectedPhotoErrorStatus.message.toString(),
            photoStatus.message.toString(),
            "Photo status message"
        )

        val albumStatus = album.uploadStatus
        assertTrue(
            albumStatus is UploadStatus.Error, "Album status: Expected Error, is $albumStatus"
        )
        val expectedAlbumErrorStatus = UploadStatus.Error(
            UiTextResource(
                Res.string.error_one_or_more_photos_failed,
                expectedPhotoErrorStatus.message.toString()
            )
        )
        assertEquals(
            expectedAlbumErrorStatus.message.toString(),
            albumStatus.message.toString(),
            "Album status message"
        )
    }

    @Test
    fun `album status is error when adding photos to album fails`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val mockEngine = createMockEngine(requestLog = requests, shouldFailAddToAlbum = true)
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)

        val photo1 = Photo(createTestKmpFile("/p1"), "/p1".toPath(), "photo1.jpg")
        val album1 = Album(
            "a1",
            createTestKmpFile("/a1"),
            "/a1".toPath(),
            "A1",
            "G1",
            listOf(photo1),
        )
        val photoRepo: PhotoDirectoryRepository by inject()
        photoRepo.updateAlbums(listOf(album1))

        val viewModel: PhotoUploaderViewModel by inject()
        val platformContext = createTestPlatformContext()
        // Activate flow
        backgroundScope.launch { viewModel.uiState.collect() }
        // Explicitly await the state synchronization
        viewModel.uiState.first { it.albumUiStates.isNotEmpty() }

        viewModel.uploadPhotos(platformContext)?.join()
        advanceUntilIdle()

        val album = viewModel.uiState.value.albumUiStates.first()
        val albumStatus = album.uploadStatus
        assertTrue(
            albumStatus is UploadStatus.Error,
            "Album status: expected = Error, actual is $albumStatus"
        )
        assertEquals(
            UploadStatus.Error(
                UiTextResource(
                    Res.string.error_one_or_more_photos_failed, listOf(
                        UiTextResource(
                            Res.string.error_add_media_items_failed_with_message,
                            "500 Internal Server Error"
                        )
                    )
                )
            ).message.toString(), albumStatus.message.toString()
        )
    }

    @Test
    fun `photo and album status is error on partial add to album failure`() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val mockEngine =
            createMockEngine(requestLog = requests, failAddToAlbumForFileName = "photo2.jpg")
        startTestKoin(mockEngine = mockEngine, fileSystem = fileSystem)

        val photo1 = Photo(createTestKmpFile("/p1"), "/p1".toPath(), "photo1.jpg")
        val photo2 = Photo(createTestKmpFile("/p2"), "/p2".toPath(), "photo2.jpg")
        val album1 = Album(
            "a1",
            createTestKmpFile("/a1"),
            "/a1".toPath(),
            "A1",
            "G1",
            listOf(photo1, photo2),
        )
        val photoRepo: PhotoDirectoryRepository by inject()
        photoRepo.updateAlbums(listOf(album1))

        val viewModel: PhotoUploaderViewModel by inject()
        val platformContext = createTestPlatformContext()
        // Activate flow
        backgroundScope.launch { viewModel.uiState.collect() }
        // Explicitly await the state synchronization
        viewModel.uiState.first { it.albumUiStates.isNotEmpty() }

        viewModel.uploadPhotos(platformContext)?.join()
        advanceUntilIdle()

        val album = viewModel.uiState.value.albumUiStates.first()
        val updatedPhoto1 = album.photoUiStates.find { it.name == "photo1.jpg" }!!
        val updatedPhoto2 = album.photoUiStates.find { it.name == "photo2.jpg" }!!

        assertEquals(UploadStatus.Success, updatedPhoto1.uploadStatus)
        assertNotNull(updatedPhoto1.mediaItemId)
        assertTrue(
            updatedPhoto2.uploadStatus is UploadStatus.Error, "Photo should fail to add to album"
        )
        val expectedPhotoErrorMessage = UploadStatus.Error(
            UiTextResource(
                Res.string.error_add_to_album_failed, "3: Failed to add to album"
            )
        ).message.toString()
        assertEquals(
            expectedPhotoErrorMessage,
            updatedPhoto2.uploadStatus.message.toString(),
            "Photo error message"
        )
        assertTrue(album.uploadStatus is UploadStatus.Error, "Album should fail to add photos")
        assertEquals(
            "${Res.string.error_one_or_more_photos_failed.key} '$expectedPhotoErrorMessage'",
            album.uploadStatus.message.toString(),
            "Album error text"
        )

    }

    // --- Helpers ---

    private fun UiState.getAlbumContaining(namePart: String) =
        this.albumUiStates.find { it.name.contains(namePart) }!!

    private fun createMockEngine(
        requestLog: MutableList<HttpRequestData>,
        shouldFailAlbumCreation: Boolean = false,
        shouldFailUploadForFile: String? = null,
        shouldFailAddToAlbum: Boolean = false,
        shouldRetryFailure: Boolean = false,
        failAddToAlbumForFileName: String? = null,
    ): MockEngine = MockEngine { request ->
        requestLog.add(request)
        val url = request.url.toString()
        when {
            url.endsWith(ENDPOINT_ALBUMS) -> {
                if (shouldFailAlbumCreation) {
                    respond("Error creating album", status = HttpStatusCode.BadRequest)
                } else {
                    val response = AlbumResponse(
                        id = "album_123", title = "Album Title", productUrl = "http://url"
                    )
                    respond(
                        content = json.encodeToString(response),
                        status = HttpStatusCode.OK,
                        headers = headersOf(
                            HttpHeaders.ContentType, ContentType.Application.Json.toString()
                        )
                    )
                }
            }

            url.endsWith(ENDPOINT_UPLOADS) -> {
                val fileName = request.headers["X-Goog-Upload-File-Name"]
                if (shouldFailUploadForFile != null && fileName == shouldFailUploadForFile) {
                    val errorCode =
                        if (shouldRetryFailure) HttpStatusCode.InternalServerError else HttpStatusCode.NotAcceptable
                    val errorResponse = GooglePhotosErrorResponse(
                        error = GooglePhotosErrorContent(
                            code = errorCode.value,
                            message = "Simulated upload failure for $shouldFailUploadForFile",
                            status = errorCode.description
                        )
                    )
                    respond(
                        content = json.encodeToString(errorResponse),
                        status = errorCode,
                        headers = headersOf(
                            HttpHeaders.ContentType, ContentType.Application.Json.toString()
                        )
                    )
                } else {
                    respond(
                        content = "upload_token_$fileName", status = HttpStatusCode.OK
                    )
                }
            }

            url.endsWith(ENDPOINT_BATCH_CREATE) -> {
                if (shouldFailAddToAlbum) {
                    respond("Batch create failed", status = HttpStatusCode.InternalServerError)
                } else {
                    val body = request.body as TextContent
                    val batchRequest =
                        json.decodeFromString<BatchCreateMediaItemsRequest>(body.text)
                    val results = batchRequest.newMediaItems.mapIndexed { _, item ->
                        val status =
                            if (failAddToAlbumForFileName != null && item.simpleMediaItem.fileName == failAddToAlbumForFileName) {
                                StatusInfo(code = 3, message = "Failed to add to album")
                            } else {
                                StatusInfo(0, "OK")
                            }

                        MediaItemResult(
                            uploadToken = item.simpleMediaItem.uploadToken,
                            status = status,
                            mediaItem = if (status.code == 0) MediaItem(
                                id = "media-id-for-${item.simpleMediaItem.uploadToken}",
                                filename = item.simpleMediaItem.fileName
                            ) else null
                        )
                    }
                    val response = BatchCreateMediaItemsResponse(results)

                    respond(
                        content = json.encodeToString(response),
                        status = HttpStatusCode.OK,
                        headers = headersOf(
                            HttpHeaders.ContentType, ContentType.Application.Json.toString()
                        )
                    )
                }
            }

            url.contains("?updateMask=coverPhotoMediaItemId") -> {
                respond(
                    content = "{}", status = HttpStatusCode.OK, headers = headersOf(
                        HttpHeaders.ContentType, ContentType.Application.Json.toString()
                    )
                )
            }

            else -> error("Unhandled request: ${request.url}")
        }
    }

    private fun createDummyPhotoData(path: String) = Photo(
        kmpFile = createTestKmpFile(path),
        path = path.toPath(),
        // Derive name from path
        name = path.substringAfterLast('/'),
    )

    private fun createDummyAlbumData(
        path: String,
        name: String = "Test Album",
        photos: List<Photo> = listOf(createDummyPhotoData(path = "$path/p1.jpg")),
        group: String = "Test Group",
    ) = Album(
        id = path.replace('/', '_'),
        kmpFile = createTestKmpFile(path),
        path = path.toPath(),
        name = name,
        group = group,
        photos = photos,
    )

    private fun createTestFiles(vararg relativePaths: String) {
        if (!fileSystem.exists(rootPath)) {
            fileSystem.createDirectory(rootPath)
        }
        relativePaths.forEach { pathStr ->
            val fullPath = rootPath / pathStr
            ensureDirectory(fullPath.parent!!)
            fileSystem.write(fullPath) { writeUtf8("content") }
        }
    }

    private fun ensureDirectory(path: Path) {
        if (!fileSystem.exists(path)) {
            path.parent?.let { ensureDirectory(it) }
            fileSystem.createDirectory(path)
        }
    }

    /**
     * A helper function for tests that simulates a full user flow for uploading photos.
     * It initializes the ViewModel, sets the root path for photo scanning, waits for the
     * scan to complete, initiates the upload process, and then waits for the upload
     * to finish. This provides a standardized way to test the entire upload sequence.
     */
    private suspend fun TestScope.uploadPhotos() {
        val viewModel: PhotoUploaderViewModel by inject()
        val platformContext = createTestPlatformContext()

        backgroundScope.launch { viewModel.loadingState.collect() }
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.updatePath(
            kmpFile = createTestKmpFile(ROOT_PATH), platformContext = platformContext
        )
        advanceUntilIdle() // Wait for scan
        viewModel.uploadPhotos(platformContext)?.join()
        advanceUntilIdle()
    }

    private fun List<HttpRequestData>.assertAlbumCreated(expectedTitle: String) {
        val albumRequests = this.filter { it.url.toString().endsWith(ENDPOINT_ALBUMS) }
        var actualTitle = ""
        val found = albumRequests.any { req ->
            val title = getAlbumTitleFromRequest(req)
            if (title.isNullOrBlank()) {
                false
            } else if (title == expectedTitle) {
                actualTitle = title
                true
            } else {
                if (actualTitle.isEmpty() && title.contains(expectedTitle)) {
                    actualTitle = title
                }
                false
            }
        }

        assertTrue(
            found,
            "Album creation request: expected title '$expectedTitle', actual title '$actualTitle'"
        )
    }

    private fun List<HttpRequestData>.assertAlbumCoverPatched() {
        val patchRequest = this.find {
            it.method == HttpMethod.Patch && it.url.toString()
                .contains("updateMask=coverPhotoMediaItemId")
        }
        assertNotNull(patchRequest, "PATCH request to update album cover not found")
    }

    private fun List<HttpRequestData>.assertAlbumNotCreated(titlePart: String) {
        assertTrue(this.none {
            it.url.toString().endsWith(ENDPOINT_ALBUMS) && getAlbumTitleFromRequest(it)?.contains(
                titlePart
            ) == true
        }, "Album with title containing '$titlePart' should not have been created")
    }

    private fun getAlbumTitleFromRequest(request: HttpRequestData): String? {
        val content = request.body
        val text = if (content is TextContent) content.text else ""
        val jsonElement = json.parseToJsonElement(text)
        return jsonElement.jsonObject["album"]?.jsonObject?.get("title")?.jsonPrimitive?.content
    }

    private fun List<HttpRequestData>.assertPhotoUploaded(fileName: String) {
        val uploadRequests = this.filter { it.url.toString().endsWith(ENDPOINT_UPLOADS) }
        assertTrue(
            uploadRequests.any { it.headers["X-Goog-Upload-File-Name"] == fileName },
            "Upload for $fileName not found"
        )
    }

    private fun List<HttpRequestData>.assertPhotoNotUploaded(fileName: String) {
        val uploadRequests = this.filter { it.url.toString().endsWith(ENDPOINT_UPLOADS) }
        assertFalse(
            uploadRequests.any { it.headers["X-Goog-Upload-File-Name"] == fileName },
            "Upload for $fileName should NOT have happened"
        )
    }

    companion object {
        private const val ROOT_PATH = "/photos"
        private const val ENDPOINT_ALBUMS = "/albums"
        private const val ENDPOINT_UPLOADS = "/uploads"
        private const val ENDPOINT_BATCH_CREATE = "/mediaItems:batchCreate"
    }
}
