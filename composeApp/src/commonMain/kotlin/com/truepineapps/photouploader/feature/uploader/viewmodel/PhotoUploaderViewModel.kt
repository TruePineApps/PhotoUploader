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

package com.truepineapps.photouploader.feature.uploader.viewmodel

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.mohamedrejeb.calf.core.PlatformContext
import com.mohamedrejeb.calf.io.KmpFile
import com.truepineapps.photouploader.core.io.getAbsolutePath
import com.truepineapps.photouploader.core.presentation.base.LoadingViewModel
import com.truepineapps.photouploader.core.util.UiText
import com.truepineapps.photouploader.core.util.UiTextResource
import com.truepineapps.photouploader.core.util.UiTextString
import com.truepineapps.photouploader.feature.uploader.data.dto.MediaItemResult
import com.truepineapps.photouploader.feature.uploader.data.dto.NewMediaItem
import com.truepineapps.photouploader.feature.uploader.data.dto.SimpleMediaItem
import com.truepineapps.photouploader.feature.uploader.domain.repository.PhotoDirectoryRepository
import com.truepineapps.photouploader.feature.uploader.domain.repository.PhotoUploader
import com.truepineapps.photouploader.feature.uploader.domain.repository.UploadException
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.AlbumUiState
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.AppStatus
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.GroupUiState
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.PhotoUiState
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UiState
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UploadCompletionStatus
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UploadError
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UploadReport
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UploadStatus
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.ViewState
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.toAlbumUiState
import com.truepineapps.photouploader.foundation.auth.domain.model.AuthException
import com.truepineapps.photouploader.foundation.auth.domain.repository.GoogleAuthService
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.app_busy_upload_not_possible
import com.truepineapps.photouploader.resources.error_add_to_album_failed
import com.truepineapps.photouploader.resources.error_sign_in_failed
import com.truepineapps.photouploader.resources.error_unknown
import com.truepineapps.photouploader.resources.photo_folders
import com.truepineapps.photouploader.resources.select_photos_before_uploading
import com.truepineapps.photouploader.resources.session_expired
import com.truepineapps.photouploader.resources.sign_in_before_uploading
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okio.Path
import kotlin.time.Duration.Companion.milliseconds

