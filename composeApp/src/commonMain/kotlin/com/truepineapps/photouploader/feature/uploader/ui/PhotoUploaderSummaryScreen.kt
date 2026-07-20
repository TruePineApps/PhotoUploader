package com.truepineapps.photouploader.feature.uploader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.truepineapps.photouploader.app.theme.AppTheme
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.foundation.auth.domain.model.UserProfile

@Composable
fun PhotoUploaderSummaryScreen(
    userProfile: UserProfile,
    totalAlbums: Int,
    totalPhotos: Int,
    onCancel: () -> Unit,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .padding(Dimensions.padding_medium)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(top = Dimensions.padding_large)) {
                Text(
                    text = "📤 Ready to Upload",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = Dimensions.padding_large)
                )

                Text(
                    text = "Uploading to:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = Dimensions.padding_small)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = userProfile.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.padding_medium))
                    Column {
                        Text(text = userProfile.name, style = MaterialTheme.typography.titleMedium)
                        userProfile.email?.let {
                            Text(text = it, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.padding_large))

                Text(text = "Albums to create: $totalAlbums", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Photos to upload: $totalPhotos", style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(Dimensions.padding_large))

                Text(
                    text = "This operation cannot be undone from within the App. Review and removal of uploaded content is available directly in Google Photos. Please verify your uploads in Google Photos before deleting any local copies.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimensions.padding_medium),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.padding_medium)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onProceed,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Proceed")
                }
            }
        }
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun PreviewPhotoUploaderSummaryScreen() {
    AppTheme {
        PhotoUploaderSummaryScreen(
            userProfile = UserProfile(
                name = "marcel",
                email = "marcel@google.com",
                avatarUrl = null,
                accessToken = "1234567890"
            ),
            totalAlbums = 3,
            totalPhotos = 47,
            onCancel = {  },
            onProceed = {  },
        )
    }
}
