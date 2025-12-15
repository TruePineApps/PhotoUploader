package com.truepineapps.photouploader.ui.screen.uploader

import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.auth.GoogleAuthService
import com.truepineapps.photouploader.data.PhotoDirectoryRepository
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.network.PhotoUploader
import com.truepineapps.photouploader.network.UploadedPhoto
import com.truepineapps.photouploader.ui.screen.LoadingViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM

class PhotoUploaderViewModel(
    private val authService: GoogleAuthService,
    private val repository: PhotoDirectoryRepository,
) : LoadingViewModel(repository) {

    var platformContext: PlatformContext? = null

    private val _viewState = MutableStateFlow(ViewState())
    
    val uiState: StateFlow<UiState> = combine(
        _viewState,
        repository.albums
    ) { viewState, albums ->
        UiState(
            isAuthenticated = viewState.isAuthenticated,
            isShowDirPicker = viewState.isShowDirPicker,
            isUploading = viewState.isUploading,
            path = viewState.path,
            albums = albums
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = UiState()
    )

    init {
        checkInitialAuth()
    }

    private fun checkInitialAuth() {
        viewModelScope.launch {
            val token = authService.restoreSignIn()
            if (token != null) {
                _viewState.update { it.copy(isAuthenticated = true) }
            }
        }
    }

    fun signIn() {
        viewModelScope.launch {
            val token = authService.signIn()
            if (token != null) {
                _viewState.update { it.copy(isAuthenticated = true) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
            _viewState.update { it.copy(isAuthenticated = false) }
        }
    }

    fun updatePath(kmpFile: KmpFile, path: String) {
        _viewState.update { it.copy(kmpFile = kmpFile, path = path) }
        println("Setting path to $path")
        repository.setPath(kmpFile, path.toPath(), platformContext!!)
        reload()
    }

    fun updateShowDirPicker(isShowing: Boolean) {
        _viewState.update { it.copy(isShowDirPicker = isShowing) }
    }

    fun updateIsUploading(isUploading: Boolean) {
        _viewState.update { it.copy(isUploading = isUploading) }
    }
    
    fun toggleAlbum(albumId: String) {
        val currentAlbums = repository.albums.value
        val updatedAlbums = currentAlbums.map { album ->
            if (album.id == albumId) {
                album.copy(isEnabled = !album.isEnabled)
            } else {
                album
            }
        }
        repository.updateAlbums(updatedAlbums)
    }

    fun togglePhoto(albumId: String, photoPath: Path) {
        val currentAlbums = repository.albums.value
        val updatedAlbums = currentAlbums.map { album ->
            if (album.id == albumId) {
                val updatedPhotos = album.photos.map { photo ->
                    if (photo.path == photoPath) {
                        photo.copy(isEnabled = !photo.isEnabled)
                    } else {
                        photo
                    }
                }
                album.copy(photos = updatedPhotos)
            } else {
                album
            }
        }
        repository.updateAlbums(updatedAlbums)
    }
    
    fun renameAlbum(albumId: String, newName: String) {
        val currentAlbums = repository.albums.value
        val updatedAlbums = currentAlbums.map { album ->
            if (album.id == albumId) {
                album.copy(name = newName)
            } else {
                album
            }
        }
        repository.updateAlbums(updatedAlbums)
    }


    /**
     * Uploads photos based on the current UI state to Google Photos.
     *
     * @param fileSystem File system to use for reading the files
     */
    fun uploadPhotos(fileSystem: FileSystem = FileSystem.SYSTEM): Job? {
        val state = uiState.value
        // Must have albums and not be busy
        if (state.albums.isNotEmpty() && !state.busy()) {
            
            updateIsUploading(true)
            return viewModelScope.launch {
                try {
                    uploadPhotosImpl(state.albums, fileSystem)
                } finally {
                    updateIsUploading(false)
                }
            }
        }
        return null
    }

    /** Signs in to obtain an access token and starts uploading the photo's from the albums list
     * @param albums List of albums to process
     * @param fileSystem File system to use
     * @throws Exception if the sign in fails or the upload fails
     * @return true if successful, false otherwise
     */
    private suspend fun uploadPhotosImpl(
        albums: List<Album>,
        fileSystem: FileSystem = FileSystem.SYSTEM,
    ): Boolean {
        try {
            val accessToken = authService.signIn()
            require(accessToken != null) {
                "Failed to sign in"
            }
            val photoUploader = PhotoUploader(accessToken)
            
            val albumsToUpload = albums.filter { it.isEnabled && it.photos.any { p -> p.isEnabled } }

            println("Starting upload for ${albumsToUpload.size} albums")
            
            for (album in albumsToUpload) {
                val photosToUpload = album.photos.filter { it.isEnabled }
                if (photosToUpload.isNotEmpty()) {
                    uploadPhotosToNewAlbum(
                        album.name,
                        photosToUpload.map { it.path },
                        fileSystem,
                        photoUploader
                    )
                }
            }
            println("\nUpload process completed!")
            return true
        } catch (e: Exception) {
            println("ERROR: ${e.message}")
            e.printStackTrace()
            return false
        }
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

        // Add photos to album
        if (uploadTokens.isNotEmpty()) {
            photoUploader.addPhotosToAlbum(albumId, uploadTokens)
            println("    Added ${uploadTokens.size} photos to album")
        }
        return true
    }
}

// Internal state for view model specific fields
data class ViewState(
    val isAuthenticated: Boolean = false,
    val isShowDirPicker: Boolean = false,
    val isUploading: Boolean = false,
    val kmpFile: KmpFile? = null,
    val path: String = "",
)

data class UiState(
    val isAuthenticated: Boolean = false,
    val isShowDirPicker: Boolean = false,
    val isUploading: Boolean = false,
    val path: String = "",
    val albums: List<Album> = emptyList(),
) {
    fun busy() = isShowDirPicker || isUploading
}
