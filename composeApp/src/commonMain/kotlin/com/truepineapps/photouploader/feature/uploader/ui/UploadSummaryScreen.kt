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
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import com.truepineapps.photouploader.app.theme.AppTheme
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.foundation.auth.domain.model.UserProfile
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.albums_to_create
import com.truepineapps.photouploader.resources.avatar
import com.truepineapps.photouploader.resources.cancel
import com.truepineapps.photouploader.resources.photos_to_upload
import com.truepineapps.photouploader.resources.proceed
import com.truepineapps.photouploader.resources.ready_to_upload
import com.truepineapps.photouploader.resources.summary_care_message
import com.truepineapps.photouploader.resources.uploading_to
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhotoUploaderSummaryScreen(
    userProfile: UserProfile,
    totalAlbums: Int,
    totalPhotos: Int,
    log: Logger,
    onCancel: () -> Unit,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier
) {
    log.d("Show pre-upload summary screen: Albums = $totalAlbums Photos = $totalPhotos")
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .padding(Dimensions.padding_medium)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(top = Dimensions.padding_large)) {
                Text(
                    text = stringResource(Res.string.ready_to_upload),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = Dimensions.padding_large)
                )

                Text(
                    text = stringResource(Res.string.uploading_to),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = Dimensions.padding_small)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = userProfile.avatarUrl,
                        contentDescription = stringResource(Res.string.avatar),
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

                Text(
                    text = stringResource(Res.string.albums_to_create, totalAlbums),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(Res.string.photos_to_upload, totalPhotos),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(Dimensions.padding_large))

                Text(
                    text = stringResource(Res.string.summary_care_message),
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
                    Text(stringResource(Res.string.cancel))
                }
                Button(
                    onClick = onProceed,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.proceed))
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
            log = Logger,
            onCancel = { },
            onProceed = { },
        )
    }
}
