package com.truepineapps.photouploader.di

import com.truepineapps.photouploader.AppInfo
import com.truepineapps.photouploader.PlatformInfo
import com.truepineapps.photouploader.auth.GoogleAuthService
import org.koin.core.module.Module

/** This module must define the Logger instance, [PlatformInfo], [AppInfo] and [GoogleAuthService] */
expect fun platformModule(): Module
