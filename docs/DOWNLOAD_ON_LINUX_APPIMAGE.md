## How to Verify Your Linux Download (AppImage)

When downloading software, verifying files protects you against two distinct risks:
1. **Authenticity (GPG Signature):** Ensuring the files genuinely came from the official developer.
2. **Integrity (Checksum):** Ensuring the executable was not corrupted or altered in transit.

> **Note:** Verification is optional — you can run the application immediately. However, taking a minute to verify cryptographic signatures and checksums ensures your download is 100% authentic and has not been corrupted or tampered with in transit.

---

### Step-by-Step Verification Guide

The release consists of:
* **The Executable** (`<actual PhotoUploader-*-linux-*.AppImage>`) — The portable application package for compatible x86-64 Linux systems.
* **The SHA256 checksum** — The official checksum list used to verify file integrity.
* **The Direct Installer Signature** (`<actual PhotoUploader-*-linux-*.AppImage.asc>`) — A GPG signature verifying the authenticity of the AppImage.
* **The Public Release Key** (`photouploader-release-key.asc`) — The developer's public GPG signing key.
* **The Fingerprint of Release Key** (`photouploader-release-key.asc`) — The fingerprint: `21CD 4245 8B26 1BE9 219F  FB3E 7CC6 A56F C8A2 0457`.

#### Step 1: Import and Trust the Developer's Public GPG Key
1. Download [`photouploader-release-key.asc`](../docs/photouploader-release-key.asc) into the same folder as your downloaded AppImage.
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

#### Step 2: Verify File Integrity (Checksum Match)
1. Run the checksum utility in your terminal:
   ```bash
   echo <actual_sha256_checksum_of_the_file> <actual PhotoUploader-*-linux-*.AppImage> | sha256sum --check
   ```
2. The utility must report **`OK`** next to your filename.

---

#### Step 3: Verify Authenticity (GPG Signature)
1. Run the verification command:
   ```bash
   gpg --verify <actual PhotoUploader-*-linux-*.AppImage.asc> <actual PhotoUploader-*-linux-*.AppImage>
   ```
2. You must see **`Good signature from "True Pine Apps..."`**.

> **Note on GPG Warnings:** Seeing `[unknown]` and a warning about *"not certified with a trusted signature"*
> is normal when verifying software with a newly imported developer key. As long as GPG reports
> **`Good signature`**, the AppImage is authentic and untampered with. You establish trust by confirming that the 40-character fingerprint matches the one published above.

## Run the AppImage

The downloaded `.AppImage` is the complete application in one portable file. It does not need to be installed, extracted, or run with root/sudo privileges.

**From your desktop:** In your file manager, open the AppImage's Properties and enable **Allow executing file as a program** (the wording varies by desktop environment). Then double-click the file to launch Photo Uploader.

**From a terminal:** Make the downloaded file executable, then run it. Replace the placeholder with the downloaded filename:

```bash
chmod +x -- "<actual PhotoUploader-*-linux-*.AppImage>"
./"<actual PhotoUploader-*-linux-*.AppImage>"
```

If it cannot start because FUSE is unavailable, install your distribution's FUSE 2 compatibility package. Alternatively, extract it without installing anything and launch the extracted app:

```bash
./"<actual PhotoUploader-*-linux-*.AppImage>" --appimage-extract
./squashfs-root/AppRun
```
