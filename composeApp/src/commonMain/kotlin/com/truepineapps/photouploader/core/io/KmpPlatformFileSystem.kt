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
import com.mohamedrejeb.calf.io.getName
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.io.isDirectory
import okio.Sink
import okio.Source

class KmpPlatformFileSystem : PlatformFileSystem {
    override fun list(file: KmpFile, context: PlatformContext): List<KmpFile> =
        file.list(context)

    override fun isDir(file: KmpFile, context: PlatformContext): Boolean =
        file.isDir(context)

    override fun isDirectory(file: KmpFile, context: PlatformContext): Boolean =
        file.isDirectory(context)

    override fun getDisplayName(file: KmpFile, context: PlatformContext): String =
        file.getDisplayName(context)

    override fun getPath(file: KmpFile, context: PlatformContext): String? =
        file.getPath(context)

    override fun getName(file: KmpFile, context: PlatformContext): String? =
        file.getName(context)

    override fun source(file: KmpFile, context: PlatformContext): Source =
        file.source(context)

    override fun sink(file: KmpFile, context: PlatformContext): Sink =
        file.sink(context)
}
