## How to Verify Your Linux Download (.rpm)

When downloading software, verifying files protects you against two distinct risks:
1. **Authenticity (GPG Signature):** Ensuring the files genuinely came from the official developer.
2. **Integrity (Checksum):** Ensuring the installer was not corrupted or altered in transit.

> **Note:** Verification is optional — you can install the package immediately. However, taking a minute to verify cryptographic signatures and checksums ensures your download is 100% authentic and has not been corrupted or tampered with in transit.
>
> *Tip:* The steps below outline the **universal industry standard** for verifying secure software downloads on Linux using GPG and SHA256 checksums.

---

### Step-by-Step Verification Guide

The release consists of:
* **The Installer** (`<actual PhotoUploader-*-linux-*.rpm>`) — The RPM installation package for Fedora, RHEL, openSUSE, etc.
* **The SHA256 checksum** — The official checksum list used to verify file integrity.
* **The Direct Installer Signature** (`<actual PhotoUploader-*-linux-*.rpm.asc>`) — A GPG signature verifying the authenticity of the `.rpm` package.
* **The Public Release Key** (`photouploader-release-key.asc`) — The developer's public GPG signing key.
* **The Fingerprint of Release Key** (`photouploader-release-key.asc`) — The fingerprint: `21CD 4245 8B26 1BE9 219F  FB3E 7CC6 A56F C8A2 0457`.

#### Step 1: Import and Trust the Developer's Public GPG Key
1. Download [`photouploader-release-key.asc`](../docs/photouploader-release-key.asc) into the same folder as your downloaded installer.
2. Open a terminal and import the public key:
   ```bash
   gpg --import photouploader-release-key.asc
   ```
3. **Verify the Key Fingerprint**:
   ```bash
   gpg --fingerprint "True Pine Apps" | grep -q "21CD 4245 8B26 1BE9 219F  FB3E 7CC6 A56F C8A2 0457" && echo "OK" || echo "FAILED"
   ```
   The command must output **`OK`**.

---

#### Step 2: Verify Installer File Integrity (Checksum Match)
1. Run the checksum utility in your terminal:
   ```bash
   echo <actual_sha256_checksum_of_the_file> <actual PhotoUploader-*-linux-*.rpm> | sha256sum --check
   ```
2. The utility must report **`OK`** next to your installer filename.

---

#### Step 3: Verify Installer Authenticity (GPG Signature)
1. Run the verification command:
   ```bash
   gpg --verify <actual PhotoUploader-*-linux-*.rpm.asc> <actual PhotoUploader-*-linux-*.rpm>
   ```
2. You must see **`Good signature from "True Pine Apps..."`**.

> **Note on GPG Warnings:** Seeing `[unknown]` and a warning about *"not certified with a trusted signature"*
> is normal when verifying software with a newly imported developer key. As long as GPG reports
> **`Good signature`**, the RPM package is authentic and untampered with. You establish trust by confirming that the 40-character fingerprint matches the one published above.

## Installation Instructions

Install the `.rpm` package using your package manager (e.g., `sudo dnf install ./<actual PhotoUploader-*-linux-*.rpm>` or `sudo rpm -ivh <actual PhotoUploader-*-linux-*.rpm>`).
