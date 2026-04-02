package packaging

import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Patches the generated .dmg package by adding license files to
 * PhotoUploader.app/Contents/Resources/:
 * - LICENSE, NOTICE, OFL.txt, NOTICES
 *
 * Note: Code signing and notarization are not yet implemented.
 * These will be added as additional steps once an Apple Developer
 * account is available. Signing must happen after license files are
 * added (between steps 4 and 5 below) to avoid invalidating the signature.
 */
abstract class PatchDmgPackage : PackageTask() {

    @TaskAction
    override fun patchPackage() {
        val dmg = findPackageFile("dmg")
            ?: run {
                logger.error("No .dmg file found in: ${packageFileDir.asFile.get().absolutePath}")
                return
            }

        logger.lifecycle("Processing .dmg: ${dmg.absolutePath}")

        withWorkDir { workDir ->
            val mountPoint = workDir.resolve("mounted")
            val modifiedDir = workDir.resolve("modified")
            mountPoint.mkdirs()
            modifiedDir.mkdirs()

            // Step 1: Mount the original DMG read-only
            execOperations.exec {
                commandLine(
                    "hdiutil", "attach", dmg.absolutePath,
                    "-mountpoint", mountPoint.absolutePath,
                    "-readonly", "-nobrowse"
                )
            }

            // Step 2: Copy contents and always unmount, even if copy fails
            try {
                execOperations.exec {
                    commandLine("cp", "-r", "${mountPoint.absolutePath}/.", modifiedDir.absolutePath)
                }
            } finally {
                execOperations.exec {
                    commandLine("hdiutil", "detach", mountPoint.absolutePath)
                }
            }

            // Step 3: Locate the app bundle
            val appBundle = modifiedDir.listFiles { f -> f.extension == "app" }
                ?.firstOrNull()
                ?: error("No .app bundle found in DMG contents at: ${modifiedDir.absolutePath}")

            // Step 4: Copy license files into the app bundle's Resources directory
            copyLicenseFiles(File(appBundle, "Contents/Resources"))

            // Step 5: Rebuild the DMG from the modified contents
            val patchedDmg = File(dmg.parentFile, dmg.nameWithoutExtension + "-patched.dmg")
            execOperations.exec {
                commandLine(
                    "hdiutil", "create",
                    "-volname", "PhotoUploader",
                    "-srcfolder", modifiedDir.absolutePath,
                    "-ov", "-format", "UDZO",
                    patchedDmg.absolutePath
                )
            }

            // Step 6: Replace the original DMG with the patched one
            dmg.delete()
            patchedDmg.renameTo(dmg)

            logger.lifecycle("Successfully patched .dmg: ${dmg.name}")
        }
    }
}