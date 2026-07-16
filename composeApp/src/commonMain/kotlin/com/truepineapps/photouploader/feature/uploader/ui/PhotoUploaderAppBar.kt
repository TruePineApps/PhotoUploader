package com.truepineapps.photouploader.feature.uploader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import coil3.compose.AsyncImage
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.truepineapps.photouploader.core.feature.moremenu.ui.MoreMenu
import com.truepineapps.photouploader.core.feature.moremenu.navigation.MoreMenuNavigator
import com.truepineapps.photouploader.core.presentation.component.ThemedIconButton
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.core.presentation.design.Opacity
import com.truepineapps.photouploader.feature.uploader.viewmodel.PhotoUploaderViewModel
import com.truepineapps.photouploader.foundation.auth.domain.model.UserProfile
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.appicon
import com.truepineapps.photouploader.resources.back_button
import com.truepineapps.photouploader.resources.cancel
import com.truepineapps.photouploader.resources.choose_folder
import com.truepineapps.photouploader.resources.close_button
import com.truepineapps.photouploader.resources.connected_as
import com.truepineapps.photouploader.resources.sign_in
import com.truepineapps.photouploader.resources.sign_in_menu
import com.truepineapps.photouploader.resources.sign_out
import com.truepineapps.photouploader.resources.sign_out_menu
import com.truepineapps.photouploader.resources.upload_photos
import com.truepineapps.photouploader.resources.uploading
import com.truepineapps.photouploader.resources.waiting_for_browser_sign_in
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.stringResource

