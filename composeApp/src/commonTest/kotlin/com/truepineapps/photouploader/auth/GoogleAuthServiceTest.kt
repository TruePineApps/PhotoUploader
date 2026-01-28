package com.truepineapps.photouploader.auth

import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.di.platformModule
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// This is basically a test that the AuthService is configured correctly.
class GoogleAuthServiceTest : KoinTest {

    // Inject the service using Koin
    private val authService: GoogleAuthService by inject()
    private val log: Logger by inject()

    @BeforeTest
    fun setup() {
        startKoin {
            // Load the same module used in the app
            modules(platformModule())
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testSignInWithStub() = runTest {
        assertNotNull(authService, "AuthService service must be initialized in platformModule")

        val isStub = authService::class.simpleName?.contains("Stub") == true
        log.d { "Running signIn test: $isStub" }

        if (isStub) {
            // 1. Call the signIn method
            // Since it's a stub, we expect it to handle the "delay" and return our fake token
            val result = authService.signIn()

            // 2. Assert the result
            assertNotNull(result, "Stub service should return a user profile for signIn")
            // In StubGoogleAuthService, we now return "fake_access_token_12345"
            assertEquals("fake_access_token_12345", result.accessToken)
        }
    }
}
