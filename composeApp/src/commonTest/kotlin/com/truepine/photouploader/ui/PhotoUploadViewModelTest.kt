package com.truepine.photouploader.ui

import com.truepine.photouploader.auth.GoogleAuthService
import com.truepine.photouploader.di.AppModule
import com.truepine.photouploader.di.viewModelModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoUploadViewModelTest : KoinTest {

    private val testDispatcher = StandardTestDispatcher()

    // Reusable stub class for different test scenarios
    private class GoogleAuthServiceStub(
        private val signInToken: String? = null,
        private val restoreToken: String? = null
    ) : GoogleAuthService {
        override suspend fun signIn(): String? = signInToken
        override suspend fun signOut() {}
        override suspend fun restoreSignIn(): String? = restoreToken
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    private fun startTestKoin(serviceStub: GoogleAuthService) {
        startKoin {
            modules(
                // Load the app module (for any other KSP-generated definitions)
                AppModule().module,
                // Load the new ViewModel module where PhotoUploadViewModel is defined
                viewModelModule(),
                // Provide the mock/stub service for this specific test
                module {
                    single<GoogleAuthService> { serviceStub }
                }
            )
        }
    }

    @Test
    fun testRestoreSignInSuccess() = runTest {
        // 1. Prepare the stub
        val successStub = GoogleAuthServiceStub(restoreToken = "restored_token")
        startTestKoin(successStub)

        // 2. Inject the ViewModel via Koin.
        // Accessing the property triggers creation, which runs the init block.
        val viewModel: PhotoUploadViewModel by inject()
        
        // Force creation by accessing property and check initial state
        val state = viewModel.isAuthenticated
        assertFalse(state.value, "Initially should not be authenticated")

        // 3. Run pending coroutines (from the init block)
        testDispatcher.scheduler.advanceUntilIdle()

        // 4. Verify state changed to true
        assertTrue(viewModel.isAuthenticated.value, "Should be authenticated after successful restore")
    }

    @Test
    fun testSignInSuccess() = runTest {
        val successStub = GoogleAuthServiceStub(signInToken = "valid_token")
        startTestKoin(successStub)

        val viewModel: PhotoUploadViewModel by inject()

        // Run initial auth check (which will fail/do nothing since restoreToken is null)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.isAuthenticated.value, "Initially should not be authenticated")

        // 3. Trigger Sign In
        viewModel.signIn()
        testDispatcher.scheduler.advanceUntilIdle()

        // 4. Verify state changed to true
        assertTrue(viewModel.isAuthenticated.value, "Should be authenticated after successful sign-in")
    }

    @Test
    fun testSignInFailure() = runTest {
        val failureStub = GoogleAuthServiceStub(signInToken = null)
        startTestKoin(failureStub)

        val viewModel: PhotoUploadViewModel by inject()

        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.isAuthenticated.value, "Initially should not be authenticated")

        // 3. Trigger Sign In
        viewModel.signIn()
        testDispatcher.scheduler.advanceUntilIdle()

        // 4. Verify state remains false
        assertFalse(viewModel.isAuthenticated.value, "Should NOT be authenticated if signIn returns null")
    }
}
