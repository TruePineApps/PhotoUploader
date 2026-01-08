package com.truepineapps.photouploader.ui.screen.uploader

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
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.draw.clip
import coil3.compose.AsyncImage
import com.truepineapps.photouploader.auth.UserProfile
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.about
import com.truepineapps.photouploader.resources.appicon
import com.truepineapps.photouploader.resources.back_button
import com.truepineapps.photouploader.resources.cancel
import com.truepineapps.photouploader.resources.choose_folder
import com.truepineapps.photouploader.resources.close_button
import com.truepineapps.photouploader.resources.connected_as
import com.truepineapps.photouploader.resources.licenses
import com.truepineapps.photouploader.resources.menu
import com.truepineapps.photouploader.resources.preferences
import com.truepineapps.photouploader.resources.sign_out
import com.truepineapps.photouploader.resources.upload_photos
import com.truepineapps.photouploader.resources.user_avatar_content_desc
import com.truepineapps.photouploader.resources.waiting_for_browser_sign_in
import com.truepineapps.photouploader.ui.Dimensions
import com.truepineapps.photouploader.ui.components.ThemedIconButton
import com.truepineapps.photouploader.ui.navigation.MenuNavigator
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.stringResource

/**
 * App bar to display from left to right:
 * # NavigationIcon:
 *  - On PhotoUploader Screen: Sign in status:
 *   . not signed in: Launcher icon
 *   . signing in: Progress indicator
 *   . signed in: Avatar with dropdown menu
 *  - On Album Screen: Back navigation
 * # Path
 * # Custom actions for the screen
 * # Upload button
 * # Upload status:
 *   . not uploading: Upload button
 *   . uploading: Progress indicator
 * # Menu button
 *   . Preferences
 *   . About
 *   . Licenses
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoUploaderAppBar(
    menuNavigator: MenuNavigator,
    scrollBehavior: TopAppBarScrollBehavior?,
    title: String,
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
    val canChooseDirectory = uiState.idle()
    // Only allow uploading if we have albums, path is set, and not currently busy
    val canUploadPhotos = uiState.albums.isNotEmpty() && uiState.path.isNotBlank() && uiState.idle()
    val userProfile = uiState.userProfile

    CenterAlignedTopAppBar(
        title = { Text(text = title, color = MaterialTheme.colorScheme.primary) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (isSigningIn) {
                SignInProgressMenu { viewModel.cancelSignIn() }
            } else if (closeDialog != null) {
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
            } else if (userProfile != null) {
                AvatarMenu(userProfile) { viewModel.signOut() }
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
                onClick = {
                    // The ViewModel's uploadPhotos launches a coroutine that just needs to be
                    // triggered here. The returned Job can be ignored here or handled if needed.
                    viewModel.uploadPhotos()
                },
            )
            MoreMenu(menuNavigator)
        },
        colors = colors
    )
}


@Composable
private fun SignInProgressMenu(cancelSignIn: () -> Unit) {
    // The expanded state of the sign in in progress dropdown menu.
    var signInMenuExpanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { signInMenuExpanded = true }) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimensions.icon_size),
                strokeWidth = Dimensions.stroke_size,
                color = MaterialTheme.colorScheme.primary
            )
        }
        DropdownMenu(
            expanded = signInMenuExpanded,
            onDismissRequest = { signInMenuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.waiting_for_browser_sign_in)) },
                onClick = { signInMenuExpanded = false },
                enabled = false
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.cancel)) },
                onClick = {
                    signInMenuExpanded = false
                    cancelSignIn()
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
    signOut: () -> Unit
) {
    // The expanded state of the avatar dropdown menu.
    var avatarExpanded by remember { mutableStateOf(false) }

    Box {
        if (userProfile.avatarUrl != null) {
            AsyncImage(
                model = userProfile.avatarUrl,
                contentDescription = stringResource(Res.string.user_avatar_content_desc),
                modifier = Modifier
                    .padding(Dimensions.padding_small)
                    .size(Dimensions.medium_icon_size)
                    .clip(CircleShape)
                    .clickable { avatarExpanded = true }
            )
        } else {
            // Fallback icon if no avatar URL
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = stringResource(Res.string.user_avatar_content_desc),
                modifier = Modifier
                    .padding(Dimensions.padding_small)
                    .size(Dimensions.medium_icon_size)
                    .clip(CircleShape)
                    .clickable { avatarExpanded = true }
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

@Composable
private fun MoreMenu(menuNavigator: MenuNavigator) {
    // The expanded state of the dropdown menu.
    var expanded by remember { mutableStateOf(false) }

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
}
