package com.truepineapps.photouploader.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun createPlatformHttpClient(): HttpClient = HttpClient(CIO) {
    engine {
        requestTimeout = 60_000
    }
}