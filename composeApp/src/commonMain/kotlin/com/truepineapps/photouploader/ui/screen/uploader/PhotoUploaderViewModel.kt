package com.truepineapps.photouploader.ui.screen.uploader

import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.auth.GoogleAuthService
import com.truepineapps.photouploader.data.PhotoDirectoryRepository
import com.truepineapps.photouploader.io.getAbsolutePath
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.model.UploadStatus
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
            albums = albums,
            globalErrorMessage = viewState.globalErrorMessage
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

    fun clearGlobalError() {
        _viewState.update { it.copy(globalErrorMessage = null) }
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
                        photo.path == coverPhoto.path -> photo.copy(isCoverPhoto = true)
                        photo.isCoverPhoto -> photo.copy(isCoverPhoto = false)
                        else -> photo
                    }
                }
                album.copy(photos = updatedPhotos, coverPhoto = coverPhoto)
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
        if (state.albums.isNotEmpty() && !state.busy()) {
            updateIsUploading(true)
            return viewModelScope.launch {
                try {
                    uploadPhotosImpl(state.albums)
                } catch (e: Exception) {
                    val message = e.message ?: "An unknown error occurred."
                    _viewState.update { it.copy(globalErrorMessage = message) }
                } finally {
                    updateIsUploading(false)
                }
            }
        }
        return null
    }

    /** Signs in to obtain an access token and starts uploading the photo's from the albums list
     * @param albums List of albums to process
     * @return true if successful, false otherwise
     */
    private suspend fun uploadPhotosImpl(albums: List<Album>): Boolean {
        val context = platformContext
        require(context != null) { "Platform context not set" }

        val accessToken = authService.signIn()
        require(accessToken != null) { "Failed to sign in" }

        val photoUploader = PhotoUploader(accessToken, context)
        val albumsToUpload = albums.filter { it.isEnabled && it.photos.any { p -> p.isEnabled } }

        // Set initial 'Waiting' status on all items to be uploaded
        albumsToUpload.forEach { album ->
            album.uploadStatus = UploadStatus.Waiting
            album.photos.filter { it.isEnabled }.forEach { photo ->
                photo.uploadStatus = UploadStatus.Waiting
            }
        }
        repository.updateAlbums(repository.albums.value)

        println("Starting upload for ${albumsToUpload.size} albums")
        
        for (album in albumsToUpload) {
            uploadPhotosToNewAlbum(album, photoUploader)
        }
        println("\nUpload process completed!")
        return true
    }

    private suspend fun uploadPhotosToNewAlbum(
        album: Album,
        photoUploader: PhotoUploader,
    ): Boolean {
        album.uploadStatus = UploadStatus.Uploading
        repository.updateAlbums(repository.albums.value)

        val googleAlbumId = try {
            photoUploader.createAlbum(album.name)
        } catch (e: Exception) {
            album.uploadStatus = UploadStatus.Error("Failed to create album: ${e.message}")
            repository.updateAlbums(repository.albums.value)
            return false
        }

        if (googleAlbumId == null) {
            album.uploadStatus = UploadStatus.Error("Failed to create album.")
            repository.updateAlbums(repository.albums.value)
            return false
        }
        album.albumId = googleAlbumId
        println("    Created album with ID: $googleAlbumId for ${album.name}")

        val photosToUpload = album.photos.filter { it.isEnabled }
        val uploadedItems = mutableListOf<Pair<Photo, String>>()

        // The 'Waiting' status is now set in uploadPhotosImpl, so we just set Uploading here.

        for ((index, photo) in photosToUpload.withIndex()) {
            photo.uploadStatus = UploadStatus.Uploading
            repository.updateAlbums(repository.albums.value)
            println("    Uploading photo ${index + 1}/${photosToUpload.size}: ${photo.name}")

            val uploadToken = photoUploader.uploadPhoto(photo)
            if (uploadToken != null) {
                uploadedItems.add(photo to uploadToken)
                photo.uploadStatus = UploadStatus.Success
                println("      Success: ${photo.name}")
            } else {
                photo.uploadStatus = UploadStatus.Error("Upload failed")
                println("      ERROR: Failed to upload ${photo.name}")
            }
            repository.updateAlbums(repository.albums.value)
        }

        if (uploadedItems.isNotEmpty()) {
            val newMediaItems = uploadedItems.map { (photo, token) ->
                NewMediaItem(
                    description = photo.getDisplayName(),
                    simpleMediaItem = SimpleMediaItem(fileName = photo.name, uploadToken = token)
                )
            }
            
            val results = photoUploader.addPhotosToAlbum(googleAlbumId, newMediaItems)
            
            if (results != null) {
                results.forEachIndexed { i, result ->
                    val photo = uploadedItems[i].first
                    if (result.status.code == 0 && result.mediaItem != null) {
                        photo.mediaItemId = result.mediaItem.id
                    } else {
                        photo.uploadStatus = UploadStatus.Error(result.status.message ?: "Failed to add to album")
                    }
                }
                repository.updateAlbums(repository.albums.value)

                val coverMediaItemId = album.coverPhoto.mediaItemId
                if (coverMediaItemId != null) {
                    println("    Setting cover photo to: ${album.coverPhoto.name}")
                    photoUploader.updateAlbumCover(googleAlbumId, coverMediaItemId)
                }
            }
        }
        
        album.uploadStatus = album.getDerivedUploadStatus()
        repository.updateAlbums(repository.albums.value)
        
        return album.uploadStatus is UploadStatus.Success
    }
}

// Internal state for view model specific fields
data class ViewState(
    val isAuthenticated: Boolean = false,
    val isShowDirPicker: Boolean = false,
    val isUploading: Boolean = false,
    val kmpFile: KmpFile? = null,
    val path: String = "",
    val globalErrorMessage: String? = null
)

data class UiState(
    val isAuthenticated: Boolean = false,
    val isShowDirPicker: Boolean = false,
    val isUploading: Boolean = false,
    val path: String = "",
    val albums: List<Album> = emptyList(),
    val globalErrorMessage: String? = null
) {
    fun busy() = isShowDirPicker || isUploading
}
