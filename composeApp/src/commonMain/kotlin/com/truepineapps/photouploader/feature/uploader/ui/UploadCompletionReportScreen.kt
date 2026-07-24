package com.truepineapps.photouploader.feature.uploader.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.core.util.normalizeWhitespace
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UploadCompletionStatus
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UploadReport
import com.truepineapps.photouploader.foundation.auth.domain.model.UserProfile
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.albums_created
import com.truepineapps.photouploader.resources.albums_failed
import com.truepineapps.photouploader.resources.albums_skipped
import com.truepineapps.photouploader.resources.close_button
import com.truepineapps.photouploader.resources.hide_error_details
import com.truepineapps.photouploader.resources.photos_failed
import com.truepineapps.photouploader.resources.photos_skipped
import com.truepineapps.photouploader.resources.photos_uploaded
import com.truepineapps.photouploader.resources.report_cancelled_message
import com.truepineapps.photouploader.resources.report_care_message
import com.truepineapps.photouploader.resources.show_error_details
import com.truepineapps.photouploader.resources.upload_cancelled
import com.truepineapps.photouploader.resources.upload_complete
import com.truepineapps.photouploader.resources.upload_with_errors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// Note that userProfile may become null because e.g., an HTTP 401 error signs out
// automatically
@Composable
fun UploadCompletionReportScreen(
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
                    text = stringResource(
                        when (report.status) {
                            UploadCompletionStatus.SUCCESS -> Res.string.upload_complete
                            UploadCompletionStatus.CANCELLED -> Res.string.upload_cancelled
                            UploadCompletionStatus.ERRORS -> Res.string.upload_with_errors
                        }
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = Dimensions.padding_large)
                )

                val showSkipped = report.albumsSkipped > 0 || report.photosSkipped > 0
                val showFailed = report.albumsFailed > 0 || report.photosFailed > 0

                Column(verticalArrangement = Arrangement.spacedBy(Dimensions.padding_small)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ReportCounter(
                            textId = Res.string.albums_created,
                            count = report.albumsCreated,
                            isRequired = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (showSkipped) {
                            ReportCounter(
                                textId = Res.string.albums_skipped,
                                count = report.albumsSkipped,
                                isRequired = false,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (showFailed) {
                            ReportCounter(
                                textId = Res.string.albums_failed,
                                count = report.albumsFailed,
                                isRequired = false,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ReportCounter(
                            textId = Res.string.photos_uploaded,
                            count = report.photosUploaded,
                            isRequired = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (showSkipped) {
                            ReportCounter(
                                textId = Res.string.photos_skipped,
                                count = report.photosSkipped,
                                isRequired = false,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (showFailed) {
                            ReportCounter(
                                textId = Res.string.photos_failed,
                                count = report.photosFailed,
                                isRequired = false,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.padding_large))

                UploadUserReportSection(userProfile = userProfile)

                Spacer(modifier = Modifier.height(Dimensions.padding_large))

                if (report.status == UploadCompletionStatus.CANCELLED) {
                    Text(
                        text = stringResource(Res.string.report_cancelled_message).normalizeWhitespace(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = Dimensions.padding_medium)
                    )
                }

                Text(
                    text = stringResource(Res.string.report_care_message).normalizeWhitespace(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Dimensions.padding_large))

                if (report.status == UploadCompletionStatus.ERRORS) {
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(
                            if (expanded) stringResource(Res.string.hide_error_details) else stringResource(
                                Res.string.show_error_details,
                                report.errors.size
                            )
                        )
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
                Text(stringResource(Res.string.close_button))
            }
        }
    }
}

@Composable
private fun ReportCounter(
    textId: StringResource,
    count: Int,
    modifier: Modifier = Modifier,
    isRequired: Boolean
) {
    val text = if (isRequired || count > 0) stringResource(textId, count) else ""
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge, modifier = modifier
    )
}

