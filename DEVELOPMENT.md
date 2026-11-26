# Developer Guide

This document contains information on how to build and run **Photo Uploader** from source.

## 🛠️ Prerequisites

- **JDK 21** or later (The build system uses a JDK 21 toolchain; the project is configured to target Java 11 bytecode for compatibility with older Android devices).
- **Android SDK** (for Android builds).
- **Xcode** (for iOS builds, requires macOS).
- **Gradle** (Wrapper included in the project).

## 🚀 Building and Running

> [!IMPORTANT]
> **Android & iOS are Work In Progress:** Support for mobile platforms is currently experimental. While the UI is shared, **Google Authentication is not yet implemented** for Android and iOS. These targets are currently for development and UI preview only.

### Desktop (JVM) Application
Run these commands from the project root. For Linux packaging on Ubuntu/Debian, install the same native tools used by the CI and release workflows first:

```bash
sudo apt-get update
sudo apt-get install -y --no-install-recommends fakeroot rpm squashfs-tools
```

The `rpm` package provides `rpmbuild`, which `jpackage` needs for RPM output. `squashfs-tools` provides `mksquashfs` for creating the AppImage. The AppImage task downloads pinned, SHA-256-verified AppImage tooling. Verify the local tools are available before packaging:

```bash
command -v fakeroot
command -v rpmbuild
command -v mksquashfs
```

- **Run:**
  - Unix: `./gradlew :desktopApp:run`
  - Windows: `gradlew.bat :desktopApp:run`
- **Package Linux formats (Debian, RPM, AppImage):**
  - `./gradlew :desktopApp:packageReleaseDistributionForCurrentOS`
- **Package Debian only:**
  - `./gradlew :desktopApp:packageReleaseDeb`
- **Package RPM only:**
  - `./gradlew :desktopApp:packageReleaseRpm`
- **Package AppImage only:**
  - `./gradlew :desktopApp:packageReleaseAppImage`
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

### Testing Unsigned CI Builds & Injecting Client Secrets

Every push or Pull Request triggers GitHub Actions CI. When desktop packaging runs, Linux uploads the unsigned Debian package as `unsigned-linux-deb`; changes to Linux packaging/build/dependency configuration also build and upload RPM and AppImage as `unsigned-linux-formats`. Windows and macOS upload `unsigned-windows`, `unsigned-macos-arm`, and `unsigned-macos-intel`. Find these under the **Artifacts** section of the workflow run on GitHub.

To download and test a CI build locally:

1. **Download & Install**: Download the relevant `.zip` artifact from the GitHub Actions run summary page, extract it, and install or launch the package. Linux packaging-configuration changes produce separate Debian and additional-format artifacts; the AppImage can be marked executable and launched directly.
2. **Bypass macOS Gatekeeper (macOS only)**: For unsigned CI test builds on macOS, right-click `photo-uploader-beta.app` in Finder → select **Open** to allow Gatekeeper to launch the application.
3. **Inject Google OAuth Credentials**: For security reasons, CI test builds omit production secrets (`client_secrets.json`). To test Google Sign-In on an installed CI test build, run the helper script from the project root:
    - **macOS:** `sudo ./scripts/inject-client-secrets.sh /Applications/photo-uploader-beta.app`
    - **Linux:** `sudo ./scripts/inject-client-secrets.sh /opt/photo-uploader-beta`
    - **Windows (Git Bash):** `./scripts/inject-client-secrets.sh "/c/Program Files/Photo-Uploader"`
    - **Windows (CMD / PowerShell):** `bash scripts/inject-client-secrets.sh "C:/Program Files/Photo-Uploader"`

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
- **[NOTICES](composeApp/src/commonMain/composeResources/files/NOTICES)**: This file is updated automatically during regular builds when dependencies change. You can manually trigger a report update by running the `:desktopApp:generateLicenseReport` task.
- **[LICENSE](LICENSE) / [COPYRIGHT](COPYRIGHT)**: Ensure headers in new source files are correct and that project-wide dates are updated when necessary.

---

## 📦 Release Process

A release starts **only** when you manually push a version tag `vMAJOR.MINOR.PATCH` (e.g. `v1.0.3`).
Commits and merges never release anything.

