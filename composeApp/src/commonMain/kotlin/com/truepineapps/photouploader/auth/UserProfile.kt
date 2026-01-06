package com.truepineapps.photouploader.auth

data class UserProfile(
    val name: String,
    val email: String?,
    val avatarUrl: String?,
    val accessToken: String
)
