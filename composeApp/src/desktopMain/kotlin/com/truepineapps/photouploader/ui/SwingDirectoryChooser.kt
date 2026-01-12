package com.truepineapps.photouploader.ui

import java.io.File
import java.lang.Boolean.TRUE
import javax.swing.JFileChooser
import javax.swing.UIManager

private const val FILE_CHOOSER_READ_ONLY = "FileChooser.readOnly"

/**
 * Minimal wrapper around the Swing JFileChooser
 * Based on https://github.com/MohamedRejeb/Calf/blob/main/calf-file-picker/src/desktopMain/kotlin/com/mohamedrejeb/calf/picker/platform/windows/api/JnaFileChooser.kt
 *
 * @see JFileChooser
 */
internal class SwingDirectoryChooser(val dialogTitle: String = "Select a folder") {
    private var selectedFiles: Array<File?>
    private var currentDirectory: File? = null

    private var openButtonText: String = ""

    val selectedFile: File?
        get() = selectedFiles[0]

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
        val fc = getReadOnlyFileChooser()

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

    /*
     * Since there is no read-only property, change the UIManager property temporarily. Though the
     * fact that the UIManager is queried once at construction time of the chooser is an
     * implementation detail, so strictly speaking shouldn't be used, it's less heavyweight
     * intrusion than fiddling with action properties and listeners.
     * See: https://forums.oracle.com/ords/apexds/post/how-does-the-jfilechooser-readonly-property-work-1238
     */
    private fun getReadOnlyFileChooser(): JFileChooser {
        // Disable the ability to edit filenames directly within the dialog
        val old = UIManager.getBoolean(FILE_CHOOSER_READ_ONLY)
        UIManager.put(FILE_CHOOSER_READ_ONLY, TRUE)
        val fc = JFileChooser(currentDirectory)
        UIManager.put(FILE_CHOOSER_READ_ONLY, old)
        return fc
    }

}