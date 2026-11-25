## Verification Instructions (Optional)

When downloading software, verifying files protects you against transit corruption.

> **Note:** Verification is optional — you can install the package immediately. However, taking a minute to verify checksums ensures your download is 100% intact and has not been corrupted or modified in transit.
> *Tip:* The steps below outline the universal industry standard for verifying software downloads using SHA-256 checksums.

---

### Step-by-Step Verification Guide

The release consists of:
* **The Installer** (`<actual PhotoUploader-*-macos-*.dmg>`) — The disk image installation package.
* **The SHA256 checksum** — The official checksum list used to verify file integrity: <actual_sha256_checksum_of_the_dmg_file>. <copy_checksum_button>.

#### Step 1: Verify Installer File Integrity (Checksum Match)
Verify that your downloaded `.dmg` file was not corrupted or modified in transit:

1. Open Terminal in that folder and run:
   ```bash
   echo <actual_sha256_checksum_of_the_dmg_file>  <actual PhotoUploader-*-macos-*.dmg> | shasum -a 256 -c - | grep -E "OK$|FAILED$"
   ```
   <copy_script_button>
2. The script must report **`OK`** next to your installer filename.

---

#### Step 2: Verify Authenticity (Automatic Notarization)
* macOS installers are **signed with an Apple Developer ID and notarized by Apple**.
* When you open the `.dmg` installer and drag the app to Applications, macOS Gatekeeper automatically verifies the signature and notarization ticket upon launch.
* Optional Manual Verification (Terminal):
  ```bash
  spctl --assess --type open --context context:primary-signature <actual PhotoUploader-*-macos-*.dmg>
  ```
  <copy_script_button>
  *Must report `accepted`.*

## Installation Instructions

1. Open `<actual PhotoUploader-*-macos-*.dmg>` and drag the **Photo Uploader** app icon, on the left side of the window, onto the **Applications** folder on the right side. Doing so will install **Photo Uploader** on your computer.
2. Double click on the **Photo Uploader** app icon and wait for your mac to verify **Photo Uploader**.
