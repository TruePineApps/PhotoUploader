package packaging

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

/**
 * Abstract base class for tasks that patch generated installer packages
 * (.deb, .dmg, .msi) by injecting license files after the package is built.
 *
 * Subclasses provide the platform-specific patching logic by implementing
 * [patchPackage], and receive the resolved inputs as plain values so they
 * do not need to interact with Gradle property APIs directly.
 */
abstract class PackageTask : DefaultTask() {

    @get:Internal
    @get:Inject
    abstract val execOperations: ExecOperations

    /** Directory containing the generated installer package file. */
    @get:InputDirectory
    abstract val packageFileDir: DirectoryProperty

    /** The Apache 2.0 LICENSE file. */
    @get:InputFile
    abstract val licenseSourceFile: RegularFileProperty

    /**
     * Directory containing shared resource files (OFL.txt, NOTICES, etc.)
     * Corresponds to composeApp/src/commonMain/composeResources/files.
     */
    @get:InputDirectory
    abstract val sharedResourceDir: DirectoryProperty

    /**
     * File name of the generated third-party notices file (e.g. "NOTICES"), an
     * Apache 2.0 third-party obligation.
     */
    @get:Input
    abstract val noticesFileName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        outputDir.set(packageFileDir)
        // Always run: this task modifies the package file produced by the
        // previous task in-place, so up-to-date checks are not meaningful.
        outputs.upToDateWhen { false }
    }

    /**
     * Finds the most recently modified package file with the given [extension]
     * in [packageFileDir]. Returns null if no matching file is found.
     *
     * Sorting by lastModified handles the case where previous builds left
     * older versions in the output directory.
     */
    protected fun findPackageFile(extension: String): File? =
        packageFileDir.asFile.get().listFiles()?.filter { it.extension == extension }
            ?.maxByOrNull { it.lastModified() }

    /**
     * Copies all required license files to [targetDir]:
     * - LICENSE (Apache 2.0)
     * - OFL.txt (Noto Sans font license)
     * - NOTICES (generated third-party notices)
     *
     * OFL.txt and NOTICES are sourced from [sharedResourceDir] and logged as errors if not found.
     * NOTICES is a file generate by [generateLicenseReport]. Since it is shown on the
     * [LicenseScreen], it is also checked in.
     */
    protected fun copyLicenseFiles(targetDir: File) {
        targetDir.mkdirs()

        licenseSourceFile.asFile.get().copyTo(File(targetDir, "LICENSE"), overwrite = true)
        logger.lifecycle("Added: LICENSE")

        copyResourceFile("OFL.txt", targetDir)
        copyResourceFile(noticesFileName.get(), targetDir)
    }

    /**
     * Copies a single file from [sharedResourceDir] to [targetDir].
     * Logs a warning if the file does not exist.
     */
    private fun copyResourceFile(fileName: String, targetDir: File) {
        val source = File(sharedResourceDir.asFile.get(), fileName)
        if (source.exists()) {
            source.copyTo(File(targetDir, fileName), overwrite = true)
            logger.lifecycle("Added: $fileName")
        } else {
            logger.error("$fileName not found at: ${source.absolutePath}")
        }
    }

    /**
     * Creates a temporary working directory alongside the package file,
     * executes [block] with it, and always deletes it afterward.
     */
    protected fun withWorkDir(block: (workDir: File) -> Unit) {
        val workDir =
            packageFileDir.asFile.get().resolve("temp_patch_${System.currentTimeMillis()}")
        workDir.mkdirs()
        try {
            block(workDir)
        } finally {
            workDir.deleteRecursively()
        }
    }

    /**
     * Platform-specific patching logic. Called by [org.gradle.api.tasks.TaskAction].
     * Implementations should use [findPackageFile], [copyLicenseFiles],
     * [withWorkDir] and [execOperations] as needed.
     */
    abstract fun patchPackage()
}