package com.truepineapps.photouploader

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.about
import com.truepineapps.photouploader.resources.app_name
import com.truepineapps.photouploader.resources.appicon
import com.truepineapps.photouploader.resources.back_button
import com.truepineapps.photouploader.resources.choose_folder
import com.truepineapps.photouploader.resources.close_button
import com.truepineapps.photouploader.resources.licenses
import com.truepineapps.photouploader.resources.menu
import com.truepineapps.photouploader.resources.preferences
import com.truepineapps.photouploader.resources.upload_photos
import com.truepineapps.photouploader.ui.Dimensions
import com.truepineapps.photouploader.ui.components.platformpicker.PlatformPicker
import com.truepineapps.photouploader.ui.components.ThemedIconButton
import com.truepineapps.photouploader.ui.localization.AppEnvironment
import com.truepineapps.photouploader.ui.navigation.MenuNavigator
import com.truepineapps.photouploader.ui.navigation.MenuNavigatorImpl
import com.truepineapps.photouploader.ui.navigation.PhotoUploaderAppNavHost
import com.truepineapps.photouploader.ui.screen.uploader.PhotoUploaderDestination
import com.truepineapps.photouploader.ui.screen.uploader.PhotoUploaderViewModel
import com.truepineapps.photouploader.ui.theme.AppTheme
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun App(
    modifier: Modifier = Modifier,
    startDestination: String = PhotoUploaderDestination.route,
) {
    val windowClass = calculateWindowSizeClass()
    val isHorizontalLayout = windowClass.widthSizeClass != WindowWidthSizeClass.Compact

    AppTheme {
        AppEnvironment(localeViewModel = koinInject()) {
            ThemedLocalizedApp(
                startDestination = startDestination,
                isHorizontalLayout = isHorizontalLayout,
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemedLocalizedApp(
    startDestination: String,
    isHorizontalLayout: Boolean,
    modifier: Modifier = Modifier,
    filePicker: PlatformPicker = koinInject(),
    viewModel: PhotoUploaderViewModel = koinInject(),
) {
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

    // Make sure the platform context is set
    viewModel.platformContext = LocalPlatformContext.current

    // File Picker is a global state in the app
    val uiState by viewModel.uiState.collectAsState()
    filePicker.PlatformDirectoryPicker(uiState.isShowDirPicker) { kmpFile ->
        if (kmpFile != null) {
            viewModel.updatePath(kmpFile)
        }
        viewModel.updateShowDirPicker(false)
    }

    val busy = uiState.busy()
    // Only allow uploading if we have albums, path is set, and not currently busy
    val canUpload = uiState.albums.isNotEmpty() && uiState.path.isNotBlank() && !busy

    val showDirPickerAction = {
        viewModel.updateShowDirPicker(true)
        navController.popBackStack(PhotoUploaderDestination.route, inclusive = false)
        Unit
    }

    Scaffold(
        modifier = if (scrollBehavior != null) modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else modifier,
        topBar = {
            PhotoLoaderAppBar(
                menuNavigator = MenuNavigatorImpl(navController),
                title = title,
                canNavigateBack = navController.previousBackStackEntry != null,
                closeDialog = if (closeAction.value == defaultCloseAction) null else closeAction.value,
                navigateUp = { navController.navigateUp() },
                showDirPicker = showDirPickerAction,
                uploadPhotos = {
                    // Since uploadPhotos is a suspend function or launches a coroutine,
                    // and we just need to trigger it here.
                    // The ViewModel's uploadPhotos returns a Job? which we can ignore here or handle if needed.
                    viewModel.uploadPhotos()
                },
                canChooseDirectory = !busy,
                canUploadPhotos = canUpload,
                scrollBehavior = scrollBehavior,
                actions = actions.value
            )
        },
    ) { innerPadding ->
        PhotoUploaderAppNavHost(
            navController = navController,
            startDestination = startDestination,
            isHorizontalLayout = isHorizontalLayout,
            onUpdateTopAppBar = { newTitle, newCloseDialog, newActions ->
                title = newTitle
                closeAction.value = newCloseDialog ?: defaultCloseAction
                actions.value = newActions
            },
            showDirPicker = showDirPickerAction,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            viewModel = viewModel
        )
    }
}

/**
 * App bar to display title and conditionally display the back navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoLoaderAppBar(
    menuNavigator: MenuNavigator,
    scrollBehavior: TopAppBarScrollBehavior?,
    title: String,
    canNavigateBack: Boolean,
    closeDialog: (() -> Unit)?,
    navigateUp: () -> Unit,
    showDirPicker: () -> Unit,
    uploadPhotos: () -> Unit,
    canChooseDirectory: Boolean,
    canUploadPhotos: Boolean,
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit) = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
) {
    // The expanded state of the dropdown menu.
    var expanded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = { Text(text = title, color = MaterialTheme.colorScheme.primary) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (closeDialog != null) {
                ThemedIconButton(
                    imageVector = Icons.Filled.Close,
                    contentDescriptionResource = Res.string.close_button,
                    onClick = {
                        closeDialog()
                        navigateUp()
                    },
                    enabled = true
                )
            } else if (canNavigateBack) {
                ThemedIconButton(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescriptionResource = Res.string.back_button,
                    onClick = navigateUp,
                    enabled = true
                )
            } else {
                Image(
                    bitmap = imageResource(Res.drawable.appicon),
                    contentDescription = "",
                    modifier = Modifier.size(Dimensions.medium_icon_size)
                )
            }
        },
        actions = {
            actions()
            ThemedIconButton(
                imageVector = Icons.Filled.PermMedia,
                contentDescriptionResource = Res.string.choose_folder,
                enabled = canChooseDirectory,
                onClick = showDirPicker
            )
            ThemedIconButton(
                imageVector = Icons.Filled.Upload,
                contentDescriptionResource = Res.string.upload_photos,
                enabled = canUploadPhotos,
                onClick = uploadPhotos,
            )
            ThemedIconButton(
                imageVector = Icons.Filled.MoreVert,
                contentDescriptionResource = Res.string.menu,
                enabled = true,
                onClick = { expanded = true }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.preferences)) },
                    onClick = { menuNavigator.navigateToSettings(); expanded = false }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.about)) },
                    onClick = { menuNavigator.navigateToAbout(); expanded = false }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.licenses)) },
                    onClick = { menuNavigator.navigateToLicenseScreen(); expanded = false }
                )
            }
        },
        colors = colors
    )
}
