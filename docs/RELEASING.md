# Releasing PhotoUploader

## 1. Release Architecture Overview

| Feature &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | Overview                                                                                                                                                                                                                                                                                                                                                                                                                  |
| :-------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Trigger**                                                     | `git push origin vMAJOR.MINOR.PATCH` only.<br>Commits never release.                                                                                                                                                                                                                                                                                                                                                      |
| **Gate**                                                        | The tag must match `^v\d+\.\d+\.\d+$`, point at a commit **on main**, and be exactly `v` + the version in `libs.versions.toml` (`cfg-appMajor.cfg-appMinor.cfg-appPatch`).<br>It must also be higher than every earlier release tag. `MAJOR` must be at least 1 (macOS packaging), `MINOR` at most 255 and `PATCH` at most 65535 (MSI limits).                                                                            |
| **Version**                                                     | The version is maintained in `libs.versions.toml`: `cfg-appMajor`, `cfg-appMinor`, `cfg-appPatch` (human-maintained, bumped in a commit) and `cfg-appStage` (e.g. "Beta 1"; a non-empty stage marks the GitHub Release as *pre-release*).<br>The tag only confirms that version; it is never the source of the version.<br>The CI build number (`GITHUB_RUN_NUMBER`) is kept separate and is not part of the app version. |
| **Jobs**                                                        | `verify` → `test` (`allTests` on Linux, Windows, macOS) → `build-linux` / `build-windows` / `build-macos` (parallel) → `sign-linux` / `sign-windows` (SignPath, needs your approval) → `checksums` (`SHA256SUMS.txt` + GPG signature) → `publish`                                                                                                                                                                         |
| **Secrets**                                                     | Only in the GitHub Environment `release`.<br>Signing jobs are separate from the Gradle build jobs, except macOS, where Compose signs during packaging.                                                                                                                                                                                                                                                                    |
| **Publish order**                                               | Draft GitHub Release → `scp` to website → un-draft.<br>`latest.json` on the website (version, file names, SHA-256 of each file) is uploaded last; the landing page can read it and link to `<version>/SHA256SUMS.txt` and `.asc`.<br>User-facing text: [VERIFYING_DOWNLOADS.md](VERIFYING_DOWNLOADS.md).                                                                                                                  |

## 2. GitHub Repository Setup (One-Time)

1. **Settings → Environments → New environment `release`.**
   - *Deployment branches and tags*: "Selected branches and tags" → add tag rule `v*`. Secrets are then
     unavailable to any other ref, even if someone edits a workflow.
   - Required reviewers: optional. As sole developer, the tag push is already your conscious decision;
     adding yourself as reviewer only adds a second click.
2. **Settings → Rules → Rulesets → New tag ruleset**, target `v*`: restrict creations, updates and deletions
   (bypass: repository admin). Release tags become immutable.
3. **Settings → Actions → General**:
   - *Approval for running fork pull request workflows from contributors*: Choose **Require approval for all external contributors**.
   - *Workflow permissions*: Choose **Read repository contents and packages permissions**.
4. Enable 2FA (hardware key/passkey) on your GitHub account. Whoever controls it controls every secret below.
5. Add the secrets and variables from the next section under **Environments → release** (not repository-wide).
6. **Supply Chain Security**:
   - *Enable Dependabot*: In **Settings → Advanced Security**, enable **Dependabot version updates** by clicking 'Create dependabot.yml'. For contents, see [dependabot.yml](../.github/dependabot.yml). When `dependabot.yml` exists, GitHub enables Dependabot version updates automatically.
   - *Pin Actions to Commit SHAs*: In your workflow `.yml` files, third-party actions use tags like `uses: actions/checkout@v4`. Pinning the version of the actions to an immutable 40-character commit SHA (e.g. `uses: actions/checkout@b4ffde6... # v4.1.1`) from the open-source Git repository for workflows on GitHub Marketplace prevents supply-chain attacks if an action tag is ever maliciously altered.
   - *Replacing Version Tags with SHAs*: You can replace version tags with SHAs automatically across all workflow files using `npx pin-github-action .github/workflows/*.yml` in your terminal. (If `npx` is not installed on your machine, install Node.js/npm first: `sudo apt install nodejs npm` on Linux, or `brew install node` on macOS).
   - *When to Rerun Pinning*: You only need to rerun `npx pin-github-action` if you manually add or update action references in your `.yml` files. When Dependabot is enabled (step above), Dependabot will automatically open Pull Requests updating your pinned SHAs whenever new action releases are published.
