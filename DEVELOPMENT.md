# Developer Guide

This document contains information on how to build and run **PhotoUploader** from source.

## 🛠️ Prerequisites

- **JDK 21** or later (The build system uses a JDK 21 toolchain; the project is configured to target Java 11 bytecode for compatibility with older Android devices).
- **Android SDK** (for Android builds).
- **Xcode** (for iOS builds, requires macOS).
- **Gradle** (Wrapper included in the project).

## 🚀 Building and Running

> [!IMPORTANT]
> **Android & iOS are Work In Progress:** Support for mobile platforms is currently experimental. While the UI is shared, **Google Authentication is not yet implemented** for Android and iOS. These targets are currently for development and UI preview only.

### Desktop (JVM) Application
Run these commands from the project root:

- **Run:**
  - Unix: `./gradlew :desktopApp:run`
  - Windows: `gradlew.bat :desktopApp:run`
- **Package (Debian/Linux):**
  - `./gradlew :desktopApp:packageReleaseDeb`
- **Package (MSI/Windows):**
  - `gradlew.bat :desktopApp:packageReleaseMsi`
- **Package (DMG/macOS):**
  - `./gradlew :desktopApp:packageReleaseDmg`

### Android Application
- **Build (Debug):**
  - Unix: `./gradlew :androidApp:assembleDebug`
  - Windows: `gradlew.bat :androidApp:assembleDebug`
- **Run:** Use the run configuration in Android Studio.

### iOS Application
- **Build:** Open the `iosApp/iosApp.xcworkspace` in Xcode.
- **Run:** Use the run configuration in Xcode or Android Studio (with KMP plugin).

---

## 🏗️ Project Structure

*   `composeApp/`: Shared Kotlin Multiplatform code (Common, Android, Desktop, iOS).
*   `desktopApp/`: Entry point and packaging logic for the Desktop application.
*   `androidApp/`: Entry point for the Android application.
*   `iosApp/`: Entry point and SwiftUI code for the iOS application.

---

## 🛠️ Maintenance Checklist

When contributing to the codebase, ensure the following metadata files are kept in sync:
- **[CHANGELOG.md](CHANGELOG.md)**: Add an entry under the `[Unreleased]` section for any user-facing changes or significant fixes.
- **[NOTICES](composeApp/src/commonMain/composeResources/files/NOTICES)**: This file is updated automatically during regular builds when dependencies change. You can manually trigger a report update by running the `:desktopApp:generateLicenseReport` task. Commit relevant updates to this file alongside your code changes.
- **[LICENSE](LICENSE) / [COPYRIGHT](COPYRIGHT)**: Ensure headers in new source files are correct and that project-wide dates are updated when necessary.

---

## 🧪 Testing
See [TESTING.md](TESTING.md) for details on running unit and integration tests.
