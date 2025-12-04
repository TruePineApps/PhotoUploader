package com.truepineapps.photouploader.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createHttpClientWithEngine(): HttpClient = HttpClient(Darwin) {
    engine {
        configureRequest {
            setAllowsCellularAccess(true)
            setAllowsConstrainedNetworkAccess(true)
            setAllowsExpensiveNetworkAccess(true)
        }
    }
}