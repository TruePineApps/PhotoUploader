package com.truepineapps.photouploader.app.di

import com.truepineapps.photouploader.core.util.AppInfo
import com.truepineapps.photouploader.core.util.PlatformInfo
import com.truepineapps.photouploader.feature.auth.GoogleAuthService
import org.koin.core.module.Module

/** This module must define the Logger instance, [PlatformInfo], [AppInfo] and [GoogleAuthService] */
expect fun platformModule(): Module
