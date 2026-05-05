package com.truepineapps.photouploader.ui.util

import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.loggerConfigInit
import com.truepineapps.photouploader.app.di.viewModelModule
import com.truepineapps.photouploader.core.io.PlatformFileSystem
import com.truepineapps.photouploader.core.log.TimestampMessageFormatter
import com.truepineapps.photouploader.feature.uploader.data.repository.PhotoDirectoryRepositoryImpl
import com.truepineapps.photouploader.feature.uploader.data.repository.PhotoUploaderImpl
import com.truepineapps.photouploader.feature.uploader.domain.repository.PhotoDirectoryRepository
import com.truepineapps.photouploader.feature.uploader.domain.repository.PhotoUploader
import com.truepineapps.photouploader.foundation.auth.domain.repository.GoogleAuthService
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.fakefilesystem.FakeFileSystem
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun startTestKoin(
    mockEngine: MockEngine? = null,
    fileSystem: FakeFileSystem = FakeFileSystem(),
    serviceStub: GoogleAuthService = GoogleAuthServiceTestStub(signInToken = "valid_token")
) {
    val httpEngine =
        mockEngine ?: MockEngine { request -> error("Unhandled request: ${request.url}") }
    startKoin {
        modules(
            viewModelModule(),
            module {
                single<FileSystem> { fileSystem }
                single<PlatformFileSystem> { FakePlatformFileSystem(fileSystem) }
                single<Json> { Json { ignoreUnknownKeys = true } }
                single<HttpClient> {
                    HttpClient(httpEngine) {
                        if (mockEngine != null) {
                            install(ContentNegotiation) { json(get()) }
                        }
                    }
                }
                single<GoogleAuthService> { serviceStub }
                single { loggerConfigInit(CommonWriter(TimestampMessageFormatter)) }
                single { Logger(config = get(), tag = "Test") }
                singleOf(::PhotoUploaderImpl) { bind<PhotoUploader>() }
                singleOf(::PhotoDirectoryRepositoryImpl) { bind<PhotoDirectoryRepository>() }
            },
        )
    }
}
