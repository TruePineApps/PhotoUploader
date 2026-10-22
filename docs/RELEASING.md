# Releasing PhotoUploader

## 1. Release Architecture Overview

| Feature &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;   | Overview                                                                                                                                                                                                                                                                                                                                          |
|:------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Trigger**                                                       | `git push origin vMAJOR.MINOR.PATCH` only.<br>Commits never release.                                                                                                                                                                                                                                                                              |
| **Gate**                                                          | Tag must match `^v\d+\.\d+\.\d+$`, point at a commit **on main**,<br>and its `MAJOR.MINOR` must equal `cfg-appMajor`/`cfg-appMinor` in `libs.versions.toml`.                                                                                                                                                                                      |
| **Version**                                                       | The tag's `PATCH` is passed as `BUILD_NUMBER`, so tag `v1.0.3` ships as `1.0.3` in MSI/DMG/DEB.<br>(CI builds keep using `GITHUB_RUN_NUMBER`.) The "Beta 1" stage still comes from `libs.versions.toml`;<br>a non-empty stage marks the GitHub Release as *pre-release*.<br>**PATCH must increase with every release** or MSI/DMG upgrades break. |
| **Jobs**                                                          | `verify` $\rightarrow$ `test` (`allTests`) $\rightarrow$ `build-linux` / `build-windows` / `build-macos` (parallel)<br>$\rightarrow$ `sign-linux` / `sign-windows` (SignPath, needs your approval)<br>$\rightarrow$ `checksums` (`SHA256SUMS.txt` + GPG signature) $\rightarrow$ `publish`                                                        |
| **Secrets**                                                       | Only in the GitHub Environment `release`.<br>Signing jobs are separate from the Gradle build jobs (except macOS, where Compose signs during packaging).                                                                                                                                                                                           |
| **Publish order**                                                 | Draft GitHub Release $\rightarrow$ `scp` to website $\rightarrow$ un-draft.<br>`latest.json` on the website (version, file names, SHA-256 of each file) is uploaded last;<br>the landing page can read it and link to `<version>/SHA256SUMS.txt` and `.asc`.<br>User-facing text: [`docs/VERIFYING_DOWNLOADS.md`](VERIFYING_DOWNLOADS.md).        |

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
5. Add the secrets and variables in the next section under **Environments → release** (not repository-wide).
6. Supply chain: pin every `uses:` to a full commit SHA before the first release (`npx pinact run` does this) and enable Dependabot for `github-actions`.

## 3. Secrets & Environment Variables

In GitHub Actions, items added under **Settings → Environments → release** are split into two categories:

* **Environment Secrets** (`secrets.X`): Sensitive credentials (passwords, private keys, API tokens). GitHub encrypts these values and automatically masks them with `***` in build execution logs.
* **Environment Variables** (`vars.X`): Public, non-sensitive configuration parameters (project slugs, organization IDs, flags). These remain visible in plain text in GitHub settings and build logs.

### Environment Secrets (`secrets.*`)

| Secret Name &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | Used by Job &nbsp; &nbsp; &nbsp; &nbsp; | What it contains                                                                                                            |
|:------------------------------------------------------------------------------------------------|:----------------------------------------|:----------------------------------------------------------------------------------------------------------------------------|
| `GOOGLE_CLIENT_SECRETS_JSON`                                                                    | build jobs                              | Contents of `client_secrets.json`. (Ships inside the application; Google treats installed-app secrets as non-confidential). |
| `MACOS_CERT_P12_BASE64`                                                                         | build-macos                             | Base64 string of the Developer ID Application `.p12` certificate.                                                           |
| `MACOS_CERT_PASSWORD`                                                                           | build-macos                             | Password protecting the `.p12` certificate.                                                                                 |
| `MACOS_SIGNING_IDENTITY`                                                                        | build-macos                             | Developer identity string (the exact text after `Developer ID Application: `, e.g. `Your Name (TEAMID)`).                   |
| `APPLE_NOTARY_KEY_P8_BASE64`                                                                    | build-macos                             | Base64 string of the App Store Connect API key (`AuthKey_XXXX.p8`).                                                         |
| `APPLE_NOTARY_KEY_ID`                                                                           | build-macos                             | Key ID of the App Store Connect API key.                                                                                    |
| `APPLE_NOTARY_ISSUER_ID`                                                                        | build-macos                             | Issuer ID (UUID) from App Store Connect.                                                                                    |
| `SIGNPATH_API_TOKEN`                                                                            | sign-windows                            | API token of a SignPath *submitter* user account.                                                                           |
| `GPG_PRIVATE_KEY`                                                                               | sign-linux                              | ASCII-armored export of the GPG **signing subkey**.                                                                         |
| `GPG_PASSPHRASE`                                                                                | sign-linux                              | Passphrase for the GPG signing subkey.                                                                                      |
| `GPG_KEY_FINGERPRINT`                                                                           | sign-linux                              | Full fingerprint of the GPG signing subkey.                                                                                 |
| `WEBSITE_SSH_KEY`                                                                               | publish                                 | Private OpenSSH key dedicated to cPanel SCP deployment.                                                                     |
| `WEBSITE_KNOWN_HOSTS`                                                                           | publish                                 | Server host key entry (pins the SSH host key to prevent MITM attacks).                                                      |

### Environment Variables (`vars.*`)

