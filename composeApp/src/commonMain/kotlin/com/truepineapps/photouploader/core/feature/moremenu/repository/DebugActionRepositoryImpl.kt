package com.truepineapps.photouploader.core.feature.moremenu.repository

import com.truepineapps.photouploader.core.feature.moremenu.domain.model.DebugAction
import com.truepineapps.photouploader.core.feature.moremenu.domain.repository.DebugActionRepository

class DebugActionRepositoryImpl(
    private val allActions: List<DebugAction>
) : DebugActionRepository {
    override fun getActions(): List<DebugAction> = allActions
}