/**
 * App bar to display from left to right:
 * # NavigationIcon:
 *  - On PhotoUploader Screen: Sign in status:
 *   . not signed in: Launcher icon with sign in dropdown menu
 *   . signing in: Progress indicator with cancel dropdown menu
 *   . signed in: Avatar with sign out dropdown menu
 *  - On Album Screen: Back navigation
 * # Path
 * # Custom actions for the screen
 * # Upload button
 *  - Upload status:
 *   . not uploading: Upload button
 *   . uploading: Progress indicator with cancel dropdown menu
 * # Menu button
 *   . Preferences
 *   . About
 *   . Licenses
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoUploaderAppBar(
    moreMenuNavigator: MoreMenuNavigator,
    scrollBehavior: TopAppBarScrollBehavior?,
    title: String,
    isEnabled: Boolean,
    canNavigateBack: Boolean,
    closeDialog: (() -> Unit)?,
    navigateUp: () -> Unit,
    showDirPicker: () -> Unit,
    viewModel: PhotoUploaderViewModel,
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit) = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSigningIn = uiState.isSigningIn
    val isUploading = uiState.isUploading
    val canChooseDirectory = uiState.idle()
    // Only allow uploading if we have albums, path is set, and not currently busy
    val canUploadPhotos = uiState.albumUiStates.isNotEmpty() && uiState.path.isNotBlank() && uiState.idle()
    val userProfile = uiState.userProfile


    // Get the disabled text color
    val titleTextColor = MaterialTheme.colorScheme.primary
    val disabledTitleTextColor = titleTextColor.copy(alpha = Opacity.DISABLED.value)

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                color = if (isEnabled) titleTextColor else disabledTitleTextColor
            )
        },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (isSigningIn) {
                ProgressMenu(
                    stringResource(Res.string.waiting_for_browser_sign_in)
                ) {
                    viewModel.cancelProcess()
                }
            } else if (closeDialog != null) {
                ThemedIconButton(
                    imageVector = Icons.Filled.Close,
                    contentDescriptionResource = Res.string.close_button,
                    onClick = {
                        closeDialog()
                        navigateUp()
                    },
                    enabled = isEnabled
                )
            } else if (canNavigateBack) {
                ThemedIconButton(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescriptionResource = Res.string.back_button,
                    onClick = navigateUp,
                    enabled = isEnabled
                )
            } else if (userProfile != null) {
                AvatarMenu(
                    userProfile,
                    isEnabled
                ) { viewModel.signOut() }
            } else {
                SignInMenu(
                    isEnabled
                ) { viewModel.signIn() }
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
            if (isUploading) {
                ProgressMenu(
                    stringResource(Res.string.uploading)
                ) {
                    viewModel.cancelProcess()
                }
            } else {
                val platformContext = LocalPlatformContext.current
                ThemedIconButton(
                    imageVector = Icons.Filled.Upload,
                    contentDescriptionResource = Res.string.upload_photos,
                    enabled = canUploadPhotos,
                    onClick = {
                        // The ViewModel's uploadPhotos launches a coroutine that just needs to be
                        // triggered here. The returned Job can be ignored here or handled if needed.
                        viewModel.uploadPhotos(platformContext)
                    },
                )
            }
            MoreMenu(
                isEnabled,
                moreMenuNavigator
            )
        },
        colors = colors
    )
}

@Composable
private fun SignInMenu(
    isEnabled: Boolean,
    signIn: () -> Unit,
) {
    // The expanded state of the sign in dropdown menu.
    var signInExpanded by remember { mutableStateOf(false) }

    Box {
        Image(
            bitmap = imageResource(Res.drawable.appicon),
            contentDescription = stringResource(Res.string.sign_in_menu),
            modifier = Modifier
                .size(Dimensions.medium_icon_size)
                .clickable { signInExpanded = true }
                .alpha(if (isEnabled) Opacity.FULL.value else Opacity.DISABLED.value),
        )
        DropdownMenu(
            expanded = signInExpanded,
            onDismissRequest = { signInExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.sign_in)) },
                onClick = {
                    signIn()
                    signInExpanded = false
                }
            )
        }
    }
}


@Composable
private fun ProgressMenu(progressText: String, onCancel: () -> Unit) {
    // The expanded state of the sign in in progress dropdown menu.
    var progressMenuExpanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { progressMenuExpanded = true }) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimensions.icon_size),
                strokeWidth = Dimensions.stroke_size,
                color = MaterialTheme.colorScheme.primary
            )
        }
        DropdownMenu(
            expanded = progressMenuExpanded,
            onDismissRequest = { progressMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(progressText) },
                onClick = { progressMenuExpanded = false },
                enabled = false
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.cancel)) },
                onClick = {
                    progressMenuExpanded = false
                    onCancel()
                },
                leadingIcon = {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            )
        }
    }
}

@Composable
fun AvatarMenu(
    userProfile: UserProfile,
    isEnabled: Boolean,
    signOut: () -> Unit,
) {
    // The expanded state of the avatar dropdown menu.
    var avatarExpanded by remember { mutableStateOf(false) }

    Box {
        if (userProfile.avatarUrl != null) {
            AsyncImage(
                model = userProfile.avatarUrl,
                contentDescription = stringResource(Res.string.sign_out_menu),
                colorFilter = if (!isEnabled) ColorFilter.colorMatrix(
                    // A saturation of 0f will remove all color, resulting in a grayscale image.
                    ColorMatrix().apply { setToSaturation(0f) }
                ) else null,
                modifier = Modifier
                    .padding(Dimensions.padding_small)
                    .size(Dimensions.medium_icon_size)
                    .clip(CircleShape)
                    .clickable { avatarExpanded = true }
            )
        } else {
            // Fallback icon if no avatar URL
            ThemedIconButton(
                imageVector = Icons.Filled.Person,
                contentDescriptionResource = Res.string.sign_out_menu,
                enabled = isEnabled,
                onClick = { avatarExpanded = true },
                modifier = Modifier.clip(CircleShape)
            )
        }
        DropdownMenu(
            expanded = avatarExpanded,
            onDismissRequest = { avatarExpanded = false }
        ) {
            val displayName = if (userProfile.email != null) {
                "${userProfile.name} (${userProfile.email})"
            } else {
                userProfile.name
            }
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.connected_as, displayName)) },
                onClick = { avatarExpanded = false },
                enabled = false // Info only
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.sign_out)) },
                onClick = {
                    signOut()
                    avatarExpanded = false
                }
            )
        }
    }
}

