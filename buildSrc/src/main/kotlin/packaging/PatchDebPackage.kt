package packaging

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Patches the generated .deb package by:
 * 1. Replacing the auto-generated .desktop file with a custom one that
 *    includes StartupWMClass (required for correct taskbar icon behavior).
 *    See: https://youtrack.jetbrains.com/issue/CMP-9837
 * 2. Fixing the Maintainer field and metadata in the control file.
 * 3. Replacing the auto-generated copyright file with a Debian-compliant one
 *    in the standard location (/usr/share/doc/...).
 * 4. Adding a compressed changelog.gz.
 * 5. Standardizing file permissions and ownership.
 * 6. Adding LICENSE, NOTICE, OFL.txt and NOTICES to
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
            addChangelog(extractDir)
            copyLicenseFiles(File(extractDir, "/opt/photo-uploader/share/doc"))
            copyLintianOverrides(extractDir)

            // Fix permissions and ownership before repacking
            fixPermissionsAndOwnership(extractDir)

            // Repack: preserves permissions and handles compression automatically
            // Use --root-owner-group to fix UID/GID issues easily
            execOperations.exec {
                commandLine("dpkg-deb", "--root-owner-group", "-b", extractDir.absolutePath, deb.absolutePath)
            }

            logger.lifecycle("Successfully patched .deb: ${deb.name}")
        }
    }

    /**
     * Fix the Maintainer, Section and Description fields in the control file.
     * jpackage often fails to correctly include the email and produces generic descriptions.
     */
    private fun fixControlFile(extractDir: File) {
        val controlFile = File(extractDir, "DEBIAN/control")
        if (!controlFile.exists()) {
            logger.warn("Control file not found at: ${controlFile.absolutePath}")
            return
        }

        val lines = controlFile.readLines()
        val fixedLines = mutableListOf<String>()
        var descriptionHeaderAdded = false

        for (line in lines) {
            when {
                line.isEmpty() -> { /* Skip empty line */ }
                line.startsWith("Maintainer:") -> {
                    fixedLines.add("Maintainer: True Pine Apps <photouploader@truepineapps.com>")
                }
                line.startsWith("Section:") -> {
                    fixedLines.add("Section: utils")
                }
                line.startsWith("Description:") -> {
                    // This is the short description, it should not start with the package name.
                    fixedLines.add("Description: Upload photos organized in folders to Google Photos")
                    descriptionHeaderAdded = true
                }
                line.startsWith(" ") && descriptionHeaderAdded -> {
                    // Skip existing description body lines, we'll replace it with the one from AboutScreen
                }
                else -> {
                    fixedLines.add(line)
                }
            }
        }

        // Add the mandatory extended description (one space at the beginning)
        // Using the description from AboutScreen.kt
        fixedLines.add(" This tool uploads a photo collection organized in folders into albums")
        fixedLines.add(" on Google Photos. The album name is derived from the folder name.")

        controlFile.writeText(fixedLines.joinToString("\n") + "\n")
        logger.lifecycle("Fixed metadata in control file")
    }

    private fun addChangelog(extractDir: File) {
        val docDir = File(extractDir, "usr/share/doc/photo-uploader")
        docDir.mkdirs()
        val changelogFile = File(docDir, "changelog")
        
        // Format: "EEE, dd MMM yyyy HH:mm:ss Z"
        val date = ZonedDateTime.now().format(
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
        )
        
        val content = """
            photo-uploader (${appVersion.get()}) stable; urgency=medium

              * Initial release.

             -- True Pine Apps <photouploader@truepineapps.com>  $date
        """.trimIndent()
        
        changelogFile.writeText(content + "\n")
        
        // Compress the changelog file
        execOperations.exec {
            commandLine("gzip", "-n", "-9", changelogFile.absolutePath)
        }
        logger.lifecycle("Added compressed changelog")
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

    private fun copyLintianOverrides(extractDir: File) {
        val debianDir = File(extractDir, "DEBIAN")
        debianDir.mkdirs()
        val overridesFile = File(debianDir, "lintian-overrides")
        val sourceFile = File(desktopSourceFile.asFile.get().parentFile.parentFile, "linux/lintian-overrides")
        if (sourceFile.exists()) {
            sourceFile.copyTo(overridesFile, overwrite = true)
            logger.lifecycle("Copied lintian-overrides to DEBIAN folder")
        } else {
            logger.warn("lintian-overrides source not found at ${sourceFile.absolutePath}")
        }
    }

    private fun fixPermissionsAndOwnership(extractDir: File) {
        // Standard permissions: 755 for directories, 644 for files
        execOperations.exec {
            commandLine("find", extractDir.absolutePath, "-type", "d", "-exec", "chmod", "755", "{}", "+")
        }
        execOperations.exec {
            commandLine("find", extractDir.absolutePath, "-type", "f", "-exec", "chmod", "644", "{}", "+")
        }

        // Strip ELF binaries (executables and shared libraries).
        // Use find to locate ELF binaries and pass them to 'strip'.
        // Exclude .jar files explicitly via -not -name "*.jar".
        execOperations.exec {
            commandLine("bash", "-c", "find ${extractDir.absolutePath} -type f -not -name \"*.jar\" -not -name \"*.so.*\" -exec file {} + | grep ELF | cut -d: -f1 | xargs -r strip --strip-unneeded")
        }

        // Make the main executable and launcher executable
        val binDir = File(extractDir, "opt/photo-uploader/bin")
        if (binDir.exists()) {
            execOperations.exec {
                commandLine("find", binDir.absolutePath, "-type", "f", "-exec", "chmod", "755", "{}", "+")
            }
        }
        
        // Ensure shared libraries are NOT executable (Lintian error)
        execOperations.exec {
            commandLine("find", extractDir.absolutePath, "-name", "*.so", "-exec", "chmod", "644", "{}", "+")
        }

        // DEBIAN scripts (if any) must be executable
        val debianDir = File(extractDir, "DEBIAN")
        if (debianDir.exists()) {
            execOperations.exec {
                commandLine("find", debianDir.absolutePath, "-type", "f", "-not", "-name", "control", "-exec", "chmod", "755", "{}", "+")
            }
        }
    }

}
