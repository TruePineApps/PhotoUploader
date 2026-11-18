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
*   **Operating System:** Modern 64-bit distribution (Ubuntu 22.04 LTS or newer recommended).
*   **Dependencies:** `glibc` 2.17 or later (Check your version by running `ldd --version` in a 
terminal).
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
1.  Download the `Photo Uploader Beta 1-1.0.0.deb` package.
2.  Open a terminal in the download folder.
3.  Install using `apt` (this will also resolve any system dependencies):
    ```bash
    sudo apt install ./Photo Uploader Beta 1-1.0.0.deb
    ```
4.  Launch the app from your application menu or by typing `PhotoUploader` in the terminal.

---

## 🛡️ Privacy & Security
Photo Uploader Beta 1 requires permission to access your local file system (to read your photos) and the 
internet (to upload to Google Photos). It uses secure OAuth 2.0 to connect to your Google account; 
your password is never seen or stored by this application.

---

## 🗑️ Uninstallation

### Windows
1. Open **Settings** $\rightarrow$ **Apps** $\rightarrow$ **Installed apps** (or **Control Panel** $\rightarrow$ **Programs and Features**).
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
*Disclaimer: This application is not officially affiliated with or endorsed by Google LLC. Google Photos is a trademark of Google LLC.*
