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

package com.truepineapps.photouploader.core.feature.legal.di

import com.truepineapps.photouploader.core.feature.legal.data.repository.LegalSettingsRepository
import com.truepineapps.photouploader.core.feature.legal.data.source.LegalLocalDataSource
import com.truepineapps.photouploader.core.feature.legal.data.source.LegalRemoteDataSource
import com.truepineapps.photouploader.core.feature.legal.domain.repository.LegalRepository
import com.truepineapps.photouploader.core.feature.legal.viewmodel.LegalViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val legalModule = module {
    // Data; LegalConfig is configured in [AppModule]
    single { LegalLocalDataSource(fileSystem = get(), log = get()) }
    single { LegalRemoteDataSource(httpClient = get(), legalConfig = get(), log = get()) }
    single<LegalRepository> {
        LegalSettingsRepository(
            settings = get(),
            remoteDataSource = get(),
            localDataSource = get(),
            log = get(),
        )
    }
    viewModel { LegalViewModel(legalRepository = get(), log = get()) }
}