| Variable Name &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;   | Used by Job &nbsp; &nbsp; &nbsp; &nbsp;   | What it contains                                                                                          |
|:----------------------------------------------------------------------------------------------------|:------------------------------------------|:----------------------------------------------------------------------------------------------------------|
| `SIGNPATH_ORGANIZATION_ID`                                                                          | sign-windows                              | Your SignPath Organization ID string.                                                                     |
| `SIGNPATH_PROJECT_SLUG`                                                                             | sign-windows                              | Your SignPath Project slug (e.g., `photouploader`).                                                       |
| `SIGNPATH_SIGNING_POLICY_SLUG`                                                                      | sign-windows                              | Your SignPath Signing Policy slug (e.g., `release-signing`).                                              |
| `WINDOWS_SIGNING`                                                                                   | sign-windows                              | Set to `signpath` once SignPath has accepted the project. (If unset, Windows MSI is published unsigned).  |
| `WEBSITE_HOST`                                                                                      | publish                                   | Hostname or IP address of the cPanel hosting server.                                                      |
| `WEBSITE_USER`                                                                                      | publish                                   | SSH username for cPanel.                                                                                  |
| `WEBSITE_PORT`                                                                                      | publish                                   | SSH port (e.g. `22` or cPanel custom SSH port).                                                           |
| `WEBSITE_DOWNLOAD_DIR`                                                                              | publish                                   | Absolute directory path on cPanel for downloads (e.g., `/home/USER/public_html/photouploader/downloads`). |

## 4. Obtaining Credentials

### 4.1 macOS (Apple Developer ID + Notarization)

1. Enrolment alone does not give you a Developer ID: you must create the *Developer ID Application*
   certificate yourself. Only the **Account Holder** can create it, so do this with the Apple ID that
   enrolled. As a sole trader you are most likely enrolled as an individual, so the certificate (and the
   Gatekeeper dialog) shows your personal name; an organization enrolment needs a legal entity and a D-U-N-S number.
2. On a Mac: Keychain Access → Certificate Assistant → *Request a Certificate from a Certificate Authority*
   → save the CSR to disk.
3. developer.apple.com → Certificates → **+ → Developer ID Application** → upload the CSR → download the
   `.cer` and double-click it (it pairs with the private key already in your login keychain).
4. Keychain Access → My Certificates → right-click "Developer ID Application: …" → *Export* → `.p12`
   with a strong password (generate it in your password manager).
5. Base64-encode the `.p12` file to a text file:
   ```bash
   base64 -i cert.p12 -o cert_base64.txt
   ```
   Paste the contents of `cert_base64.txt` into `MACOS_CERT_P12_BASE64`.
   The identity text in the certificate name (after `Developer ID Application: `) → `MACOS_SIGNING_IDENTITY`.
6. App Store Connect → Users and Access → **Integrations → App Store Connect API → Team Keys → +**.
   Access "Developer" is enough for notarization. Download `AuthKey_XXXX.p8` **once** (Apple will not show it again).
   Note Key ID and Issuer ID. Base64-encode the `.p8` file:
   ```bash
   base64 -i AuthKey_XXXX.p8 -o authkey_base64.txt
   ```
   Paste the contents of `authkey_base64.txt` into `APPLE_NOTARY_KEY_P8_BASE64`.
7. Store the `.p12`, its password, and the `.p8` file in your password manager / encrypted backup,
   then delete the loose files from Downloads and the Trash.

The workflow uses `xcrun notarytool` with the API key (no Apple ID password or app-specific password).

### 4.2 Windows (SignPath Foundation)

The certificate is issued to *SignPath Foundation*, so Windows shows that name as publisher. This is the
free route for open-source projects.

**Steps**:

1. **Publish a first release unsigned.** Leave the repository variable `WINDOWS_SIGNING` unset; the workflow
   then publishes the MSI as built (the job passes it through). Make sure the landing page describes the app.
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
5. In GitHub (Environment `release`): add secret `SIGNPATH_API_TOKEN`; add variables
   `SIGNPATH_ORGANIZATION_ID`, `SIGNPATH_PROJECT_SLUG`, `SIGNPATH_SIGNING_POLICY_SLUG`, then
   `WINDOWS_SIGNING=signpath`.

How a release behaves: the `sign-windows` job submits the MSI, then **waits up to 3 hours for you to approve**
the request in SignPath (you get an e-mail). After approval, it downloads the signed MSI and verifies the
signature and signer name before publishing.

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
  (encrypted USB/password manager). The CI only ever receives the subkey.
- Publish the public key and fingerprint on the website and in the repo
  (`README`/`INSTALL.md`: `gpg --verify PhotoUploader-1.0.3-linux-amd64.deb.asc PhotoUploader-1.0.3-linux-amd64.deb`).

### 4.4 cPanel Upload Key

1. Locally: `ssh-keygen -t ed25519 -f photouploader_deploy -C "github-actions-photouploader" -N ""`.
2. cPanel → **Security → SSH Access → Manage SSH Keys → Import Key**: paste the *public* key,
   then **Manage → Authorize**.
3. Host key for pinning: `ssh-keyscan -p PORT -t ed25519 your.host.example` → paste the output line into
   `WEBSITE_KNOWN_HOSTS`.
4. Paste the **private** key (`photouploader_deploy`) into `WEBSITE_SSH_KEY`.

## 5. Cutting a Release

1. Make sure `main` is green in CI and `cfg-appMajor/Minor/Stage` in `libs.versions.toml` are right.
2. `git switch main && git pull`
3. `git tag -a v1.0.3 -m "PhotoUploader 1.0.3" && git push origin v1.0.3`
4. Watch Actions → *Release*. Approve the Windows signing request in SignPath when prompted.
5. The release appears as a draft first, then is published when the website upload has succeeded.
