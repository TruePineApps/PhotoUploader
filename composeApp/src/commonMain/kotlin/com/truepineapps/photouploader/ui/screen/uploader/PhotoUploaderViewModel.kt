package com.truepineapps.photouploader.ui.screen.uploader

import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.auth.GoogleAuthService
import com.truepineapps.photouploader.data.PhotoDirectoryRepository
import com.truepineapps.photouploader.io.getAbsolutePath
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.network.NewMediaItem
import com.truepineapps.photouploader.network.PhotoUploader
import com.truepineapps.photouploader.network.SimpleMediaItem
import com.truepineapps.photouploader.ui.screen.LoadingViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.Path

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

    fun updatePath(kmpFile: KmpFile) {
        val path = kmpFile.getAbsolutePath(platformContext!!)
        _viewState.update { it.copy(kmpFile = kmpFile, path = path ?: "") }
        println("Setting path to $path")
        repository.setPath(kmpFile, platformContext!!)
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

    fun updateCoverPhoto(albumId: String, coverPhoto: Photo) {
        val currentAlbums = repository.albums.value
        val updatedAlbums = currentAlbums.map { album ->
            if (album.id == albumId) {
                val updatedPhotos = album.photos.map { photo ->
                    when {
                        // Set the new cover photo
                        photo.path == coverPhoto.path -> photo.copy(isCoverPhoto = true)
                        // Explicitly unset the old cover photo
                        photo.isCoverPhoto -> photo.copy(isCoverPhoto = false)
                        else -> photo
                    }
                }

                album.copy(
                    photos = updatedPhotos,
                    coverPhoto = coverPhoto,
                )
            } else {
                album
            }
        }
        repository.updateAlbums(updatedAlbums)
    }


    /**
     * Uploads photos based on the current UI state to Google Photos.
     */
    fun uploadPhotos(): Job? {
        val state = uiState.value
        // Must have albums and not be busy
        if (state.albums.isNotEmpty() && !state.busy()) {
            
            updateIsUploading(true)
            return viewModelScope.launch {
                try {
                    uploadPhotosImpl(state.albums)
                } finally {
                    updateIsUploading(false)
                }
            }
        }
        return null
    }

    /** Signs in to obtain an access token and starts uploading the photo's from the albums list
     * @param albums List of albums to process
     * @throws Exception if the sign in fails or the upload fails
     * @return true if successful, false otherwise
     */
    private suspend fun uploadPhotosImpl(albums: List<Album>): Boolean {
        try {
            val context = platformContext
            require(context != null) { "Platform context not set" }

            val accessToken = authService.signIn()
            require(accessToken != null) { "Failed to sign in" }

            val photoUploader = PhotoUploader(accessToken, context)
            val albumsToUpload = albums.filter { it.isEnabled && it.photos.any { p -> p.isEnabled } }

            println("Starting upload for ${albumsToUpload.size} albums")
            
            for (album in albumsToUpload) {
                if (album.photos.any { it.isEnabled }) {
                    uploadPhotosToNewAlbum(
                        album,
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
        album: Album,
        photoUploader: PhotoUploader,
    ): Boolean {
        // Create album
        val googleAlbumId = photoUploader.createAlbum(album.name)
        if (googleAlbumId == null) {
            println("    ERROR: Failed to create album: ${album.name}")
            return false
        }
        println("    Created album with ID: $googleAlbumId for ${album.name}")

        val photosToUpload = album.photos.filter { it.isEnabled }
        val uploadedItems = mutableListOf<Pair<Photo, String>>() // Photo -> UploadToken

        for ((index, photo) in photosToUpload.withIndex()) {
            println("    Uploading photo ${index + 1}/${photosToUpload.size}: ${photo.name}")

            val uploadToken = photoUploader.uploadPhoto(photo)
            if (uploadToken != null) {
                uploadedItems.add(photo to uploadToken)
                println("      Success: ${photo.name}")
            } else {
                println("      ERROR: Failed to upload ${photo.name}")
            }
        }

        // Add photos to album
        if (uploadedItems.isNotEmpty()) {
            val newMediaItems = uploadedItems.map { (photo, token) ->
                NewMediaItem(
                    description = photo.getDisplayName(),
                    simpleMediaItem = SimpleMediaItem(
                        fileName = photo.name,
                        uploadToken = token
                    )
                )
            }
            
            val results = photoUploader.addPhotosToAlbum(googleAlbumId, newMediaItems)
            
            if (results != null) {
                results.forEachIndexed { i, result ->
                    if (result.status.code == 0 && result.mediaItem != null) {
                        uploadedItems[i].first.mediaItemId = result.mediaItem.id
                    }
                }
                
                val coverMediaItemId = album.coverPhoto.mediaItemId
                if (coverMediaItemId != null) {
                    println("    Setting cover photo to: ${album.coverPhoto.name}")
                    photoUploader.updateAlbumCover(googleAlbumId, coverMediaItemId)
                }
            }
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
