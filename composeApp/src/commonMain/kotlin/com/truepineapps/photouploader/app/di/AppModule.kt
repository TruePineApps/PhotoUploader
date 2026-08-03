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

package com.truepineapps.photouploader.app.di

import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalConfig
import com.truepineapps.photouploader.core.io.KmpPlatformFileSystem
import com.truepineapps.photouploader.core.io.PlatformFileSystem
import com.truepineapps.photouploader.feature.uploader.data.repository.PhotoDirectoryRepositoryImpl
import com.truepineapps.photouploader.feature.uploader.data.repository.PhotoUploaderImpl
import com.truepineapps.photouploader.feature.uploader.domain.repository.PhotoDirectoryRepository
import com.truepineapps.photouploader.feature.uploader.domain.repository.PhotoUploader
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule: Module = module {
    singleOf(::PhotoDirectoryRepositoryImpl) { bind<PhotoDirectoryRepository>() }
    singleOf(::PhotoUploaderImpl) { bind<PhotoUploader>() }

    // core/io
    singleOf(::KmpPlatformFileSystem) { bind<PlatformFileSystem>() }

    // core/feature/legal
    single {
        LegalConfig(
            versionUrl = "https://truepineapps.com/photouploader/legal_version.txt",
            termsUrl = "https://truepineapps.com/photouploader/terms_of_service.txt",
            privacyPolicyUrl = "https://truepineapps.com/photouploader/privacy_policy.txt",
        )
    }

}
