package com.truepineapps.photouploader.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            encodeDefaults = true
            isLenient = true
        }
    }
    single {
        HttpClient {
            install(ContentNegotiation) { json(get()) }
        }
    }
}
