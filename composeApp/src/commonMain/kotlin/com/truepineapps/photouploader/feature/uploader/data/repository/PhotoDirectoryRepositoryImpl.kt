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

package com.truepineapps.photouploader.feature.uploader.data.repository

import co.touchlab.kermit.Logger
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.core.domain.state.DataLoadingState
import com.truepineapps.photouploader.core.io.PlatformFileSystem
import com.truepineapps.photouploader.core.util.FileUtils
import com.truepineapps.photouploader.feature.uploader.domain.model.Album
import com.truepineapps.photouploader.feature.uploader.domain.model.Photo
import com.truepineapps.photouploader.feature.uploader.domain.repository.PhotoDirectoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import okio.Path.Companion.toPath

class PhotoDirectoryRepositoryImpl(
    private val platformFileSystem: PlatformFileSystem,
    private val log: Logger
) : PhotoDirectoryRepository {

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    override val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    var platformContext: PlatformContext? = null
    private var currentKmpFile: KmpFile? = null

    // Flag to control when a disk scan is actually necessary
    private var needsRefresh = true

    override fun setPath(kmpFile: KmpFile, platformContext: PlatformContext) {
        currentKmpFile = kmpFile
        this.platformContext = platformContext
        // A new path means we definitely need to scan
        needsRefresh = true
    }

    // Allow updating albums from outside (e.g. toggling check boxes in ViewModel)
    override fun updateAlbums(newAlbums: List<Album>) {
        log.d("Repository updating albums, new list size is ${newAlbums.size}")
        _albums.value = newAlbums
        needsRefresh = false
    }

    override val loadingState: Flow<DataLoadingState> = flow {
        // If we have data and don't need a forced refresh, emit Success immediately.
        // This prevents re-scanning (and resetting user selections) when navigating back.
        if (!needsRefresh && _albums.value.isNotEmpty()) {
            emit(DataLoadingState.Success)
            return@flow
        }

        try {
            emit(DataLoadingState.Loading)
            if (currentKmpFile != null) {
                val result = scanDirectoryInternal(currentKmpFile)
                _albums.value = result
                needsRefresh = false
            } else {
                _albums.value = emptyList()
            }
            emit(DataLoadingState.Success)
        } catch (e: Exception) {
            log.e(e) { "Exception while loading photos" }
            emit(DataLoadingState.Error(e))
        }
    }

    override fun prepareReload() {
        // Force a scan on the next collection
        needsRefresh = true
    }

    /**
     * Scans the directory structure starting from [rootDir] and returns a list of [Album]s.
     */
    private fun scanDirectoryInternal(rootDir: KmpFile?): List<Album> {

        requireNotNull(rootDir)

        // Get a safe non-mutable context variable
        val currentContext = platformContext ?: throw IllegalStateException("current platformContext is null")
        log.d { "Scanning directory: ${platformFileSystem.getDisplayName(rootDir, currentContext)}" }

        // The root itself might contain photos
        val albums = mutableListOf<Album>()
        processDirectory(rootDir, null, "", albums, currentContext)

        return albums
    }

    private fun processDirectory(
        currentDir: KmpFile,
        parentTitle: String?,
        groupName: String,
        albums: MutableList<Album>,
        currentContext: PlatformContext,
    ) {
        // Construct the album name based on the hierarchy
        val albumName: String
        val titlePrefix: String

        val name = platformFileSystem.getDisplayName(currentDir, currentContext)
        if (parentTitle == null) {
            // Root folder
            albumName = name
            titlePrefix = ""
        } else {
            albumName = "$parentTitle$name"
            titlePrefix = "$albumName - "
        }

        val entries = platformFileSystem.list(currentDir, currentContext)
        log.d { "Processing directory: $name with ${entries.size} entries" }

        // 1. Identify photos in the current directory
        val photoFiles = entries
            .filter {
                !platformFileSystem.isDir(it, currentContext) &&
                FileUtils.isPhotoFile(platformFileSystem.getName(it, currentContext))
            }
            .sortedBy { platformFileSystem.getName(it, currentContext) }
            .map { path ->
                Photo(
                    kmpFile = path,
                    path = (platformFileSystem.getPath(path, currentContext) ?: "").toPath(),
                    name = platformFileSystem.getName(path, currentContext) ?: "",
                )
            }

        // 2. Identify Subdirectories
        val subDirectories = entries
            .filter { platformFileSystem.isDirectory(it, currentContext) }
            .sortedBy { platformFileSystem.getPath(it, currentContext) }

        // 3. Create an Album if there are photos
        if (photoFiles.isNotEmpty()) {
            val albumPath = platformFileSystem.getPath(currentDir, currentContext) ?: ""
            albums.add(
                Album(
                    // Replace slashes for navigation
                    id = albumPath.replace("/", "|"),
                    kmpFile = currentDir,
                    path = albumPath.toPath(),
                    name = albumName,
                    group = groupName,
                    photos = photoFiles,
                )
            )
        }

        // 4. Recurse
        for (subDir in subDirectories) {
            // The name of the direct child dirs of rootDir are the sticky group headers in the UI
            val group = if (parentTitle == null) platformFileSystem.getDisplayName(subDir, currentContext) else groupName
            processDirectory(
                currentDir = subDir,
                parentTitle = if (parentTitle == null) "" else titlePrefix,
                groupName = group,
                albums = albums,
                currentContext = currentContext,
            )
        }
    }
}