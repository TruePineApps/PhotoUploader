package com.truepineapps.photouploader.ui.screen.uploader.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import com.truepineapps.photouploader.ui.screen.uploader.uistate.UploadStatus
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.arrow_upload_progress
import com.truepineapps.photouploader.resources.arrow_upload_ready
import com.truepineapps.photouploader.resources.file_upload_off
import com.truepineapps.photouploader.ui.Dimensions
import com.truepineapps.photouploader.ui.theme.StatusPalette
import kotlinx.datetime.DateTimeUnit.Companion.SECOND
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun UploadStatusIndicator(
    uploadStatus: UploadStatus,
    isAlbum: Boolean,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val userDisabled = !isEnabled && uploadStatus != UploadStatus.Success

    val iconColor = if (userDisabled) {
        StatusPalette.Disabled
    } else {
        uploadStatus.getColor()
    }

    val statusIcon = getStatusIcon(uploadStatus, isAlbum, userDisabled)

    if (statusIcon != null) {
        val rotation by if (uploadStatus.isUploading) {
            val animationDuration = 1 * SECOND.duration.inWholeMilliseconds.toInt()
            val infiniteTransition = rememberInfiniteTransition()
            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = animationDuration, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            remember { mutableStateOf(0f) }
        }

        val imageModifier = modifier.size(Dimensions.icon_size).rotate(rotation)

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Top) {
            if (statusIcon is StatusIcon.Vector) {
                Icon(
                    imageVector = statusIcon.imageVector,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = imageModifier
                )
            } else if (statusIcon is StatusIcon.Drawable) {
                Icon(
                    painter = painterResource(statusIcon.resource),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = imageModifier
                )
            }
            Text(
                text = uploadStatus.getDescription(),
                color = uploadStatus.getColor(),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun UploadErrorText(
    uploadStatus: UploadStatus,
    modifier: Modifier = Modifier
) {
    val errorMessage = when (uploadStatus) {
        is UploadStatus.Error -> uploadStatus.message
        is UploadStatus.UploadingError -> uploadStatus.message
        else -> null
    }

    if (errorMessage != null) {
        Text(
            text = errorMessage.asString(),
            color = StatusPalette.Error,
            style = MaterialTheme.typography.bodySmall,
            modifier = modifier
        )
    }
}

private sealed class StatusIcon {
    data class Vector(val imageVector: ImageVector) : StatusIcon()
    data class Drawable(val resource: DrawableResource) : StatusIcon()
}

@OptIn(ExperimentalResourceApi::class)
private fun getStatusIcon(
    uploadStatus: UploadStatus,
    isAlbum: Boolean,
    isDisabled: Boolean,
): StatusIcon? {
    if (isDisabled) {
        return StatusIcon.Drawable(Res.drawable.file_upload_off)
    }

    return when (uploadStatus) {
        UploadStatus.None -> {
            if (isAlbum) StatusIcon.Vector(Icons.Filled.DriveFolderUpload)
            else StatusIcon.Vector(Icons.Filled.UploadFile)
        }
        UploadStatus.Waiting -> StatusIcon.Vector(Icons.Filled.ArrowCircleUp)
        UploadStatus.Uploading, is UploadStatus.UploadingError -> StatusIcon.Drawable(Res.drawable.arrow_upload_progress)
        UploadStatus.Success -> StatusIcon.Drawable(Res.drawable.arrow_upload_ready)
        UploadStatus.Cancelled -> StatusIcon.Drawable(Res.drawable.arrow_upload_ready)
        is UploadStatus.Error -> StatusIcon.Vector(Icons.Filled.Error)
    }
}
