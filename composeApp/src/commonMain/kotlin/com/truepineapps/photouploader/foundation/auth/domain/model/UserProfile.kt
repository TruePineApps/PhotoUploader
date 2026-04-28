package com.truepineapps.photouploader.foundation.auth.domain.model

data class UserProfile(
    val name: String,
    val email: String?,
    val avatarUrl: String?,
    val accessToken: String
)