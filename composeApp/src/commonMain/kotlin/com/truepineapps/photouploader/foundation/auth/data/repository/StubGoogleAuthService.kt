/*
 * Copyright (c) 2026 True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.truepineapps.photouploader.foundation.auth.data.repository

import com.truepineapps.photouploader.foundation.auth.domain.model.UserProfile
import com.truepineapps.photouploader.foundation.auth.domain.repository.GoogleAuthService
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class StubGoogleAuthService : GoogleAuthService {
    override suspend fun signIn(): UserProfile {
        //delay(1000) // Simulate network delay
        return UserProfile(
            name = "Test User",
            email = "test@example.com",
            avatarUrl = null,
            accessToken = "fake_access_token_12345"
        )
    }

    override suspend fun signOut() {
        delay(500.milliseconds)
    }

    override suspend fun restoreSignIn(): UserProfile? {
        return null
    }

    override fun shutdown() {
        // No-op for stub
    }
}