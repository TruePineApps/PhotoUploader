# GitHub Workflows Architecture & Runbook

This document describes the design, architecture, and operational runbook for the GitHub Actions workflows powering **PhotoUploader**.

---

## 🏗️ Core Design Principles

1. **Least Privilege**: All workflows run with minimal required GitHub Token permissions (`contents: read` by default, elevated only where strictly necessary such as publishing releases).
2. **Supply Chain Security**:
   - All third-party GitHub Actions are pinned to immutable 40-character commit SHAs.
   - Dependabot is configured for automated version updates, grouped into single combined pull requests to streamline review and testing.
3. **Build Reproducibility**:
   - CI workflows utilize dependency caching to ensure fast feedback on pull requests.
   - Release workflows explicitly run with `cache-disabled: "true"` to guarantee a clean, pristine build environment free from stale cache state.
4. **Secret Isolation**:
   - Production signing credentials (Apple Developer ID, SignPath API token, GPG signing subkey, cPanel SSH deploy key) are stored strictly inside the GitHub Environment `release`, which is restricted to tag rules matching `v*`.
   - Gradle build jobs and signing jobs are strictly decoupled so that build plugins never have access to private signing keys.

---

## 📂 Workflow Summary

| Workflow File &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | Trigger &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | Purpose & Description                                                                                                                                                                              |
|:--------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **[ci.yml](../.github/workflows/ci.yml)**               | Push & PR to `main` (ignores doc-only changes)                                                                          | Runs unit tests and builds unsigned packages across Linux, Windows, macOS (ARM & Intel) to catch packaging or ProGuard regressions early in PRs.                                                   |
| **[release.yml](../.github/workflows/release.yml)**     | Pushed tag matching `vMAJOR.MINOR.PATCH`                                                                                | Validates the tag, builds signed and notarized multi-platform binaries, generates GPG-signed checksums, attests build provenance, uploads to cPanel via SSH/SCP, and publishes the GitHub Release. |
| **[dco.yml](../.github/workflows/dco.yml)**             | Pull requests to `main`                                                                                                 | Enforces the Developer Certificate of Origin (DCO) by verifying that all human commit messages contain a valid `Signed-off-by:` tag (automatically bypassing Dependabot PRs).                      |

---

## 1. Continuous Integration (`ci.yml`)

The CI workflow provides rapid validation on every push and pull request.

* **Path Filtering (`paths-ignore`)**: Skips CI runs for documentation-only edits (`docs/**`, `README.md`, `DEVELOPMENT.md`, etc.) to conserve runner minutes.
* **Concurrency Control**: Automatically cancels in-progress CI runs when a newer commit is pushed to the same pull request or branch.
* **Matrix Build (`build`)**:
  - **Linux (`ubuntu-24.04`)**: Executes unit tests (`./gradlew allTests`) and packages the `.deb` installer.
  - **Windows (`windows-2025`)**: Packages the `.msi` installer.
  - **macOS (`macos-15`)**: Matrixes build for both **Apple Silicon (`arm64`)** and **Intel (`x64`)** `.dmg` installers.
* **Artifact Retention**: Unsigned packages are uploaded as workflow artifacts and retained for 7 days for local testing.

---

## 2. Release Automation (`release.yml`)

The release workflow is triggered **only** when an annotated tag matching `vMAJOR.MINOR.PATCH` (e.g. `v1.0.3`) is pushed to the repository.

### Pipeline Stages

1. **Gate (`verify`)**:
   - Validates the tag format `vX.Y.Z`.
   - Confirms the tagged commit exists on the `main` branch.
   - Verifies that the tag's `MAJOR.MINOR.PATCH` matches `cfg-appMajor.cfg-appMinor.cfg-appPatch` in [`libs.versions.toml`](../gradle/libs.versions.toml).
   - Enforces `MAJOR >= 1` (required for macOS packaging).
2. **Tests (`test`)**:
   - Executes `./gradlew allTests` on Linux (`ubuntu-24.04`).
3. **Build (`build-linux`, `build-windows`, `build-macos`)**:
   - Runs in parallel with caching disabled (`cache-disabled: "true"`).
   - Injects Google OAuth client configuration (`client_secrets.json`).
   - Produces unsigned Debian (`.deb`), MSI (`.msi`), and notarized macOS DMG (`.dmg` for arm64 and x64) packages.
4. **Sign (`sign-linux`, `sign-windows`)**:
   - **Linux**: Creates a GPG detached signature (`.deb.asc`) using the release subkey.
   - **Windows**: Submits the MSI to SignPath Foundation and **waits up to 3 hours for manual approval** in the SignPath web UI. (Passes through unsigned if `WINDOWS_SIGNING` variable is unset).
5. **Checksums (`checksums`)**:
   - Collects all signed deliverables into a flat `dist/` directory.
   - Generates `SHA256SUMS.txt` over all final binary bytes.
   - Signs `SHA256SUMS.txt` with the GPG release subkey (`SHA256SUMS.txt.asc`).
6. **Publish (`publish`)**:
   - Attests build provenance.
   - Creates a draft GitHub Release and uploads all distribution files.
   - Uploads distribution files and `latest.json` to the cPanel hosting server via SCP using a dedicated SSH deployment key (`WEBSITE_SSH_KEY`) and host key pinning (`WEBSITE_KNOWN_HOSTS`).
   - Publishes the GitHub Release.

---

## 3. Developer Guidelines & Reusing Workflows

* **Shared Actions**: All setup logic (JDK 21, Gradle setup, platform prerequisites) is modularized in the composite action [`.github/actions/setup-build/action.yml`](../.github/actions/setup-build/action.yml).
* **Testing CI Builds**: To test unsigned CI builds locally, download the `.zip` artifact from any GitHub Actions run, install the package, and inject local OAuth secrets using [`inject-client-secrets.sh`](../inject-client-secrets.sh).
* **Dependabot Integration**: Configured in [`.github/dependabot.yml`](../.github/dependabot.yml) to combine updates into single pull requests (`gradle-updates` and `actions-updates`), ensuring PR checks remain clean and manageable.
