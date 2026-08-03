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

package com.truepineapps.photouploader.feature.uploader.domain.model

import com.mohamedrejeb.calf.io.KmpFile
import okio.Path

data class Photo(
    val kmpFile: KmpFile,
    val path: Path,
    /** File name */
    val name: String,
) {
    /** File name without extension */
    fun getDisplayName(): String {
        // Safe filename parsing
        val dotIndex = name.lastIndexOf('.')
        return if (dotIndex > 0) name.take(dotIndex) else name
    }
}