7. Make sure `composeApp/src/desktopMain/resources/client_secrets.json` is in `.gitignore`. The release
   workflow writes that file from a secret; it must never be committed.

## 3. Secrets & Environment Variables

Items added under **Settings → Environments → release** are split into two categories:

* **Environment Secrets** (`secrets.X`): sensitive credentials (passwords, private keys, API tokens, server
  details). GitHub encrypts them and masks them as `***` in build logs.
* **Environment Variables** (`vars.X`): public, non-sensitive configuration (project slugs, IDs, flags). They
  are shown in plain text in the GitHub settings **and in the logs of this public repository**.

### Environment Secrets (`secrets.*`)

| Secret Name &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | Used by Job &nbsp; &nbsp; &nbsp; &nbsp; | What it contains                                                                                                                                                            |
| :------------------------------------------------------------------ | :-------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `GOOGLE_CLIENT_SECRETS_JSON`                                        | build jobs                              | Contents of `client_secrets.json`. It ships inside the application, and Google treats installed-app secrets as non-confidential: keep it out of git, not out of the binary. |
| `MACOS_CERT_P12_BASE64`                                             | build-macos                             | Base64 string of the Developer ID Application `.p12` certificate.                                                                                                           |
| `MACOS_CERT_PASSWORD`                                               | build-macos                             | Password protecting the `.p12` certificate.                                                                                                                                 |
| `MACOS_SIGNING_IDENTITY`                                            | build-macos                             | The exact text after `Developer ID Application: `, e.g. `Your Name (TEAMID)`.                                                                                               |
| `APPLE_NOTARY_KEY_P8_BASE64`                                        | build-macos                             | Base64 string of the App Store Connect API key (`AuthKey_XXXX.p8`).                                                                                                         |
| `APPLE_NOTARY_KEY_ID`                                               | build-macos                             | Key ID of the App Store Connect API key.                                                                                                                                    |
| `APPLE_NOTARY_ISSUER_ID`                                            | build-macos                             | Issuer ID (UUID) from App Store Connect.                                                                                                                                    |
| `SIGNPATH_API_TOKEN`                                                | sign-windows                            | API token of the SignPath CI user (Reader on the project, Submitter on the release policy).                                                                                 |
| `GPG_PRIVATE_KEY`                                                   | checksums, sign-linux                   | ASCII-armored export of the GPG **signing subkey**.                                                                                                                         |
| `GPG_PASSPHRASE`                                                    | checksums, sign-linux                   | Passphrase of the GPG signing subkey.                                                                                                                                       |
| `GPG_KEY_FINGERPRINT`                                               | checksums, sign-linux                   | Full fingerprint of the GPG signing subkey.                                                                                                                                 |
| `WEBSITE_SSH_KEY`                                                   | publish                                 | Private ed25519 key dedicated to the cPanel upload.                                                                                                                         |
| `WEBSITE_KNOWN_HOSTS`                                               | publish                                 | The server's host key line (pins the server, no blind trust).                                                                                                               |
| `WEBSITE_HOST`                                                      | publish                                 | Hostname or IP address of the cPanel server.                                                                                                                                |
| `WEBSITE_USER`                                                      | publish                                 | SSH username on cPanel.                                                                                                                                                     |
| `WEBSITE_PORT`                                                      | publish                                 | SSH port (`22` or the custom cPanel SSH port).                                                                                                                              |
| `WEBSITE_DOWNLOAD_DIR`                                              | publish                                 | Absolute download directory, e.g. `/home/USER/public_html/photouploader/downloads`.                                                                                         |

The four `WEBSITE_*` connection values are secrets on purpose: `release.yml` reads them as `secrets.*`, and as
variables they would publish your SSH host, username and server path in the logs of a public repository.

### Environment Variables (`vars.*`)

| Variable Name &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | Used by Job &nbsp; &nbsp; &nbsp; &nbsp; | What it contains                                                                                              |
| :-------------------------------------------------------------------- | :-------------------------------------- | :------------------------------------------------------------------------------------------------------------ |
| `SIGNPATH_ORGANIZATION_ID`                                            | sign-windows                            | Your SignPath organization ID.                                                                                |
| `SIGNPATH_PROJECT_SLUG`                                               | sign-windows                            | Your SignPath project slug, e.g. `photouploader`.                                                             |
| `SIGNPATH_SIGNING_POLICY_SLUG`                                        | sign-windows                            | Your SignPath signing policy slug, e.g. `release-signing`.                                                    |
| `WINDOWS_SIGNING`                                                     | sign-windows                            | Set to `signpath` once SignPath has accepted the project. While unset, the Windows MSI is published unsigned. |

