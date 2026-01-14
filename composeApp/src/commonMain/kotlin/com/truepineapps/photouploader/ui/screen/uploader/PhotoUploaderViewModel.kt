package com.truepineapps.photouploader.ui.screen.uploader

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.auth.AuthException
import com.truepineapps.photouploader.auth.GoogleAuthService
import com.truepineapps.photouploader.auth.UserProfile
import com.truepineapps.photouploader.data.PhotoDirectoryRepository
import com.truepineapps.photouploader.io.getAbsolutePath
import com.truepineapps.photouploader.model.Album
import com.truepineapps.photouploader.model.Photo
import com.truepineapps.photouploader.model.UploadStatus
import com.truepineapps.photouploader.network.MediaItemResult
import com.truepineapps.photouploader.network.NewMediaItem
import com.truepineapps.photouploader.network.PhotoUploader
import com.truepineapps.photouploader.network.SimpleMediaItem
import com.truepineapps.photouploader.network.UploadException
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.error_add_to_album_failed
import com.truepineapps.photouploader.resources.error_sign_in_failed
import com.truepineapps.photouploader.resources.error_unknown
import com.truepineapps.photouploader.resources.session_expired
import com.truepineapps.photouploader.ui.screen.LoadingViewModel
import com.truepineapps.photouploader.util.UiText
import com.truepineapps.photouploader.util.UiTextResource
import com.truepineapps.photouploader.util.UiTextString
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
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
    private val log: Logger,
) : LoadingViewModel(repository) {

    var platformContext: PlatformContext? = null
    private var processJob: Job? = null

    private val _viewState = MutableStateFlow(ViewState())

    val uiState: StateFlow<UiState> = combine(
        _viewState, repository.albums
    ) { viewState, albums ->
        UiState(viewState, albums)
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
            try {
                val userProfile = authService.restoreSignIn()
                if (userProfile != null) {
                    _viewState.update { it.copy(userProfile = userProfile) }
                }
            } catch (e: Exception) {
                // Ignore errors during initial restore
                log.e(e) { "Error during initial auth check" }
            }
        }
    }

    /**
     * Public entry point for manual Sign In button (if any).
     */
    fun signIn() {
        if (processJob?.isActive == true) return

        processJob = viewModelScope.launch {
            try {
                performSignIn()
            } catch (e: CancellationException) {
                // Job was cancelled (e.g. via cancelProcess), stop gracefully
                log.d { "Sign in cancelled: ${e.message}" }
            } finally {
                processJob = null
            }
        }
    }

    /**
     * Centralized suspend function that handles the sign-in flow, error handling,
     * and UI state updates.
     * @return true if the user is successfully authenticated, false otherwise.
     */
    private suspend fun performSignIn(): Boolean {
        // If we are already authenticated, no need to do anything
        if (_viewState.value.isAuthenticated) return true

        try {
            _viewState.update { it.copy(status = AppStatus.SIGNING_IN) }

            // This blocks until the user signs in or cancels
            val userProfile = authService.signIn()

            if (userProfile != null) {
                _viewState.update { it.copy(userProfile = userProfile) }
                return true
            }
        } catch (e: CancellationException) {
            log.d { "User cancelled sign in: ${e.message}" }
            throw e // Re-throw to ensure the calling Job is cancelled
        } catch (e: AuthException) {
            _viewState.update { it.copy(globalErrorMessage = e.uiText) }
        } catch (e: Exception) {
            log.e(e) { "Sign in failed" }
            val uiText = if (e.message == null) {
                UiTextResource(Res.string.error_unknown)
            } else {
                UiTextString(e.message!!)
            }
            _viewState.update { it.copy(globalErrorMessage = uiText) }
        } finally {
            _viewState.update { it.copy(status = AppStatus.IDLE) }
        }

        return false
    }

    /**
     * Cancels the current sign-in or upload job if it's still running.
     */
    fun cancelProcess() {
        // This will cancel the job running performSignIn(), triggering the CancellationException there
        // The finally block in performSignIn() will handle the state update
        log.d { "Cancel process" }
        processJob?.cancel()
        processJob = null
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
            _viewState.update { it.copy(userProfile = null) }
        }
    }

    fun clearGlobalErrorMessage() {
        _viewState.update { it.copy(globalErrorMessage = null) }
    }

    fun updatePath(kmpFile: KmpFile) {
        val path = kmpFile.getAbsolutePath(platformContext!!)
        _viewState.update { it.copy(kmpFile = kmpFile, path = path ?: "") }
        log.d { "Setting path to '$path'" }
        repository.setPath(kmpFile, platformContext!!)
        reload()
    }

    fun updateShowDirPicker(isShowing: Boolean) {
        _viewState.update {
            it.copy(status = if (isShowing) AppStatus.CHOOSING_DIRECTORY else AppStatus.IDLE)
        }
    }

    fun toggleAlbum(albumId: String) {
        updateAlbum(albumId) { album -> album.copy(isEnabled = !album.isEnabled) }
    }

    fun toggleAlbums(albums: List<Album>, isEnabled: Boolean) {
        val currentAlbums = repository.albums.value
        val albumIdsToUpdate = albums.map { it.id }.toSet()
        val updatedAlbums = currentAlbums.map { album ->
            if (album.id in albumIdsToUpdate) {
                album.copy(isEnabled = isEnabled)
            } else {
                album
            }
        }
        repository.updateAlbums(updatedAlbums)
    }

    fun togglePhoto(albumId: String, photoPath: Path) {
        updatePhoto(albumId, photoPath) { photo -> photo.copy(isEnabled = !photo.isEnabled) }
    }

    fun renameAlbum(albumId: String, newName: String) {
        updateAlbum(albumId) { it.copy(name = newName) }
    }

    fun renamePhoto(albumId: String, photoPath: Path, newName: String) {
        updatePhoto(albumId, photoPath) { it.copy(name = newName) }
    }

    fun updateCoverPhoto(albumId: String, coverPhoto: Photo) {
        updateAlbum(albumId) { album ->
            val coverPhotoPath = coverPhoto.path
            val updatedPhotos = album.photos.map { photo ->
                when {
                    photo.path == coverPhotoPath -> photo.copy(isCoverPhoto = true)
                    photo.isCoverPhoto -> photo.copy(isCoverPhoto = false)
                    else -> photo
                }
            }
            album.copy(photos = updatedPhotos, coverPhoto = coverPhoto)
        }
    }


    /**
     * Uploads photos based on the current UI state to Google Photos.
     */
    fun uploadPhotos(): Job? {
        val state = uiState.value
        if (state.albums.isNotEmpty() && state.idle()) {
            val job = viewModelScope.launch {
                try {
                    // Wait for sign-in to complete
                    val isAuthSuccess = performSignIn()

                    // Only proceed if authenticated
                    if (isAuthSuccess) {
                        // Start the actual upload
                        _viewState.update { it.copy(status = AppStatus.UPLOADING) }
                        uploadPhotosImpl(state.albums)
                    }
                } catch (e: UploadException.GlobalException) {
                    resetNonFinalUploadStatuses()
                    if (e.status == HttpStatusCode.Unauthorized
                        || e.uiText.toString()
                            .contains(other = "UNAUTHENTICATED", ignoreCase = true)
                    ) {
                        handleAuthExpiry()
                    } else {
                        _viewState.update { it.copy(globalErrorMessage = e.uiText) }
                    }
                } catch (e: AuthException) {
                    resetNonFinalUploadStatuses()
                    _viewState.update { it.copy(globalErrorMessage = e.uiText) }
                } catch (e: CancellationException) {
                    // Job was cancelled (e.g. via cancelProcess), stop gracefully
                    log.d { "Upload process cancelled: ${e.message}" }
                    // When uploading, reset statuses to remove "Uploading" indicators
                    if (_viewState.value.status == AppStatus.UPLOADING) {
                        resetNonFinalUploadStatuses()
                    }
                } catch (e: Exception) {
                    log.e(e) { "Upload failed" }
                    resetNonFinalUploadStatuses()

                    val uiText = if (e.message == null) {
                        UiTextResource(Res.string.error_unknown)
                    } else {
                        UiTextString(e.message!!)
                    }
                    _viewState.update { it.copy(globalErrorMessage = uiText) }
                } finally {
                    _viewState.update { it.copy(status = AppStatus.IDLE) }
                    // Ensure processJob is cleared if this specific job finishes
                    if (processJob == coroutineContext[Job]) {
                        processJob = null
                    }
                }
            }

            // Assign this job to processJob.
            // If the user clicks "Cancel" in the AppBar dialog while isSigningIn or isUploading is
            // true, cancelProcess() will cancel THIS job, stopping the upload flow immediately.
            processJob = job

            return job
        }
        return null
    }

    private fun resetNonFinalUploadStatuses() {
        val albumsWithResetStatus = repository.albums.value.map { currentAlbum ->
            if (currentAlbum.isEnabled && !currentAlbum.uploadStatus.isFinal) {
                val updatedPhotos = currentAlbum.photos.map { p ->
                    if (p.isEnabled && !p.uploadStatus.isFinal) p.copy(uploadStatus = UploadStatus.None) else p
                }
                currentAlbum.copy(
                    uploadStatus = UploadStatus.None,
                    photos = updatedPhotos
                )
            } else {
                currentAlbum
            }
        }
        repository.updateAlbums(albumsWithResetStatus)
    }

    /**
     * Handles 401 errors by signing out locally so the next attempt forces a fresh login.
     */
    private suspend fun handleAuthExpiry() {
        // Clear the invalid token from the local cache/file
        authService.signOut()

        // Update UI to reflect we are not authenticated
        _viewState.update {
            it.copy(
                userProfile = null,
                // Show a helpful message: "Session expired. Please click Upload to sign in again."
                globalErrorMessage = UiTextResource(
                    Res.string.error_sign_in_failed,
                    Res.string.session_expired
                )
            )
        }
    }

    /** Signs in to obtain an access token and starts uploading the photo's from the albums list
     * @param albums List of albums to process
     */
    private suspend fun uploadPhotosImpl(albums: List<Album>) {
        val context = platformContext
            ?: throw IllegalStateException("Platform context not set")
        val userProfile = _viewState.value.userProfile
            ?: throw IllegalStateException("User profile not set")

        log.d { "Starting upload process" }

        val photoUploader = PhotoUploader(userProfile.accessToken, context)
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

        log.d { "Starting upload for ${albumsToUpload.size} albums" }

        for (album in albumsToUpload) {
            uploadPhotosToNewAlbum(album, photoUploader)
        }

        log.d { "Upload process completed!" }
    }

    private suspend fun uploadPhotosToNewAlbum(album: Album, photoUploader: PhotoUploader) {
        // Notify the user that this album starts uploading
        updateAlbumStatus(album.id, UploadStatus.Uploading)
        val googleAlbumId = getOrCreateGoogleAlbum(album, photoUploader) ?: return
        val uploadedItems: List<Pair<Photo, String>> = try {
            uploadPhotosInAlbum(album, photoUploader)
        } catch (e: GracefulCancellationException) {
            // Create media items for the uploaded bytes to make them appear on Google Photos.
            // Use an independent thread since a child thread already has status Cancelling which
            // will immediately cancel any network requests.
            viewModelScope.launch {
                createMediaItemsForUpload(
                    album = album,
                    googleAlbumId = googleAlbumId,
                    uploadedItems = e.successfullyUploaded,
                    isCancelled = true,
                    photoUploader = photoUploader
                )

            }
            // Re-throw the exception to ensure the main loop in `uploadPhotosImpl` stops and
            // doesn't proceed to the next album.
            throw CancellationException("Upload process started finalizing partial uploads.")
        }
        createMediaItemsForUpload(album, googleAlbumId, uploadedItems, false, photoUploader)
    }

    private suspend fun createMediaItemsForUpload(
        album: Album,
        googleAlbumId: String,
        uploadedItems: List<Pair<Photo, String>>,
        isCancelled: Boolean,
        photoUploader: PhotoUploader,
    ) {
        // If no photos were successfully uploaded (e.g. all failed), the only thing to do is set
        // the album to a final status.
        if (uploadedItems.isEmpty()) {
            log.d { "    No photos uploaded successfully for album ${album.name}" }
            updateAlbumStatus(
                album.id,
                album.getDerivedUploadStatus(if (isCancelled) UploadStatus.Cancelled else UploadStatus.Success)
            )
            // If cancelled, rethrow to stop the entire upload process
            if (isCancelled) throw CancellationException("Upload process cancelled by user.")
            return
        }

        addMediaItemsToAlbum(album, googleAlbumId, uploadedItems, photoUploader)

        // Use the updated album to set the cover photo to the media item id added earlier
        val finalAlbum = repository.albums.value.find { it.id == album.id }!!
        setAlbumCover(finalAlbum, googleAlbumId, photoUploader)

        // Mark the album as final
        updateAlbumStatus(
            album.id,
            finalAlbum.getDerivedUploadStatus(if (isCancelled) UploadStatus.Cancelled else UploadStatus.Success)
        )
    }

    private suspend fun getOrCreateGoogleAlbum(album: Album, photoUploader: PhotoUploader): String? {
        // Check if an albumId was already created
        if (album.albumId != null) {
            val albumId = album.albumId
            try {
                // Verify if the album ID is still valid on the server
                if (photoUploader.verifyAlbumExists(albumId)) {
                    log.d { "Album '${album.name}' already exists on Google Photos. Re-using it." }
                    return albumId
                } else {
                    log.d { "Album ID for '${album.name}' is stale. It will be recreated." }
                    // Clear the invalid ID from our local state before creating a new one.
                    updateAlbum(album.id) { currentAlbum -> currentAlbum.copy(albumId = null) }
                }
            } catch (e: UploadException.AlbumException) {
                log.e(e) { "Error verifying album '${album.name}'" }
                updateAlbumStatus(album.id, UploadStatus.Error(e.uiText))
                return null
            }
        }

        // Create a new album.
        return try {
            log.d { "Creating new album on Google Photos for '${album.name}'." }
            val googleAlbumId = photoUploader.createAlbum(album.name)
            updateAlbum(album.id) { it.copy(albumId = googleAlbumId) }
            log.d { "    Created album with ID: $googleAlbumId for '${album.name}'" }
            googleAlbumId
        } catch (e: UploadException.AlbumException) {
            log.e(e) { "    Failed to create album for '${album.name}'" }
            updateAlbumStatus(album.id, UploadStatus.Error(e.uiText))
            null
        }
    }

    private suspend fun uploadPhotosInAlbum(
        album: Album,
        photoUploader: PhotoUploader,
    ): List<Pair<Photo, String>> {
        val photosToUpload = album.photos.filter { it.isEnabled }
        val successfullyUploaded = mutableListOf<Pair<Photo, String>>()

        try {
            for ((index, photo) in photosToUpload.withIndex()) {
                try {
                    updatePhotoStatus(album.id, photo.path, UploadStatus.Uploading)
                    log.d { "    Uploading photo ${index + 1}/${photosToUpload.size}: ${photo.name}" }
                    val uploadToken = photoUploader.uploadPhoto(photo)
                    successfullyUploaded.add(photo to uploadToken)
                    updatePhotoStatus(album.id, photo.path, UploadStatus.Success)
                    log.d { "      Success: ${photo.name}" }
                } catch (e: UploadException.PhotoException) {
                    log.e(e) { "      ERROR: Failed to upload '${photo.name}'" }
                    updatePhotoStatus(album.id, photo.path, UploadStatus.Error(e.uiText))
                }
            }
        } catch (e: CancellationException) {
            // The loop was cancelled. Throw our custom exception with the partial results
            log.d { "Caught cancellation exception: ${e.message}" }
            log.d { "Gracefully cancelling photo upload loop for album '${album.name}'. ${successfullyUploaded.size} photos were completed." }
            throw GracefulCancellationException(successfullyUploaded)
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
        try {
            val results = photoUploader.addPhotosToAlbum(googleAlbumId, newMediaItems)
            updateAlbum(album.id) { currentAlbum ->
                val updatedPhotos = updatePhotoWithResult(currentAlbum, uploadedItems, results)
                val newCoverPhoto =
                        updatedPhotos.find { it.path == currentAlbum.coverPhoto.path }?.copy(
                            mediaItemId = updatedPhotos.find { it.path == currentAlbum.coverPhoto.path }?.mediaItemId
                        ) ?: currentAlbum.coverPhoto

                currentAlbum.copy(photos = updatedPhotos, coverPhoto = newCoverPhoto)
            }
        } catch (e: UploadException.PhotoException) {
            log.e(e) { "      ERROR: Failed to add media items to album '${album.name}'" }
            updateAlbum(album.id) { currentAlbum ->
                // Mark all photos that were part of this upload attempt as failed
                val updatedPhotos = currentAlbum.photos.map { p ->
                    if (uploadedItems.any { it.first.path == p.path }) {
                        p.copy(uploadStatus = UploadStatus.Error(e.uiText))
                    } else {
                        p
                    }
                }
                currentAlbum.copy(photos = updatedPhotos)
            }
        }
    }

    private fun updatePhotoWithResult(
        currentAlbum: Album,
        uploadedItems: List<Pair<Photo, String>>,
        results: List<MediaItemResult>,
    ): List<Photo> {
        return currentAlbum.photos.map { p ->
            val index = uploadedItems.indexOfFirst { it.first.path == p.path }
            if (index != -1 && index < results.size) {
                val mediaResult = results[index]
                if (mediaResult.isSuccess() && mediaResult.mediaItem != null) {
                    p.copy(mediaItemId = mediaResult.mediaItem.id)
                } else {
                    log.e { "      ERROR: Failed to add '${p.name}' to album '${currentAlbum.name}'" }
                    val errorString = mediaResult.status.toString()
                    val errorMessage = if (errorString.isEmpty()) {
                        UiTextResource(Res.string.error_unknown)
                    } else {
                        UiTextString(errorString)
                    }
                    p.copy(
                        uploadStatus = UploadStatus.Error(
                            UiTextResource(
                                Res.string.error_add_to_album_failed,
                                errorMessage
                            )
                        )
                    )
                }
            } else {
                p
            }
        }
    }

    private suspend fun setAlbumCover(
        album: Album,
        googleAlbumId: String,
        photoUploader: PhotoUploader,
    ) {
        val coverMediaItemId = album.coverPhoto.mediaItemId
        if (coverMediaItemId != null) {
            log.d { "    Setting cover photo to: ${album.coverPhoto.name}" }
            try {
                photoUploader.updateAlbumCover(googleAlbumId, coverMediaItemId)
            } catch (e: UploadException) {
                log.e(e) { "    Failed to update cover photo" }
            }
        }
    }

    private fun updateAlbumStatus(albumId: String, status: UploadStatus) {
        updateAlbum(albumId) {
            it.copy(
                uploadStatus = status,
                isEnabled = it.isEnabled && status != UploadStatus.Success
            )
        }
    }

    private fun updatePhotoStatus(albumId: String, photoPath: Path, status: UploadStatus) {
        updatePhoto(albumId, photoPath) {
            it.copy(
                uploadStatus = status,
                isEnabled = it.isEnabled && status != UploadStatus.Success
            )
        }
    }

    private fun updateAlbum(albumId: String, transform: (Album) -> Album) {
        val currentAlbums = repository.albums.value
        val updatedAlbums = currentAlbums.map { album ->
            if (album.id == albumId) {
                transform(album)
            } else {
                album
            }
        }
        repository.updateAlbums(updatedAlbums)
    }

    private fun updatePhoto(albumId: String, photoPath: Path, transform: (Photo) -> Photo) {
        updateAlbum(albumId) { album ->
            var isPhotoStatusUpdated = false
            val updatedPhotos = album.photos.map { photo ->
                if (photo.path == photoPath) {
                    val updatedPhoto = transform(photo)
                    isPhotoStatusUpdated = (updatedPhoto.uploadStatus != photo.uploadStatus)
                    updatedPhoto
                } else {
                    photo
                }
            }
            album.copy(
                photos = updatedPhotos,
                uploadStatus = if (isPhotoStatusUpdated) album.getDerivedUploadStatus(album.uploadStatus) else album.uploadStatus
            )
        }
    }
}

