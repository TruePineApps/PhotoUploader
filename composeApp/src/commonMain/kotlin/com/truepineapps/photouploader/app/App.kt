package com.truepineapps.photouploader.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.picker.coil.KmpFileFetcher
import com.truepineapps.photouploader.app.navigation.PhotoUploaderAppNavHost
import com.truepineapps.photouploader.app.theme.AppTheme
import com.truepineapps.photouploader.core.feature.legal.ui.LegalGateScreen
import com.truepineapps.photouploader.core.feature.moremenu.navigation.MoreMenuNavigatorImpl
import com.truepineapps.photouploader.core.feature.settings.ui.AppEnvironment
import com.truepineapps.photouploader.core.presentation.component.platformpicker.PlatformPicker
import com.truepineapps.photouploader.core.presentation.design.Opacity
import com.truepineapps.photouploader.feature.uploader.ui.PhotoUploaderAppBar
import com.truepineapps.photouploader.feature.uploader.ui.PhotoUploaderDestination
import com.truepineapps.photouploader.feature.uploader.viewmodel.PhotoUploaderViewModel
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.app_name
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    modifier: Modifier = Modifier,
    startDestination: String = PhotoUploaderDestination.route,
) {
    // Configure the Coil image loader for KmpFile and using max 25% of avail memory for thumbnails
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KmpFileFetcher.Factory()) }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .crossfade(true)
            .build()
    }

    AppTheme {
        AppEnvironment(localeViewModel = koinInject()) {
            LegalGateScreen {
                ThemedLocalizedLegalAcceptedApp(
                    startDestination = startDestination,
                    modifier = modifier
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemedLocalizedLegalAcceptedApp(
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    val filePicker: PlatformPicker = koinInject()
    val viewModel: PhotoUploaderViewModel = koinViewModel()

    val appName = stringResource(resource = Res.string.app_name)
    var title by rememberSaveable { mutableStateOf(appName) }

    // Additional action icons shown on the top app bar
    val defaultAction: @Composable (RowScope.() -> Unit) = {}
    val actions = remember { mutableStateOf(defaultAction) }
    // When the screen shows a dialog, a close action must be provided.
    val defaultCloseAction = { }
    val closeAction = remember { mutableStateOf(defaultCloseAction) }

    val navController = rememberNavController()
    // The scroll state of the overview must not be applied to the other screens
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination
    val scrollBehavior = if (currentRoute == PhotoUploaderDestination.route) {
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    } else {
        null
    }

    // File Picker is a global state in the app
    val context = LocalPlatformContext.current
    val uiState by viewModel.uiState.collectAsState()
    filePicker.PlatformDirectoryPicker(uiState.isShowDirPicker) { kmpFile ->
        if (kmpFile != null) {
            viewModel.updatePath(kmpFile, context)
        }
        viewModel.updateShowDirPicker(false)
    }

    val showDirPickerAction = {
        viewModel.updateShowDirPicker(true)
        navController.popBackStack(PhotoUploaderDestination.route, inclusive = false)
        Unit
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    uiState.globalErrorMessage?.let {
        val message = it.asString()
        LaunchedEffect(it) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Long,
                    withDismissAction = true
                )
                viewModel.clearGlobalErrorMessage()
            }
        }
    }

    val isEnabled = !uiState.isShowDirPicker
    Scaffold(
        modifier = if (scrollBehavior != null) modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else modifier,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                // Custom Snackbar with error styling
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    actionColor = MaterialTheme.colorScheme.onError
                )
            }
        },
        topBar = {
            PhotoUploaderAppBar(
                moreMenuNavigator = MoreMenuNavigatorImpl(navController),
                title = title,
                isEnabled = isEnabled,
                canNavigateBack = navController.previousBackStackEntry != null,
                closeDialog = if (closeAction.value == defaultCloseAction) null else closeAction.value,
                navigateUp = { navController.navigateUp() },
                showDirPicker = showDirPickerAction,
                scrollBehavior = scrollBehavior,
                actions = actions.value,
                viewModel = viewModel,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            PhotoUploaderAppNavHost(
                navController = navController,
                onUpdateTopAppBar = { newTitle, newCloseDialog, newActions ->
                    title = newTitle
                    closeAction.value = newCloseDialog ?: defaultCloseAction
                    actions.value = newActions
                },
                showDirPicker = showDirPickerAction,
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isEnabled) Opacity.FULL.value else Opacity.DISABLED.value),
                startDestination = startDestination
            )
            // When disabled, lay a transparent Surface over the top to intercept all clicks.
            if (!isEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = Opacity.TRANSPARENT.value) // Transparent
                ) { /* This Surface consumes all touch events, effectively disabling the content below */ }
            }
        }
    }

}
