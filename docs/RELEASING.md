# Releasing PhotoUploader

## 1. Release Architecture Overview

| Feature &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  | Overview                                                                                                                                                                                                                                                                                                                                                                         |
|:-----------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Trigger**                                                      | `git push origin vMAJOR.MINOR.PATCH` only.<br>Commits never release.                                                                                                                                                                                                                                                                                                             |
| **Gate**                                                         | Tag must match `^v\d+\.\d+\.\d+$`, point at a commit **on main**, and its `MAJOR.MINOR` must equal `cfg-appMajor` / `cfg-appMinor` in `libs.versions.toml`.<br>`MAJOR` must be at least 1 (macOS packaging) and `PATCH` at most 65535 (MSI limit).                                                                                                                               |
| **Version**                                                      | The tag's `PATCH` is passed as `BUILD_NUMBER`, so tag `v1.0.3` ships as `1.0.3` in MSI/DMG/DEB.<br>CI builds keep using `GITHUB_RUN_NUMBER` and are never distributed.<br>The stage (e.g. "Beta 1") still comes from `libs.versions.toml`; a non-empty stage marks the GitHub Release as *pre-release*.<br>**PATCH must increase with every release** or MSI/DMG upgrades break. |
| **Jobs**                                                         | `verify` → `test` (`allTests` on Linux, Windows, macOS) → `build-linux` / `build-windows` / `build-macos` (parallel) → `sign-linux` / `sign-windows` (SignPath, needs your approval) → `checksums` (`SHA256SUMS.txt` + GPG signature) → `publish`                                                                                                                                |
| **Secrets**                                                      | Only in the GitHub Environment `release`.<br>Signing jobs are separate from the Gradle build jobs, except macOS, where Compose signs during packaging.                                                                                                                                                                                                                           |
| **Publish order**                                                | Draft GitHub Release → `scp` to website → un-draft.<br>`latest.json` on the website (version, file names, SHA-256 of each file) is uploaded last; the landing page can read it and link to `<version>/SHA256SUMS.txt` and `.asc`.<br>User-facing text: [VERIFYING_DOWNLOADS.md](VERIFYING_DOWNLOADS.md).                                                                         |

## 2. GitHub Repository Setup (One-Time)

1. **Settings → Environments → New environment `release`.**
   - *Deployment branches and tags*: "Selected branches and tags" → add tag rule `v*`. Secrets are then
     unavailable to any other ref, even if someone edits a workflow.
   - Required reviewers: optional. As sole developer, the tag push is already your conscious decision;
     adding yourself as reviewer only adds a second click.
2. **Settings → Rules → Rulesets → New tag ruleset**, target `v*`: restrict creations, updates and deletions
   (bypass: repository admin). Release tags become immutable.
3. **Settings → Actions → General**: "Workflow permissions" = read-only; require approval for fork PR workflows.
4. Enable 2FA (hardware key/passkey) on your GitHub account. Whoever controls it controls every secret below.
5. Add the secrets and variables from the next section under **Environments → release** (not repository-wide).
6. Supply chain: pin every `uses:` to a full commit SHA before the first release (`npx pinact run` does this)
   and enable Dependabot for `github-actions`.
7. Make sure `composeApp/src/desktopMain/resources/client_secrets.json` is in `.gitignore`. The release
   workflow writes that file from a secret; it must never be committed.

## 3. Secrets & Environment Variables

Items added under **Settings → Environments → release** are split into two categories:

* **Environment Secrets** (`secrets.X`): sensitive credentials (passwords, private keys, API tokens, server
  details). GitHub encrypts them and masks them as `***` in build logs.
* **Environment Variables** (`vars.X`): public, non-sensitive configuration (project slugs, IDs, flags). They
  are shown in plain text in the GitHub settings **and in the logs of this public repository**.

### Environment Secrets (`secrets.*`)