Details, secrets and runbooks:
- **[docs/RELEASING.md](docs/RELEASING.md)**: release setup, credentials management, security runbooks.
- **[docs/CODE_SIGNING_POLICY.md](CODE_SIGNING_POLICY.md)**: public code-signing policy text that SignPath Foundation requires on the README and the website.
- **[docs/VERIFYING_DOWNLOADS.md](docs/VERIFYING_DOWNLOADS.md)**: user-facing text for verifying SHA-256 checksums and the GPG signature.

### 1. Versioning
- **Major / Minor**: `cfg-appMajor`, `cfg-appMinor` in [`gradle/libs.versions.toml`](gradle/libs.versions.toml); must match the tag. MAJOR must be ≥ 1 (macOS packaging).
- **Patch / build number**: the tag's PATCH (`v1.0.3` → `BUILD_NUMBER=3`), read by [`buildSrc/ProjectVersion.kt`](buildSrc/src/main/kotlin/ProjectVersion.kt). It must increase with every release and be ≤ 65535 (MSI limit). CI builds use the run number to distinguish artifact names; it is not included in installer or app versions, and CI builds are never distributed.
- **Stage**: `cfg-appStage` (`"Beta 1"`, or `""` for stable). A non-empty stage marks the GitHub Release as a pre-release.

### 2. Pre-Release Checklist
1. `cfg-appMajor` / `cfg-appMinor` / `cfg-appStage` match the target release.
2. Update [`CHANGELOG.md`](CHANGELOG.md): move `[Unreleased]` entries under the new version.
3. Commit with sign-off and push to `main`; wait until the **CI workflow is green on that commit**.
4. Be available to approve the SignPath Windows signing request when prompted (via email or directly at [signpath.org](https://signpath.org)).

### 3. Triggering a Release
Execute the release helper script from the root project directory:
```bash
./scripts/release.sh
```
This script verifies that your working directory is clean, you are on `main` and synced with `origin/main`, confirms that the version tag matching [`gradle/libs.versions.toml`](gradle/libs.versions.toml) does not already exist, and automatically creates and pushes the annotated version tag to GitHub.

*(Manual `git tag` execution is strongly discouraged to prevent version mismatch or uncommitted code errors).*

Tags are protected and cannot be moved or deleted. If a job fails, use **Re-run failed jobs**. If the code must change, bump `cfg-appPatch` in `libs.versions.toml`, commit to `main`, and run `./scripts/release.sh` again.

### 4. Pipeline ([`release.yml`](.github/workflows/release.yml))
1. **Verification gate**: tag format `vX.Y.Z`, commit is on `main`, tag matches `libs.versions.toml`, MAJOR ≥ 1.
2. **Tests**: `allTests` on Linux (`ubuntu-24.04`).
3. **Build** (parallel):
    - **Linux**: `:desktopApp:packageReleaseDistributionForCurrentOS` builds `.deb`, `.rpm`, and x86-64 `.AppImage` packages; each receives a GPG detached signature from the release subkey.
    - **Windows**: `:desktopApp:packageReleaseMsi`, then SignPath Foundation signing. When `WINDOWS_SIGNING` = `signpath`, the job submits the MSI and **waits up to 3 h for manual approval**. To approve: click the link in your notification email or log into [signpath.org](https://signpath.org) → **Signing Requests** → click **Approve**. (While `WINDOWS_SIGNING` remains unset, the MSI is published unsigned immediately).
    - **macOS** (both Apple Silicon (arm64) and Intel (x64)): `:desktopApp:packageReleaseDmg` with Developer ID signing, then notarization with `xcrun notarytool` (App Store Connect API key) and stapling.
4. **Checksums**: `SHA256SUMS.txt` over the final files, signed with the GPG release key (`SHA256SUMS.txt.asc`).
5. **Publish**: build provenance attestation, draft GitHub Release with all files, `scp` to the cPanel download directory plus an updated `latest.json` (versions and hashes), then the GitHub Release is published.


---

## 🧪 Testing
See [TESTING.md](TESTING.md) for details on running unit and integration tests.
