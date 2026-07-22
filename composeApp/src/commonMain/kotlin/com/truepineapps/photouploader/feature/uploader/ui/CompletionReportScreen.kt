package com.truepineapps.photouploader.feature.uploader.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UploadCompletionStatus
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UploadReport
import com.truepineapps.photouploader.foundation.auth.domain.model.UserProfile
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.appicon
import org.jetbrains.compose.resources.imageResource

@Composable
fun CompletionReportScreen(
    userProfile: UserProfile?,
    report: UploadReport,
    log: Logger,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    log.d("Report status: ${report.status}")
    log.d("Report album counts: uploaded ${report.albumsCreated} skipped ${report.albumsSkipped} failed ${report.albumsFailed}")
    log.d("Report photo counts: uploaded ${report.photosUploaded} skipped ${report.photosSkipped} failed ${report.photosFailed}")
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .padding(Dimensions.padding_medium)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Text(
                    text = when (report.status) {
                        UploadCompletionStatus.SUCCESS -> "✅ Upload Complete"
                        UploadCompletionStatus.CANCELLED -> "⚠️ Upload Cancelled"
                        UploadCompletionStatus.ERRORS -> "⚠️ Upload Completed with Errors"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = Dimensions.padding_large)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.padding_medium)
                ) {
                    Text(
                        text = "Albums created: ${report.albumsCreated}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (report.albumsSkipped > 0) Text(
                        text = "Albums skipped: ${report.albumsSkipped}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (report.albumsFailed > 0) Text(
                        text = "Albums failed: ${report.albumsFailed}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.padding_medium)
                ) {
                    Text(
                        text = "Photos uploaded: ${report.photosUploaded}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (report.photosSkipped > 0) Text(
                        text = "Photos skipped: ${report.photosSkipped}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (report.photosFailed > 0) Text(
                        text = "Photos failed: ${report.photosFailed}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.padding_large))

                Text(
                    text = "Uploading to:",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // UserProfile may become null because e.g. an HTTP 401 error signs out 
                    // automatically
                    if (userProfile == null) {
                        Image(
                            bitmap = imageResource(Res.drawable.appicon),
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.medium_icon_size),
                        )
                        Spacer(modifier = Modifier.width(Dimensions.padding_medium))
                        Text("No longer signed in", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        AsyncImage(
                            model = userProfile.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(Dimensions.medium_icon_size).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(Dimensions.padding_medium))
                        Column {
                            Text(
                                text = userProfile.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            userProfile.email?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.padding_large))

                if (report.status == UploadCompletionStatus.CANCELLED) {
                    Text(
                        text = "The operation was cancelled before completion. Already uploaded content remains in Google Photos.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = Dimensions.padding_medium)
                    )
                }

                Text(
                    text = "The App reports what was sent and acknowledged — what Google Photos ultimately stores is beyond the App's visibility. Therefore, please verify your uploads in Google Photos before deleting any local copies.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Dimensions.padding_large))

                if (report.status == UploadCompletionStatus.ERRORS) {
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Hide error details" else "Show error details (${report.errors.size})")
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        Column {
                            report.errors.forEach { error ->
                                Text(
                                    "${error.name}: ${error.reason}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth().padding(top = Dimensions.padding_medium)
            ) {
                Text("Close")
            }
        }
    }
}
