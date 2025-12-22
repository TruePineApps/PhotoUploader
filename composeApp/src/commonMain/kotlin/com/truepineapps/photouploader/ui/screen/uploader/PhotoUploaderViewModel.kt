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
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.error_add_media_items_failed
import com.truepineapps.photouploader.resources.error_add_to_album_failed
import com.truepineapps.photouploader.resources.error_album_creation_failed
import com.truepineapps.photouploader.resources.error_album_creation_failed_with_message
import com.truepineapps.photouploader.resources.error_platform_context_not_set
import com.truepineapps.photouploader.resources.error_sign_in_failed
import com.truepineapps.photouploader.resources.error_unknown
import com.truepineapps.photouploader.resources.error_upload_failed
import com.truepineapps.photouploader.ui.screen.LoadingViewModel
import com.truepineapps.photouploader.util.UiText
import com.truepineapps.photouploader.util.UiTextResource
import com.truepineapps.photouploader.util.UiTextString
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

    fun clearGlobalErrorMessage() {
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
                    // If message is generic, use resource, else string
                    val uiText =
                            if (e.message == null) UiTextResource(Res.string.error_unknown) else UiTextString(
                                message
                            )
                    _viewState.update { it.copy(globalErrorMessage = uiText) }
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
    private suspend fun uploadPhotosImpl(albums: List<Album>) {
        val context = platformContext
        if (context == null) {
            _viewState.update { it.copy(globalErrorMessage = UiTextResource(Res.string.error_platform_context_not_set)) }
            return
        }

        val accessToken = authService.signIn()
        if (accessToken == null) {
            _viewState.update { it.copy(globalErrorMessage = UiTextResource(Res.string.error_sign_in_failed)) }
            return
        }

        val photoUploader = PhotoUploader(accessToken, context)
        val albumsToUpload = albums.filter { it.isEnabled && it.photos.any { p -> p.isEnabled } }

        // Set initial 'Waiting' status on all items to be uploaded
        val initialUpdate = repository.albums.value.map { currentAlbum ->
            if (albumsToUpload.any { it.id == currentAlbum.id }) {
                val updatedPhotos = currentAlbum.photos.map { p ->
                    if (p.isEnabled) p.copy(uploadStatus = UploadStatus.Waiting) else p
                }
                currentAlbum.copy(uploadStatus = UploadStatus.Waiting, photos = updatedPhotos)
            } else {
                currentAlbum
            }
        }
        repository.updateAlbums(initialUpdate)

        println("Starting upload for ${albumsToUpload.size} albums")

        for (album in albumsToUpload) {
            uploadPhotosToNewAlbum(album, photoUploader)
        }

        println("\nUpload process completed!")
    }

    private suspend fun uploadPhotosToNewAlbum(album: Album, photoUploader: PhotoUploader) {
        // Notify the user that this album starts uploading
        updateAlbumStatus(album.id, UploadStatus.Uploading)

        val googleAlbumId = createGoogleAlbum(album, photoUploader) ?: return

        val uploadedItems = uploadPhotosInAlbum(album, photoUploader)

        // If no photos were successfully uploaded (e.g. all failed), there's nothing left to do.
        if (uploadedItems.isEmpty()) {
            val finalAlbum = repository.albums.value.find { it.id == album.id }!!
            updateAlbumStatus(
                album.id,
                finalAlbum.copy(uploadStatus = UploadStatus.Success).getDerivedUploadStatus()
            )
            return
        }

        addMediaItemsToAlbum(album, googleAlbumId, uploadedItems, photoUploader)

        // Use the updated album to set the cover photo to the media item id added earlier
        val finalAlbum = repository.albums.value.find { it.id == album.id }!!
        setAlbumCover(finalAlbum, googleAlbumId, photoUploader)
        updateAlbumStatus(
            album.id,
            finalAlbum.copy(uploadStatus = UploadStatus.Success).getDerivedUploadStatus()
        )
    }

    private suspend fun createGoogleAlbum(album: Album, photoUploader: PhotoUploader): String? {
        val googleAlbumId = try {
            photoUploader.createAlbum(album.name)
        } catch (e: Exception) {
            println("    Failed to create album for ${album.name}: ${e.message}")
            val msg = e.message
            updateAlbumStatus(
                album.id, UploadStatus.Error(
                    if (msg == null)
                        UiTextResource(Res.string.error_album_creation_failed)
                    else {
                        UiTextResource(
                            Res.string.error_album_creation_failed_with_message,
                            listOf(msg)
                        )
                    }
                )
            )
            return null
        }

        if (googleAlbumId == null) {
            println("    Failed to create album for ${album.name}")
            updateAlbumStatus(
                album.id,
                UploadStatus.Error(UiTextResource(Res.string.error_album_creation_failed))
            )
            return null
        }

        val currentAlbums = repository.albums.value
        val updatedAlbumsWithId = currentAlbums.map {
            if (it.id == album.id) it.copy(albumId = googleAlbumId) else it
        }
        repository.updateAlbums(updatedAlbumsWithId)
        println("    Created album with ID: $googleAlbumId for ${album.name}")
        return googleAlbumId
    }

    private suspend fun uploadPhotosInAlbum(
        album: Album,
        photoUploader: PhotoUploader,
    ): List<Pair<Photo, String>> {
        val photosToUpload = album.photos.filter { it.isEnabled }
        val successfullyUploaded = mutableListOf<Pair<Photo, String>>()

        for ((index, photo) in photosToUpload.withIndex()) {
            updatePhotoStatus(album.id, photo.path, UploadStatus.Uploading)
            println("    Uploading photo ${index + 1}/${photosToUpload.size}: ${photo.name}")

            val uploadToken = photoUploader.uploadPhoto(photo)
            if (uploadToken != null) {
                successfullyUploaded.add(photo to uploadToken)
                updatePhotoStatus(album.id, photo.path, UploadStatus.Success)
                println("      Success: ${photo.name}")
            } else {
                updatePhotoStatus(
                    album.id,
                    photo.path,
                    UploadStatus.Error(UiTextResource(Res.string.error_upload_failed))
                )
                println("      ERROR: Failed to upload ${photo.name}")
            }
        }
        return successfullyUploaded
    }

    private suspend fun addMediaItemsToAlbum(
        album: Album,
        googleAlbumId: String,
        uploadedItems: List<Pair<Photo, String>>,
        photoUploader: PhotoUploader,
    ) {
        val newMediaItems = uploadedItems.map { (photo, token) ->
            NewMediaItem(
                description = photo.getDisplayName(),
                simpleMediaItem = SimpleMediaItem(fileName = photo.name, uploadToken = token)
            )
        }

        val results = photoUploader.addPhotosToAlbum(googleAlbumId, newMediaItems)

        if (results != null) {
            val currentRepoAlbums = repository.albums.value
            val finalUpdatedAlbums = currentRepoAlbums.map { currentAlbum ->
                if (currentAlbum.id == album.id) {
                    val updatedPhotos = currentAlbum.photos.map { p ->
                        val index = uploadedItems.indexOfFirst { it.first.path == p.path }
                        if (index != -1 && index < results.size) {
                            val result = results[index]
                            if (result.status.code == 0 && result.mediaItem != null) {
                                p.copy(mediaItemId = result.mediaItem.id)
                            } else {
                                println("      ERROR: Failed to add ${p.name} to album ${currentAlbum.name}")
                                p.copy(
                                    uploadStatus = UploadStatus.Error(
                                        if (result.status.message != null) UiTextString(result.status.message)
                                        else UiTextResource(Res.string.error_add_to_album_failed)
                                    )
                                )
                            }
                        } else {
                            p
                        }
                    }
                    val newCoverPhoto =
                            updatedPhotos.find { it.path == currentAlbum.coverPhoto.path }
                                ?: currentAlbum.coverPhoto

                    currentAlbum.copy(photos = updatedPhotos, coverPhoto = newCoverPhoto)
                } else {
                    currentAlbum
                }
            }
            repository.updateAlbums(finalUpdatedAlbums)
        } else {
            // Handle the case where the batch creation request itself failed (results is null)
            val currentRepoAlbums = repository.albums.value
            val finalUpdatedAlbums = currentRepoAlbums.map { currentAlbum ->
                if (currentAlbum.id == album.id) {
                    val updatedPhotos = currentAlbum.photos.map { p ->
                        // Mark all photos that were part of this upload attempt as failed
                        if (uploadedItems.any { it.first.path == p.path }) {
                            p.copy(uploadStatus = UploadStatus.Error(UiTextResource(Res.string.error_add_media_items_failed)))
                        } else {
                            p
                        }
                    }
                    currentAlbum.copy(photos = updatedPhotos)
                } else {
                    currentAlbum
                }
            }
            repository.updateAlbums(finalUpdatedAlbums)
        }
    }

    private suspend fun setAlbumCover(
        album: Album,
        googleAlbumId: String,
        photoUploader: PhotoUploader,
    ) {
        val coverMediaItemId = album.coverPhoto.mediaItemId

        if (coverMediaItemId != null) {
            println("    Setting cover photo to: ${album.coverPhoto.name}")
            photoUploader.updateAlbumCover(googleAlbumId, coverMediaItemId)
        }
    }

    private fun updateAlbumStatus(albumId: String, status: UploadStatus) {
        val currentAlbums = repository.albums.value
        val updatedAlbums = currentAlbums.map { album ->
            if (album.id == albumId) {
                album.copy(uploadStatus = status)
            } else {
                album
            }
        }
        repository.updateAlbums(updatedAlbums)
    }

    private fun updatePhotoStatus(albumId: String, photoPath: Path, status: UploadStatus) {
        val currentAlbums = repository.albums.value
        val updatedAlbums = currentAlbums.map { album ->
            if (album.id == albumId) {
                val updatedPhotos = album.photos.map { photo ->
                    if (photo.path == photoPath) {
                        photo.copy(uploadStatus = status)
                    } else {
                        photo
                    }
                }
                val tempAlbum = album.copy(photos = updatedPhotos)
                tempAlbum.copy(uploadStatus = tempAlbum.getDerivedUploadStatus())
            } else {
                album
            }
        }
        repository.updateAlbums(updatedAlbums)
    }
}

data class ViewState(
    val isAuthenticated: Boolean = false,
    val isShowDirPicker: Boolean = false,
    val isUploading: Boolean = false,
    val kmpFile: KmpFile? = null,
    val path: String = "",
    val globalErrorMessage: UiText? = null,
)

data class UiState(
    val isAuthenticated: Boolean = false,
    val isShowDirPicker: Boolean = false,
    val isUploading: Boolean = false,
    val path: String = "",
    val albums: List<Album> = emptyList(),
    val globalErrorMessage: UiText? = null,
) {
    fun busy() = isShowDirPicker || isUploading
}
