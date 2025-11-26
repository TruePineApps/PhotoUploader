package com.truepine.photouploader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truepine.photouploader.auth.GoogleAuthService
import com.truepine.photouploader.network.PhotoUploader
import com.truepine.photouploader.network.UploadedPhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM

class PhotoUploadViewModel(
    private val authService: GoogleAuthService,
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()


    init {
        // Automatically check for existing sign-in when the ViewModel is created
        checkInitialAuth()
    }

    private fun checkInitialAuth() {
        viewModelScope.launch {
            val token = authService.restoreSignIn()
            if (token != null) {
                _isAuthenticated.value = true
            }
        }
    }

    fun signIn() {
        viewModelScope.launch {
            val token = authService.signIn()
            if (token != null) {
                _isAuthenticated.value = true
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
            _isAuthenticated.value = false
        }
    }


    /**
     * Uploads photos from a directory structure to Google Photos.
     * Expected structure: path/year/topic/photos
     *
     * @param path Root directory path containing year folders
     */
    suspend fun uploadPhotos(path: String, fileSystem: FileSystem = FileSystem.SYSTEM) {

        val rootPath = path.toPath()

        require(fileSystem.exists(rootPath)) {
            "Root path does not exist: $path"
        }
        require(fileSystem.metadata(rootPath).isDirectory) {
            "Root path is not a directory: $path"
        }
        try {
            val accessToken = authService.signIn()
            require(accessToken != null) {
                "Failed to sign in"
            }
            val photoUploader = PhotoUploader(accessToken)

            handleDirectory(rootPath, fileSystem, photoUploader)
            println("\nUpload process completed!")
        } catch (e: Exception) {
            println("ERROR: ${e.message}")
            e.printStackTrace()
        }
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
        albumTitle: String = "",
    ): Boolean {
        var result = true
        val albumName = "$albumTitle${path.name}"

        // List all photo files on this path
        val photoFiles = fileSystem.list(path)
            .filter {
                val metadata = fileSystem.metadata(it)
                metadata.isRegularFile && photoUploader.isPhotoFile(it.name)
            }
            .sortedBy { it.name }

        println("    Found ${photoFiles.size} photos in ${path.name}")

        if (photoFiles.isEmpty()) {
            println("    WARNING: No photos found in directory")
        } else {
            // Upload all photos on this path
            result = result && uploadPhotosToNewAlbum(
                albumName,
                photoFiles,
                fileSystem,
                photoUploader
            )
        }

        // List all topic directories on this path
        val topicDirs = fileSystem.list(path)
            .filter { fileSystem.metadata(it).isDirectory }
            .sortedBy { it.name }

        println("  Found ${topicDirs.size} topic directories in $path")

        // Handle all directories on this path
        val titlePrefix = "$albumName - "
        for (topicDir in topicDirs) {
            val topic = topicDir.name
            handleDirectory(topicDir, fileSystem, photoUploader, titlePrefix)
        }

        println("Completed year: ${path.name}")
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
        println("    Created album with ID: $albumId")

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

    /**
     * Uploads photos from a directory structure to Google Photos.
     * Expected structure: path/year/topic/photos
     *
     * @param path Root directory path containing year folders
     */
    suspend fun uploadPhotosYearTopic(path: String, fileSystem: FileSystem = FileSystem.SYSTEM) {

        val rootPath = path.toPath()

        require(fileSystem.exists(rootPath)) {
            "Root path does not exist: $path"
        }
        require(fileSystem.metadata(rootPath).isDirectory) {
            "Root path is not a directory: $path"
        }
        try {
            val accessToken = authService.signIn()
            require(accessToken != null) {
                "Failed to sign in"
            }

            val photoUploader = PhotoUploader(accessToken)

            // List all year directories
            val yearDirs = fileSystem.list(rootPath)
                .filter { fileSystem.metadata(it).isDirectory }
                .sortedBy { it.name }

            println("Found ${yearDirs.size} year directories")

            for (yearDir in yearDirs) {
                val year = yearDir.name
                println("\nProcessing year: $year")

                // List all topic directories within the year
                val topicDirs = fileSystem.list(yearDir)
                    .filter { fileSystem.metadata(it).isDirectory }
                    .sortedBy { it.name }

                println("  Found ${topicDirs.size} topic directories in $year")

                for (topicDir in topicDirs) {
                    val topic = topicDir.name
                    val albumName = "$year - $topic"

                    println("  Processing topic: $topic")
                    println("    Album name: $albumName")

                    // List all photo files in the topic directory
                    val photoFiles = fileSystem.list(topicDir)
                        .filter {
                            val metadata = fileSystem.metadata(it)
                            metadata.isRegularFile && photoUploader.isPhotoFile(
                                it.name
                            )
                        }
                        .sortedBy { it.name }

                    println("    Found ${photoFiles.size} photos in $topic")

                    if (photoFiles.isEmpty()) {
                        println("    WARNING: No photos found in directory")
                        continue
                    }

                    // Create album
                    val albumId = photoUploader.createAlbum(albumName)
                    if (albumId == null) {
                        println("    ERROR: Failed to create album: $albumName")
                        continue
                    }
                    println("    Created album with ID: $albumId")

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

                    println("  Completed topic: $topic")
                }

                println("Completed year: $year")
            }

            println("\nUpload process completed!")

        } catch (e: Exception) {
            println("ERROR: ${e.message}")
            e.printStackTrace()
        }
    }
}