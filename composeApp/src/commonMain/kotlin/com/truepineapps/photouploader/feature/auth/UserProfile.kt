package com.truepineapps.photouploader.feature.auth

data class UserProfile(
    val name: String,
    val email: String?,
    val avatarUrl: String?,
    val accessToken: String
)
