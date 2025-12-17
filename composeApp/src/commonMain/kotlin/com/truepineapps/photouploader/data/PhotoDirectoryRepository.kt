package com.truepineapps.photouploader.data

import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.io.PlatformFileSystem
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.util.FileUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import okio.Path.Companion.toPath

class PhotoDirectoryRepository(
    private val platformFileSystem: PlatformFileSystem
) : DataLoadingRepository {

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    var context: PlatformContext? = null
    private var currentKmpFile: KmpFile? = null

    // Flag to control when a disk scan is actually necessary
    private var needsRefresh = true

    fun setPath(kmpFile: KmpFile, platformContext: PlatformContext) {
        currentKmpFile = kmpFile
        context = platformContext
        // A new path means we definitely need to scan
        needsRefresh = true
    }

    // Allow updating albums from outside (e.g. toggling check boxes in ViewModel)
    fun updateAlbums(newAlbums: List<Album>) {
        _albums.value = newAlbums
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
            println("Exception while loading photos: ${e::class.simpleName} - ${e.message}")
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
        println("Scanning directory: $rootDir")

        requireNotNull(rootDir)

        // Get a safe non-mutable context variable
        val currentContext = context ?: throw IllegalStateException("currentContext is null")

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
        println("Processing directory: $name with ${entries.size} entries")

        // 1. Identify photos in the current directory
        var isFirst = true
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
                    isEnabled = true,
                    isCoverPhoto = isFirst
                ).also {
                    isFirst = false
                }
            }

        // 2. Identify Subdirectories
        val subDirectories = entries
            .filter { platformFileSystem.isDirectory(it, currentContext) }
            .sortedBy { platformFileSystem.getPath(it, currentContext) }

        // 3. Create an Album if there are photos
        if (photoFiles.isNotEmpty()) {
            val coverPhoto = photoFiles.first()
            albums.add(
                Album(
                    id = currentDir.toString().replace("/", "|"), // Escape slashes for navigation
                    kmpFile = currentDir,
                    path = (platformFileSystem.getPath(currentDir, currentContext) ?: "").toPath(),
                    name = albumName,
                    group = groupName,
                    photos = photoFiles,
                    coverPhoto = coverPhoto.kmpFile,
                    coverDescription = coverPhoto.getDisplayName(),
                    isEnabled = true
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
