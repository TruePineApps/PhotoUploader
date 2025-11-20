package com.truepine.photouploader.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val client = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = false
        })
    }
}

//suspend fun uploadPhoto(photo: PlatformFile, albumId: String, accessToken: String) {
//    val response: HttpResponse = client.post("https://photoslibrary.googleapis.com/v1/uploads") {
//        headers {
//            append(
//                HttpHeaders.Authorization,
//                "Bearer $accessToken"
//            )
//            append (HttpHeaders.ContentType, "application/octet-stream")
//        }
////        body = photo.readBytes()
//    }
//    // Handle response
//}