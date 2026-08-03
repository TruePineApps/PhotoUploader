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

package com.truepineapps.photouploader.core.util

import com.truepineapps.photouploader.resources.Res

/**
 * Asynchronously loads the content of a resource file located in the "files/" directory.
 *
 * @param fileName The name of the file to be loaded.
 * @return A [Result] containing the trimmed file content as a string on success,
 * or the caught exception on failure.
 */
suspend fun loadResourceFile(fileName: String): Result<String> = runCatching {
    Res.readBytes("files/$fileName").decodeToString().trim()
}

