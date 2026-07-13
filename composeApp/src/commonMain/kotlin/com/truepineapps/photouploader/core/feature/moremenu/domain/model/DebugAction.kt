package com.truepineapps.photouploader.core.feature.moremenu.domain.model

interface DebugAction {
    val name: String
    val action: () -> Unit
}
