/*
 * Copyright (c) 2026 True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.truepineapps.photouploader.core.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.truepineapps.photouploader.app.theme.AppTheme
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.core.presentation.design.Dimensions.padding_medium
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.error
import com.truepineapps.photouploader.resources.retry_button
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorScreen(message: String, retryAction: () -> Unit, modifier: Modifier = Modifier) {
    // Positioning
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
        // Layout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(padding_medium),
        ) {
            Icon(
                imageVector = Icons.Filled.BrokenImage,
                contentDescription = stringResource(Res.string.error),
                modifier = Modifier.size(Dimensions.big_icon_size)
            )
            Text(text = message, modifier = Modifier.padding(padding_medium))
            Button(onClick = retryAction) {
                Text(stringResource(Res.string.retry_button))
            }
        }
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun PreviewErrorScreen() {
    AppTheme { ErrorScreen("Preview Error 1\nPreview Error 2", {}) }
}