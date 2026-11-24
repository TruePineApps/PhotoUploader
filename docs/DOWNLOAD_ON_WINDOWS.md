## How to Verify Your Windows Download (.msi)

When downloading software, verifying files protects you against transit corruption.

> **Note:** Verification is optional — you can install the package immediately. However, taking a minute to verify checksums ensures your download is 100% intact and has not been corrupted or modified in transit.
> *Tip:* The steps below outline the universal industry standard for verifying software downloads using SHA-256 checksums.

---

### Step-by-Step Verification Guide

The release consists of:
* **The Installer** (`<actual PhotoUploader-*-windows-*.msi>`) — The Windows installation package.
* **The SHA256 checksum** — The official checksum list used to verify file integrity: <actual_sha256_checksum_of_the_msi_file>. <copy_checksum_button>.

#### Step 1: Verify Installer File Integrity (Checksum Match)
Verify that your downloaded `.msi` file was not corrupted or modified in transit:

1. Run the checksum utility for your platform:
   * **GUI (via 7-Zip)**: 
     - Right-click the `<actual PhotoUploader-*-windows-*.msi>` file → **CRC SHA** → **SHA-256**.
     - Copy this SHA256 hash code: <actual_sha256_checksum_of_the_msi_file>
     - Paste the hash into the comparison box to verify.
   * **PowerShell**:
     ```powershell
     $actual = (Get-FileHash .\<actual PhotoUploader-*-windows-*.msi> -Algorithm SHA256).Hash; if ($actual -eq "<actual_sha256_checksum_of_the_msi_file>") { Write-Host "OK" -ForegroundColor Green } else { Write-Host "MISMATCH" -ForegroundColor Red }
     ```
   <copy_script_button>
2. The script must report **`OK`** (or match the expected hash exactly).

---

#### Step 2: Verify Authenticity (Automatic Code Signing)
* Windows installers are **code-signed by SignPath Foundation**. 
* When you run the `.msi` installer, Windows SmartScreen automatically verifies the digital signature. 
* *Optional: You can inspect certificate details by right-clicking the `.msi` → **Properties** → **Digital Signatures**.*

## Installation Instructions

Double-click the downloaded `<actual PhotoUploader-*-windows-*.msi>` file and follow the setup wizard.
