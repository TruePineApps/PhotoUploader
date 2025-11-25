package com.truepine.photouploader.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

actual fun createHttpClientWithEngine(): HttpClient = HttpClient(Android) {
    engine {
        connectTimeout = 60_000
    }
}
