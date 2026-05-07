package com.truepineapps.photouploader.app.di

import org.koin.core.KoinApplication

/* Called from Koin.swift */

private var koinApp: KoinApplication? = null

@Suppress("unused")
fun initKoinIos(): KoinApplication {
    koinApp = initKoin()
    return koinApp!!
}

@Suppress("unused")
fun stopKoinIos() {
    koinApp?.koin?.let { exitKoin(it) }
    koinApp = null
}
