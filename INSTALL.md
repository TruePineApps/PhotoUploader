# Installation Guide & System Requirements

Photo Uploader Beta 1 is a native desktop application for Windows, macOS, and Linux. This guide covers the 
system requirements and installation steps for each platform.

---

## 💻 System Requirements

### Windows
*   **Operating System:** Windows 10 or 11 (64-bit).
*   **Architecture:** x86-64 (Intel/AMD) or ARM64.
*   **Graphics:** Support for DirectX 9 or later (DirectX 12 recommended).

### macOS
*   **Operating System:** macOS 12 (Monterey) or later.
*   **Architecture:** Apple Silicon (M1, M2, M3, M4) or Intel x86-64.

### Linux
*   **Operating System:** 64-bit Linux. Linux packages are built on Ubuntu 24.04; older distributions may require newer system libraries.
*   **Display:** X11 or Wayland.

### Hardware Recommendations
*   **RAM:** 2 GB minimum (4 GB recommended).
*   **Disk Space:** ~150 MB for installation, plus additional space for photo caching.
*   **Internet:** Required for signing in to Google Photos and uploading content.

---

## 🛠️ Installation Steps

> [!NOTE]
> **Java is included:** You do **not** need to install Java or any other runtime environment. 
> Photo Uploader Beta 1 comes with its own optimized, private Java runtime bundled inside the installer.

### Windows (.msi)
1.  Download the `Photo Uploader Beta 1-1.0.0.msi` installer.
2.  Double-click the file to launch the setup wizard.
3.  Follow the on-screen instructions.
4.  Launch the app from the Start Menu or Desktop shortcut.

### macOS (.dmg)
1.  Download the `Photo Uploader Beta 1-1.0.0.dmg` disk image.
2.  Open the `.dmg` file.
3.  Drag the **PhotoUploader** icon into your **Applications** folder.
4.  **First Launch:** If you see a message saying the developer cannot be verified, right-click 
(or Control-click) the app in your Applications folder and select **Open**.

### Linux (.deb)
For Debian and Ubuntu:
1.  Download the `.deb` package from the [Linux download guide](docs/DOWNLOAD_ON_LINUX.md).
2.  Open a terminal in the download folder.
3.  Install using `apt` (this will also resolve any system dependencies):
    ```bash
    sudo apt install ./PhotoUploader-*-linux-amd64.deb
    ```
4.  Launch the app from your application menu.

### Linux (.rpm)
For Fedora, RHEL, and openSUSE:
1. Download the `.rpm` package using the [RPM download guide](docs/DOWNLOAD_ON_LINUX_RPM.md).
2. Install it with your package manager, for example `sudo dnf install ./PhotoUploader-*-linux-amd64.rpm`.
3. Launch Photo Uploader from your application menu.

### Linux (AppImage)
For compatible x86-64 Linux distributions, use the portable [AppImage download guide](docs/DOWNLOAD_ON_LINUX_APPIMAGE.md). The AppImage bundles the Java runtime but still requires compatible system libraries. After downloading, enable **Allow executing file as a program** in the file's Properties, then double-click it; alternatively, use `chmod +x` and run it from a terminal as shown in the guide. It is a single portable file and does not require installation or root privileges.

---

## 🛡️ Privacy & Security
Photo Uploader Beta 1 requires permission to access your local file system (to read your photos) and the 
internet (to upload to Google Photos). It uses secure OAuth 2.0 to connect to your Google account; 
your password is never seen or stored by this application.

---

## 🗑️ Uninstallation

### Windows
1. Open **Settings** → **Apps** → **Installed apps** (or **Control Panel** → **Programs and Features**).
2. Locate **Photo Uploader Beta 1** in the list.
3. Click the menu options next to the application and select **Uninstall**.

### macOS
1. Open **Finder** and navigate to your **Applications** folder.
2. Drag **Photo Uploader Beta 1.app** into the **Trash** (or right-click and select **Move to Trash**).
3. Empty the Trash to complete uninstallation.

### Linux (Debian / Ubuntu)
1. Open a terminal.
2. Remove the installed package using `apt`:
   ```bash
   sudo apt remove photo-uploader-beta
   ```
3. To remove application configuration and cached credentials as well, run:
   ```bash
   sudo apt purge photo-uploader-beta
   ```
### Linux (RPM-based distributions)
Remove the package using your package manager, for example:
```bash
sudo dnf remove photo-uploader-beta
```
*Disclaimer: This application is not officially affiliated with or endorsed by Google LLC. Google Photos is a trademark of Google LLC.*
