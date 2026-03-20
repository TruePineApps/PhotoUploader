Windows: Reboot prompt on uninstall
When uninstalling the app on Windows, the system may show the following prompt:

"The setup must update files or services that cannot be updated while the system is running. If you choose to continue, a reboot will be required to complete the setup."

You can safely click Accept. The app will be fully uninstalled immediately. Windows will schedule cleanup of any remaining files for the next time your system restarts naturally — no manual reboot is needed. Clicking Cancel will abort the uninstall and leave the app installed.
This is caused by a limitation in the Compose Multiplatform build tooling (see [CMP-9837](https://youtrack.jetbrains.com/issue/CMP-9837)) that currently prevents the installer from suppressing this prompt. The issue has been reported and will be resolved in a future release.
