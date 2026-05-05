package com.truepineapps.photouploader.ui.viewmodel

import app.cash.turbine.test
import com.truepineapps.photouploader.feature.uploader.data.repository.PhotoDirectoryRepositoryImpl
import com.truepineapps.photouploader.feature.uploader.domain.model.Album
import com.truepineapps.photouploader.feature.uploader.domain.model.Photo
import com.truepineapps.photouploader.feature.uploader.domain.repository.PhotoDirectoryRepository
import com.truepineapps.photouploader.feature.uploader.viewmodel.PhotoUploaderViewModel
import com.truepineapps.photouploader.feature.uploader.viewmodel.uistate.UiState
import com.truepineapps.photouploader.ui.util.createTestKmpFile
import com.truepineapps.photouploader.ui.util.startTestKoin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.Path.Companion.toPath
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoUploaderViewModelStateTest : KoinTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Ensure clean Koin instance for each test
        stopKoin()
        startTestKoin()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `toggleAlbum correctly updates isEnabled state of the target album`() = runTest {
        val viewModel: PhotoUploaderViewModel by inject()
        val repository: PhotoDirectoryRepositoryImpl by inject()
        val initialAlbums = listOf(
            createDummyAlbum(id = "album1"),
            createDummyAlbum(id = "album2")
        )

        viewModel.uiState.test {
            // 1. Assert initial state
            assertEquals(emptyList(), awaitItem().albumUiStates)

            // 2. Trigger state change
            repository.updateAlbums(initialAlbums)

            // 3. Assert populated state and toggle once
            var state = awaitItem()
            assertTrue(state.albumUiStates.find { it.id == "album1" }!!.isEnabled)

            viewModel.toggleAlbum("album1")
            state = awaitItem()
            assertFalse(state.albumUiStates.find { it.id == "album1" }!!.isEnabled)
            assertTrue(state.albumUiStates.find { it.id == "album2" }!!.isEnabled) // Should not change

            // 4. Toggle back
            viewModel.toggleAlbum("album1")
            state = awaitItem()
            assertTrue(state.albumUiStates.find { it.id == "album1" }!!.isEnabled)
        }
    }

    @Test
    fun `toggleAlbums correctly updates isEnabled state for a group`() = runTest {
        val viewModel: PhotoUploaderViewModel by inject()
        val repository: PhotoDirectoryRepositoryImpl by inject()
        val albums = listOf(
            createDummyAlbum(id = "album1"),
            createDummyAlbum(id = "album2"),
            createDummyAlbum(id = "album3")
        )

        viewModel.uiState.test {
            assertEquals(emptyList(), awaitItem().albumUiStates)

            repository.updateAlbums(albums)
            val populatedState = awaitItem()
            assertEquals(3, populatedState.albumUiStates.size)

            viewModel.toggleAlbums(
                listOf(populatedState.albumUiStates[0], populatedState.albumUiStates[2]),
                isEnabled = false
            )

            val finalState = awaitItem()
            assertFalse(finalState.albumUiStates.find { it.id == "album1" }!!.isEnabled)
            assertTrue(finalState.albumUiStates.find { it.id == "album2" }!!.isEnabled) // Unchanged
            assertFalse(finalState.albumUiStates.find { it.id == "album3" }!!.isEnabled)
        }
    }

    @Test
    fun `togglePhoto correctly updates isEnabled state of the target photo`() = runTest {
        val viewModel: PhotoUploaderViewModel by inject()
        val repository: PhotoDirectoryRepository by inject()
        val photo1 = createDummyPhoto(path = "/photo1")
        val photo2 = createDummyPhoto(path = "/photo2")
        val album = createDummyAlbum(id = "album1", photos = listOf(photo1, photo2))

        viewModel.uiState.test {
            assertEquals(emptyList(), awaitItem().albumUiStates)

            repository.updateAlbums(listOf(album))
            awaitItem() // Consume populated state

            viewModel.togglePhoto("album1", "/photo2".toPath())

            val updatedAlbum = awaitItem().albumUiStates.first()
            assertFalse(updatedAlbum.photoUiStates.find { it.path == "/photo2".toPath() }!!.isEnabled)
            assertTrue(updatedAlbum.photoUiStates.find { it.path == "/photo1".toPath() }!!.isEnabled)
        }
    }

    @Test
    fun `renameAlbum correctly updates the name of the target album`() = runTest {
        val viewModel: PhotoUploaderViewModel by inject()
        val repository: PhotoDirectoryRepository by inject()
        val album = createDummyAlbum(id = "album1", name = "Old Name")

        viewModel.uiState.test {
            assertEquals(emptyList(), awaitItem().albumUiStates)

            repository.updateAlbums(listOf(album))
            awaitItem() // Consume populated state

            viewModel.renameAlbum("album1", "New Album Name")

            val updatedAlbum = awaitItem().albumUiStates.first()
            assertEquals("New Album Name", updatedAlbum.name)
        }
    }

    @Test
    fun `toggleGroup correctly updates isEnabled state for the group and its albums`() = runTest {
        val viewModel: PhotoUploaderViewModel by inject()
        val repository: PhotoDirectoryRepository by inject()

        // Create albums across two groups
        val albums = listOf(
            createDummyAlbum(id = "albumA1", group = "Year 2023"),
            createDummyAlbum(id = "albumA2", group = "Year 2023"),
            createDummyAlbum(id = "albumB1", group = "Year 2024")
        )

        viewModel.uiState.test {
            // Consume initial empty state
            assertEquals(emptyList(), awaitItem().albumUiStates)

            // Trigger repository update
            repository.updateAlbums(albums)
            // Consume the first state update where albums and groups are populated
            val initialState = awaitItem()
            assertGroupAndAlbumStates(
                initialState,
                expected2023GroupEnabled = true,
                expectedAlbumA1Enabled = true,
                expectedAlbumA2Enabled = true
            )

            // --- Test 1: Toggle 'Year 2023' to disabled ---
            val targetGroup = initialState.groupUiStates.find { it.group == "Year 2023" }!!
            viewModel.toggleGroup(targetGroup, isEnabled = false)

            val disabledGroupState = awaitItem() // State after toggling group "Year 2023" to false
            assertGroupAndAlbumStates(
                disabledGroupState,
                expected2023GroupEnabled = false,
                expectedAlbumA1Enabled = false,
                expectedAlbumA2Enabled = false
            )

            // --- Test 2: Toggle 'Year 2023' back to enabled ---
            viewModel.toggleGroup(targetGroup, isEnabled = true)

            val enabledGroupState = awaitItem() // State after toggling group "Year 2023" back to true
            assertGroupAndAlbumStates(
                enabledGroupState,
                expected2023GroupEnabled = true,
                expectedAlbumA1Enabled = true,
                expectedAlbumA2Enabled = true
            )
        }
    }

    private fun assertGroupAndAlbumStates(
        state: UiState,
        expected2023GroupEnabled: Boolean,
        expectedAlbumA1Enabled: Boolean,
        expectedAlbumA2Enabled: Boolean
    ) {
        val group2023 = state.groupUiStates.find { it.group == "Year 2023" }
        val group2024 = state.groupUiStates.find { it.group == "Year 2024" }
        val albumA1 = state.albumUiStates.find { it.id == "albumA1" }
        val albumA2 = state.albumUiStates.find { it.id == "albumA2" }
        val albumB1 = state.albumUiStates.find { it.id == "albumB1" }

        // Assert group states
        assertEquals(
            expected2023GroupEnabled,
            group2023?.isEnabled,
            "Group 'Year 2023' enabled state mismatch"
        )
        assertEquals(true, group2024?.isEnabled, "Group 'Year 2024' enabled state mismatch")

        // Assert album states
        assertEquals(
            expectedAlbumA1Enabled,
            albumA1?.isEnabled,
            "Album 'albumA1' enabled state mismatch"
        )
        assertEquals(
            expectedAlbumA2Enabled,
            albumA2?.isEnabled,
            "Album 'albumA2' enabled state mismatch"
        )
        assertEquals(true, albumB1?.isEnabled, "Album 'albumB1' enabled state mismatch")
    }

    // Helper functions to create test data
    private fun createDummyPhoto(path: String) = Photo(
        kmpFile = createTestKmpFile(path),
        path = path.toPath(),
        name = "photo.jpg",
    )

    private fun createDummyAlbum(
        id: String,
        name: String = "Test Album",
        group: String = "Default Group",
        photos: List<Photo> = listOf(createDummyPhoto(path = "/$id/p1")),
    ) = Album(
        id = id,
        kmpFile = createTestKmpFile("/$id"),
        path = "/$id".toPath(),
        name = name,
        group = group,
        photos = photos,
    )
}
