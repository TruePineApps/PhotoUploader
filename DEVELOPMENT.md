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

## 📦 Release Process

A release starts **only** when you manually push a version tag `vMAJOR.MINOR.PATCH` (e.g. `v1.0.3`).
Commits and merges never release anything.

Details, secrets and runbooks:
- **[docs/RELEASING.md](docs/RELEASING.md)**: release setup, credentials management, security runbooks.
- **[docs/CODE_SIGNING_POLICY.md](docs/CODE_SIGNING_POLICY.md)**: public code-signing policy text that SignPath Foundation requires on the README and the website.
- **[docs/VERIFYING_DOWNLOADS.md](docs/VERIFYING_DOWNLOADS.md)**: user-facing text for verifying SHA-256 checksums and the GPG signature.

### 1. Versioning
- **Major / Minor**: `cfg-appMajor`, `cfg-appMinor` in [`gradle/libs.versions.toml`](gradle/libs.versions.toml); must match the tag. MAJOR must be ≥ 1 (macOS packaging).
- **Patch / build number**: the tag's PATCH (`v1.0.3` → `BUILD_NUMBER=3`), read by [`buildSrc/ProjectVersion.kt`](buildSrc/src/main/kotlin/ProjectVersion.kt). It must increase with every release and be ≤ 65535 (MSI limit). CI builds use the run number instead and are never distributed.
- **Stage**: `cfg-appStage` (`"Beta 1"`, or `""` for stable). A non-empty stage marks the GitHub Release as a pre-release.

### 2. Pre-Release Checklist
1. `cfg-appMajor` / `cfg-appMinor` / `cfg-appStage` match the target release.
2. Update [`CHANGELOG.md`](CHANGELOG.md): move `[Unreleased]` entries under the new version.
3. Commit with sign-off and push to `main`; wait until the **CI workflow is green on that commit**.
4. Be available for the SignPath approval (see below).

### 3. Triggering a Release
```bash
git switch main && git pull
git tag -a v1.0.3 -m "PhotoUploader 1.0.3"
git push origin v1.0.3
```
Tags are protected and cannot be moved or deleted. If a job fails, use **Re-run failed jobs**. If the code must change, bump PATCH and tag again.

### 4. Pipeline ([`release.yml`](.github/workflows/release.yml))
1. **Verification gate**: tag format `vX.Y.Z`, commit is on `main`, tag matches `libs.versions.toml`, MAJOR ≥ 1.
2. **Tests**: `allTests` on Linux, Windows and macOS.
3. **Build** (parallel):
    - **Linux**: `:desktopApp:packageReleaseDeb`, then a GPG detached signature (`.deb.asc`) from the release subkey.
    - **Windows**: `:desktopApp:packageReleaseMsi`, then SignPath Foundation signing. **Each release waits up to 3 h for your manual approval in SignPath.** Until SignPath has accepted the project (it must already have a public release), `WINDOWS_SIGNING` stays unset and the MSI is published unsigned.
    - **macOS** (arm64): `:desktopApp:packageReleaseDmg` with Developer ID signing, then notarization with `xcrun notarytool` (App Store Connect API key) and stapling.
4. **Checksums**: `SHA256SUMS.txt` over the final files, signed with the GPG release key (`SHA256SUMS.txt.asc`).
5. **Publish**: build provenance attestation, draft GitHub Release with all files, `scp` to the cPanel download directory plus an updated `latest.json` (versions and hashes), then the GitHub Release is published.


---

## 🧪 Testing
See [TESTING.md](TESTING.md) for details on running unit and integration tests.
