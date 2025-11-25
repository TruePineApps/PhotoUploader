package com.truepine.photouploader.ui

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UploadPhotosViewModelTest : KoinTest {

    private lateinit var fileSystem: FakeFileSystem
    private val rootPath = "/photos".toPath()
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fileSystem = FakeFileSystem()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    private fun setupKoin(mockEngine: MockEngine) {
        startKoin {
            modules(module {
                single {
                    HttpClient(mockEngine) {
                        install(ContentNegotiation) {
                            json(Json { ignoreUnknownKeys = true })
                        }
                    }
                }
            })
        }
    }

    @Test
    fun `uploadPhotos throws IllegalArgumentException when root does not exist`() = runTest {
        val mockEngine = MockEngine { respond("OK") }
        setupKoin(mockEngine)
        val viewModel = UploadPhotosViewModel()
        viewModel.accessToken = "test_token"

        assertFailsWith<IllegalArgumentException> {
            viewModel.uploadPhotos("/photos", fileSystem)
        }
    }

    @Test
    fun `uploadPhotos throws IllegalArgumentException when root is not a directory`() = runTest {
        val mockEngine = MockEngine { respond("OK") }
        setupKoin(mockEngine)
        fileSystem.write(rootPath) { writeUtf8("not a directory") }
        val viewModel = UploadPhotosViewModel()
        viewModel.accessToken = "test_token"

        assertFailsWith<IllegalArgumentException> {
            viewModel.uploadPhotos("/photos", fileSystem)
        }
    }

    @Test
    fun `uploadPhotos successfully uploads photos`() = runTest {
        // Setup FileSystem
        fileSystem.createDirectory(rootPath)
        val yearDir = rootPath / "2023"
        fileSystem.createDirectory(yearDir)
        val topicDir = yearDir / "Holiday"
        fileSystem.createDirectory(topicDir)
        val photo1 = topicDir / "photo1.jpg"
        fileSystem.write(photo1) { writeUtf8("image data 1") }
        val photo2 = topicDir / "photo2.png"
        fileSystem.write(photo2) { writeUtf8("image data 2") }

        val requests = mutableListOf<HttpRequestData>()

        val mockEngine = MockEngine { request ->
            requests.add(request)
            val url = request.url.toString()
            when {
                url.endsWith("/albums") -> {
                    respond(
                        content = """{"id": "album_123", "title": "2023 - Holiday", "productUrl": "http://url"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
                url.endsWith("/uploads") -> {
                    respond(
                        content = "upload_token_${request.headers["X-Goog-Upload-File-Name"]}",
                        status = HttpStatusCode.OK
                    )
                }
                url.endsWith("/mediaItems:batchCreate") -> {
                    respond(
                        content = """
                            {
                                "newMediaItemResults": [
                                    { "uploadToken": "t1", "status": { "code": 0, "message": "OK" } },
                                    { "uploadToken": "t2", "status": { "code": 0, "message": "OK" } }
                                ]
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
                else -> error("Unhandled request: ${request.url}")
            }
        }

        setupKoin(mockEngine)
        val viewModel = UploadPhotosViewModel()
        viewModel.accessToken = "test_token"

        viewModel.uploadPhotos("/photos", fileSystem)

        // Verify requests
        assertEquals(4, requests.size) // 1 create album, 2 uploads, 1 batch create
        
        // Verify Create Album
        val createAlbumReq = requests.find { it.url.toString().endsWith("/albums") }
        assertTrue(createAlbumReq != null)
        
        // Verify Uploads
        val uploadRequests = requests.filter { it.url.toString().endsWith("/uploads") }
        assertEquals(2, uploadRequests.size)
        assertTrue(uploadRequests.any { it.headers["X-Goog-Upload-File-Name"] == "photo1.jpg" })
        assertTrue(uploadRequests.any { it.headers["X-Goog-Upload-File-Name"] == "photo2.png" })
        
        // Verify Batch Create
        val batchReq = requests.find { it.url.toString().endsWith("/mediaItems:batchCreate") }
        assertTrue(batchReq != null)
    }

    @Test
    fun `uploadPhotos skips topic if album creation fails`() = runTest {
        // Setup FileSystem
        fileSystem.createDirectory(rootPath)
        val yearDir = rootPath / "2023"
        fileSystem.createDirectory(yearDir)
        val topicDir = yearDir / "Holiday"
        fileSystem.createDirectory(topicDir)
        val photo1 = topicDir / "photo1.jpg"
        fileSystem.write(photo1) { writeUtf8("image data 1") }

        val requests = mutableListOf<HttpRequestData>()

        val mockEngine = MockEngine { request ->
            requests.add(request)
            val url = request.url.toString()
            when {
                url.endsWith("/albums") -> {
                    respond("Error creating album", status = HttpStatusCode.BadRequest)
                }
                else -> error("Should not be called: ${request.url}")
            }
        }

        setupKoin(mockEngine)
        val viewModel = UploadPhotosViewModel()
        viewModel.accessToken = "test_token"

        viewModel.uploadPhotos("/photos", fileSystem)

        // Verify only album creation was attempted
        assertEquals(1, requests.size)
        assertTrue(requests[0].url.toString().endsWith("/albums"))
    }

    @Test
    fun `uploadPhotos skips photo if upload fails`() = runTest {
        // Setup FileSystem
        fileSystem.createDirectory(rootPath)
        val yearDir = rootPath / "2023"
        fileSystem.createDirectory(yearDir)
        val topicDir = yearDir / "Holiday"
        fileSystem.createDirectory(topicDir)
        val photo1 = topicDir / "photo1.jpg"
        fileSystem.write(photo1) { writeUtf8("image data 1") }
        val photo2 = topicDir / "photo2.jpg"
        fileSystem.write(photo2) { writeUtf8("image data 2") }

        val requests = mutableListOf<HttpRequestData>()

        val mockEngine = MockEngine { request ->
            requests.add(request)
            val url = request.url.toString()
            when {
                url.endsWith("/albums") -> {
                    respond(
                        content = """{"id": "album_123", "title": "2023 - Holiday", "productUrl": "http://url"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
                url.endsWith("/uploads") -> {
                    val fileName = request.headers["X-Goog-Upload-File-Name"]
                    if (fileName == "photo1.jpg") {
                        respond("upload_token_1", status = HttpStatusCode.OK)
                    } else {
                        respond("Upload failed", status = HttpStatusCode.InternalServerError)
                    }
                }
                url.endsWith("/mediaItems:batchCreate") -> {
                     respond(
                        content = """
                            {
                                "newMediaItemResults": [
                                    { "uploadToken": "upload_token_1", "status": { "code": 0, "message": "OK" } }
                                ]
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
                else -> error("Unhandled request: ${request.url}")
            }
        }

        setupKoin(mockEngine)
        val viewModel = UploadPhotosViewModel()
        viewModel.accessToken = "test_token"

        viewModel.uploadPhotos("/photos", fileSystem)

        // Verify requests: 1 album, 2 uploads, 1 batch (only containing 1 photo)
        assertEquals(4, requests.size)
        
        // Check that batch create was called, implying at least one photo succeeded
        assertTrue(requests.any { it.url.toString().endsWith("/mediaItems:batchCreate") })
    }

    @Test
    fun `uploadPhotos batches media creation requests`() = runTest {
        // Setup FileSystem with 55 photos
        fileSystem.createDirectory(rootPath)
        val yearDir = rootPath / "2023"
        fileSystem.createDirectory(yearDir)
        val topicDir = yearDir / "Holiday"
        fileSystem.createDirectory(topicDir)
        
        repeat(55) { i ->
            val photo = topicDir / "photo_$i.jpg"
            fileSystem.write(photo) { writeUtf8("data") }
        }

        val requests = mutableListOf<HttpRequestData>()

        val mockEngine = MockEngine { request ->
            requests.add(request)
            val url = request.url.toString()
            when {
                url.endsWith("/albums") -> {
                    respond(
                        content = """{"id": "album_123", "title": "2023 - Holiday", "productUrl": "http://url"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
                url.endsWith("/uploads") -> {
                    respond("token", status = HttpStatusCode.OK)
                }
                url.endsWith("/mediaItems:batchCreate") -> {
                    // Don't strictly validate count here for simplicity, just return success
                    respond(
                        content = """{ "newMediaItemResults": [] }""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
                else -> error("Unhandled request: ${request.url}")
            }
        }

        setupKoin(mockEngine)
        val viewModel = UploadPhotosViewModel()
        viewModel.accessToken = "test_token"

        viewModel.uploadPhotos("/photos", fileSystem)

        val batchRequests = requests.filter { it.url.toString().endsWith("/mediaItems:batchCreate") }
        assertEquals(2, batchRequests.size) // 55 photos -> 50 + 5 -> 2 batches
    }
}
