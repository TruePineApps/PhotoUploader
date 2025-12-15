package com.truepineapps.photouploader.di

import com.truepineapps.photouploader.auth.GoogleAuthService
import org.koin.core.module.Module
import com.truepineapps.photouploader.PlatformInfo
import com.truepineapps.photouploader.AppInfo
import okio.FileSystem

/** This module must define the [PlatformInfo], [AppInfo], [GoogleAuthService] and [FileSystem] */
expect fun platformModule(): Module
