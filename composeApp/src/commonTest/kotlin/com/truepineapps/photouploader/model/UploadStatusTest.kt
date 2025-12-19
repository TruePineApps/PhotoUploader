package com.truepineapps.photouploader.model

import com.truepineapps.photouploader.ui.createTestKmpFile
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UploadStatusTest {

    private fun createDummyPhoto(path: String, status: UploadStatus) = Photo(
        kmpFile = createTestKmpFile(path),
        path = path.toPath(),
        name = "photo.jpg",
        uploadStatus = status
    )

    private fun createDummyAlbum(photos: List<Photo>) = Album(
        id = "album1",
        kmpFile = createTestKmpFile("/album1"),
        path = "/album1".toPath(),
        name = "Test Album",
        group = "Test",
        photos = photos,
        coverPhoto = photos.first(),
    )

    @Test
    fun `getDerivedUploadStatus returns Success if all photos are Success`() {
        val photos = listOf(
            createDummyPhoto("/p1", UploadStatus.Success),
            createDummyPhoto("/p2", UploadStatus.Success)
        )
        val album = createDummyAlbum(photos)
        assertTrue(album.getDerivedUploadStatus() is UploadStatus.Success)
    }

    @Test
    fun `getDerivedUploadStatus returns Error if any photo is Error`() {
        val photos = listOf(
            createDummyPhoto("/p1", UploadStatus.Success),
            createDummyPhoto("/p2", UploadStatus.Error("Failed"))
        )
        val album = createDummyAlbum(photos)
        val derivedStatus = album.getDerivedUploadStatus()
        assertTrue(derivedStatus is UploadStatus.Error)
        assertEquals("One or more photos failed: Failed", (derivedStatus as UploadStatus.Error).message)
    }

    @Test
    fun `getDerivedUploadStatus returns Uploading if any photo is Uploading`() {
        val photos = listOf(
            createDummyPhoto("/p1", UploadStatus.Success),
            createDummyPhoto("/p2", UploadStatus.Uploading)
        )
        val album = createDummyAlbum(photos)
        assertTrue(album.getDerivedUploadStatus() is UploadStatus.Uploading)
    }

    @Test
    fun `getDerivedUploadStatus returns Waiting if photos are Waiting or None`() {
        val photos = listOf(
            createDummyPhoto("/p1", UploadStatus.Waiting),
            createDummyPhoto("/p2", UploadStatus.None)
        )
        val album = createDummyAlbum(photos)
        assertTrue(album.getDerivedUploadStatus() is UploadStatus.Waiting)
    }

    @Test
    fun `getDerivedUploadStatus returns UploadingError if uploading and an error exists`() {
        val photos = listOf(
            createDummyPhoto("/p1", UploadStatus.Uploading),
            createDummyPhoto("/p2", UploadStatus.Error("Failed"))
        )
        val album = createDummyAlbum(photos)
        val derivedStatus = album.getDerivedUploadStatus()
        assertTrue(derivedStatus is UploadStatus.UploadingError)
        assertEquals("One or more photos failed: Failed", (derivedStatus as UploadStatus.UploadingError).message)
    }

    @Test
    fun `getDerivedUploadStatus returns own status if no other condition met`() {
        val photos = listOf(
            createDummyPhoto("/p1", UploadStatus.None),
            createDummyPhoto("/p2", UploadStatus.None)
        )
        val album = createDummyAlbum(photos).copy(uploadStatus = UploadStatus.Error("Album Creation Failed"))
        val derivedStatus = album.getDerivedUploadStatus()
        assertTrue(derivedStatus is UploadStatus.Error)
        assertEquals("Album Creation Failed", (derivedStatus as UploadStatus.Error).message)
    }
}
