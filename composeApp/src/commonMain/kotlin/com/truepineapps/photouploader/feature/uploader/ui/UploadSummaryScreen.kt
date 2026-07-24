package com.truepineapps.photouploader.feature.uploader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.app.theme.AppTheme
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.core.util.normalizeWhitespace
import com.truepineapps.photouploader.foundation.auth.domain.model.UserProfile
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.albums_to_create
import com.truepineapps.photouploader.resources.cancel
import com.truepineapps.photouploader.resources.photos_to_upload
import com.truepineapps.photouploader.resources.proceed
import com.truepineapps.photouploader.resources.ready_to_upload
import com.truepineapps.photouploader.resources.summary_care_message
import org.jetbrains.compose.resources.stringResource

// Note that the userProfile may be null when the user canceled the sign-in process.
@Composable
fun UploadSummaryScreen(
    userProfile: UserProfile?,
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

                UploadUserReportSection(userProfile = userProfile)

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
                    text = stringResource(Res.string.summary_care_message).normalizeWhitespace(),
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
                    modifier = Modifier.weight(1f),
                    enabled = userProfile != null
                ) {
                    Text(stringResource(Res.string.proceed))
                }
            }
        }
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun PreviewUploadSummaryScreen() {
    AppTheme {
        UploadSummaryScreen(
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
