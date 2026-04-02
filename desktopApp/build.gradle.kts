import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask
import com.github.jk1.license.render.*
import com.github.jk1.license.filter.*
import packaging.PatchDebPackage
import packaging.PatchDmgPackage

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("com.github.jk1.dependency-license-report")
}

// Run a report for 3rd party dependencies with `./gradlew :desktopApp:generateLicenseReport --no-parallel`.
// Output in desktopApp/build/reports/dependency-license/: NOTICES and index.html.
// Note: As of version 3.1.1, the 'com.github.jk1.dependency-license-report' plugin
// is not fully compatible with the Gradle Configuration Cache.
// Running the task creates a message:
// "1 problem was found storing the configuration cache."
// "Task :desktopApp:generateLicenseReport ... cannot serialize object of type 'DefaultProject'".
// This is a known issue with the plugin's internal task design and does not affect
// the correctness of the generated NOTICES.
val sharedResourceFiles: FileSystemLocation =
    project(":composeApp").layout.projectDirectory.dir("src/commonMain/composeResources/files")
val dependencyDir: Provider<Directory> = layout.buildDirectory.dir("reports/dependency-license")
val noticesName = "NOTICES"
licenseReport {
    renderers = arrayOf(
        InventoryHtmlReportRenderer("index.html", "PhotoUploader – Third Party Licenses"),
        // Pass the output directory explicitly to help with Gradle's configuration cache
        NoticesRenderer(noticesName, dependencyDir.get()?.asFile)
    )
    // LicenseBundleNormalizer normalizes different namings of the same license,
    // SpdxLicenseBundleNormalizer also replaces it with the standardized name.
    filters = arrayOf(SpdxLicenseBundleNormalizer())
    excludeGroups = arrayOf("com.truepineapps")
}

// Task to copy the generated report to composeResources/files in composeApp so shared code can see it
val copyNoticesToResources = tasks.register<Copy>("copyNoticesToResources") {
    dependsOn("generateLicenseReport")
    // Source: desktopApp build folder
    from(dependencyDir.map { it?.file(noticesName) })
    // Target: composeApp resources (relative to project root)
    into(sharedResourceFiles)
}

// Ensure the file is there during normal development runs
tasks.named("processResources") {
    dependsOn(copyNoticesToResources)
}
// Ensure packaging tasks wait for the report to be generated and copied
tasks.withType<AbstractJPackageTask>().configureEach {
    dependsOn(copyNoticesToResources)
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
            version.set("7.9.0")
            configurationFiles.from("proguard-rules.pro")
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            // Module httpServer is used by Google API. Explicitly include it to prevent being
            // stripped in the release version
            modules("jdk.httpserver")

            packageName = "Photo-Uploader"
            packageVersion = libs.versions.appVersionName.get()
            copyright = "© 2026 True Pine Apps. All rights reserved."
            vendor = "True Pine Apps"
            licenseFile.set(project.file("../LICENSE"))

            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/main/resources"))

            linux {
                iconFile.set(project.file("src/main/resources/desktopicon.png"))
                appCategory = "Utility"
                rpmLicenseType = "Apache 2.0"
                shortcut = true
                debMaintainer = "True Pine Apps <photouploader@truepineapps.com>"
            }
            macOS {
                iconFile.set(project.file("src/main/resources/desktopicon.icns"))
                appCategory = "public.app-category.utilities"
            }
            windows {
                iconFile.set(project.file("src/main/resources/desktopicon.ico"))
                menuGroup = "Photo_Uploader"
                shortcut = true
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
 * 1. Fix .desktop file.
To make the app icon appear in the launch bar and on top of the screenshot in the change task bar,
the .desktop file must contain the property StartupWMClass. Run 'lg' by pressing [Alt-F2] and
entering lg and click the 'Windows' button to find the value. Alternatively, start the app, run
'xprop WM_CLASS' in a terminal and click the app window.
Since updating the generated .desktop file doesn't work, see bug https://youtrack.jetbrains.com/issue/CMP-9837/packageDeb-task-fails-to-include-custom-.desktop-files,
a custom .desktop file is created in the resources folder and replaces the default .desktop file
in the assembled .deb file.
This custom .desktop file src/main/resources/linux/Photo-Uploader.desktop now contains the
necessary StartupWMClass.
2. Add license files
- copyright: Use COPYRIGHT instead of the generated copyright file
- LICENSE: The Apache 2.0 license for non-Debian users
- OFL.txt: For the Noto Sans font license
- NOTICES: For the third party license notices

 This class must not reference `project` or global variables in this file.
 */

afterEvaluate {
    // Find all realized package tasks (e.g., packageDeb, packageReleaseDeb)
    tasks.names.filter { it.matches(Regex("package.*Deb")) }.forEach { packageTaskName ->
        // Determine the folder name based on the package type
        val folderName = if (packageTaskName.contains("Release")) "main-release" else "main"
        // Create a dedicated task name (e.g., fixPackageReleaseDeb)
        val fixTaskName = "fix${packageTaskName.replaceFirstChar { it.uppercase() }}"

        // Register a unique fix task for this package task
        val fixTask = tasks.register<PatchDebPackage>(fixTaskName) {
            description = "Patch .desktop file and add license files in generated .deb pacakge"
            packageFileDir.set(layout.buildDirectory.dir("compose/binaries/$folderName/deb"))
            desktopSourceFile.set(layout.projectDirectory.file("src/main/resources/linux/Photo-Uploader.desktop"))
            copyrightSourceFile.set(layout.projectDirectory.file("../COPYRIGHT"))
            licenseSourceFile.set(layout.projectDirectory.file("../LICENSE"))
            sharedResourceDir.set(project.file(sharedResourceFiles))
            noticesFileName.set(noticesName)
        }
        tasks.named(packageTaskName) { finalizedBy(fixTask) }
    }

    tasks.names.filter { it.matches(Regex("package.*Dmg")) }.forEach { packageTaskName ->
        val folderName = if (packageTaskName.contains("Release")) "main-release" else "main"
        val fixTask = tasks.register<PatchDmgPackage>(
            "fix${packageTaskName.replaceFirstChar { it.uppercase() }}"
        ) {
            description = "Add license files to generated .dmg package"
            packageFileDir.set(layout.buildDirectory.dir("compose/binaries/$folderName/dmg"))
            licenseSourceFile.set(layout.projectDirectory.file("../LICENSE"))
            sharedResourceDir.set(project.file(sharedResourceFiles))
            noticesFileName.set(noticesName)
        }
        tasks.named(packageTaskName) { finalizedBy(fixTask) }
    }
}