## 4. Obtaining Credentials

### 4.1 macOS (Apple Developer ID + Notarization)

Do steps 1 to 4 on **one Mac**, logged in as the same user throughout, because the private key of the
certificate lives in that user's login keychain. The Apple ID used must be the **Account Holder** of the
developer account. A sole trader is most likely enrolled as an individual, so the certificate shows your personal
name; organization enrolment needs a legal entity and a D-U-N-S number. Enrolment alone does not give you a
Developer ID: you create the certificate yourself below.

**Step 1: create the certificate.**

1. Keychain Access → menu *Keychain Access* → *Certificate Assistant* → *Request a Certificate From a
   Certificate Authority…* Fill in your Apple ID e-mail and a Common Name (any label, e.g.
   `True Pine Apps Developer ID`), leave *CA Email Address* empty, choose **Saved to disk**. This creates the
   file `CertificateSigningRequest.certSigningRequest` **and a private key in your login keychain**. Never
   delete that key.
2. developer.apple.com → Account → *Certificates, IDs & Profiles* → *Certificates* → **+** → *Developer ID
   Application* → profile type *G2 Sub-CA* → upload the CSR → download `developerID_application.cer`.
3. Double-click the `.cer`. In Keychain Access → *login* → *My Certificates*, the entry
   "Developer ID Application: Your Name (ABCDE12345)" must show a private key when you expand its arrow. If
   there is no key below it, you are on the wrong Mac or in the wrong keychain.

**Step 2: find the identity text.** Run:

```bash
security find-identity -v -p codesigning
```

```text
  1) 3F1A9C...(40 hex characters)  "Developer ID Application: Jan Jansen (ABCDE12345)"
```

Copy only what follows `Developer ID Application: ` up to the closing quote: `Jan Jansen (ABCDE12345)`.
That is the secret `MACOS_SIGNING_IDENTITY`. The 10 characters in parentheses are your Team ID (also shown
at developer.apple.com → Membership). The 40-hex number at the start is not used.

**Step 3: export the certificate.** Keychain Access → *My Certificates* → select the certificate row (not the
key below it) → *File* → *Export Items…* → format *Personal Information Exchange (.p12)* → name it
`developerID_application.p12`. You are asked twice for a password:

* The **export password** you set: invent a long random one, save it in your password manager first. It
  becomes the secret `MACOS_CERT_PASSWORD`.
* Your **Mac login password**, to allow the export. It is not stored anywhere.

**Step 4: copy the certificate to GitHub without leaving a file.**

```bash
base64 -i developerID_application.p12 | pbcopy
```

Paste it immediately into the secret `MACOS_CERT_P12_BASE64`, then copy any harmless text so the clipboard no
longer holds the key. Store the `.p12` file itself in your password manager (this is your only backup; Apple
cannot re-issue the private key).

**Step 5: create the notarization API key.** App Store Connect → *Users and Access* → *Integrations* →
*App Store Connect API* → *Team Keys*. The first time, the Account Holder may have to click *Request Access*.
Then **+** → name `GitHub notarization`, access *Developer*. You need three values from this page:

| What you see &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | Where on the page                                       | GitHub secret                            |
| :----------------------------------------------------- | :------------------------------------------------------ | :--------------------------------------- |
| Issuer ID (a UUID)                                     | at the top of the keys list                             | `APPLE_NOTARY_ISSUER_ID`                 |
| Key ID (10 characters)                                 | column *Key ID* of your new key (also in the file name) | `APPLE_NOTARY_KEY_ID`                    |
| `AuthKey_<KeyID>.p8` file                              | *Download API Key*: **possible only once**              | `APPLE_NOTARY_KEY_P8_BASE64` (see below) |

```bash
base64 -i AuthKey_XXXXXXXXXX.p8 | pbcopy
```

Paste the result into `APPLE_NOTARY_KEY_P8_BASE64`, and store the `.p8` file in your password manager.

**Step 6: test the notarization credentials** before the first release:

```bash
xcrun notarytool history --key AuthKey_XXXXXXXXXX.p8 --key-id XXXXXXXXXX --issuer YOUR-ISSUER-UUID
```

"No submission history" or a list of submissions means the three values are correct; an authentication error
means one of them is wrong.

**Step 7: clean up.** Delete `CertificateSigningRequest.certSigningRequest`, the `.cer`, the `.p12` and the `.p8`
from Downloads once they are in your password manager, and empty the Trash. If you saved any Base64 output to
a file instead of the clipboard, delete that file too: it contains a private key.

