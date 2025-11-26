package com.truepine.photouploader.ui

import com.truepine.photouploader.auth.GoogleAuthService
import com.truepine.photouploader.di.viewModelModule
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
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.getValue
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UploadPhotosTest : KoinTest {

    private lateinit var fileSystem: FakeFileSystem
    private val rootPath = "/photos".toPath()
    private val testDispatcher = StandardTestDispatcher()
    private val json = Json { ignoreUnknownKeys = true }

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

    @Test
    fun `uploadPhotos throws IllegalArgumentException when root does not exist`() = runTest {
        val mockEngine = MockEngine { respond("OK") }
        setupKoin(mockEngine)
        val viewModel: PhotoUploadViewModel by inject()

        assertFailsWith<IllegalArgumentException> {
            viewModel.uploadPhotos( "/photos", fileSystem)
        }
    }

    @Test
    fun `uploadPhotos throws IllegalArgumentException when root is not a directory`() = runTest {
        val mockEngine = MockEngine { respond("OK") }
        setupKoin(mockEngine)
        fileSystem.write(rootPath) { writeUtf8("not a directory") }
        val viewModel: PhotoUploadViewModel by inject()

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
                        headers = headersOf(
                            HttpHeaders.ContentType,
                            ContentType.Application.Json.toString()
                        )
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
                        headers = headersOf(
                            HttpHeaders.ContentType,
                            ContentType.Application.Json.toString()
                        )
                    )
                }

                else -> error("Unhandled request: ${request.url}")
            }
        }

        setupKoin(mockEngine)
        val viewModel: PhotoUploadViewModel by inject()

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
    fun `uploadPhotos successfully uploads from multiple years and topics`() = runTest {
        // Setup FileSystem: 2 Years, each with 2 Topics, each Topic with 1 Photo
        fileSystem.createDirectory(rootPath)

        val years = listOf("2023", "2024")
        val topics = listOf("Holiday", "Work")

        years.forEach { year ->
            val yearDir = rootPath / year
            fileSystem.createDirectory(yearDir)
            topics.forEach { topic ->
                val topicDir = yearDir / topic
                fileSystem.createDirectory(topicDir)
                val photo = topicDir / "photo.jpg"
                fileSystem.write(photo) { writeUtf8("image data") }
            }
        }

        val requests = mutableListOf<HttpRequestData>()

        val mockEngine = MockEngine { request ->
            requests.add(request)
            val url = request.url.toString()
            when {
                url.endsWith("/albums") -> {
                    respond(
                        content = """{"id": "album_id", "title": "Album Title", "productUrl": "http://url"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(
                            HttpHeaders.ContentType,
                            ContentType.Application.Json.toString()
                        )
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
                                    { "uploadToken": "t1", "status": { "code": 0, "message": "OK" } }
                                ]
                            }
                        """.trimIndent(),
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

        setupKoin(mockEngine)
        val viewModel: PhotoUploadViewModel by inject()

        viewModel.uploadPhotos("/photos", fileSystem)

        // Total expected requests:
        // 2 Years * 2 Topics = 4 Topics
        // Per Topic: 1 Album Create + 1 Upload + 1 Batch Create = 3 requests
        // Total: 4 * 3 = 12 requests
        assertEquals(12, requests.size)

        // Verify 4 Album Creation Requests
        val albumRequests = requests.filter { it.url.toString().endsWith("/albums") }
        assertEquals(4, albumRequests.size)

        // Verify album titles
        years.forEach { year ->
            topics.forEach { topic ->
                val expectedTitle = "$year - $topic"
                val found = albumRequests.any { req ->
                    // We need to read the body to check the title
                    val content = req.body
                    (content is TextContent) && content.text.contains(expectedTitle)
                }
                assertTrue(found, "Album creation request for '$expectedTitle' not found")
            }
        }

        // Verify 4 Upload Requests
        val uploadRequests = requests.filter { it.url.toString().endsWith("/uploads") }
        assertEquals(4, uploadRequests.size)

        // Verify 4 Batch Create Requests
        val batchRequests =
                requests.filter { it.url.toString().endsWith("/mediaItems:batchCreate") }
        assertEquals(4, batchRequests.size)
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
        val viewModel: PhotoUploadViewModel by inject()

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
                        headers = headersOf(
                            HttpHeaders.ContentType,
                            ContentType.Application.Json.toString()
                        )
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
                        headers = headersOf(
                            HttpHeaders.ContentType,
                            ContentType.Application.Json.toString()
                        )
                    )
                }

                else -> error("Unhandled request: ${request.url}")
            }
        }

        setupKoin(mockEngine)
        val viewModel: PhotoUploadViewModel by inject()

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
                        headers = headersOf(
                            HttpHeaders.ContentType,
                            ContentType.Application.Json.toString()
                        )
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
                        headers = headersOf(
                            HttpHeaders.ContentType,
                            ContentType.Application.Json.toString()
                        )
                    )
                }

                else -> error("Unhandled request: ${request.url}")
            }
        }

        setupKoin(mockEngine)
        val viewModel: PhotoUploadViewModel by inject()

        viewModel.uploadPhotos("/photos", fileSystem)

        val batchRequests =
                requests.filter { it.url.toString().endsWith("/mediaItems:batchCreate") }
        assertEquals(2, batchRequests.size) // 55 photos -> 50 + 5 -> 2 batches
    }

    @Test
    fun `uploadPhotos skips album creation if topic directory is empty`() = runTest {
        // Setup FileSystem
        fileSystem.createDirectory(rootPath)
        val yearDir = rootPath / "2023"
        fileSystem.createDirectory(yearDir)
        val topicDir = yearDir / "Holiday"
        fileSystem.createDirectory(topicDir)
        // No photos added

        val requests = mutableListOf<HttpRequestData>()

        val mockEngine = MockEngine { request ->
            requests.add(request)
            // Should not be called
            error("Should not be called: ${request.url}")
        }

        setupKoin(mockEngine)
        val viewModel: PhotoUploadViewModel by inject()

        viewModel.uploadPhotos("/photos", fileSystem)

        // Verify no requests were made
        assertTrue(requests.isEmpty(), "No requests should be made for empty topic directory")
    }

    @Test
    fun `uploadPhotos successfully uploads nested structure`() = runTest {
        // Setup FileSystem
        val startPath = rootPath / "2024"
        fileSystem.createDirectory(rootPath)
        fileSystem.createDirectory(startPath)

        // Level 1: 2024
        val photo1 = startPath / "Our family.jpg"
        fileSystem.write(photo1) { writeUtf8("family") }

        // Level 2: Holiday France
        val holidayDir = startPath / "Holiday France"
        fileSystem.createDirectory(holidayDir)
        val photo2 = holidayDir / "Paris at night.png"
        fileSystem.write(photo2) { writeUtf8("paris") }

        // Level 3: Flower garden
        val flowerDir = holidayDir / "Flower garden"
        fileSystem.createDirectory(flowerDir)
        val photo3 = flowerDir / "Tree with blossom.webp"
        fileSystem.write(photo3) { writeUtf8("tree") }

        val requests = mutableListOf<HttpRequestData>()

        val mockEngine = MockEngine { request ->
            requests.add(request)
            val url = request.url.toString()
            when {
                url.endsWith("/albums") -> {
                    respond(
                        content = """{"id": "album_id", "title": "Title", "productUrl": "http://url"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(
                            HttpHeaders.ContentType,
                            ContentType.Application.Json.toString()
                        )
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
                                    { "uploadToken": "t1", "status": { "code": 0, "message": "OK" } }
                                ]
                            }
                        """.trimIndent(),
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

        setupKoin(mockEngine)
        val viewModel: PhotoUploadViewModel by inject()

        // Call uploadPhotos with the start path "2024"
        viewModel.uploadPhotos(startPath.toString(), fileSystem)

        // Expectations:
        // 3 Directories with photos -> 3 Albums created, 3 Uploads, 3 Batch creates.
        // Total 9 requests.

        assertEquals(9, requests.size)

        val albumRequests = requests.filter { it.url.toString().endsWith("/albums") }
        assertEquals(3, albumRequests.size)

        // Check Album Titles
        // 1. "2024"
        // 2. "2024 - Holiday France"
        // 3. "2024 - Holiday France - Flower garden"

        val expectedTitles = listOf(
            "2024",
            "2024 - Holiday France",
            "2024 - Holiday France - Flower garden"
        )

        expectedTitles.forEach { title ->
            val found = albumRequests.any { req ->
                val content = req.body
                (content is TextContent) && content.text.contains(title)
            }
            assertTrue(found, "Album creation request for '$title' not found")
        }

        val uploadRequests = requests.filter { it.url.toString().endsWith("/uploads") }
        assertEquals(3, uploadRequests.size)

        // Check File Names in Headers
        val expectedFiles = listOf("Our family.jpg", "Paris at night.png", "Tree with blossom.webp")
        expectedFiles.forEach { fileName ->
            assertTrue(
                uploadRequests.any { it.headers["X-Goog-Upload-File-Name"] == fileName },
                "Upload for $fileName not found"
            )
        }
    }
}
