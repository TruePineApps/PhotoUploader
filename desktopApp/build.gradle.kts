import com.android.build.api.variant.impl.capitalizeFirstChar
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask
import packaging.PatchDebPackage

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val sharedResourceFiles = project(":composeApp").layout.projectDirectory.dir("src/commonMain/composeResources/files")
val noticesName = "NOTICES"
val versionInfo = rootProject.extensions.getByType<ProjectVersion>()

kotlin {
    dependencies {
        implementation(projects.composeApp)
    }
}

compose.desktop {
    application {
        mainClass = "com.truepineapps.photouploader.MainKt"

        // Use the configured JDK toolchain so packaging uses the same complete JDK as compilation.
        javaHome = javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        }.get().metadata.installationPath.asFile.absolutePath

        description = "Upload a photo collection organized in folders to Google Photo"

        buildTypes.release.proguard {
            version.set("7.9.0")
            configurationFiles.from("proguard-rules.pro")
        }

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,      // macOS disk image (ARM64 & Intel)
                TargetFormat.Msi,      // Windows installer package
                TargetFormat.Deb,      // Debian / Ubuntu installer package (.deb)
                TargetFormat.Rpm       // Red Hat / Fedora / openSUSE installer package (.rpm)
            )

            // Module httpServer is used by Google API. Explicitly include it to prevent being
            // stripped in the release version
            modules("jdk.httpserver")

            // Get PlatformVersion object for app name and version based on libs.versions.toml
            // User facing name: Title Case with spaces
            packageName = versionInfo.appLabel
            packageVersion = versionInfo.numericVersion
            copyright = "© 2026 True Pine Apps. All rights reserved."
            vendor = "True Pine Apps"
            licenseFile.set(project.file("../LICENSE"))

            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/main/resources"))

            linux {
                // Debian requires lowercase letters, numbers, and hyphens ONLY
                packageName = versionInfo.packageName
                iconFile.set(project.file("src/main/resources/desktopicon.png"))
                appCategory = "Utility"
                rpmLicenseType = "Apache 2.0" // Mandatory for .rpm packaging
                shortcut = true
                debMaintainer = "True Pine Apps <photouploader@truepineapps.com>"
            }
            macOS {
                bundleID = versionInfo.appId
                iconFile.set(project.file("src/main/resources/desktopicon.icns"))
                appCategory = "public.app-category.utilities"
            }
            windows {
                iconFile.set(project.file("src/main/resources/desktopicon.ico"))
                menuGroup = "Photo Uploader"
                shortcut = true
                upgradeUuid = "0D40844D-0D36-4889-A1D4-5BF995A9B471"
            }
        }
    }
}

val appImageOutput = layout.buildDirectory.file(
    "compose/binaries/main-release/appimage/${versionInfo.packageName}-${versionInfo.artifactVersion}-linux-x86_64.AppImage"
)

val packageReleaseAppImage = tasks.register<Exec>("packageReleaseAppImage") {
    description = "Package the release application image as a standalone Linux AppImage."
    group = "distribution"
    dependsOn("createReleaseDistributable")
    inputs.dir(layout.buildDirectory.dir("compose/binaries/main-release/app/${versionInfo.appLabel}"))
    inputs.file(layout.projectDirectory.file("src/main/resources/desktopicon.png"))
    inputs.file(rootProject.file("scripts/package-appimage.sh"))
    outputs.file(appImageOutput)

    commandLine(
        "bash",
        rootProject.file("scripts/package-appimage.sh").absolutePath,
        layout.buildDirectory.dir("compose/binaries/main-release/app/${versionInfo.appLabel}").get().asFile.absolutePath,
        layout.projectDirectory.file("src/main/resources/desktopicon.png").asFile.absolutePath,
        versionInfo.appLabel,
        appImageOutput.get().asFile.absolutePath
    )
}

if (System.getProperty("os.name").lowercase().contains("linux")) {
    tasks.matching { it.name == "packageReleaseDistributionForCurrentOS" }.configureEach {
        dependsOn(packageReleaseAppImage)
    }
}

/**
 * 1. Add license files
 * - copyright: Use COPYRIGHT instead of the generated copyright file in the
 *   standard location (/usr/share/doc/...) and the app share folder.
 * - LICENSE: The Apache 2.0 license for non-Debian users
 * - OFL.txt: For the Noto Sans font license
 * - NOTICES: For the third party license notices
 * 2. Add compressed changelog.gz in the standard location.
 * 3. Fix Linux .desktop file and metadata (Maintainer, Section, Description).
 * To make the app icon appear in the launch bar and on top of the screenshot in the change task bar,
 * the .desktop file must contain the property StartupWMClass. Run 'lg' by pressing [Alt-F2] and
 * entering lg and click the 'Windows' button to find the value. Alternatively, start the app, run
 * 'xprop WM_CLASS' in a terminal and click the app window.
 * Since updating the generated .desktop file doesn't work, see bug https://youtrack.jetbrains.com/issue/CMP-9837/packageDeb-task-fails-to-include-custom-.desktop-files,
 * a custom .desktop file is created in the resources folder and replaces the default .desktop file
 * in the assembled .deb file.
 * This custom .desktop file src/main/resources/linux/Photo-Uploader.desktop now contains the
 * necessary StartupWMClass.
 * 4. Standardize file permissions and ownership (root:root).
 * 
 * This class must not reference `project` or global variables in this file.
 *
 * Note that althoug using afterEvaluate is generally discourages, using it for a reporting task is
 * appropriate.
 */