If App Store Connect does not offer Team keys for your individual account, use
`notarytool --apple-id ... --team-id ... --password <app-specific password>` instead and replace the three
`APPLE_NOTARY_*` secrets accordingly (the workflow step must then be changed as well).

### 4.2 Windows (SignPath Foundation)

The certificate is issued to *SignPath Foundation*, so Windows shows that name as publisher. This is the
free route for open-source projects and the only free one that works for a sole trader.

**Conditions** (https://signpath.org/terms): OSI-approved license (Apache 2.0 qualifies), actively maintained,
documented on the download page, MFA for everyone with access to GitHub and SignPath, no proprietary
components, and the project **must already be released in the form that will be signed**.

**Before SignPath:**

1. **Publish a first release unsigned.** Leave the variable `WINDOWS_SIGNING` unset; the workflow then
   publishes the MSI as built (the job passes it through). Make sure the landing page describes the app.
2. **Add the "Code signing policy" section** from `docs/CODE_SIGNING_POLICY.md` to `README.md` *and* the
   website's home and download pages (mandatory wording).
3. **Apply** at https://signpath.org/apply (they review project reputation; acceptance is their decision).

**After acceptance.** SignPath Foundation sets up your organization and project and tells you what is left
to do, so some of the items below may already exist. Enable MFA on your SignPath account first. Then collect
these values in the SignPath web application:

| GitHub name &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | Type &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | Where to find it in SignPath                                                                                                                                                          |
| :----------------------------------------------------------- | :-------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `SIGNPATH_ORGANIZATION_ID`                                   | variable                                | Open your project → *Signing policies* → open the policy.<br>The browser address contains two UUIDs: the **first** is the organization ID,<br>the last is the policy ID (not needed). |
| `SIGNPATH_PROJECT_SLUG`                                      | variable                                | The slug (short lower-case name) of your project,<br>shown on the project page and in its address.                                                                                    |
| `SIGNPATH_SIGNING_POLICY_SLUG`                               | variable                                | The slug of the signing policy used for releases, e.g. `release-signing`.                                                                                                             |
| `SIGNPATH_API_TOKEN`                                         | secret                                  | Create a **CI user** in SignPath; the token is shown **once** when you add it.<br>Copy it into the secret and your password manager.                                                  |

Then in SignPath:

* The CI user needs *Reader* on the project and *Submitter* on the release signing policy. The token must
  belong to such a user, or the workflow is rejected.
* You are the *Approver* of the policy (manual approval for every release).
* The project's **default artifact configuration** is the one from `signpath/artifact-configuration.xml`
  (adjust `subject`/`author` to your MSI's real properties). The workflow passes no configuration slug, so
  SignPath uses the default one.
* The **GitHub.com trusted build system** is linked to the project (organization settings → trusted build
  systems). Without it, signing requests from GitHub Actions are refused.

Finally, in GitHub (Environment `release`): add the secret and the three variables from the table, then set
`WINDOWS_SIGNING` = `signpath`.

How a release behaves: the `sign-windows` job submits the MSI, then **waits up to 3 hours for you to approve**
the request in SignPath (you get an e-mail). After approval, it downloads the signed MSI and verifies the
signature and signer name before publishing. SignPath only accepts workflows whose jobs all ran on
GitHub-hosted runners (true for this workflow). SignPath Foundation may also configure pipeline policies for
your project (e.g. branch rulesets); follow their onboarding instructions. Menu names in the SignPath web
application may differ slightly from the ones above; their documentation is authoritative.

### 4.3 Linux GPG Signature

Run all commands below in **one terminal session on your own computer** (not in CI). The design uses two keys:

* a **primary key** (`[C]`, certify only): the identity. It never goes to GitHub. It can create, extend and
  revoke subkeys.
* a **signing subkey** (`[S]`): the only part GitHub receives. If it leaks, you revoke it with the primary key
  and your identity stays intact.

**Step 1: create the primary key.**
Create a strong password:
```bash
openssl rand -base64 16
```
Save it in your password manager right away; it becomes the secret `GPG_PASSPHRASE`.
Copy the password. Generate the GPG key. When gpg asks for a passphrase, paste it.
```bash
gpg --quick-generate-key "True Pine Apps <photouploader@truepineapps.com>" ed25519 cert 3y
FPR=$(gpg --list-keys --with-colons photouploader@truepineapps.com | awk -F: '/^fpr:/ {print $10; exit}')
echo "$FPR"
```

`FPR` is the **primary key fingerprint**: 40 hexadecimal characters. gpg also writes a ready-made revocation
certificate for this key to `~/.gnupg/openpgp-revocs.d/<FPR>.rev` (used in step 3).

**Step 2: add the signing subkey.**

```bash
gpg --quick-add-key "$FPR" ed25519 sign 1y
SUBFPR=$(gpg --list-keys --with-colons "$FPR" | awk -F: '/^sub:/ {insub=1} insub && /^fpr:/ {print $10; exit}')
gpg --list-secret-keys --with-subkey-fingerprints
```

The listing shows how to tell the two keys apart:

```text
sec   ed25519 2026-10-23 [C] [expires: 2029-10-22]     <- "sec" + [C] = PRIMARY key
      EA1E6E367CDA91CE341ECF3FE97F428A92D0CE8B           <- this is FPR
uid           [ultimate] True Pine Apps Releases <photouploader@truepineapps.com>
ssb   ed25519 2026-10-23 [S] [expires: 2027-10-23]     <- "ssb" + [S] = SIGNING subkey
      93A022AA62F65719FA49EC6ED970FB7E4CF34389           <- this is SUBFPR
```

(The fingerprints above are examples from a test run; yours differ. Always use the full 40 characters, never
the short key ID.)

**Step 3: export the files.** The first line creates the *primary secret key* backup.

```bash
gpg --armor --export-secret-keys "$FPR" > primary-secret-key.asc
gpg --armor --export-secret-subkeys "$SUBFPR!" > ci-signing-subkey.asc
gpg --armor --export "$FPR" > photouploader-release-key.asc
cp ~/.gnupg/openpgp-revocs.d/"$FPR".rev revocation-certificate.rev
```

The `!` after `SUBFPR` is required: it exports exactly that one subkey. If your gpg home is not `~/.gnupg`,
check with `gpg --version` (line "Home:").

**Step 4: put each item where it belongs.** The complete overview for all credentials is in section 4.5; for
GPG it is:

* `primary-secret-key.asc` and `revocation-certificate.rev` → **password manager only** (as file
  attachments). Never GitHub, never the website.
* `ci-signing-subkey.asc` → open it in a text editor, copy **the whole text** including the
  `-----BEGIN PGP PRIVATE KEY BLOCK-----` and `-----END ...-----` lines into the secret `GPG_PRIVATE_KEY`.
  Optionally back it up in your password manager so you can easily restore `GPG_PRIVATE_KEY` if the GitHub secret is ever deleted.
* The passphrase from step 1 → secret `GPG_PASSPHRASE`.
* `$SUBFPR` (the 40 characters under the `ssb` line) → secret `GPG_KEY_FINGERPRINT`.
* `photouploader-release-key.asc` and `$FPR` → **public**: website download page, the repository, and the
  fingerprint placeholder in [VERIFYING_DOWNLOADS.md](VERIFYING_DOWNLOADS.md).

**Step 5: check what CI will have.** This simulates the CI keyring: only the subkey, no primary secret key.

```bash
export GNUPGHOME=$(mktemp -d) && chmod 700 "$GNUPGHOME"
gpg --batch --import ci-signing-subkey.asc
gpg --list-secret-keys --with-subkey-fingerprints   # must show "sec#" (the # = primary secret key absent)
unset GNUPGHOME
```

**Step 6: clean up.** After the secrets are saved and the backups are in your password manager, delete the
exported secret files: `shred -u primary-secret-key.asc ci-signing-subkey.asc revocation-certificate.rev`
(on macOS use `rm -P`).

**Renewal.** The subkey expires after one year and signing then fails in CI. Before that, run
`gpg --quick-set-expire "$FPR" 1y "$SUBFPR"`, export the subkey again (step 3) and update `GPG_PRIVATE_KEY`.
Re-export and republish `photouploader-release-key.asc`, because the expiry date is part of the public key.
Put a calendar reminder in 11 months. To extend the primary key use `gpg --quick-set-expire "$FPR" 3y`.

### 4.4 cPanel Upload Key

Use a dedicated SSH key pair for the upload, so it can be revoked without touching anything else. This limits exposure if the secret is ever compromised and makes it easy to revoke access for GitHub without affecting your own access.Do this on your own computer.

**Step 1: create the key pair.**

```bash
ssh-keygen -t ed25519 -f photouploader_deploy -C "github-actions-photouploader" -N ""
```

This creates **two** files. `-N ""` means no passphrase, which is required because CI cannot type one.

* `photouploader_deploy`: the **private** key. It goes to GitHub only (secret `WEBSITE_SSH_KEY`).
* `photouploader_deploy.pub`: the **public** key (one line, starts with `ssh-ed25519`). It goes to cPanel only.

**Step 2: authorize the public key in cPanel.** cPanel → *Security* → *SSH Access* → *Manage SSH Keys* →
*Import Key*. Enter a name (e.g. `github-actions-photouploader`), leave the **private key box empty**, paste the
whole content of `photouploader_deploy.pub` into the public key box and click *Import*. Then go back to
*Manage SSH Keys*, click *Manage* next to the new key, and **Authorize** it. If *SSH Access* is missing in
cPanel, ask your host to enable it.

**Step 3: collect the connection values.**

| GitHub secret &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | How to get it                                                                                                                                                                           |
| :------------------------------------------------------ | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `WEBSITE_HOST`                                          | The server name you connect to, e.g. `server42.example-host.com` or your domain. Use the same name in step 4.                                                                           |
| `WEBSITE_USER`                                          | Your cPanel username (shown in cPanel under *General Information*).                                                                                                                     |
| `WEBSITE_PORT`                                          | The SSH port from your host's documentation or the *SSH Access* page; 22 or a custom port.                                                                                              |
| `WEBSITE_DOWNLOAD_DIR`                                  | The absolute folder for releases, e.g. `/home/USER/public_html/photouploader/downloads`. Find the home directory with the test in step 5; the workflow creates the version sub-folders. |

**Step 4: pin the server (`WEBSITE_KNOWN_HOSTS`).**

```bash
ssh-keyscan -p PORT -t ed25519 HOST
```

The line(s) it prints (not the comment line starting with `#`) go **unchanged** into the secret
`WEBSITE_KNOWN_HOSTS`. Before you trust them, check the fingerprint against your host (cPanel or support):

```bash
ssh-keyscan -p PORT -t ed25519 HOST | ssh-keygen -lf -
```

If the server has no ed25519 host key, run the command with `-t ecdsa` or `-t rsa` instead.

**Step 5: test the whole chain before the first release.**

```bash
ssh -i photouploader_deploy -p PORT -o IdentitiesOnly=yes USER@HOST 'echo connected; pwd'
echo test > test.txt
scp -i photouploader_deploy -P PORT -o IdentitiesOnly=yes test.txt USER@HOST:/home/USER/public_html/
```

`pwd` prints your home directory, which is the start of `WEBSITE_DOWNLOAD_DIR`. Delete `test.txt` on the
server afterward. If the login asks for a password, the key was not authorized in step 2.

**Step 6: store the private key.** Copy the **whole** file including the `-----BEGIN OPENSSH PRIVATE KEY-----`
and `-----END ...-----` lines into the secret `WEBSITE_SSH_KEY`:

```bash
pbcopy < photouploader_deploy        # macOS;  Linux: xclip -selection clipboard < photouploader_deploy
```

Then delete the local copies: `shred -u photouploader_deploy photouploader_deploy.pub` (macOS: `rm -P`).
A backup is not needed: if the key is lost or leaks, create a new pair and replace the key in cPanel.

Optional hardening: a dedicated cPanel account or SSH user whose home only contains the downloads folder.

### 4.5 Where Does Each Item Go?

| Item &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | Password manager | GitHub name (Environment `release`)                                    | Public                                       |
| :----------------------------------------------------------- | :--------------- | :--------------------------------------------------------------------- | :------------------------------------------- |
| GPG `primary-secret-key.asc`                                 | Yes (file)       | never                                                                  | never                                        |
| GPG `revocation-certificate.rev`                             | Yes (file)       | never                                                                  | never                                        |
| GPG `ci-signing-subkey.asc` (text)                           | Optional backup  | `GPG_PRIVATE_KEY`                                                      | never                                        |
| GPG passphrase                                               | Yes              | `GPG_PASSPHRASE`                                                       | never                                        |
| GPG subkey fingerprint (`SUBFPR`)                            | Yes (note)       | `GPG_KEY_FINGERPRINT`                                                  | no                                           |
| GPG primary fingerprint (`FPR`)                              | Yes (note)       | none                                                                   | Yes: website, README, VERIFYING_DOWNLOADS.md |
| GPG `photouploader-release-key.asc`                          | Yes (file)       | none                                                                   | Yes: website and repository                  |
| Apple `.p12` certificate file                                | Yes (file)       | `MACOS_CERT_P12_BASE64` (Base64 text)                                  | never                                        |
| Apple `.p12` password                                        | Yes              | `MACOS_CERT_PASSWORD`                                                  | never                                        |
| Apple identity text, `Name (TEAMID)`                         | Yes (note)       | `MACOS_SIGNING_IDENTITY`                                               | no                                           |
| Apple `AuthKey_XXXX.p8` file                                 | Yes (file)       | `APPLE_NOTARY_KEY_P8_BASE64` (Base64 text)                             | never                                        |
| Apple Key ID and Issuer ID                                   | Yes (note)       | `APPLE_NOTARY_KEY_ID`, `APPLE_NOTARY_ISSUER_ID`                        | no                                           |
| SignPath CI user API token                                   | Yes              | `SIGNPATH_API_TOKEN`                                                   | never                                        |
| SignPath organization ID and slugs                           | Yes (note)       | variables `SIGNPATH_*`                                                 | no                                           |
| cPanel private key `photouploader_deploy`                    | Yes (file)       | `WEBSITE_SSH_KEY`                                                      | never                                        |
| cPanel public key `photouploader_deploy.pub`                 | not needed       | none                                                                   | Authorized in cPanel only                    |
| cPanel host key line (`ssh-keyscan`)                         | Yes (note)       | `WEBSITE_KNOWN_HOSTS`                                                  | no                                           |
| cPanel host, user, port, download folder                     | Yes (note)       | `WEBSITE_HOST`, `WEBSITE_USER`, `WEBSITE_PORT`, `WEBSITE_DOWNLOAD_DIR` | no                                           |
| Google `client_secrets.json`                                 | Yes (file)       | `GOOGLE_CLIENT_SECRETS_JSON`                                           | never in git                                 |

## 5. Cutting a Release

1. Prepare the version in one commit on `main`: set `cfg-appPatch` (and `cfg-appMinor`, `cfg-appMajor`,
   `cfg-appStage` if they change) in `gradle/libs.versions.toml` and update `CHANGELOG.md`. Commit with
   sign-off, push, and wait until CI is green on that commit.
2. Run the release helper script from the root project folder:
   ```bash
   ./release.sh
   ```
   The script verifies that your working directory is clean, you are on `main` and synced with `origin/main`, confirms that the tag matching `libs.versions.toml` does not already exist, and creates/pushes the annotated version tag. (Manual `git tag` commands are strongly discouraged to avoid version mismatch or uncommitted change errors).
3. Watch Actions → *Release*. **Approve the Windows signing request in SignPath when prompted** (the job
   waits up to 3 hours).
4. If a job fails, fix the cause and use **Re-run failed jobs** (same tag, same version). If the fix needs a
   code change, do not touch the tag: tags are protected. Bump `cfg-appPatch`, commit, and run `./release.sh` again.
5. The release appears as a draft first, then is published when the website upload has succeeded.

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

| Secret &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | Action                                                                                              |
| :------------------------------------------------------------- | :-------------------------------------------------------------------------------------------------- |
| macOS certificate                                              | developer.apple.com → revoke the Developer ID certificate; issue a new one.                         |
| Notary API key                                                 | App Store Connect → revoke the key; create a new one.                                               |
| SignPath token                                                 | Revoke the API token in SignPath and create a new one; tell SignPath support if you suspect misuse. |
| GPG subkey                                                     | Revoke the subkey with the primary key and publish the updated public key.                          |
| SSH key                                                        | Remove it in cPanel → Manage SSH Keys; create a new pair.                                           |
| Google client secrets                                          | Create new OAuth client credentials in the Google Cloud console and update the secret.              |

## 8. Credential Overview Tables

### 8.1 Linux GPG Credentials

| Output / Asset                                              | What it is                                                     | Where it goes                                                                    |
|:------------------------------------------------------------|:---------------------------------------------------------------| :------------------------------------------------------------------------------- |
| **Passphrase**                                              | Password chosen during key generation.                         | **GitHub Secret**: `GPG_PASSPHRASE`<br>+ **Password Manager**                    |
| **Primary Fingerprint (`FPR`)**                             | 40-character hex string of the primary master key.             | **Password Manager** (used to manage/revoke subkeys).                            |
| **Primary Master Key** (`primary-secret-key.asc`)           | Private master key (`gpg --export-secret-keys <FPR>`).         | **Password Manager ONLY**. *(NEVER upload to GitHub!)*                           |
| **Revocation Certificate** (`revocation-certificate.rev`)   | Revocation file (`cp ~/.gnupg/openpgp-revocs.d/$FPR.rev`).     | **Password Manager ONLY**.                                                       |
| **Subkey Fingerprint (`SUBFPR`)**                           | 40-character hex string of the signing subkey.                 | **GitHub Secret**: `GPG_KEY_FINGERPRINT`                                         |
| **Signing Subkey** (`ci-signing-subkey.asc`)                | Subkey export (`gpg --export-secret-subkeys <SUBFPR>!`).       | **GitHub Secret**: `GPG_PRIVATE_KEY`<br>*(Delete file from disk after pasting).* |
| **Public Key** (`photouploader-release-key.asc`)            | Public key export (`gpg --export <FPR>`).                      | **Website Downloads Directory** + Repository                                     |

### 8.2 macOS Developer ID & Notarization Credentials

| Output / Asset                                          | What it is                                                           | Where it goes                                                                             |
| :------------------------------------------------------ | :------------------------------------------------------------------- | :---------------------------------------------------------------------------------------- |
| **Developer ID `.p12` Certificate** (`cert.p12`)        | Base64 string of Developer ID Application `.p12` file.               | **GitHub Secret**: `MACOS_CERT_P12_BASE64`<br>+ **Password Manager** (`.p12` file)        |
| **`.p12` Certificate Password**                         | Password protecting the exported `.p12` file.                        | **GitHub Secret**: `MACOS_CERT_PASSWORD`<br>+ **Password Manager**                        |
| **Developer Signing Identity**                          | Text after `Developer ID Application: ` (e.g. `Your Name (TEAMID)`). | **GitHub Secret**: `MACOS_SIGNING_IDENTITY`<br>+ **Password Manager**                     |
| **App Store Connect API Key `.p8`** (`AuthKey_XXXX.p8`) | Base64 string of `AuthKey_XXXX.p8` file.                             | **GitHub Secret**: `APPLE_NOTARY_KEY_P8_BASE64`<br>+ **Password Manager** (`.p8` file)    |
| **App Store Connect Key ID**                            | 10-character Key ID string (e.g. `4A2B3C4D5E`).                      | **GitHub Secret**: `APPLE_NOTARY_KEY_ID`<br>+ **Password Manager**                        |
| **App Store Connect Issuer ID**                         | Issuer ID UUID string from App Store Connect.                        | **GitHub Secret**: `APPLE_NOTARY_ISSUER_ID`<br>+ **Password Manager**                     |

### 8.3 Windows SignPath Credentials

| Output / Asset                     | What it is                                                | Where it goes                                                     |
|:-----------------------------------|:----------------------------------------------------------|:------------------------------------------------------------------|
| **SignPath API Token**             | Secret API Token generated for submitter user.            | **GitHub Secret**: `SIGNPATH_API_TOKEN`<br>+ **Password Manager** |
| **SignPath Organization ID**       | Organization identifier string from SignPath dashboard.   | **GitHub Variable**: `SIGNPATH_ORGANIZATION_ID`                   |
| **SignPath Project Slug**          | Project slug identifier (e.g. `photouploader`).           | **GitHub Variable**: `SIGNPATH_PROJECT_SLUG`                      |
| **SignPath Signing Policy Slug**   | Policy slug identifier (e.g. `release-signing`).          | **GitHub Variable**: `SIGNPATH_SIGNING_POLICY_SLUG`               |
| **Windows Signing Enable Flag**    | Flag string (`signpath`) enabling Windows signing job.    | **GitHub Variable**: `WINDOWS_SIGNING`                            |

### 8.4 cPanel Deployment SSH Credentials

| Output / Asset                                    | What it is                                                      | Where it goes                                                                                  |
|:--------------------------------------------------|:----------------------------------------------------------------|:-----------------------------------------------------------------------------------------------|
| **Private SSH Key** (`photouploader_deploy`)      | Private Ed25519 SSH key content.                                | **GitHub Secret**: `WEBSITE_SSH_KEY`<br>+ **Password Manager**                                 |
| **Public SSH Key** (`photouploader_deploy.pub`)   | Public Ed25519 SSH key content.                                 | **cPanel SSH Access** (Authorized Keys)                                                        |
| **Server Known Hosts Line**                       | Host key output line from `ssh-keyscan`.                        | **GitHub Secret**: `WEBSITE_KNOWN_HOSTS`                                                       |
| **Server Connection Settings**                    | Hostname, username, port, and remote download directory path.   | **GitHub Secrets**: `WEBSITE_HOST`, `WEBSITE_USER`, `WEBSITE_PORT`, `WEBSITE_DOWNLOAD_DIR`     |

