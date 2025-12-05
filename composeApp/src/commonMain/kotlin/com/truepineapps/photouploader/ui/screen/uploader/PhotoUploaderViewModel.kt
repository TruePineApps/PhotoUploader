package com.truepineapps.photouploader.ui.screen.uploader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truepineapps.photouploader.auth.GoogleAuthService
import com.truepineapps.photouploader.network.PhotoUploader
import com.truepineapps.photouploader.network.UploadedPhoto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM

class PhotoUploaderViewModel(
    private val authService: GoogleAuthService,
) : ViewModel() {


    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Automatically check for existing sign-in when the ViewModel is created
        checkInitialAuth()
    }

    private fun checkInitialAuth() {
        viewModelScope.launch {
            val token = authService.restoreSignIn()
            if (token != null) {
                _uiState.update { it.copy(isAuthenticated = true) }
            }
        }
    }

    fun signIn() {
        viewModelScope.launch {
            val token = authService.signIn()
            if (token != null) {
                _uiState.update { it.copy(isAuthenticated = true) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
            _uiState.update { it.copy(isAuthenticated = false) }
        }
    }

    fun updatePath(path: String) {
        _uiState.update { it.copy(path = path) }
    }

    fun updateShowDirPicker(isShowing: Boolean) {
        _uiState.update { it.copy(isShowDirPicker = isShowing) }
    }

    fun updateIsUploading(isUploading: Boolean) {
        _uiState.update { it.copy(isUploading = isUploading) }
    }

    /**
     * Uploads photos from a directory structure to Google Photos.
     *
     * @param fileSystem File system to use for reading the directory structure
     */
    fun uploadPhotos(fileSystem: FileSystem = FileSystem.SYSTEM): Job? {
        val state = _uiState.value
        if (state.path.isNotBlank() && !state.busy()) {
            val rootPath = state.path.toPath()

            require(fileSystem.exists(rootPath)) {
                "Root path does not exist: ${state.path}"
            }
            require(fileSystem.metadata(rootPath).isDirectory) {
                "Root path is not a directory: ${state.path}"
            }

            updateIsUploading(true)
            return viewModelScope.launch {
                try {
                    uploadPhotosImpl(rootPath, fileSystem)
                } finally {
                    updateIsUploading(false)
                }
            }
        }
        return null
    }

    /** Signs in to obtain an access token and starts uploading the photo's from the root directory
     * @param rootPath Path to the root directory containing the photos
     * @param fileSystem File system to use
     * @throws Exception if the sign in fails or the upload fails
     * @return true if successful, false otherwise
     */
    private suspend fun uploadPhotosImpl(
        rootPath: Path,
        fileSystem: FileSystem = FileSystem.SYSTEM,
    ): Boolean {
        var result = false
        try {
            val accessToken = authService.signIn()
            require(accessToken != null) {
                "Failed to sign in"
            }
            val photoUploader = PhotoUploader(accessToken)

            result = handleDirectory(rootPath, fileSystem, photoUploader)
            println("\nUpload process completed!")
        } catch (e: Exception) {
            println("ERROR: ${e.message}")
            e.printStackTrace()
        }
        return result
    }

    /** recursively uploads the photos in the path to a new Google Album.
     * @param path Path to the directory containing the photos
     * @param fileSystem File system to use
     * @param photoUploader Photo uploader to use
     * @param albumTitle Album title from the path, current name can be appended
     * @return true if successful, false otherwise
     */
    private suspend fun handleDirectory(
        path: Path,
        fileSystem: FileSystem,
        photoUploader: PhotoUploader,
        albumTitle: String? = null,
    ): Boolean {
        var result = true
        val albumName: String
        val titlePrefix: String
        if (albumTitle == null) {
            // Root folder must only be used for album name for root level pictures, not in the album path name
            albumName = path.name
            titlePrefix = ""
        } else {
            albumName = "$albumTitle${path.name}"
            titlePrefix = "$albumName - "
        }


        // List all photo files on this path
        val photoFiles = fileSystem.list(path)
            .filter {
                val metadata = fileSystem.metadata(it)
                metadata.isRegularFile && photoUploader.isPhotoFile(it.name)
            }
            .sortedBy { it.name }

        println("  Found ${photoFiles.size} photos in ${path.name}")

        // List all topic directories on this path
        val topicDirs = fileSystem.list(path)
            .filter { fileSystem.metadata(it).isDirectory }
            .sortedBy { it.name }
        println("  Found ${topicDirs.size} topic directories in $path")

        // Upload all photos on this path
        if (photoFiles.isEmpty()) {
            if (topicDirs.isEmpty()) {
                println("  WARNING: No photos found in directory")
            }
        } else {
            result = result && uploadPhotosToNewAlbum(
                albumName,
                photoFiles,
                fileSystem,
                photoUploader
            )
        }

        // Handle all directories on this path
        for (topicDir in topicDirs) {
            handleDirectory(topicDir, fileSystem, photoUploader, titlePrefix)
        }

        println("Completed dir: ${path.name}")
        return result
    }

    /** Create a Google Album, upload the photos in the path and add them to the new album.
     * @return true if successful, false otherwise
     */
    private suspend fun uploadPhotosToNewAlbum(
        albumName: String,
        photoFiles: List<Path>,
        fileSystem: FileSystem,
        photoUploader: PhotoUploader,
    ): Boolean {
        // Create album
        val albumId = photoUploader.createAlbum(albumName)
        if (albumId == null) {
            println("    ERROR: Failed to create album: $albumName")
            return false
        }
        println("    Created album with ID: $albumId for $albumName")

        // Upload photos and collect upload tokens
        val uploadTokens = mutableListOf<UploadedPhoto>()

        for ((index, photoFile) in photoFiles.withIndex()) {
            println("    Uploading photo ${index + 1}/${photoFiles.size}: ${photoFile.name}")

            val uploadToken = photoUploader.uploadPhoto(
                photoFile,
                fileSystem
            )
            if (uploadToken != null) {
                uploadTokens.add(UploadedPhoto(uploadToken, photoFile.name))
                println("      Success: ${photoFile.name}")
            } else {
                println("      ERROR: Failed to upload ${photoFile.name}")
            }
        }

        // Add photos to album in batches of 50 (API limit)
        if (uploadTokens.isNotEmpty()) {
            photoUploader.addPhotosToAlbum(albumId, uploadTokens)
            println("    Added ${uploadTokens.size} photos to album")
        }
        return true
    }

}

data class UiState(
    val isAuthenticated: Boolean = false,
    val isShowDirPicker: Boolean = false,
    val isUploading: Boolean = false,
    val path: String = "",
) {
    fun busy() = isShowDirPicker || isUploading
}