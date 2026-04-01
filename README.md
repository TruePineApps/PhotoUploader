# PhotoUploader
Photo Uploader uploads a photo collection organized in folders into albums on Google Photos. The 
album name is derived from the folder name.

# Manual
TODO

# Screenshots
TODO

# Download
TODO

## Getting Started

This is a Kotlin Multiplatform project targeting Android, iOS, Desktop (JVM).

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [desktopMain](./composeApp/src/desktopMain/kotlin)
    folder is the appropriate location. You can use Java classes here.
* [./desktopApp](./desktopApp/src) contains the desktop application. Even if you’re sharing your UI
  with Compose Multiplatform, you need this entry point for your desktop app.
* [/androidApp](./desktopApp/src) contains Android applications. This is also
  where you should add code that depends on Android libraries.
* [/iosApp](./iosApp/iosApp) contains iOS applications. This is the entry point for the iOS app. This is also 
  where you should add SwiftUI code for your project.


### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run 
widget in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux: `./gradlew :desktopApp:run`
- on Windows: `.\gradlew.bat :desktopApp:run`
To build a debug version of the app install package, run:
- on macOS/Linux: `./gradlew :desktopApp:packageDeb`
- on Windows: `.\gradlew.bat :desktopApp:packageMsi`
To build a release version of the app install package, run:
- on macOS/Linux: `./gradlew :desktopApp:packageReleaseDeb`
- on Windows: `.\gradlew.bat :desktopApp:packageReleaseMsi`

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html), [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform), [Kotlin/Wasm](https://kotl.in/wasm/)…
Feedback on Compose/Web and Kotlin/Wasm is appreciated in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
Issues can be reported on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).

## License

PhotoUploader is licensed under the Apache License, Version 2.0.
See [LICENSE](LICENSE) for the full license text.

```
Copyright 2026 True Pine Apps

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

Third-party software notices: see [NOTICES](composeApp/src/commonMain/composeResources/files/NOTICES).