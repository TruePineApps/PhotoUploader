package com.truepineapps.photouploader.ui.uistate

import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.error_one_or_more_photos_failed
import com.truepineapps.photouploader.ui.util.createTestKmpFile
import com.truepineapps.photouploader.ui.screen.uploader.uistate.AlbumUiState
import com.truepineapps.photouploader.ui.screen.uploader.uistate.PhotoUiState
import com.truepineapps.photouploader.ui.screen.uploader.uistate.UploadStatus
import com.truepineapps.photouploader.util.UiTextString
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UploadStatusTest {

    private fun createDummyPhoto(path: String, status: UploadStatus) = PhotoUiState(
        kmpFile = createTestKmpFile(path),
        path = path.toPath(),
        name = "photo.jpg",
        uploadStatus = status
    )

    private fun createDummyAlbum(photoUiStates: List<PhotoUiState>) = AlbumUiState(
        id = "album1",
        kmpFile = createTestKmpFile("/album1"),
        path = "/album1".toPath(),
        name = "Test Album",
        group = "Test",
        photoUiStates = photoUiStates,
        coverPhotoUiState = photoUiStates.first(),
    )

    @Test
    fun `getDerivedUploadStatus returns Success if all photos are Success`() {
        val photos = listOf(
            createDummyPhoto("/p1", UploadStatus.Success),
            createDummyPhoto("/p2", UploadStatus.Success)
        )
        val album = createDummyAlbum(photos)
        assertTrue(album.getDerivedUploadStatus(UploadStatus.None) is UploadStatus.Success)
    }

    @Test
    fun `getDerivedUploadStatus returns Error if any photo is Error`() {
        val photos = listOf(
            createDummyPhoto("/p1", UploadStatus.Success),
            createDummyPhoto("/p2", UploadStatus.Error(UiTextString("Failed")))
        )
        val album = createDummyAlbum(photos)
        val derivedStatus = album.getDerivedUploadStatus(UploadStatus.None)
        assertTrue(derivedStatus is UploadStatus.Error)
        assertEquals(
            "${Res.string.error_one_or_more_photos_failed.key} 'Failed'",
            derivedStatus.message.toString()
        )
    }

    @Test
    fun `getDerivedUploadStatus returns Uploading if any photo is Uploading`() {
        val photos = listOf(
            createDummyPhoto("/p1", UploadStatus.Success),
            createDummyPhoto("/p2", UploadStatus.Uploading)
        )
        val album = createDummyAlbum(photos)
        assertTrue(album.getDerivedUploadStatus(UploadStatus.None) is UploadStatus.Uploading)
    }

    @Test
    fun `getDerivedUploadStatus returns Waiting if photos are Waiting or None`() {
        val photos = listOf(
            createDummyPhoto("/p1", UploadStatus.Waiting),
            createDummyPhoto("/p2", UploadStatus.None)
        )
        val album = createDummyAlbum(photos)
        assertTrue(album.getDerivedUploadStatus(UploadStatus.None) is UploadStatus.Waiting)
    }

    @Test
    fun `getDerivedUploadStatus returns UploadingError if uploading and an error exists`() {
        val photos = listOf(
            createDummyPhoto("/p1", UploadStatus.Uploading),
            createDummyPhoto("/p2", UploadStatus.Error(UiTextString("Failed")))
        )
        val album = createDummyAlbum(photos)
        val derivedStatus = album.getDerivedUploadStatus(UploadStatus.None)
        assertTrue(derivedStatus is UploadStatus.UploadingError)
        assertEquals(
            "${Res.string.error_one_or_more_photos_failed.key} 'Failed'",
            derivedStatus.message.toString()
        )
    }

    @Test
    fun `getDerivedUploadStatus returns passed in status if no other condition met`() {
        val photos = listOf(
            createDummyPhoto("/p1", UploadStatus.None),
            createDummyPhoto("/p2", UploadStatus.None)
        )
        val album =
                createDummyAlbum(photos).copy(uploadStatus = UploadStatus.None)
        val expectedStatus = UploadStatus.Error(UiTextString("Album Creation Failed"))
        val derivedStatus = album.getDerivedUploadStatus(expectedStatus)
        assertTrue(derivedStatus is UploadStatus.Error)
        assertEquals(expectedStatus, derivedStatus)
    }
}
