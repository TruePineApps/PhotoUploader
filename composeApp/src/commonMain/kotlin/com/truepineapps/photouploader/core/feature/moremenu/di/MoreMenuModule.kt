package com.truepineapps.photouploader.core.feature.moremenu.di

import com.russhwolf.settings.Settings
import com.truepineapps.photouploader.core.feature.moremenu.domain.model.DebugAction
import com.truepineapps.photouploader.core.feature.moremenu.domain.repository.DebugActionRepository
import com.truepineapps.photouploader.core.feature.moremenu.repository.DebugActionRepositoryImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module


val moreMenuModule = module {
    // Core actions; don't bother with translations for debug actions
    single<List<DebugAction>>(named("core_actions")) {
        listOf(
            object : DebugAction {
                override val name = "Clear Settings"
                override val action = { get<Settings>().clear() }
            }
        )
    }

    single<DebugActionRepository> {
        DebugActionRepositoryImpl(
            // Use getAll to find all lists named "_actions"
            allActions = getAll<List<DebugAction>>().flatten()
        )
    }
}