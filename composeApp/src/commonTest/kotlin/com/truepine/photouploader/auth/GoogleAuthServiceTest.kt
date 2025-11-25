package com.truepine.photouploader.auth

import com.truepine.photouploader.di.AppModule
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.ksp.generated.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GoogleAuthServiceTest : KoinTest {

    // Inject the service using Koin
    private val authService: GoogleAuthService by inject()

    @BeforeTest
    fun setup() {
        startKoin {
            // Load the same module used in the app
            modules(AppModule().module)
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testSignInWithStub() = runTest {
        // 1. Call the signIn method
        // Since it's a stub, we expect it to handle the "delay" and return our fake token
        val result = authService.signIn()

        // 2. Assert the result
        // In StubGoogleAuthService, we now return "fake_access_token_12345"
        assertNotNull(result, "Stub service should return a token for signIn")
        assertEquals("fake_access_token_12345", result)
    }
}
