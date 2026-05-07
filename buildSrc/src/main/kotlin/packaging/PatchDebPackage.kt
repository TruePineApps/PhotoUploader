package packaging

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Patches the generated .deb package by:
 * 1. Replacing the auto-generated .desktop file with a custom one that
 *    includes StartupWMClass (required for correct taskbar icon behavior).
 *    See: https://youtrack.jetbrains.com/issue/CMP-9837
 * 2. Fixing the Maintainer field in the control file (removing <Unknown>).
 * 3. Replacing the auto-generated copyright file with a Debian-compliant one
 *    in the standard location (/usr/share/doc/...).
 * 4. Adding LICENSE, NOTICE, OFL.txt and NOTICES to
 *    /opt/photo-uploader/share/doc/.
 */
abstract class PatchDebPackage : PackageTask() {

    /** Custom .desktop file containing StartupWMClass. */
    @get:InputFile
    abstract val desktopSourceFile: RegularFileProperty

    /** Debian-format copyright file to replace the generated one. */
    @get:InputFile
    abstract val copyrightSourceFile: RegularFileProperty

    @TaskAction
    override fun patchPackage() {
        val deb = findPackageFile("deb")
            ?: run {
                logger.error("No .deb file found in: ${packageFileDir.asFile.get().absolutePath}")
                return
            }

        logger.lifecycle("Processing .deb: ${deb.absolutePath}")

        withWorkDir { workDir ->
            val extractDir = workDir.resolve("extract")
            extractDir.mkdirs()

            // Extract both filesystem and control files (DEBIAN/ folder)
            execOperations.exec {
                commandLine("dpkg-deb", "-R", deb.absolutePath, extractDir.absolutePath)
            }

            replaceDesktopFile(extractDir)
            fixControlFile(extractDir)
            replaceCopyrightFile(extractDir)
            copyLicenseFiles(File(extractDir, "/opt/photo-uploader/share/doc"))

            // Repack: preserves permissions and handles compression automatically
            execOperations.exec {
                commandLine("dpkg-deb", "-b", extractDir.absolutePath, deb.absolutePath)
            }

            logger.lifecycle("Successfully patched .deb: ${deb.name}")
        }
    }

    /**
     * Fix the Maintainer field in the control file.
     * jpackage often fails to correctly include the email, resulting in "<Unknown>".
     */
    private fun fixControlFile(extractDir: File) {
        val controlFile = File(extractDir, "DEBIAN/control")
        if (!controlFile.exists()) {
            logger.warn("Control file not found at: ${controlFile.absolutePath}")
            return
        }

        val content = controlFile.readText()
        if (content.contains("<Unknown>")) {
            val fixedContent = content.replace("<Unknown>", "<photouploader@truepineapps.com>")
            controlFile.writeText(fixedContent)
            logger.lifecycle("Fixed Maintainer in control file")
        }
    }

    /**
     * Fix .desktop file.
    To make the app icon appear in the launch bar and on top of the screenshot in the change task bar,
    the .desktop file must contain the property StartupWMClass. Run 'lg' by pressing [Alt-F2] and
    entering lg and click the 'Windows' button to find the value. Alternatively, start the app, run
    'xprop WM_CLASS' in a terminal and click the app window.
    Since updating the generated .desktop file doesn't work, see bug https://youtrack.jetbrains.com/issue/CMP-9837/packageDeb-task-fails-to-include-custom-.desktop-files,
    a custom .desktop file is created in the resources folder and replaces the default .desktop file
    in the assembled .deb file.
    This custom .desktop file src/main/resources/linux/Photo-Uploader.desktop now contains the
    necessary StartupWMClass.
    */
    private fun replaceDesktopFile(extractDir: File) {
        val desktopSource = desktopSourceFile.asFile.get()
        if (!desktopSource.exists()) {
            logger.warn("Custom .desktop file not found at: ${desktopSource.absolutePath}")
            return
        }
        val desktopFiles = extractDir.walk().filter {
            it.isFile && it.name.endsWith(".desktop") && !it.absolutePath.contains("/legal/")
        }
        if (desktopFiles.any()) {
            desktopFiles.forEach { desktopFile ->
                desktopSource.copyTo(desktopFile, overwrite = true)
                logger.lifecycle("Replaced .desktop file: ${desktopFile.absolutePath}")
            }
        } else {
            logger.warn("No .desktop files found in extracted .deb content")
        }
    }

    private fun replaceCopyrightFile(extractDir: File) {
        val copyrightSource = copyrightSourceFile.asFile.get()
        // Debian standard location for copyright files
        val copyrightTarget = File(extractDir, "/usr/share/doc/photo-uploader/copyright")
        copyrightTarget.parentFile.mkdirs()
        copyrightSource.copyTo(copyrightTarget, overwrite = true)
        logger.lifecycle("Replaced copyright file at standard location")
    }
}
