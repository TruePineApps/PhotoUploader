package com.truepineapps.photouploader.ui

import com.truepineapps.photouploader.auth.GoogleAuthService
import com.truepineapps.photouploader.di.appModule
import com.truepineapps.photouploader.di.viewModelModule
import com.truepineapps.photouploader.ui.screen.uploader.PhotoUploaderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoUploaderViewModelTest : KoinTest {

    private val testDispatcher = StandardTestDispatcher()

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
                // Load the app module
                appModule,
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
        val successStub = GoogleAuthServiceTestStub(restoreToken = "restored_token")
        startTestKoin(successStub)

        // 2. Inject the ViewModel via Koin.
        // Accessing the property triggers creation, which runs the init block.
        val viewModel: PhotoUploaderViewModel by inject()
        
        // Start collecting the state flow to ensure it updates
        backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect()
        }
        
        // Force creation by accessing property and check initial state
        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated, "Initially should not be authenticated")

        // 3. Run pending coroutines (from the init block)
        testDispatcher.scheduler.advanceUntilIdle()

        // 4. Verify state changed to true
        assertTrue(viewModel.uiState.value.isAuthenticated, "Should be authenticated after successful restore")
    }

    @Test
    fun testSignInSuccess() = runTest {
        val successStub = GoogleAuthServiceTestStub(signInToken = "valid_token")
        startTestKoin(successStub)

        val viewModel: PhotoUploaderViewModel by inject()

        // Start collecting the state flow to ensure it updates
        backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect()
        }

        // Run initial auth check (which will fail/do nothing since restoreToken is null)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isAuthenticated, "Initially should not be authenticated")

        // 3. Trigger Sign In
        viewModel.signIn()
        testDispatcher.scheduler.advanceUntilIdle()

        // 4. Verify state changed to true
        assertTrue(viewModel.uiState.value.isAuthenticated, "Should be authenticated after successful sign-in")
    }

    @Test
    fun testSignInFailure() = runTest {
        val failureStub = GoogleAuthServiceTestStub(signInToken = null)
        startTestKoin(failureStub)

        val viewModel: PhotoUploaderViewModel by inject()

        // Start collecting the state flow to ensure it updates
        backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect()
        }

        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isAuthenticated, "Initially should not be authenticated")

        // 3. Trigger Sign In
        viewModel.signIn()
        testDispatcher.scheduler.advanceUntilIdle()

        // 4. Verify state remains false
        assertFalse(viewModel.uiState.value.isAuthenticated, "Should NOT be authenticated if signIn returns null")
    }
}
