import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    dependencies {
        implementation(projects.composeApp)

        implementation(compose.desktop.currentOs)
    }
}

compose.desktop {
    application {
        mainClass = "com.truepineapps.photouploader.MainKt"

        // javaHome must be set here at the application level to be recognized.
        // We use an OS check so the correct path is taken depending on the platform
        val osName = System.getProperty("os.name").lowercase()
        when {
            osName.contains("linux") -> {
                javaHome = "/usr/lib/jvm/java-21-openjdk-amd64"
            }
            osName.contains("windows") -> {
                javaHome = System.getenv("JAVA_HOME")
                    ?: "C:\\Program Files\\Microsoft\\jdk-21.0.10.7-hotspot"
            }
        }

        description = "Upload a photo collection organized in folders to Google Photo"

        buildTypes.release.proguard {
            version.set("7.5.0")
            configurationFiles.from("proguard-rules.pro")
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            // Module httpServer is used by Google API. Explicitly include it to prevent being
            // stripped in the release version
            modules("jdk.httpserver")

            packageName = "Photo-Uploader"
            packageVersion = libs.versions.appVersionName.get()
            description = "Upload a photo collection organized in folders to Google Photo"
            copyright = "© 2026 True Pine Apps. All rights reserved."
            vendor = "True Pine Apps"
            licenseFile.set(project.file("../LICENSE"))

            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/main/resources"))

            linux {
                iconFile.set(project.file("src/main/resources/desktopicon.png"))
                appCategory = "Utility"
                rpmLicenseType = "Apache 2.0"
                shortcut = true
            }
            macOS {
                iconFile.set(project.file("src/main/resources/desktopicon.icns"))
                appCategory = "public.app-category.utilities"
            }
            windows {
                iconFile.set(project.file("src/main/resources/desktopicon.ico"))
                menuGroup = "Photo_Uploader"
                upgradeUuid = "0D40844D-0D36-4889-A1D4-5BF995A9B471"
            }
        }
    }
}

// Target the JPackage task specifically
tasks.withType<AbstractJPackageTask>().configureEach {
    if (name.contains("Msi", ignoreCase = true)) {
        // Instruct the JVM to request that Windows not trim the application's memory when it is
        // minimized. Uploading in the background should not fight with Windows trying to swap the
        // photo to upload to disk. As a bonus, if the user restores the app to check progress, it
        // will pop up instantly rather than waiting for the disk to swap memory back in.
        launcherJvmArgs.add("-Dsun.awt.keepWorkingSetOnMinimize=true")
    }
}


