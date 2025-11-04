package com.truepine.photouploader

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform