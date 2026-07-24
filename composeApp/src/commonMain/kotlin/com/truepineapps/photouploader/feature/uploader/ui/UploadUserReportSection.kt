package com.truepineapps.photouploader.feature.uploader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import coil3.compose.AsyncImage
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.foundation.auth.domain.model.UserProfile
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.appicon
import com.truepineapps.photouploader.resources.avatar
import com.truepineapps.photouploader.resources.no_longer_signed_in
import com.truepineapps.photouploader.resources.uploading_to
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun UploadUserReportSection(
    userProfile: UserProfile?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.uploading_to),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = Dimensions.padding_small)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (userProfile == null) {
                Image(
                    bitmap = imageResource(Res.drawable.appicon),
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.medium_icon_size),
                )
                Spacer(modifier = Modifier.width(Dimensions.padding_medium))
                Text(
                    text = stringResource(Res.string.no_longer_signed_in),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                AsyncImage(
                    model = userProfile.avatarUrl,
                    contentDescription = stringResource(Res.string.avatar),
                    modifier = Modifier
                        .size(Dimensions.medium_icon_size)
                        .clip(CircleShape)
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
    }
}
