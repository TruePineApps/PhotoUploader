<!-- Text for the landing page / README: "Verify your download". -->

## Verify your download

Every release publishes `SHA256SUMS.txt` (SHA-256 checksum of each file) and `SHA256SUMS.txt.asc`
(a GPG signature of that list). Download both files into the same folder as the installer.

**1. Check the checksum**

- Linux: `sha256sum --ignore-missing -c SHA256SUMS.txt`
- macOS: `shasum -a 256 -c SHA256SUMS.txt 2>&1 | grep -v "No such file"`
- Windows (PowerShell): `(Get-FileHash .\PhotoUploader-VERSION-windows-x64.msi -Algorithm SHA256).Hash`
  and compare it with the matching line in `SHA256SUMS.txt` (case does not matter).

The installer must report `OK` / match exactly. If it does not, delete the file and download it again.

**2. Check that the list really comes from us (recommended)**

```bash
gpg --import photouploader-release-key.asc        # public key, download from this site and from GitHub
gpg --fingerprint "True Pine Apps Releases"       # Key fingerprint = 21CD 4245 8B26 1BE9 219F  FB3E 7CC6 A56F C8A2 0457
gpg --verify SHA256SUMS.txt.asc SHA256SUMS.txt    # must say: Good signature
```

A checksum alone only proves the file was not damaged in transit. The signature proves the checksum list
was produced with our release key. Compare the key fingerprint with the one published in the GitHub
repository (README), not only with the one on this page.

Windows and macOS installers are additionally code-signed (Windows: SignPath Foundation; macOS: Apple
Developer ID, notarized); the operating system checks those automatically.
