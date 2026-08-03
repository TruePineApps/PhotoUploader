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

package com.truepineapps.photouploader.core.presentation.component.platformpicker

import androidx.compose.runtime.Composable
import com.mohamedrejeb.calf.io.KmpFile

interface PlatformPicker {
    @Composable
    fun PlatformFilePicker(
        show: Boolean,
        fileExtensions: List<String>,
        onFileSelected: (String?) -> Unit,
    )

    @Composable
    fun PlatformMultipleFilePicker(
        show: Boolean,
        fileExtensions: List<String>,
        onFilesSelected: (List<String>?) -> Unit,
    )

    @Composable
    fun PlatformDirectoryPicker(show: Boolean, onDirectorySelected: (KmpFile?) -> Unit)
}