| Secret Name &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;   | Used by Job &nbsp; &nbsp; &nbsp; &nbsp;  | What it contains                                                                                                                                                            |
|:----------------------------------------------------------------------|:-----------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `GOOGLE_CLIENT_SECRETS_JSON`                                          | build jobs                               | Contents of `client_secrets.json`. It ships inside the application, and Google treats installed-app secrets as non-confidential: keep it out of git, not out of the binary. |
| `MACOS_CERT_P12_BASE64`                                               | build-macos                              | Base64 string of the Developer ID Application `.p12` certificate.                                                                                                           |
| `MACOS_CERT_PASSWORD`                                                 | build-macos                              | Password protecting the `.p12` certificate.                                                                                                                                 |
| `MACOS_SIGNING_IDENTITY`                                              | build-macos                              | The exact text after `Developer ID Application: `, e.g. `Your Name (TEAMID)`.                                                                                               |
| `APPLE_NOTARY_KEY_P8_BASE64`                                          | build-macos                              | Base64 string of the App Store Connect API key (`AuthKey_XXXX.p8`).                                                                                                         |
| `APPLE_NOTARY_KEY_ID`                                                 | build-macos                              | Key ID of the App Store Connect API key.                                                                                                                                    |
| `APPLE_NOTARY_ISSUER_ID`                                              | build-macos                              | Issuer ID (UUID) from App Store Connect.                                                                                                                                    |
| `SIGNPATH_API_TOKEN`                                                  | sign-windows                             | API token of a SignPath *submitter* user.                                                                                                                                   |
| `GPG_PRIVATE_KEY`                                                     | checksums, sign-linux                    | ASCII-armored export of the GPG **signing subkey**.                                                                                                                         |
| `GPG_PASSPHRASE`                                                      | checksums, sign-linux                    | Passphrase of the GPG signing subkey.                                                                                                                                       |
| `GPG_KEY_FINGERPRINT`                                                 | checksums, sign-linux                    | Full fingerprint of the GPG signing subkey.                                                                                                                                 |
| `WEBSITE_SSH_KEY`                                                     | publish                                  | Private ed25519 key dedicated to the cPanel upload.                                                                                                                         |
| `WEBSITE_KNOWN_HOSTS`                                                 | publish                                  | The server's host key line (pins the server, no blind trust).                                                                                                               |
| `WEBSITE_HOST`                                                        | publish                                  | Hostname or IP address of the cPanel server.                                                                                                                                |
| `WEBSITE_USER`                                                        | publish                                  | SSH username on cPanel.                                                                                                                                                     |
| `WEBSITE_PORT`                                                        | publish                                  | SSH port (`22` or the custom cPanel SSH port).                                                                                                                              |
| `WEBSITE_DOWNLOAD_DIR`                                                | publish                                  | Absolute download directory, e.g. `/home/USER/public_html/photouploader/downloads`.                                                                                         |

The four `WEBSITE_*` connection values are secrets on purpose: `release.yml` reads them as `secrets.*`, and as
variables they would publish your SSH host, username and server path in the logs of a public repository.

### Environment Variables (`vars.*`)

| Variable Name &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  | Used by Job &nbsp; &nbsp; &nbsp; &nbsp;  | What it contains                                                                                              |
|:-----------------------------------------------------------------------|:-----------------------------------------|:--------------------------------------------------------------------------------------------------------------|
| `SIGNPATH_ORGANIZATION_ID`                                             | sign-windows                             | Your SignPath organization ID.                                                                                |
| `SIGNPATH_PROJECT_SLUG`                                                | sign-windows                             | Your SignPath project slug, e.g. `photouploader`.                                                             |
| `SIGNPATH_SIGNING_POLICY_SLUG`                                         | sign-windows                             | Your SignPath signing policy slug, e.g. `release-signing`.                                                    |
| `WINDOWS_SIGNING`                                                      | sign-windows                             | Set to `signpath` once SignPath has accepted the project. While unset, the Windows MSI is published unsigned. |

## 4. Obtaining Credentials

### 4.1 macOS (Apple Developer ID + Notarization)

1. Enrolment alone does not give you a Developer ID: you must create the *Developer ID Application*
   certificate yourself (step 3). Only the **Account Holder** can create it, so do this with the Apple ID that
   enrolled. As a sole trader you are most likely enrolled as an individual, so the certificate (and the
   Gatekeeper dialog) shows your personal name; an organization enrolment needs a legal entity and a
   D-U-N-S number.
2. On a Mac: Keychain Access → Certificate Assistant → *Request a Certificate from a Certificate Authority*
   → save the CSR to disk.
3. developer.apple.com → Certificates → **+ → Developer ID Application** → upload the CSR → download the
   `.cer` and double-click it (it pairs with the private key already in your login keychain).
4. Keychain Access → My Certificates → right-click "Developer ID Application: …" → *Export* → `.p12`
   with a strong password (generate it in your password manager).
5. Copy the Base64 form of the `.p12` straight to the clipboard, so no extra file with your private key is
   created:
   ```bash
   base64 -i cert.p12 | pbcopy
   ```
   Paste it into `MACOS_CERT_P12_BASE64`. The identity text in the certificate name (after
   `Developer ID Application: `) goes into `MACOS_SIGNING_IDENTITY`.