/**
 * Custom exception to gracefully handle cancellation and carry the list of successfully uploaded photos.
 */
private class GracefulCancellationException(
    val successfullyUploaded: List<Pair<Photo, String>>,
) : CancellationException("Upload gracefully cancelled by user.")

enum class AppStatus {
    IDLE,
    CHOOSING_DIRECTORY,
    SIGNING_IN,
    UPLOADING
}

data class ViewState(
    val userProfile: UserProfile? = null,
    val status: AppStatus = AppStatus.IDLE,
    private val kmpFile: KmpFile? = null,
    val path: String = "",
    val globalErrorMessage: UiText? = null,
) {
    val isAuthenticated = userProfile != null
}

data class UiState(
    val viewState: ViewState = ViewState(),
    val albums: List<Album> = emptyList(),
) {
    val userProfile: UserProfile? get() = viewState.userProfile
    val isAuthenticated: Boolean get() = viewState.isAuthenticated
    val isShowDirPicker: Boolean get() = viewState.status == AppStatus.CHOOSING_DIRECTORY
    val isSigningIn: Boolean get() = viewState.status == AppStatus.SIGNING_IN
    val isUploading: Boolean get() = viewState.status == AppStatus.UPLOADING
    val path: String get() = viewState.path
    val globalErrorMessage: UiText? get() = viewState.globalErrorMessage

    fun busy() = viewState.status != AppStatus.IDLE
    fun idle() = viewState.status == AppStatus.IDLE

    override fun toString(): String {
        return "UiState(userProfile=$userProfile, " +
                "isAuthenticated=$isAuthenticated, " +
                "isShowDirPicker=$isShowDirPicker, " +
                "isSigningIn=$isSigningIn, " +
                "isUploading=$isUploading, " +
                "path='$path', " +
                "globalErrorMessage=$globalErrorMessage, " +
                "album size=${albums.size})"
    }
}
