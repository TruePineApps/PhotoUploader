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

interface PlatformFileSystem {
    fun list(file: KmpFile, context: PlatformContext): List<KmpFile>
    fun isDir(file: KmpFile, context: PlatformContext): Boolean
    fun isDirectory(file: KmpFile, context: PlatformContext): Boolean
    fun getDisplayName(file: KmpFile, context: PlatformContext): String
    fun getPath(file: KmpFile, context: PlatformContext): String?
    fun getName(file: KmpFile, context: PlatformContext): String?
    fun source(file: KmpFile, context: PlatformContext): Source
    fun sink(file: KmpFile, context: PlatformContext): Sink

    fun writeText(fileName: String, text: String, context: PlatformContext) {
        val homeDir = getApplicationHomeDirectory(context)
        "$homeDir/$fileName".toKmpFile().writeText(context, text)
    }

    fun readText(fileName: String, context: PlatformContext): String {
        val homeDir = getApplicationHomeDirectory(context)
        return "$homeDir/$fileName".toKmpFile().readText(context)
    }
}
