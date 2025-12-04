package com.truepineapps.photouploader.ui

import java.io.File
import javax.swing.JFileChooser

/**
 * Minimal wrapper around the Swing JFileChooser
 * Based on https://github.com/MohamedRejeb/Calf/blob/main/calf-file-picker/src/desktopMain/kotlin/com/mohamedrejeb/calf/picker/platform/windows/api/JnaFileChooser.kt
 *
 * @see JFileChooser
 */
internal class SwingDirectoryChooser() {
    private var selectedFiles: Array<File?>
    private var currentDirectory: File? = null


    private val dialogTitle: String = "Select a folder"
    private var openButtonText: String = ""

    /**
     * creates a new file chooser with multiselection disabled and mode set
     * to allow file selection only.
     */
    init {
        selectedFiles = arrayOf(null)
    }

    /**
     * shows a dialog for opening files
     *
     * @return true if the user clicked OK
     */
    fun showOpenDialog(): Boolean {
        val fc = JFileChooser(currentDirectory)
        fc.isMultiSelectionEnabled = false
        fc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

        fc.dialogTitle = dialogTitle
        if (openButtonText.isNotEmpty()) {
            fc.approveButtonText = openButtonText
        }

        val result = fc.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFiles = arrayOf(fc.selectedFile)
            currentDirectory = fc.currentDirectory
            return true
        }

        return false
    }


    val selectedFile: File?
        get() = selectedFiles[0]
}