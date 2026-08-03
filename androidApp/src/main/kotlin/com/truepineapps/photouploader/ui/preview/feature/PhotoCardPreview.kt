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

package com.truepineapps.photouploader.ui.preview.feature

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.feature.uploader.ui.components.PreviewPhotoCard
import com.truepineapps.photouploader.app.theme.AppTheme

@Preview(showBackground = true)
@Composable
fun PreviewPhotoCardDefault() {
    AppTheme {
        PreviewPhotoCard(
            KmpFile(uri = "".toUri()),
            false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPhotoCardError() {
    AppTheme {
        PreviewPhotoCard(
            KmpFile(uri = "".toUri()),
            true
        )
    }
}