class PhotoUploaderViewModel(
    private val authService: GoogleAuthService,
    private val photoUploader: PhotoUploader,
    private val repository: PhotoDirectoryRepository,
    private val log: Logger,
) : LoadingViewModel(repository) {

    private var processJob: Job? = null
    private val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _viewState = MutableStateFlow(ViewState())
    private val _albumUiStates = MutableStateFlow(emptyList<AlbumUiState>())
    private val _groupUiStates = MutableStateFlow(emptyList<GroupUiState>())

    val uiState: StateFlow<UiState> = combine(
        _viewState, _albumUiStates, _groupUiStates
    ) { viewState, albumUiStates, groupUiStates ->
        log.d("Updating uiState, album size: ${albumUiStates.size}, group size: ${groupUiStates.size}")

        // Reconstruct GroupUiStates to always contain the latest AlbumUiState list
        // and merge with the stored group-specific properties (isExpanded, isEnabled).
        val groupedAlbumsMap = albumUiStates.groupBy { it.group }
        val currentGroupUiStates = groupedAlbumsMap
            .map { (groupName, currentAlbumsInGroup) ->
                val existingGroupState = groupUiStates.find { it.group == groupName }
                existingGroupState?.copyWithDerivedStatus(
                    group = groupName,
                    albumsInGroup = currentAlbumsInGroup
                ) ?: GroupUiState(
                    group = groupName,
                    albumsInGroup = currentAlbumsInGroup
                )
            }.sortedBy { it.group } // Ensure stable order for display

        UiState(
            viewState,
            albumUiStates,
            currentGroupUiStates
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = UiState()
    )

    init {
        checkInitialAuth()

        // Observe the repository and map the data to UI state
        repository.albums
            .onEach { albumsData ->
                log.d("Viewmodel updating albums, size: ${albumsData.size}")
                val newAlbumUiStates = albumsData.map { it.toAlbumUiState() }
                _albumUiStates.update { newAlbumUiStates }

                // When new repository data comes in, the user selected a different folder, so the
                // group settings no longer apply. Updating the albums is done when creating the UiState
                val newGroupNames = newAlbumUiStates.map { it.group }.distinct()
                _groupUiStates.update {
                    newGroupNames
                        .map { group -> GroupUiState(group) }
                        .sortedBy { it.group }
                }
            }
            .launchIn(viewModelScope)
    }

    override fun getDisplayNameText(): UiText = UiTextResource(Res.string.photo_folders)

    private fun checkInitialAuth() {
        viewModelScope.launch {
            try {
                val userProfile = authService.restoreSignIn()
                if (userProfile != null) {
                    _viewState.update { it.copy(userProfile = userProfile) }
                }
            } catch (e: Exception) {
                // Ignore errors during initial restore
                log.e(e) { "checkInitialAuth caught exception: ${e.message}" }
            }
        }
    }

    /**
     * Public entry point for the manual Sign In button (if any).
     */
    fun signIn() {
        if (_viewState.value.status == AppStatus.SIGNING_IN) return
        viewModelScope.launch { performSignIn() }
    }

    /**
     * Ensures the user is authenticated. If not already signed in, it initiates
     * the sign-in process and waits for it to complete.
     * @return true if authenticated, false otherwise.
     */
    suspend fun ensureAuthenticated(): Boolean {
        if (_viewState.value.isAuthenticated) return true

        return try {
            // Wait for existing sign-in or start a new one
            if (_viewState.value.status == AppStatus.SIGNING_IN) {
                processJob?.join()
            } else {
                performSignIn()
            }
            _viewState.value.isAuthenticated
        } catch (e: Exception) {
            log.e("ensureAuthenticated failed", e)
            if (e !is CancellationException) {
                val uiText = e.message?.let { UiTextString(it) } ?: UiTextResource(Res.string.error_unknown)
                _viewState.update { it.copy(globalErrorMessage = uiText) }
            }
            false
        }
    }

    /**
     * Centralized suspend function that handles the sign-in flow, error handling,
     * and UI state updates.
     * @return true if the user is successfully authenticated, false otherwise.
     */
    private suspend fun performSignIn(): Boolean {
        // If already authenticated, no need to do anything
        if (_viewState.value.isAuthenticated) return true
        // If there is already a processJob, it can't be assigned
        if (processJob != null) return false

        try {
            _viewState.update { it.copy(status = AppStatus.SIGNING_IN) }
            processJob = currentCoroutineContext()[Job]

            // This blocks until the user signs in or cancels
            val userProfile = authService.signIn()

            if (userProfile != null) {
                log.d("sign in successful")
                _viewState.update { it.copy(userProfile = userProfile) }
                return true
            }
        } catch (e: CancellationException) {
            log.d("performSignIn: User canceled sign in: ${e.message}")
            throw e // Re-throw to ensure the calling Job is canceled
        } catch (e: AuthException.NetworkError) {
            log.d("performSignIn: AuthException ${e::class.simpleName}: ${e.message}")
            // We keep the credential, if any
            _viewState.update { it.copy(globalErrorMessage = e.uiText) }
        } catch (e: AuthException) {
            log.d("performSignIn: AuthException ${e::class.simpleName}: ${e.message}")
            // Token expired or general sign-in failure: Remove credential
            handleAuthExpiry()
        } catch (e: Exception) {
            log.e("performSignIn: Sign in failed", e)
            val uiText = e.message?.let { UiTextString(it) } ?: UiTextResource(Res.string.error_unknown)
            _viewState.update { it.copy(globalErrorMessage = uiText) }
        } finally {
            log.d("Set viewState to Idle after sign-in")
            _viewState.update { it.copy(status = AppStatus.IDLE) }
            if (processJob == currentCoroutineContext()[Job]) {
                processJob = null
            }
        }

        return false
    }

    /**
     * Cancels the current sign-in or upload job if it's still running.
     */
    fun cancelProcess() {
        // This will cancel the job running performSignIn(), triggering the CancellationException there
        // The finally-block in performSignIn() will handle the state update
        log.d("cancelProcess: Cancel process")
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

    fun clearUploadReport() {
        _viewState.update { it.copy(uploadReport = null) }
    }

    private fun generateUploadReport(isCancelled: Boolean): UploadReport {
        val allEnabledAlbums = _albumUiStates.value.filter { it.isEnabled }
        val albumsFailed = allEnabledAlbums.count { it.uploadStatus is UploadStatus.Error }
        val albumsSkipped =
            allEnabledAlbums.count { it.uploadStatus == UploadStatus.Cancelled || !it.uploadStatus.isFinal }

        val allEnabledPhotos =
            allEnabledAlbums.flatMap { it.photoUiStates.filter { p -> p.isEnabled } }
        val photosUploaded = allEnabledPhotos.count { it.uploadStatus == UploadStatus.Success }
        val photosFailed = allEnabledPhotos.count { it.uploadStatus is UploadStatus.Error }
        val photosSkipped =
            allEnabledPhotos.count { it.uploadStatus == UploadStatus.Cancelled || !it.uploadStatus.isFinal }

        val status = when {
            isCancelled -> UploadCompletionStatus.CANCELLED
            albumsFailed > 0 || photosFailed > 0 -> UploadCompletionStatus.ERRORS
            else -> UploadCompletionStatus.SUCCESS
        }

        return UploadReport(
            albumsCreated = allEnabledAlbums.count { it.googleAlbumId != null },
            albumsSkipped = albumsSkipped,
            albumsFailed = albumsFailed,
            photosUploaded = photosUploaded,
            photosSkipped = photosSkipped,
            photosFailed = photosFailed,
            errors = allEnabledPhotos.filter { it.uploadStatus is UploadStatus.Error }.map {
                UploadError(
                    it.name, (it.uploadStatus as UploadStatus.Error).message.toString()
                )
            },
            status = status
        )
    }

    fun updatePath(kmpFile: KmpFile, platformContext: PlatformContext) {
        val path = kmpFile.getAbsolutePath(platformContext)
        _viewState.update { it.copy(kmpFile = kmpFile, path = path ?: "") }
        log.d("updatePath: Setting path to '$path'")
        repository.setPath(kmpFile, platformContext)
        // Trigger the repository.albums flow to reload, causing the state to remap
        reload()
    }

    fun updateShowDirPicker(isShowing: Boolean) {
        _viewState.update {
            it.copy(status = if (isShowing) AppStatus.CHOOSING_DIRECTORY else AppStatus.IDLE)
        }
    }

    fun updateSelectedAlbum(id: String) {
        _viewState.update { it.copy(selectedAlbumId = id) }
    }

    fun toggleAlbum(albumId: String) {
        updateAlbum(albumId) { album -> album.copy(isEnabled = !album.isEnabled) }
    }

    fun toggleGroupExpanded(groupUiState: GroupUiState) {
        _groupUiStates.update { currentGroupStates ->
            currentGroupStates.map {
                if (it.group == groupUiState.group) it.copy(isExpanded = !it.isExpanded) else it
            }
        }
    }

    fun toggleGroup(groupUiState: GroupUiState, isEnabled: Boolean) {
        _groupUiStates.update { currentGroupStates ->
            currentGroupStates.map {
                if (it.group == groupUiState.group) it.copy(isEnabled = isEnabled) else it
            }
        }
        toggleAlbums(groupUiState.albumsInGroup, isEnabled)
    }

    fun toggleAlbums(albumUiStates: List<AlbumUiState>, isEnabled: Boolean) {
        val albumIdsToUpdate = albumUiStates.map { it.id }.toSet()
        var isUpdated = false
        _albumUiStates.update { currentAlbums ->
            currentAlbums.map { album ->
                if (album.id in albumIdsToUpdate) {
                    isUpdated = true
                    album.copy(isEnabled = isEnabled)
                } else {
                    album
                }
            }
        }
        log.d("toggleAlbums updated: $isUpdated")
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

    fun updateCoverPhoto(albumId: String, coverPhotoUiState: PhotoUiState) {
        updateAlbum(albumId) { album ->
            val coverPhotoPath = coverPhotoUiState.path
            val updatedPhotos = album.photoUiStates.map { photo ->
                when {
                    photo.path == coverPhotoPath -> photo.copy(isCoverPhoto = true)
                    photo.isCoverPhoto -> photo.copy(isCoverPhoto = false)
                    else -> photo
                }
            }
            album.copy(photoUiStates = updatedPhotos, coverPhotoUiState = coverPhotoUiState)
        }
    }

    /**
     * Uploads photos based on the current UI state to Google Photos without re-authenticating.
     * No upload starts if the user is not authenticated, there are no photos, or the app is not idle.
     *
     * @return The job that performs the upload, null if no job is created.
     */
    fun uploadPhotos(platformContext: PlatformContext): Job? {
        val state = uiState.value
        // If not authenticated, no need to do anything
        if (!state.viewState.isAuthenticated) {
            log.d("uploadPhotos: Not authenticated")
            _viewState.update { it.copy(globalErrorMessage = UiTextResource(Res.string.sign_in_before_uploading)) }
            return null
        }

        val isIdle = state.idle()
        val hasPhotos =
            state.albumUiStates.any { it.isEnabled && it.photoUiStates.any { p -> p.isEnabled } }
        var isCancelled = false
        if (hasPhotos && isIdle) {
            val job = viewModelScope.launch {
                try {
                    _viewState.update { it.copy(status = AppStatus.UPLOADING) }
                    uploadPhotosImpl(state.albumUiStates, platformContext)
                } catch (e: UploadException.GlobalException) {
                    log.d("Upload process caught global exception: ${e.message} (${e.status})")
                    resetNonFinalUploadStatuses()
                    if (e.status == HttpStatusCode.Unauthorized
                        || e.uiText.toString()
                            .contains(other = "UNAUTHENTICATED", ignoreCase = true)
                    ) {
                        handleAuthExpiry()
                    } else {
                        _viewState.update { it.copy(globalErrorMessage = e.uiText) }
                    }
                } catch (e: CancellationException) {
                    // Job was canceled (e.g., via cancelProcess), stop gracefully
                    log.d("Upload process canceled: ${e.message}")
                    isCancelled = true
                    // Wait for background thread to finalize already uploaded photos using a timeout
                    withContext<Unit>(NonCancellable) {
                        withTimeoutOrNull(5000L.milliseconds) {
                            cleanupScope.coroutineContext[Job]?.children?.forEach { it.join() }
                        }
                    }
                    // When uploading, reset statuses to remove "Uploading" indicators
                    if (_viewState.value.status == AppStatus.UPLOADING) {
                        resetNonFinalUploadStatuses()
                    }
                } catch (e: Exception) {
                    log.e("uploadPhotos: Upload failed", e)
                    resetNonFinalUploadStatuses()

                    val uiText = if (e.message == null) {
                        UiTextResource(Res.string.error_unknown)
                    } else {
                        UiTextString(e.message!!)
                    }
                    _viewState.update { it.copy(globalErrorMessage = uiText) }
                } finally {
                    val report = generateUploadReport(isCancelled)
                    disableSuccessfulUploads()
                    _viewState.update { it.copy(status = AppStatus.IDLE, uploadReport = report) }

                    // Ensure processJob is cleared if this specific job finishes
                    if (processJob == coroutineContext[Job]) {
                        processJob = null
                    }
                }
            }
            // Assign this job to processJob.
            // If the user clicks "Cancel" in the AppBar dialog while isSigningIn or isUploading is
            // true, cancelProcess() will cancel THIS job, stopping the upload flow immediately.
            // Finishing photos in Uploading status is done in a separate thread.
            processJob = job
            return job
        } else if (!isIdle) {
            _viewState.update { it.copy(globalErrorMessage = UiTextResource(Res.string.app_busy_upload_not_possible)) }
        } else {
            _viewState.update { it.copy(globalErrorMessage = UiTextResource(Res.string.select_photos_before_uploading)) }
        }
        log.d("No upload, albumUiStates.isEmpty() = ${state.albumUiStates.isEmpty()} and isIdle = $isIdle")
        return null
    }

    private fun resetNonFinalUploadStatuses() {
        try {
            log.d("resetNonFinalUploadStatuses called")
            _albumUiStates.update { currentAlbumStates ->
                currentAlbumStates.map { currentAlbumState ->
                    if (currentAlbumState.isEnabled
                        && (!currentAlbumState.uploadStatus.isFinal
                                || currentAlbumState.uploadStatus == UploadStatus.Cancelled)
                    ) {
                        val updatedPhotos = currentAlbumState.photoUiStates.map { p ->
                            if (p.isEnabled && !p.uploadStatus.isFinal) p.copy(uploadStatus = UploadStatus.None) else p
                        }
                        currentAlbumState.copy(
                            uploadStatus = if (currentAlbumState.uploadStatus == UploadStatus.Cancelled) {
                                UploadStatus.Cancelled
                            } else {
                                UploadStatus.None
                            },
                            photoUiStates = updatedPhotos
                        )
                    } else {
                        currentAlbumState
                    }
                }
            }
        } catch (e: Exception) {
            log.e(e) { "resetNonFinalUploadStatuses failed: ${e.message}" }
            // Do not override an earlier message with this one
            if (_viewState.value.globalErrorMessage == null) {
                val uiText = if (e.message == null) {
                    UiTextResource(Res.string.error_unknown)
                } else {
                    UiTextString(e.message!!)
                }
                _viewState.update { it.copy(globalErrorMessage = uiText) }
            }
        }
    }

    private fun disableSuccessfulUploads() {
        try {
            _albumUiStates.update { currentAlbumStates ->
                currentAlbumStates.map { currentAlbumState ->
                    if (currentAlbumState.isEnabled && currentAlbumState.uploadStatus == UploadStatus.Success) {
                        val updatedPhotos = currentAlbumState.photoUiStates.map { p ->
                            if (p.isEnabled && p.uploadStatus == UploadStatus.Success) p.copy(
                                isEnabled = false
                            ) else p
                        }
                        currentAlbumState.copy(
                            isEnabled = false,
                            photoUiStates = updatedPhotos
                        )
                    } else {
                        currentAlbumState
                    }
                }
            }
        } catch (e: Exception) {
            log.e(e) { "disableSuccessfulUploads failed: ${e.message}" }
            // Do not override an earlier message with this one
            if (_viewState.value.globalErrorMessage == null) {
                val uiText = if (e.message == null) {
                    UiTextResource(Res.string.error_unknown)
                } else {
                    UiTextString(e.message!!)
                }
                _viewState.update { it.copy(globalErrorMessage = uiText) }
            }
        }
    }

    /**
     * Handles 401 errors by signing out locally, so the next attempt forces a fresh login.
     */
    private suspend fun handleAuthExpiry() {
        log.d("handleAuthExpiry: Signing out")

        // Clear the invalid token from the local cache/file
        authService.signOut()

        // Update UI to reflect we are not authenticated
        _viewState.update {
            it.copy(
                userProfile = null,
                // Show a helpful message: "Sign in failed: Session expired. Please click Upload to sign in again."
                globalErrorMessage = UiTextResource(
                    Res.string.error_sign_in_failed,
                    Res.string.session_expired
                )
            )
        }
    }

    /** Signs in to get an access token and starts uploading the photo's from the album list
     * @param albumUiStates List of albums to process
     */
    private suspend fun uploadPhotosImpl(
        albumUiStates: List<AlbumUiState>,
        platformContext: PlatformContext
    ) {
        val userProfile = _viewState.value.userProfile
            ?: throw IllegalStateException("User profile not set")

        log.d("uploadPhotosImpl: Starting upload process")

        val albumsToUpload =
            albumUiStates.filter { it.isEnabled && it.photoUiStates.any { p -> p.isEnabled } }

        // Set initial 'Waiting' status on all items to be uploaded
        setWaitingStatus(albumsToUpload)

        log.d("uploadPhotosImpl: Starting upload for ${albumsToUpload.size} albums")

        for (album in albumsToUpload) {
            uploadPhotosToNewAlbum(album, userProfile.accessToken, platformContext)
        }

        log.d("uploadPhotosImpl: Upload process completed!")
    }

    internal fun setWaitingStatus(albumsToUpload: List<AlbumUiState>) {
        _albumUiStates.update { currentAlbumStates ->
            currentAlbumStates.map { currentAlbumState ->
                if (albumsToUpload.any { it.id == currentAlbumState.id }) {
                    val updatedPhotos = currentAlbumState.photoUiStates.map { p ->
                        if (p.isEnabled) p.copy(uploadStatus = UploadStatus.Waiting) else p
                    }
                    currentAlbumState.copy(
                        uploadStatus = UploadStatus.Waiting,
                        photoUiStates = updatedPhotos
                    )
                } else {
                    currentAlbumState
                }
            }
        }
    }

    private suspend fun uploadPhotosToNewAlbum(
        albumUiState: AlbumUiState,
        accessToken: String,
        platformContext: PlatformContext,
    ) {
        // Notify the user that this album starts uploading
        updateAlbumStatus(albumUiState.id, UploadStatus.Uploading)
        val googleAlbumId = getOrCreateGoogleAlbum(albumUiState, accessToken) ?: return
        val uploadedItems: List<Pair<PhotoUiState, String>> = try {
            uploadPhotosInAlbum(albumUiState, accessToken, platformContext)
        } catch (e: GracefulCancellationException) {
            // Create media items for the uploaded bytes to make them appear on Google Photos.
            // Use an independent thread since a child thread already has status Cancelling which
            // will immediately cancel any network requests.
            cleanupScope.launch {
                withContext(NonCancellable) {
                    createMediaItemsForUpload(
                        albumId = albumUiState.id,
                        googleAlbumId = googleAlbumId,
                        uploadedItems = e.successfullyUploaded,
                        isCancelled = true,
                        accessToken = accessToken
                    )
                }
            }
            // Re-throw the exception to ensure the main loop in `uploadPhotosImpl` stops and
            // doesn't proceed to the next album.
            throw CancellationException("uploadPhotosToNewAlbum: Upload process started finalizing partial uploads.")
        }
        createMediaItemsForUpload(
            albumId = albumUiState.id,
            googleAlbumId = googleAlbumId,
            uploadedItems = uploadedItems,
            isCancelled = false,
            accessToken = accessToken
        )
    }

    private suspend fun createMediaItemsForUpload(
        albumId: String,
        googleAlbumId: String,
        uploadedItems: List<Pair<PhotoUiState, String>>,
        isCancelled: Boolean,
        accessToken: String,
    ) {
        val currentAlbumUiState = _albumUiStates.value.find { it.id == albumId }!!
        // If no photos were successfully uploaded (e.g., all failed), the only thing to do is set
        // the album to a final status.
        if (uploadedItems.isEmpty()) {
            log.d("createMediaItemsForUpload:     No photos uploaded successfully for album ${currentAlbumUiState.name}, canceled = $isCancelled")
            updateAlbumStatus(
                albumId = albumId,
                status = currentAlbumUiState.getDerivedUploadStatus(
                    newStatus = if (isCancelled) UploadStatus.Cancelled else UploadStatus.Success
                )
            )
            // If canceled, rethrow to stop the entire upload process
            if (isCancelled) throw CancellationException("Upload process canceled by user.")
            return
        }

        addMediaItemsToAlbum(currentAlbumUiState, googleAlbumId, uploadedItems, accessToken)

        // Use the updated album to set the cover photo to the media item id added earlier
        val finalAlbumState = _albumUiStates.value.find { it.id == albumId }!!
        setAlbumCover(finalAlbumState, googleAlbumId, accessToken)

        // Mark the album as final
        updateAlbumStatus(
            albumId = albumId,
            status = finalAlbumState.getDerivedUploadStatus(
                newStatus = if (isCancelled) UploadStatus.Cancelled else UploadStatus.Success
            )
        )
    }

    private suspend fun getOrCreateGoogleAlbum(
        albumUiState: AlbumUiState,
        accessToken: String,
    ): String? {
        val albumId = albumUiState.id
        val albumName = albumUiState.name

        // Check if an albumId was already created
        if (albumUiState.googleAlbumId != null) {
            val googleAlbumId = albumUiState.googleAlbumId
            try {
                // Verify if the album ID is still valid on the server
                if (photoUploader.verifyAlbumExists(googleAlbumId, accessToken = accessToken)) {
                    log.d("getOrCreateGoogleAlbum: Album '$albumName' already exists on Google Photos. Re-using it.")
                    return googleAlbumId
                } else {
                    log.d("getOrCreateGoogleAlbum: Album ID for '$albumName' is stale. It will be recreated.")
                    // Clear the invalid ID from our local state before creating a new one.
                    updateAlbum(albumId) { currentAlbum -> currentAlbum.copy(googleAlbumId = null) }
                }
            } catch (e: UploadException.AlbumException) {
                log.e(e) { "getOrCreateGoogleAlbum: Error verifying album '$albumName' (${e.status})" }
                updateAlbumStatus(albumId, UploadStatus.Error(e.uiText))
                return null
            }
        }

        // Create a new album.
        return try {
            log.d("getOrCreateGoogleAlbum: Creating new album on Google Photos for '$albumName'.")
            val googleAlbumId = photoUploader.createAlbum(albumName, accessToken)
            updateAlbum(albumId) { it.copy(googleAlbumId = googleAlbumId) }
            log.d("getOrCreateGoogleAlbum:     Created album with ID: $googleAlbumId for '$albumName'")
            googleAlbumId
        } catch (e: UploadException.AlbumException) {
            log.e(e) { "getOrCreateGoogleAlbum:     Failed to create album for '$albumName' (${e.status})" }
            updateAlbumStatus(albumId, UploadStatus.Error(e.uiText))
            null
        }
    }

    private suspend fun uploadPhotosInAlbum(
        albumUiState: AlbumUiState,
        accessToken: String,
        platformContext: PlatformContext,
    ): List<Pair<PhotoUiState, String>> {
        val photosToUpload = albumUiState.photoUiStates.filter { it.isEnabled }
        val successfullyUploaded = mutableListOf<Pair<PhotoUiState, String>>()

        try {
            for ((index, photo) in photosToUpload.withIndex()) {
                try {
                    updatePhotoStatus(albumUiState.id, photo.path, UploadStatus.Uploading)
                    log.d("uploadPhotosInAlbum:     Uploading photo ${index + 1}/${photosToUpload.size}: ${photo.name}")
                    val uploadToken = photoUploader.uploadPhoto(
                        photo.name,
                        photo.kmpFile,
                        accessToken,
                        platformContext
                    )
                    successfullyUploaded.add(photo to uploadToken)
                    log.d("uploadPhotosInAlbum:       Successfully uploaded: ${photo.name}")
                } catch (e: UploadException.PhotoException) {
                    log.e(e) { "uploadPhotosInAlbum:       ERROR: Failed to upload '${photo.name}' (${e.status})" }
                    updatePhotoStatus(albumUiState.id, photo.path, UploadStatus.Error(e.uiText))
                }
            }
        } catch (e: CancellationException) {
            // The loop was canceled. Throw our custom exception with the partial results
            log.d("uploadPhotosInAlbum: Caught cancellation exception: ${e.message}")
            log.d("uploadPhotosInAlbum: Gracefully cancelling photo upload loop for album '${albumUiState.name}'. ${successfullyUploaded.size} photos were completed.")
            throw GracefulCancellationException(successfullyUploaded)
        }
        return successfullyUploaded
    }

    private suspend fun addMediaItemsToAlbum(
        albumUiState: AlbumUiState,
        googleAlbumId: String,
        uploadedItems: List<Pair<PhotoUiState, String>>,
        accessToken: String,
    ) {
        val newMediaItems = uploadedItems.map { (photo, token) ->
            NewMediaItem(
                description = photo.getDisplayName(),
                simpleMediaItem = SimpleMediaItem(fileName = photo.name, uploadToken = token)
            )
        }
        try {
            val results = photoUploader.addPhotosToAlbum(googleAlbumId, newMediaItems, accessToken)
            updateAlbum(albumUiState.id) { currentAlbum ->
                val updatedPhotos = updatePhotoWithResult(currentAlbum, uploadedItems, results)
                val newCoverPhoto =
                    updatedPhotos.find { it.path == currentAlbum.coverPhotoUiState.path }
                        ?.copy(
                            mediaItemId = updatedPhotos.find { it.path == currentAlbum.coverPhotoUiState.path }?.mediaItemId
                        ) ?: currentAlbum.coverPhotoUiState

                currentAlbum.copy(
                    photoUiStates = updatedPhotos,
                    coverPhotoUiState = newCoverPhoto
                )
            }
        } catch (e: UploadException.PhotoException) {
            log.e(e) { "addMediaItemsToAlbum:       ERROR: Failed to add media items to album '${albumUiState.name}' (${e.status})" }
            updateAlbum(albumUiState.id) { currentAlbum ->
                // Mark all photos that were part of this upload attempt as failed
                val updatedPhotos = currentAlbum.photoUiStates.map { p ->
                    if (uploadedItems.any { it.first.path == p.path }) {
                        p.copy(uploadStatus = UploadStatus.Error(e.uiText))
                    } else {
                        p
                    }
                }
                currentAlbum.copy(photoUiStates = updatedPhotos)
            }
        }
    }

    private fun updatePhotoWithResult(
        currentAlbumUiState: AlbumUiState,
        uploadedItems: List<Pair<PhotoUiState, String>>,
        results: List<MediaItemResult>,
    ): List<PhotoUiState> {
        return currentAlbumUiState.photoUiStates.map { p ->
            val index = uploadedItems.indexOfFirst { it.first.path == p.path }
            if (index != -1 && index < results.size) {
                val mediaResult = results[index]
                if (mediaResult.isSuccess() && mediaResult.mediaItem != null) {
                    p.copy(
                        mediaItemId = mediaResult.mediaItem.id,
                        uploadStatus = UploadStatus.Success
                    )
                } else {
                    log.e("updatePhotoWithResult:       ERROR: Failed to add '${p.name}' to album '${currentAlbumUiState.name}'")
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
        albumUiState: AlbumUiState,
        googleAlbumId: String,
        accessToken: String,
    ) {
        val coverMediaItemId = albumUiState.coverPhotoUiState.mediaItemId
        if (coverMediaItemId != null) {
            log.d("setAlbumCover:     Setting cover photo to: ${albumUiState.coverPhotoUiState.name}")
            try {
                photoUploader.updateAlbumCover(googleAlbumId, coverMediaItemId, accessToken)
            } catch (e: UploadException) {
                log.e(
                    "setAlbumCover:     Failed to update cover photo for ${albumUiState.coverPhotoUiState.name}",
                    e
                )
            }
        }
    }

    private fun updateAlbumStatus(albumId: String, status: UploadStatus) {
        log.d("Set $albumId to $status")
        updateAlbum(albumId) {
            it.copy(uploadStatus = status)
        }
    }

    internal fun updatePhotoStatus(albumId: String, photoPath: Path, status: UploadStatus) {
        updatePhoto(albumId, photoPath) {
            it.copy(uploadStatus = status)
        }
    }

    private fun updateAlbum(albumId: String, transform: (AlbumUiState) -> AlbumUiState) {
        _albumUiStates.update { currentAlbums ->
            currentAlbums.map { album ->
                if (album.id == albumId) transform(album) else album
            }
        }
    }

    private fun updatePhoto(
        albumId: String,
        photoPath: Path,
        transform: (PhotoUiState) -> PhotoUiState,
    ) {
        updateAlbum(albumId) { album ->
            var isPhotoStatusUpdated = false
            var updatedCoverPhoto: PhotoUiState = album.coverPhotoUiState
            val updatedPhotos = album.photoUiStates.map { photo ->
                if (photo.path == photoPath) {
                    val updatedPhoto = transform(photo)
                    isPhotoStatusUpdated = (updatedPhoto.uploadStatus != photo.uploadStatus)
                    if (updatedCoverPhoto.path == photo.path) {
                        updatedCoverPhoto = updatedPhoto
                    }
                    updatedPhoto
                } else {
                    photo
                }
            }

            // Copy in 2 steps, since getDerivedUploadStatus needs the updated photos to determine the status
            val updatedAlbum = album.copy(
                photoUiStates = updatedPhotos,
                coverPhotoUiState = updatedCoverPhoto
            )
            if (isPhotoStatusUpdated) {
                updatedAlbum.copy(
                    uploadStatus = updatedAlbum.getDerivedUploadStatus(updatedAlbum.uploadStatus)
                )
            } else {
                updatedAlbum
            }
        }
    }

    // Cleanup when Koin notifies the end of the life cycle
    fun shutdown() {
        processJob?.cancel()
        cleanupScope.cancel()
    }

}

/**
 * Custom exception to gracefully handle cancellation and carry the list of successfully uploaded photos.
 */
private class GracefulCancellationException(
    val successfullyUploaded: List<Pair<PhotoUiState, String>>,
) : CancellationException("Upload gracefully canceled by user.")