afterEvaluate {
    // Find all realized Debian package tasks (e.g., packageDeb, packageReleaseDeb)
    tasks.names.filter { it.matches(Regex("package.*Deb")) }.forEach { packageTaskName ->
        // Determine the folder name based on the package type
        val folderName = if (packageTaskName.contains("Release")) "main-release" else "main"
        // Create a dedicated task name (e.g., fixPackageReleaseDeb)
        val fixTaskName = "fix${packageTaskName.replaceFirstChar { it.uppercase() }}"

        // Register a unique fix task for this package task
        val fixTask = tasks.register<PatchDebPackage>(fixTaskName) {
            // Get PlatformVersion object for app name and version based on libs.versions.toml
            val versionInfo = rootProject.extensions.getByType<ProjectVersion>()

            description = "Patch .desktop file, fix metadata and add license files in generated .deb package"

            packageFileDir.set(layout.buildDirectory.dir("compose/binaries/$folderName/deb"))
            desktopSourceFile.set(layout.projectDirectory.file("src/main/resources/linux/Photo-Uploader.desktop"))
            copyrightSourceFile.set(layout.projectDirectory.file("../COPYRIGHT"))
            licenseSourceFile.set(layout.projectDirectory.file("../LICENSE"))
            sharedResourceDir.set(sharedResourceFiles)
            noticesFileName.set(noticesName)
            appVersion.set(versionInfo.numericVersion)
            packageName.set(versionInfo.packageName)
            appLabel.set(versionInfo.appLabel)
            onlyIf("Deb package output was not generated") {
                packageFileDir.get().asFile.exists()
            }
        }

        tasks.named(packageTaskName) { finalizedBy(fixTask) }
    }
}

/**
 * Copy license files to the Windows/macOS app resources directory before MSI/DMG packaging.
 * jpackage will include everything under appResourcesRootDir in the installed app\resources\ folder.
 * This avoids MSI post-processing, which requires platform-specific tooling, and changing an
 * already signed DMG file.
 */
fun prepareLicenseFiles(resourceDir: String) = tasks.register<Copy>(
    "prepare${resourceDir.substringBefore('/').capitalizeFirstChar()}LicenseFiles"
) {
    description = "Copy license files to the Windows/macOS app resources directory before MSI/DMG packaging."

    // Explicitly depend on the task that copies the generated report to composeApp resources
    dependsOn(":composeApp:copyNoticesToResources")

    from(rootProject.file("LICENSE"))
    from(sharedResourceFiles) {
        include("NOTICES", "OFL.txt", "TERMS*.md", "PRIVACY*.md")
    }
    into(layout.projectDirectory.dir("src/main/resources/$resourceDir"))
}

// Installed location: C:\Program Files\Photo-Uploader\app\resources\legal\
val prepareWindowsLicenseFiles = prepareLicenseFiles("windows/legal")
// This maps to: PhotoUploader.app/Contents/app/resources/
val prepareMacLicenseFiles = prepareLicenseFiles("macOS")

// Explicitly define the "prepareAppResources" and "processResources" dependencies.
// This ensures that when you run the package build task normally, the files are ready.
tasks.matching { it.name == "prepareAppResources" || it.name == "processResources" || it.name.startsWith("package") }.all {
    dependsOn(":composeApp:copyNoticesToResources")
    dependsOn(prepareMacLicenseFiles)
    dependsOn(prepareWindowsLicenseFiles)
}

// JPackage (Packaging) Task Dependencies
tasks.withType<AbstractJPackageTask>().configureEach {
    // This task needs the resources to be processed and ready
    dependsOn("processResources")

    val taskName = name.lowercase()
    if (taskName.contains("msi")) {
        dependsOn(prepareWindowsLicenseFiles)
        // Instruct the JVM to request that Windows not trim the application's memory when it is
        // minimized. Uploading in the background should not fight with Windows trying to swap the
        // photo to upload to disk. As a bonus, if the user restores the app to check progress, it
        // will pop up instantly rather than waiting for the disk to swap memory back in.
        launcherJvmArgs.add("-Dsun.awt.keepWorkingSetOnMinimize=true")
    }
    if (taskName.contains("dmg")) {
        dependsOn(prepareMacLicenseFiles)
    }
}
