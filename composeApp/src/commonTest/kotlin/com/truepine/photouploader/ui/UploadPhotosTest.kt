package com.truepine.photouploader.ui

import com.truepine.photouploader.auth.GoogleAuthService
import com.truepine.photouploader.di.viewModelModule
import com.truepine.photouploader.network.AlbumResponse
import com.truepine.photouploader.network.BatchCreateMediaItemsResponse
import com.truepine.photouploader.network.MediaItemResult
import com.truepine.photouploader.network.StatusInfo
import com.truepine.photouploader.ui.screen.uploader.PhotoUploaderViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
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
import kotlin.test.assertFailsWith
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
                    single {
                        HttpClient(mockEngine) {
                            install(ContentNegotiation) {
                                json(Json { ignoreUnknownKeys = true })
                            }
                        }
                    }
                    single<GoogleAuthService> { GoogleAuthServiceStub(signInToken = "valid_token") }
                })
        }
    }

    // --- Tests ---

    @Test
    fun `uploadPhotos throws IllegalArgumentException when root does not exist`() = runTest {
        val mockEngine = createMockEngine(mutableListOf())
        setupKoin(mockEngine)
        val viewModel: PhotoUploaderViewModel by inject()

        assertFailsWith<IllegalArgumentException> {
            viewModel.uploadPhotos(ROOT_PATH, fileSystem)
        }
    }

    @Test
    fun `uploadPhotos throws IllegalArgumentException when root is not a directory`() = runTest {
        val mockEngine = createMockEngine(mutableListOf())
        setupKoin(mockEngine)
        // Manually create root as file
        ensureDirectory(rootPath.parent!!)
        fileSystem.write(rootPath) { writeUtf8("not a directory") }
        
        val viewModel: PhotoUploaderViewModel by inject()

        assertFailsWith<IllegalArgumentException> {
            viewModel.uploadPhotos(ROOT_PATH, fileSystem)
        }
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
        
        val viewModel: PhotoUploaderViewModel by inject()
        viewModel.uploadPhotos(ROOT_PATH, fileSystem)

        // Verify requests
        assertEquals(4, requests.size) // 1 create album, 2 uploads, 1 batch create
        requests.assertAlbumCreated("2023 - Holiday")
        requests.assertPhotoUploaded("photo1.jpg")
        requests.assertPhotoUploaded("photo2.png")
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

        val viewModel: PhotoUploaderViewModel by inject()
        viewModel.uploadPhotos(ROOT_PATH, fileSystem)

        // Total expected requests:
        // 2 Years * 2 Topics = 4 Topics
        // Per Topic: 1 Album Create + 1 Upload + 1 Batch Create = 3 requests
        // Total: 4 * 3 = 12 requests
        assertEquals(12, requests.size)

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

        val viewModel: PhotoUploaderViewModel by inject()
        viewModel.uploadPhotos(ROOT_PATH, fileSystem)

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

        val viewModel: PhotoUploaderViewModel by inject()
        viewModel.uploadPhotos(ROOT_PATH, fileSystem)

        // Verify requests: 1 album, 2 uploads, 1 batch (only containing 1 photo)
        assertEquals(4, requests.size)
        
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

        val viewModel: PhotoUploaderViewModel by inject()
        viewModel.uploadPhotos(ROOT_PATH, fileSystem)

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

        val viewModel: PhotoUploaderViewModel by inject()
        viewModel.uploadPhotos(ROOT_PATH, fileSystem)

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

        val viewModel: PhotoUploaderViewModel by inject()
        
        // Call uploadPhotos with the start path "2024" relative to root logic
        val startPath = rootPath / "2024"
        viewModel.uploadPhotos(startPath.toString(), fileSystem)

        // Expectations: 3 Albums, 3 Uploads, 3 Batch creates -> 9 requests
        assertEquals(9, requests.size)

        requests.assertAlbumCreated("2024")
        requests.assertAlbumCreated("2024 - Holiday France")
        requests.assertAlbumCreated("2024 - Holiday France - Flower garden")

        requests.assertPhotoUploaded("Our family.jpg")
        requests.assertPhotoUploaded("Paris at night.png")
        requests.assertPhotoUploaded("Tree with blossom.webp")
    }

    // --- Helpers ---

    private fun createMockEngine(
        requestLog: MutableList<HttpRequestData>,
        shouldFailAlbumCreation: Boolean = false,
        shouldFailUploadForFile: String? = null
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
                    val results = listOf(
                         MediaItemResult("t1", StatusInfo(0, "OK")),
                         MediaItemResult("t2", StatusInfo(0, "OK"))
                    )
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

    private fun List<HttpRequestData>.assertAlbumCreated(expectedTitle: String) {
        val albumRequests = this.filter { it.url.toString().endsWith(ENDPOINT_ALBUMS) }
        val found = albumRequests.any { req ->
            val content = req.body
            (content is TextContent) && content.text.contains(expectedTitle)
        }
        assertTrue(found, "Album creation request for '$expectedTitle' not found")
    }

    private fun List<HttpRequestData>.assertPhotoUploaded(fileName: String) {
        val uploadRequests = this.filter { it.url.toString().endsWith(ENDPOINT_UPLOADS) }
        assertTrue(
            uploadRequests.any { it.headers["X-Goog-Upload-File-Name"] == fileName },
            "Upload for $fileName not found"
        )
    }

    companion object {
        private const val ROOT_PATH = "/photos"
        private const val ENDPOINT_ALBUMS = "/albums"
        private const val ENDPOINT_UPLOADS = "/uploads"
        private const val ENDPOINT_BATCH_CREATE = "/mediaItems:batchCreate"
    }
}
