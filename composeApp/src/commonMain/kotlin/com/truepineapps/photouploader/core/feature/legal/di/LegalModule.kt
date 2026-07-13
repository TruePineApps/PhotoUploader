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