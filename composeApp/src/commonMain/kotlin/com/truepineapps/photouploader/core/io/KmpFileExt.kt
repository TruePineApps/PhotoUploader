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

package com.truepineapps.photouploader.core.io

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import okio.Sink
import okio.Source

expect fun getApplicationHomeDirectory(context: PlatformContext): String
expect fun KmpFile.list(context: PlatformContext): List<KmpFile>
expect fun KmpFile.getAbsolutePath(context: PlatformContext): String?
expect fun KmpFile.isDir(context: PlatformContext): Boolean
expect fun KmpFile.getDisplayName(context: PlatformContext): String
expect fun KmpFile.source(context: PlatformContext): Source
expect fun KmpFile.sink(context: PlatformContext): Sink
expect fun KmpFile.writeText(context: PlatformContext, text: String)
expect fun KmpFile.readText(context: PlatformContext): String
expect fun String.toKmpFile(): KmpFile
