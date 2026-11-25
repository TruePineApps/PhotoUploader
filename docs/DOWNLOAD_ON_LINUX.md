### How to Verify Your Linux Download (.deb)

When downloading software, verifying files protects you against two distinct risks:
1. **Authenticity (GPG Signature):** Ensuring the files genuinely came from the official developer.
2. **Integrity (Checksum):** Ensuring the installer was not corrupted or altered in transit.

> **Note:** Verification is optional — you can install the package immediately. However, taking a minute to verify cryptographic signatures and checksums ensures your download is 100% authentic and has not been corrupted or tampered with in transit.
> 
> *Tip:* The steps below outline the **universal industry standard** for verifying secure software downloads on Linux using GPG and SHA256 checksums. You can apply these exact same techniques when downloading other security-conscious software (such as Linux distributions, Tor, or developer tools) that provide `.asc` signatures and checksum lists.

---

### Step-by-Step Verification Guide

The release consists of:
* **The Installer** (`<actual PhotoUploader-*-linux-*.deb>`) — The installation package.
* **The SHA256 checksum** — The official checksum list used to verify file integrity: <actual_sha256_checksum_of_the_deb_file>. <copy_checksum_button>.
* **The Direct Installer Signature** (`<actual PhotoUploader-*-linux-*.deb.asc>`) — A GPG signature verifying the authenticity of the `.deb` package. <download_button>
* **The Public Release Key** (`photouploader-release-key.asc`) — The developer's public GPG signing key. <download_button>
* **The Fingerprint of Release Key** (`photouploader-release-key.asc`) — The fingerprint used to verify the developer's key authenticity: `21CD 4245 8B26 1BE9 219F  FB3E 7CC6 A56F C8A2 0457`. <copy_fingerprint_button>

#### Step 1: Import and Trust the Developer's Public GPG Key
Before verifying signatures, your system needs the developer's public key.

1. Download [`photouploader-release-key.asc`](../docs/photouploader-release-key.asc) into the same folder as your downloaded installer.
2. Open a terminal and import the public key:
   ```bash
   gpg --import photouploader-release-key.asc
   ```
3. **Verify the Key Fingerprint (Crucial Security Check)**:
   ```bash
   gpg --fingerprint "True Pine Apps" | grep -q "21CD 4245 8B26 1BE9 219F  FB3E 7CC6 A56F C8A2 0457" && echo "OK" || echo "FAILED"
   ```
   The command must output **`OK`**, confirming you hold the genuine developer key with matching fingerprint `21CD 4245 8B26 1BE9 219F  FB3E 7CC6 A56F C8A2 0457`.

---

#### Step 2: Verify Installer File Integrity (Checksum Match)
Check your downloaded installer against the checksum:

1. Run the checksum utility in your terminal:
   ```bash
   echo <actual_sha256_checksum_of_the_file> <actual PhotoUploader-*-linux-*.deb> | sha256sum --check
   ```
2. The utility must report **`OK`** next to your installer filename.

---

#### Step 3: Verify Installer Authenticity (Direct GPG Signature of the Installer)
As a final, definitive step, verify the cryptographic signature of the `.deb` package itself using its companion `.deb.asc` file:
1. Download `<actual PhotoUploader-*-linux-*.deb.asc>` into the same folder as your downloaded installer.

2. Run the verification command:
```bash
gpg --verify <actual PhotoUploader-*-linux-*.deb.asc> <actual PhotoUploader-*-linux-*.deb>
```

3. **What to look for**: You must see **`Good signature from "True Pine Apps..."`**. 

> **Note on GPG Warnings:** Seeing `[unknown]` and a warning about *"not certified with a trusted signature"*
> is normal when verifying software with a newly imported developer key. As long as GPG reports
> **`Good signature`**, the `.deb` file is authentic and untampered with. You establish trust by confirming that the 40-character fingerprint matches the one published above.

### Installation Instructions

Install the `.deb` package using your package manager (e.g., `sudo apt install ./<actual PhotoUploader-*-linux-*.deb>`).  When you are asked this question: "Do you want to continue? [y/n]" type in y (short form for "yes").
