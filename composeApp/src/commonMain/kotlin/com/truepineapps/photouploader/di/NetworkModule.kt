package com.truepineapps.photouploader.di

import com.truepineapps.photouploader.network.createPlatformHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import kotlin.time.DurationUnit
import kotlin.time.toDuration

val networkModule = module {
    // A shared Json instance
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            encodeDefaults = true
            isLenient = true
        }
    }

    // A single, fully configured HttpClient for the entire app
    single<HttpClient> {
        // Start with the platform-specific engine (CIO, Darwin, etc.)
        createPlatformHttpClient().config {
            // Content Negotiation with the shared Json instance
            install(ContentNegotiation) {
                json(get())
            }

            // WebSockets configuration
            install(WebSockets) {
                pingInterval = 20.toDuration(DurationUnit.SECONDS)
            }

            // Timeout configuration
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
            }

            // Logging configuration using Kermit
            install(Logging) {
                logger = KtorKermitLogger(get())
                // Set to ALL to see headers and bodies, or INFO for less noise
                level = LogLevel.INFO
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
        }
    }
}