/**
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
abstract class FixDesktopFileTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @get:InputDirectory
    abstract val debFileDir: DirectoryProperty

    @get:InputFile
    abstract val desktopSourceFile: RegularFileProperty

    // Use OutputDirectory because the .deb filename is dynamic (contains version)
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        // Mark the directory as the output so Gradle knows it has changed
        outputDir.set(debFileDir)

        // Force the task to always run when called, because it modifies the .deb produced by the
        // previous task in-place.
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun fixDesktopFile() {
        val dir = debFileDir.asFile.get()
        val desktopSource = desktopSourceFile.asFile.get()

        // Find the .deb file by filtering for .deb and sort by lastModified to handle cases where
        // old versions might still be in the folder.
        val deb = dir.listFiles()
            ?.filter { it.extension == "deb" }
            ?.maxByOrNull { it.lastModified() }

        if (deb == null || !deb.exists()) {
            logger.error("No .deb file found in directory: ${dir.absolutePath}")
            return
        }

        logger.lifecycle("Processing Deb: ${deb.absolutePath}")
        if (!desktopSource.exists()) {
            logger.warn("Custom .desktop file not found at: ${desktopSource.absolutePath}")
            return
        }

        val tempDir = deb.parentFile.resolve("temp_deb_fix_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        try {
            logger.lifecycle("Extracting .deb file...")

            // Extract .deb using ar
            execOperations.exec {
                workingDir = tempDir
                commandLine("ar", "x", deb.absolutePath)
            }

            // Find data.tar.* and control.tar.*
            val dataTar = tempDir.listFiles()?.find { it.name.startsWith("data.tar") }
            val controlTar = tempDir.listFiles()?.find { it.name.startsWith("control.tar") }

            if (dataTar == null) {
                logger.warn("No data.tar file found in .deb")
                return
            }

            logger.lifecycle("Extracting data archive...")

            // Extract data.tar.*
            val extractCmd = when {
                dataTar.name.endsWith(".tar.gz") -> listOf("tar", "-xzf", dataTar.name)
                dataTar.name.endsWith(".tar.xz") -> listOf("tar", "-xJf", dataTar.name)
                dataTar.name.endsWith(".tar.zst") -> listOf("tar", "--zstd", "-xf", dataTar.name)
                else -> listOf("tar", "-xf", dataTar.name)
            }

            execOperations.exec {
                workingDir = tempDir
                commandLine(extractCmd)
            }

            // Find and replace the .desktop file
            val desktopFiles = tempDir.walk().filter {
                it.isFile && it.name.endsWith(".desktop")
            }

            if (desktopFiles.any()) {
                desktopFiles.forEach { desktopFile ->
                    logger.lifecycle("Found .desktop file: ${desktopFile.absolutePath}")

                    // Skip files in the 'legal' directory to avoid accidental corruption
                    if (!desktopFile.absolutePath.contains("/legal/")) {
                        desktopSource.copyTo(desktopFile, overwrite = true)
                        logger.lifecycle("Replaced: ${desktopFile.name}")
                    }
                }
            } else {
                logger.warn("Could not find any .desktop files in extracted content")
            }

            // Repackage data.tar
            logger.lifecycle("Repackaging data archive...")
            val dataTarName = dataTar.name
            dataTar.delete()

            // Use --format=gnu to make sure tar does the same as jpackage
            val repackCmd = when {
                dataTarName.endsWith(".tar.gz") -> listOf(
                    "tar",
                    "-czf", dataTarName,
                    "--format=gnu",
                    "opt"
                )

                dataTarName.endsWith(".tar.xz") -> listOf(
                    "tar",
                    "-cJf", dataTarName,
                    "--format=gnu",
                    "opt"
                )

                dataTarName.endsWith(".tar.zst") -> listOf(
                    "tar",
                    "--zstd",
                    "-cf", dataTarName,
                    "--format=gnu",
                    "opt"
                )

                else -> listOf(
                    "tar",
                    "-cf", dataTarName,
                    "--format=gnu",
                    "opt")
            }

            execOperations.exec {
                workingDir = tempDir
                commandLine(repackCmd)
            }

            // Rebuild the .deb file
            logger.lifecycle("Rebuilding .deb file...")
            deb.delete()

            // Build the ar command with available files
            val arCmd = mutableListOf("ar", "rcD", deb.absolutePath, "debian-binary")
            if (controlTar != null) arCmd.add(controlTar.name)
            arCmd.add(dataTarName)

            execOperations.exec {
                workingDir = tempDir
                commandLine(arCmd)
            }

            logger.lifecycle("✓ Successfully updated .deb with custom .desktop file")

        } catch (e: Exception) {
            logger.error("Error modifying .deb file: ${e.message}")
            throw e
        } finally {
            tempDir.deleteRecursively()
        }
    }
}

afterEvaluate {
    // Find all realized package tasks (e.g., packageDeb, packageReleaseDeb)
    tasks.names.filter { it.matches(Regex("package.*Deb")) }.forEach { packageName ->
        // Determine the folder name based on the package type
        val folderName = if (packageName.contains("Release")) "main-release" else "main"
        // Create a dedicated task name (e.g., fixPackageReleaseDeb)
        val fixTaskName = "fix${packageName.replaceFirstChar { it.uppercase() }}"

        // Register a unique fix task for this package task
        val fixTask = tasks.register<FixDesktopFileTask>(fixTaskName) {
            debFileDir.set(layout.buildDirectory.dir("compose/binaries/$folderName/deb"))
            desktopSourceFile.set(layout.projectDirectory.file("src/main/resources/linux/Photo-Uploader.desktop"))
        }

        // Link the original task to the fix task
        tasks.named(packageName) {
            finalizedBy(fixTask)
        }
    }
}