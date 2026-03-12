import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
        // We use an OS check so this path only applies when building on Linux.
        if (System.getProperty("os.name").contains("Linux")) {
            javaHome = "/usr/lib/jvm/java-21-openjdk-amd64"
        }

        description = "Upload a photo collection organized in folders to Google Photo"

        buildTypes.release.proguard {
            version.set("7.5.0")
            configurationFiles.from("proguard-rules.pro")
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = "Photo-Uploader"
            packageVersion = "1.0.0"
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

    @get:InputFile
    abstract val debFile: RegularFileProperty

    @get:InputFile
    abstract val desktopSourceFile: RegularFileProperty

    @get:OutputFile
    abstract val outputDebFile: RegularFileProperty

    init {
        // Set default output to be the same as input
        outputDebFile.convention(debFile)
    }

    @TaskAction
    fun fixDesktopFile() {
        val deb = debFile.asFile.get()
        val desktopSource = desktopSourceFile.asFile.get()

        if (!deb.exists()) {
            logger.warn("Deb file not found at: ${deb.absolutePath}")
            return
        }

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
            val desktopFile = tempDir.walk().find {
                it.isFile && it.name.endsWith(".desktop")
            }

            if (desktopFile != null) {
                logger.lifecycle("Found .desktop file: ${desktopFile.absolutePath}")
                desktopSource.copyTo(desktopFile, overwrite = true)
                logger.lifecycle("Replaced .desktop file with custom version")
            } else {
                logger.warn("Could not find .desktop file in extracted content")
            }

            // Repackage data.tar
            logger.lifecycle("Repackaging data archive...")
            dataTar.delete()

            val repackCmd = when {
                dataTar.name.endsWith(".tar.gz") -> listOf("tar", "-czf", dataTar.name, "opt")
                dataTar.name.endsWith(".tar.xz") -> listOf("tar", "-cJf", dataTar.name, "opt")
                dataTar.name.endsWith(".tar.zst") -> listOf("tar", "--zstd", "-cf", dataTar.name, "opt")
                else -> listOf("tar", "-cf", dataTar.name, "opt")
            }

            execOperations.exec {
                workingDir = tempDir
                commandLine(repackCmd)
            }

            // Rebuild the .deb file
            logger.lifecycle("Rebuilding .deb file...")
            deb.delete()

            // Build the ar command with available files
            val arCmd = mutableListOf("ar", "r", deb.absolutePath, "debian-binary")
            if (controlTar != null) arCmd.add(controlTar.name)
            arCmd.add(dataTar.name)

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

// Register and configure the task
tasks.register<FixDesktopFileTask>("fixDesktopFile") {
    debFile.set(
        layout.buildDirectory.file("compose/binaries/main/deb/photo-uploader_1.0.0_amd64.deb")
    )
    desktopSourceFile.set(
        layout.projectDirectory.file("src/main/resources/linux/Photo-Uploader.desktop")
    )
}

afterEvaluate {
    tasks.matching { task ->
        task.name.matches(Regex("package.*Deb"))
    }.configureEach {
        finalizedBy("fixDesktopFile")
    }
}