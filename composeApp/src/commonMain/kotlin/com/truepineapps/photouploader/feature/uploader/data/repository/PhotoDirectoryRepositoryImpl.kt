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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath

class PhotoDirectoryRepositoryImpl(
    private val platformFileSystem: PlatformFileSystem,
    private val log: Logger
) : PhotoDirectoryRepository {

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    override val albums: StateFlow<List<Album>> = _albums.asStateFlow()
    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    private val _isScanPartial = MutableStateFlow(false)
    override val isScanPartial: StateFlow<Boolean> = _isScanPartial.asStateFlow()
    private val scanCancellationRequested = MutableStateFlow(false)
    private val scanGeneration = MutableStateFlow(0L)
    private var activeScanGeneration: Long? = null

    var platformContext: PlatformContext? = null
    private var currentKmpFile: KmpFile? = null

    // Flag to control when a disk scan is actually necessary
    private var needsRefresh = true

    override fun setPath(kmpFile: KmpFile, platformContext: PlatformContext) {
        invalidateCurrentScan()
        currentKmpFile = kmpFile
        this.platformContext = platformContext
        _albums.value = emptyList()
        _isScanPartial.value = false
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
        // This prevents re-scanning (and resetting user selections) when navigating back, including empty and partial results.
        if (!needsRefresh) {
            emit(DataLoadingState.Success)
            return@flow
        }

        val generation = scanGeneration.value
        scanCancellationRequested.value = false
        _isScanPartial.value = false
        val rootDir = currentKmpFile
        val currentContext = platformContext
        if (rootDir == null || currentContext == null) {
            _albums.value = emptyList()
            needsRefresh = false
            emit(DataLoadingState.Success)
            return@flow
        }

        activeScanGeneration = generation
        _isScanning.value = true
        try {
            emit(DataLoadingState.Loading)
            val result = withContext(Dispatchers.Default) {
                scanDirectoryInternal(rootDir, currentContext, generation)
            }
            // Discard results if a path change or reload superseded this scan.
            if (generation != scanGeneration.value) {
                log.d { "Discarding stale scan result for superseded scan" }
                return@flow
            }
            _albums.value = result.albums
            _isScanPartial.value = result.isPartial
            needsRefresh = false
            emit(DataLoadingState.Success)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.e(e) { "Exception while loading photos" }
            emit(DataLoadingState.Error(e))
        } finally {
            if (activeScanGeneration == generation) {
                activeScanGeneration = null
                _isScanning.value = false
            }
        }
    }

    override fun prepareReload() {
        invalidateCurrentScan()
        // Force a scan on the next collection
        _isScanPartial.value = false
        needsRefresh = true
    }

    override fun cancelScan() {
        scanCancellationRequested.value = true
    }

    private fun invalidateCurrentScan() {
        scanGeneration.update { it + 1 }
        scanCancellationRequested.value = false
        activeScanGeneration = null
        _isScanning.value = false
    }

    /**
     * Scans the directory structure starting from [rootDir] and returns a list of [Album]s.
     */
    private suspend fun scanDirectoryInternal(
        rootDir: KmpFile,
        currentContext: PlatformContext,
        generation: Long,
    ): ScanResult {
        log.d {
            "Scanning directory: ${
                platformFileSystem.getDisplayName(
                    rootDir,
                    currentContext
                )
            }"
        }

        // The root itself might contain photos
        val albums = mutableListOf<Album>()
        val completed = processDirectory(rootDir, null, "", albums, currentContext, generation)
        return ScanResult(
            albums = albums,
            isPartial = !completed && generation == scanGeneration.value && scanCancellationRequested.value,
        )
    }

    private suspend fun processDirectory(
        currentDir: KmpFile,
        parentTitle: String?,
        groupName: String,
        albums: MutableList<Album>,
        currentContext: PlatformContext,
        generation: Long,
    ): Boolean {
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

        currentCoroutineContext().ensureActive()
        if (generation != scanGeneration.value) {
            return false
        }

        val photoFiles = mutableListOf<Photo>()
        val subDirectories = mutableListOf<KmpFile>()
        var wasCancelled = false

        // 1. Loop all entries in the current directory
        for (entry in entries) {
            // Propagate cancellation of the job
            currentCoroutineContext().ensureActive()
            if (generation != scanGeneration.value) {
                return false
            }
            // Stop if the user canceled the scan, keep the partial result.
            if (scanCancellationRequested.value) {
                wasCancelled = true
                break
            }

            // 2. Identify photos and subdirectories
            if (platformFileSystem.isDirectory(entry, currentContext)) {
                // Found a subdirectory in the current directory
                subDirectories.add(entry)
            } else {
                val entryName = platformFileSystem.getName(entry, currentContext)
                if (FileUtils.isPhotoFile(entryName)) {
                    // Found a photo in the current directory
                    photoFiles.add(
                        Photo(
                            kmpFile = entry,
                            path = (platformFileSystem.getPath(entry, currentContext)
                                ?: "").toPath(),
                            name = entryName ?: "",
                        )
                    )
                }
            }
        }

        photoFiles.sortBy { it.name }
        subDirectories.sortBy { platformFileSystem.getPath(it, currentContext) }

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
                    group = if (parentTitle == null) albumName else groupName,
                    photos = photoFiles,
                )
            )
        }

        if (wasCancelled || scanCancellationRequested.value) {
            return false
        }

        // 4. Recurse
        for (subDir in subDirectories) {
            currentCoroutineContext().ensureActive()
            if (generation != scanGeneration.value) {
                return false
            }
            if (scanCancellationRequested.value) {
                return false
            }
            // The names of the direct child dirs of rootDir are the sticky group headers in the UI
            val group = if (parentTitle == null) platformFileSystem.getDisplayName(
                subDir,
                currentContext
            ) else groupName
            val completed = processDirectory(
                currentDir = subDir,
                parentTitle = if (parentTitle == null) "" else titlePrefix,
                groupName = group,
                albums = albums,
                currentContext = currentContext,
                generation = generation,
            )
            if (!completed) {
                return false
            }
        }
        return true
    }

    private data class ScanResult(
        val albums: List<Album>,
        val isPartial: Boolean,
    )
}