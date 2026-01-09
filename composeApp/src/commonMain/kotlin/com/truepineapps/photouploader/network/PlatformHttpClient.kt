package com.truepineapps.photouploader.network

import io.ktor.client.HttpClient

/**
 * Leave the common configuration to the caller.
 * @return a Ktor HttpClient with the platform-specific engine.
 */
expect fun createPlatformHttpClient(): HttpClient
