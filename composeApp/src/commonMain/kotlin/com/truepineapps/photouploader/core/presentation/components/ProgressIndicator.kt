/*
 * Copyright (c) 2025. Marcel Van Heerwaarden
 * @Author Marcel van Heerwaarden
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.truepineapps.photouploader.core.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.busy
import com.truepineapps.photouploader.resources.favicon
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProgressIndicator(modifier: Modifier = Modifier, action: String? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box {
            Image(
                painter = painterResource(resource = Res.drawable.favicon),
                contentDescription = stringResource(Res.string.busy),
                modifier = Modifier.size(Dimensions.progress_indicator_size)
            )
            CircularProgressIndicator(
                modifier = Modifier.size(Dimensions.progress_indicator_size)
            )
        }
        if (action != null) {
            Text(text = action)
        }
    }
}
