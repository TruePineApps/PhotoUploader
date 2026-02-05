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
        description = "Upload a photo collection organized in folders to Google Photo"

        buildTypes.release.proguard {
            version.set("7.5.0")
            configurationFiles.from("proguard-rules.pro")
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.truepineapps.photouploader"
            packageVersion = "1.0.0"

            linux {
                iconFile.set(project.file("src/desktop/resources/desktopicon.png"))
            }
            macOS {
                iconFile.set(project.file("src/desktop/resources/desktopicon.icns"))
            }
            windows {
                iconFile.set(project.file("src/desktop/resources/desktopicon.ico"))
                menuGroup = "Photo Uploader"
                upgradeUuid = "34B4CD27-5D7A-4396-9172-CC11157BECC6"
            }

        }
    }
}
