package com.truepineapps.photouploader.ui

import com.truepineapps.photouploader.auth.GoogleAuthService
import com.truepineapps.photouploader.data.PhotoDirectoryRepository
import com.truepineapps.photouploader.di.viewModelModule
import com.truepineapps.photouploader.io.PlatformFileSystem
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.model.UploadStatus
import com.truepineapps.photouploader.network.AlbumResponse
import com.truepineapps.photouploader.network.BatchCreateMediaItemsRequest
import com.truepineapps.photouploader.network.BatchCreateMediaItemsResponse
import com.truepineapps.photouploader.network.MediaItem
import com.truepineapps.photouploader.network.MediaItemResult
import com.truepineapps.photouploader.network.StatusInfo
import com.truepineapps.photouploader.ui.screen.uploader.PhotoUploaderViewModel
import com.truepineapps.photouploader.ui.screen.uploader.UiState
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
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
import okio.FileSystem
import okio.Path
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

    private fun setupKoin(mockEngine: MockEngine) {
        startKoin {
            modules(
                viewModelModule(),
                module {
                    single<FileSystem> { fileSystem }
                    single<PlatformFileSystem> { FakePlatformFileSystem(fileSystem) }
                    single { PhotoDirectoryRepository(platformFileSystem = get()) }
                    single<Json> { Json { ignoreUnknownKeys = true } }
                    single {
                        HttpClient(mockEngine) {
                            install(ContentNegotiation) { json(get()) }
                        }
                    }
                    single<GoogleAuthService> { GoogleAuthServiceTestStub(signInToken = "valid_token") }
                })
        }
    }

    // --- Tests ---

    @Test
    fun `uploadPhotos does nothing when root does not exist`() = runTest {
        val mockEngine = createMockEngine(mutableListOf())
        setupKoin(mockEngine)
        val viewModel: PhotoUploaderViewModel by inject()
        viewModel.platformContext = createTestPlatformContext()

        backgroundScope.launch { viewModel.loadingState.collect() }
        backgroundScope.launch { viewModel.uiState.collect() }

        // In test KmpFile is just holding the path string
        viewModel.updatePath(
            kmpFile = createTestKmpFile(ROOT_PATH)
        )
        advanceUntilIdle() // Wait for scan

        // When the root path doesn't exist, the repository returns an empty album list.
        // Consequently, uploadPhotos returns null and performs no actions.
        val job = viewModel.uploadPhotos()
        assertEquals(null, job)
    }

    @Test
    fun `uploadPhotos does nothing when root is not a directory`() = runTest {
        val mockEngine = createMockEngine(mutableListOf())
        setupKoin(mockEngine)
        // Manually create root as file
        ensureDirectory(rootPath.parent!!)
        fileSystem.write(rootPath) { writeUtf8("not a directory") }

        val viewModel: PhotoUploaderViewModel by inject()
        viewModel.platformContext = createTestPlatformContext()

        backgroundScope.launch { viewModel.loadingState.collect() }
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.updatePath(
            kmpFile = createTestKmpFile(ROOT_PATH)
        )
        advanceUntilIdle() // Wait for scan

        // When the root path is not a directory, the repository returns an empty album list.
        // UploadPhotos returns null.
        val job = viewModel.uploadPhotos()
        assertEquals(null, job)
    }

    @Test
    fun `uploadPhotos successfully uploads photos`() = runTest {
        createTestFiles(
            "2023/Holiday/photo1.jpg",
            "2023/Holiday/photo2.png"
        )

        val requests = mutableListOf<HttpRequestData>()
        val mockEngine = createMockEngine(requests)
        setupKoin(mockEngine)

        uploadPhotos()

        // Verify requests
        // 1 create album, 2 uploads, 1 batch create, 1 patch for cover
        assertEquals(5, requests.size)
        requests.assertAlbumCreated("2023 - Holiday")
        requests.assertPhotoUploaded("photo1.jpg")
        requests.assertPhotoUploaded("photo2.png")
        requests.assertAlbumCoverPatched()
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
        setupKoin(mockEngine)

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
        setupKoin(mockEngine)

        uploadPhotos()

        // Verify only album creation was attempted
        assertEquals(1, requests.size)
        assertTrue(requests[0].url.toString().endsWith(ENDPOINT_ALBUMS))
    }

    @Test
    fun `uploadPhotos skips photo if upload fails`() = runTest {
        createTestFiles(
            "2023/Holiday/photo1.jpg",
            "2023/Holiday/photo2.jpg"
        )

        val requests = mutableListOf<HttpRequestData>()
        val mockEngine = createMockEngine(
            requestLog = requests,
            shouldFailUploadForFile = "photo2.jpg" // Fail upload for photo2
        )
        setupKoin(mockEngine)

        uploadPhotos()

        // Verify requests: 1 album, 2 uploads, 1 batch (only containing 1 photo), 1 patch
        assertEquals(5, requests.size)

        // Check that batch create was called, implying at least one photo succeeded
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
        setupKoin(mockEngine)

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
        setupKoin(mockEngine)

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
        setupKoin(mockEngine)

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
        setupKoin(mockEngine)
        val viewModel: PhotoUploaderViewModel by inject()
        viewModel.platformContext = createTestPlatformContext()

        backgroundScope.launch { viewModel.loadingState.collect() }
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.updatePath(
            kmpFile = createTestKmpFile(ROOT_PATH)
        )
        advanceUntilIdle()

        // Disable one album
        val disabledAlbum = viewModel.uiState.value.getAlbumContaining("DisabledAlbum")
        viewModel.toggleAlbum(disabledAlbum.id)

        // Disable one photo in the enabled album
        val enabledAlbum = viewModel.uiState.value.getAlbumContaining("EnabledAlbum")
        val photoToDisable = enabledAlbum.photos.find { it.name == "photo2.jpg" }!!
        viewModel.togglePhoto(enabledAlbum.id, photoToDisable.path)

        advanceUntilIdle() // Wait for UI state to update

        viewModel.uploadPhotos()?.join()
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
        setupKoin(mockEngine)
        val viewModel: PhotoUploaderViewModel by inject()
        viewModel.platformContext = createTestPlatformContext()

        backgroundScope.launch { viewModel.loadingState.collect() }
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.updatePath(
            kmpFile = createTestKmpFile(ROOT_PATH)
        )
        advanceUntilIdle()

        // Rename album
        val album = viewModel.uiState.value.albums.first()
        viewModel.renameAlbum(album.id, "Renamed Album Title")

        advanceUntilIdle() // Wait for UI state to update

        viewModel.uploadPhotos()?.join()
        advanceUntilIdle()

        requests.assertAlbumCreated("Renamed Album Title")
    }

    @Test
    fun testUpload_StatusUpdates() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val mockEngine = createMockEngine(requests)
        setupKoin(mockEngine)

        val photo1 = Photo(createTestKmpFile("/p1"), "/p1".toPath(), "p1.jpg", isEnabled = true)
        val photo2 = Photo(createTestKmpFile("/p2"), "/p2".toPath(), "p2.jpg", isEnabled = false)
        val album1 = Album(
            "a1",
            createTestKmpFile("/a1"),
            "/a1".toPath(),
            "A1",
            "G1",
            listOf(photo1, photo2),
            photo1,
            isEnabled = true
        )

        val photoRepo: PhotoDirectoryRepository by inject()
        photoRepo.updateAlbums(listOf(album1))

        val viewModel: PhotoUploaderViewModel by inject()
        viewModel.platformContext = createTestPlatformContext()
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect() }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uploadPhotos()
        testDispatcher.scheduler.runCurrent() // Execute only the initial status updates

        val updatedAlbum = photoRepo.albums.value.first()
        val updatedPhoto1 = updatedAlbum.photos.first()
        val updatedPhoto2 = updatedAlbum.photos.last()

        assertTrue(
            updatedAlbum.uploadStatus is UploadStatus.Uploading,
            "Album status should be Uploading"
        )
        assertTrue(
            updatedPhoto1.uploadStatus is UploadStatus.Waiting,
            "Enabled photo status should be Waiting"
        )
        assertTrue(
            updatedPhoto2.uploadStatus is UploadStatus.None,
            "Disabled photo status should remain None"
        )
    }

    // --- Helpers ---

    private fun UiState.getAlbumContaining(namePart: String) = 
        this.albums.find { it.name.contains(namePart) }!!

    private fun createMockEngine(
        requestLog: MutableList<HttpRequestData>,
        shouldFailAlbumCreation: Boolean = false,
        shouldFailUploadForFile: String? = null,
    ): MockEngine {
        return MockEngine { request ->
            requestLog.add(request)
            val url = request.url.toString()
            when {
                url.endsWith(ENDPOINT_ALBUMS) -> {
                    if (shouldFailAlbumCreation) {
                        respond("Error creating album", status = HttpStatusCode.BadRequest)
                    } else {
                        val response = AlbumResponse(
                            id = "album_123",
                            title = "Album Title",
                            productUrl = "http://url"
                        )
                        respond(
                            content = json.encodeToString(response),
                            status = HttpStatusCode.OK,
                            headers = headersOf(
                                HttpHeaders.ContentType,
                                ContentType.Application.Json.toString()
                            )
                        )
                    }
                }

                url.endsWith(ENDPOINT_UPLOADS) -> {
                    val fileName = request.headers["X-Goog-Upload-File-Name"]
                    if (shouldFailUploadForFile != null && fileName == shouldFailUploadForFile) {
                        respond("Upload failed", status = HttpStatusCode.InternalServerError)
                    } else {
                        respond(
                            content = "upload_token_$fileName",
                            status = HttpStatusCode.OK
                        )
                    }
                }

                url.endsWith(ENDPOINT_BATCH_CREATE) -> {
                    val body = request.body as TextContent
                    val batchRequest = json.decodeFromString<BatchCreateMediaItemsRequest>(body.text)
                    val results = batchRequest.newMediaItems.mapIndexed { _, item ->
                        MediaItemResult(
                            uploadToken = item.simpleMediaItem.uploadToken,
                            status = StatusInfo(0, "OK"),
                            mediaItem = MediaItem(
                                id = "media-id-for-${item.simpleMediaItem.uploadToken}", // Generate a unique mediaId
                                filename = item.simpleMediaItem.fileName
                            )
                        )
                    }
                    val response = BatchCreateMediaItemsResponse(results)

                    respond(
                        content = json.encodeToString(response),
                        status = HttpStatusCode.OK,
                        headers = headersOf(
                            HttpHeaders.ContentType,
                            ContentType.Application.Json.toString()
                        )
                    )
                }

                url.contains("?updateMask=coverPhotoMediaItemId") -> {
                    respond(content = "{}", status = HttpStatusCode.OK, headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
                }

                else -> error("Unhandled request: ${request.url}")
            }
        }
    }

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

    private suspend fun TestScope.uploadPhotos() {
        val viewModel: PhotoUploaderViewModel by inject()
        viewModel.platformContext = createTestPlatformContext()

        backgroundScope.launch { viewModel.loadingState.collect() }
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.updatePath(
            kmpFile = createTestKmpFile(ROOT_PATH)
        )
        advanceUntilIdle() // Wait for scan
        viewModel.uploadPhotos()?.join()
        advanceUntilIdle()
    }

    private fun List<HttpRequestData>.assertAlbumCreated(expectedTitle: String) {
        val albumRequests = this.filter { it.url.toString().endsWith(ENDPOINT_ALBUMS) }
        var actualTitle = ""
        val found = albumRequests.any { req ->
            val title = getAlbumTitle(req)
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
            it.method == HttpMethod.Patch && it.url.toString().contains("updateMask=coverPhotoMediaItemId")
        }
        assertNotNull(patchRequest, "PATCH request to update album cover not found")
    }

    private fun List<HttpRequestData>.assertAlbumNotCreated(titlePart: String) {
         assertTrue(this.none { it.url.toString().endsWith(ENDPOINT_ALBUMS) && getAlbumTitle(it)?.contains(titlePart) == true }, "Album with title containing '$titlePart' should not have been created")
    }

    private fun getAlbumTitle(request: HttpRequestData): String? {
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