6. App Store Connect → Users and Access → **Integrations → App Store Connect API → Team Keys → +**.
   Access "Developer" is enough for notarization. Download `AuthKey_XXXX.p8` **once** (Apple will not show it
   again). Note the Key ID and the Issuer ID, then copy the Base64 form of the key:
   ```bash
   base64 -i AuthKey_XXXX.p8 | pbcopy
   ```
   Paste it into `APPLE_NOTARY_KEY_P8_BASE64`.
7. Store the `.p12`, its password and the `.p8` in your password manager / encrypted backup, then delete the
   loose files from Downloads and empty the Trash. If you saved any Base64 output to a file instead of the
   clipboard, delete that file too: it contains the private key.

The workflow uses `xcrun notarytool` with the API key (no Apple ID password or app-specific password).
If App Store Connect does not offer Team keys for your individual account, use
`notarytool --apple-id ... --team-id ... --password <app-specific password>` instead and replace the three
`APPLE_NOTARY_*` secrets accordingly (the workflow step must then be changed as well).

### 4.2 Windows (SignPath Foundation)

The certificate is issued to *SignPath Foundation*, so Windows shows that name as publisher. This is the
free route for open-source projects and the only free one that works for a sole trader.

**Conditions** (https://signpath.org/terms): OSI-approved license (Apache 2.0 qualifies), actively maintained,
documented on the download page, MFA for everyone with access to GitHub and SignPath, no proprietary
components, and the project **must already be released in the form that will be signed**.

**Steps**:

1. **Publish a first release unsigned.** Leave the variable `WINDOWS_SIGNING` unset; the workflow then
   publishes the MSI as built (the job passes it through). Make sure the landing page describes the app.
2. **Add the "Code signing policy" section** from `docs/CODE_SIGNING_POLICY.md` to `README.md` *and* the
   website's home and download pages (mandatory wording).
3. **Apply** at https://signpath.org/apply (they review project reputation; acceptance is their decision).
4. After acceptance, in the SignPath web UI:
   - Enable MFA on your SignPath account.
   - Project settings: link the **GitHub.com** trusted build system, add the artifact configuration from
     `signpath/artifact-configuration.xml` (adjust `subject`/`author` to your MSI's real properties), and
     create the signing policy `release-signing` (release signing, manual approval, you as approver).
   - Create a user (or use yours) with *submitter* permission and generate an **API token**.
   - Note the organization ID.
5. In GitHub (Environment `release`): add the secret `SIGNPATH_API_TOKEN`; add the variables
   `SIGNPATH_ORGANIZATION_ID`, `SIGNPATH_PROJECT_SLUG`, `SIGNPATH_SIGNING_POLICY_SLUG`, then
   `WINDOWS_SIGNING` = `signpath`.

How a release behaves: the `sign-windows` job submits the MSI, then **waits up to 3 hours for you to approve**
the request in SignPath (you get an e-mail). After approval, it downloads the signed MSI and verifies the
signature and signer name before publishing. SignPath only accepts workflows whose jobs all ran on
GitHub-hosted runners (true for this workflow). SignPath Foundation may also configure pipeline policies for
your project (e.g. branch rulesets); follow their onboarding instructions.

### 4.3 Linux GPG Signature

Use a dedicated key for releases, with a signing subkey you can revoke without losing your identity:

```bash
gpg --quick-generate-key "True Pine Apps Releases <photouploader@truepineapps.com>" ed25519 cert 3y
gpg --list-keys --with-colons | grep ^fpr           # note the primary fingerprint FPR
gpg --quick-add-key FPR ed25519 sign 1y
gpg --list-secret-keys --with-subkey-fingerprints   # note the SUBKEY fingerprint -> GPG_KEY_FINGERPRINT
gpg --gen-revoke FPR > revoke-FPR.asc               # store OFFLINE
gpg --armor --export-secret-subkeys SUBKEY_FPR! > subkey.asc   # -> GPG_PRIVATE_KEY (the "!" matters)
gpg --armor --export FPR > photouploader-release-key.asc        # PUBLIC key
```

- Back up the **primary** secret key (`gpg --export-secret-keys`) and the revocation certificate offline
  (encrypted USB/password manager). The CI only ever receives the subkey. Delete `subkey.asc` after pasting
  it into the secret.
- Publish the public key and its fingerprint on the website and in the repo (`README`/`INSTALL.md`); see
  [VERIFYING_DOWNLOADS.md](VERIFYING_DOWNLOADS.md) for the user-facing instructions.
- Put a reminder in your calendar to extend the subkey expiry (`gpg --edit-key`) before it lapses.

### 4.4 cPanel Upload Key

1. Locally: `ssh-keygen -t ed25519 -f photouploader_deploy -C "github-actions-photouploader" -N ""`.
2. cPanel → **Security → SSH Access → Manage SSH Keys → Import Key**: paste the *public* key, then
   **Manage → Authorize**. (If SSH Access is missing, ask the host to enable it.)
3. Least privilege: ideally a dedicated cPanel account / SSH user whose home only contains the downloads
   folder; at minimum a dedicated key, so it can be revoked on its own.
4. Host key for pinning: `ssh-keyscan -p PORT -t ed25519 your.host.example` → paste the output line into
   `WEBSITE_KNOWN_HOSTS`. Verify the fingerprint against what cPanel / your host reports.
5. Paste the **private** key (`photouploader_deploy`, whole file including the BEGIN/END lines) into
   `WEBSITE_SSH_KEY`, then delete the local copy (`shred -u photouploader_deploy`; keep the encrypted backup
   in your password manager).

## 5. Cutting a Release

1. Make sure `main` is green in CI and `cfg-appMajor` / `cfg-appMinor` / `cfg-appStage` in
   `libs.versions.toml` are right.
2. `git switch main && git pull`
3. `git tag -a v1.0.3 -m "PhotoUploader 1.0.3" && git push origin v1.0.3`
4. Watch Actions → *Release*. **Approve the Windows signing request in SignPath when prompted** (the job
   waits up to 3 hours).
5. If a job fails, fix the cause and use **Re-run failed jobs** (same tag, same version). If the fix needs a
   code change, do not touch the tag: tags are protected. Bump PATCH and tag again.
6. The release appears as a draft first, then is published when the website upload has succeeded.

## 6. Things to Check on the First Real Run

- **macOS notarization rejects the app** (the most likely first failure). The workflow prints Apple's log.
  Common causes for a JVM app: missing hardened-runtime entitlements (`allow-jit`,
  `allow-unsigned-executable-memory`, `disable-library-validation`; pass an `entitlementsFile` in the `macOS {}`
  block if the plugin defaults are not enough) and an unsigned native library inside a jar (e.g. JNA's
  `libjnidispatch.dylib`). Keep `bundleID` in the `macOS {}` block set to a fixed value.
- **macOS is arm64 only** (runner `macos-15`). For Intel Macs add a second job on `macos-15-intel`.
- **Windows**: only the MSI is signed; the launcher `.exe` and DLLs inside are not. SignPath can deep-sign
  nested files (`<pe-file>` entries inside `<msi-file>`), but Foundation rules allow signing only binaries
  built from your own source, so keep it MSI-only unless they say otherwise.
- **JDK**: `desktopApp/build.gradle.kts` uses `JAVA_HOME` when `GITHUB_ACTIONS=true` (JDK 21 from
  `setup-java`) and your local paths otherwise.
- **Android SDK**: the root project applies the Android plugins, so Gradle needs the Android SDK
  (compileSdk 37). Hosted runners include an SDK; if configuration fails on a missing `android-37` platform,
  add `android-actions/setup-android` to `.github/actions/setup-build`.
- **License report**: `generateLicenseReport` is not configuration-cache compatible (see your own comment in
  `desktopApp/build.gradle.kts`); both workflows run packaging with `--no-parallel`. If Gradle fails on the
  configuration cache, add `--no-configuration-cache`.
- `PatchDmgPackage` is currently unused (only `PatchDebPackage` is wired in). Keep it that way: modifying the
  DMG after signing/notarization would invalidate both.

## 7. If a Secret Leaks

| Secret &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  | Action                                                                                              |
|:----------------------------------------------------------------|:----------------------------------------------------------------------------------------------------|
| macOS certificate                                               | developer.apple.com → revoke the Developer ID certificate; issue a new one.                         |
| Notary API key                                                  | App Store Connect → revoke the key; create a new one.                                               |
| SignPath token                                                  | Revoke the API token in SignPath and create a new one; tell SignPath support if you suspect misuse. |
| GPG subkey                                                      | Revoke the subkey with the primary key and publish the updated public key.                          |
| SSH key                                                         | Remove it in cPanel → Manage SSH Keys; create a new pair.                                           |
| Google client secrets                                           | Create new OAuth client credentials in the Google Cloud console and update the secret.              |
