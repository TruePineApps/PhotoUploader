import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.skie)
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}

val appId = libs.versions.appId.get()
// App version from libs.versions.toml
val versionCode: Int = libs.versions.appVersionCode.get().toInt()
val versionName: String = libs.versions.appVersionName.get()

val generateBuildProperties by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/resources/custom")
    val propsFile = outputDir.map { it.file("build-info.properties") }

    // 1. Define inputs strongly so Gradle can cache them
    // We capture the values into the task's state, breaking the link to the script scope
    val vName = versionName
    val vCode = versionCode
    val aId = appId

    inputs.property("version_name", vName)
    inputs.property("version_code", vCode)
    inputs.property("app_id", aId)

    // 2. Define the output file
    outputs.file(propsFile)

    // 3. The action
    doLast {
        val file = propsFile.get().asFile
        file.parentFile.mkdirs()

        // Use the local variables which are now safely captured by the closure's copy,
        // instead of referencing the script-level properties directly.
        file.writeText(
            """
            version_name=$vName
            version_code=$vCode
            app_id=$aId
            """.trimIndent()
        )
    }
}

kotlin {
    // Select the JDK to run the Kotlin compiler
    jvmToolchain(21)

    android {
        // Namespace must be different from the one in androidApp/build.gradle.kts
        namespace = "com.truepineapps.photouploader.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        androidResources {
            enable = true
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop") {
        compilerOptions {
            // Bytecode version for desktop
            jvmTarget.set(JvmTarget.JVM_11)
        }

        // Warn if the client_secrets.json file is missing
        val secretsPath = "src/desktopMain/resources/client_secrets.json"
        val secretsFile = project.file(secretsPath)
        if (!secretsFile.exists()) {
            project.logger.warn("⚠️ WARNING: Google Auth 'client_secrets.json' not found at $secretsPath.")
            project.logger.warn("   The app may crash at runtime. Please download the JSON from Google Cloud Console.")
        }

        // Register the output folder as a resource directory for the 'desktop' target
        compilations.getByName("main").defaultSourceSet.resources.srcDir(
            generateBuildProperties.map { it.outputs.files.singleFile.parentFile }
        )
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)

            // Window size calculation, publish calculateWindowSizeClass on the API
            api(libs.compose.material3.window.size)

            // Extended icons set
            implementation(libs.compose.material.icons.extended)

            // Dependency injection, let platforms define modules so expose that api
            api(libs.koin.core)
            implementation(libs.koin.annotations)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            // Direct JSON support & Serialization
            implementation(libs.kotlinx.serialization.bom)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            // Networking & Serialization, publish HttpClient on the API
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            // Navigation
            implementation(libs.navigation.compose)
            // Preferences
            implementation(libs.multiplatform.settings)
            // DateTime
            implementation(libs.kotlinx.dateTime)
            // File System, publish KmpFile on the API
            api(libs.calf.io)
            implementation(libs.calf.file.picker)
            implementation(libs.calf.file.picker.coil)
            implementation(libs.okio)
            // Images
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            // Logging, add kermit logging to api
            api(libs.kermit.core)
            implementation(libs.kermit.koin)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.bundles.shared.commonTest)
            implementation(libs.junit)
            // File System
            implementation(libs.okio.fakefilesystem)
            // Networking
            implementation(libs.ktor.client.mock)
            // Mock
            implementation(libs.mockito.junit.jupiter)
        }

        val androidMain by getting {
            dependencies {
                // Android preferences
                implementation(libs.androidx.preference.ktx)
                // Networking dependencies specific to Android
                implementation(libs.ktor.client.android)
            }
        }

        val iosMain by getting {
            dependencies {
                // Networking & Serialization
                implementation(libs.ktor.client.darwin)
                // Conversion of enum, sealed classes and coroutines to native iOS
                // Skie only works for iOS targets and may error for other platforms, so define
                // where it does work
                implementation(libs.touchlab.skie.annotations)
            }
        }
        val iosTest by getting {
            dependencies {
                // Conversion of enum, sealed classes and coroutines to native iOS
                implementation(libs.touchlab.skie.annotations)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.swing)

                // Networking & Serialization
                // Choose lightweight CIO engine, if not sufficient move to OkHttp
                implementation(libs.ktor.client.cio)

                // Google Auth
                implementation(libs.google.oauth.client.jetty)
                implementation(libs.google.api.client)
            }
        }

        val desktopTest by getting

    }

    // KSP Common sourceSet
    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }
}

dependencies {
    // For UI inspection in debug builds
    androidRuntimeClasspath(libs.compose.ui.tooling)

    // KSP Configuration
    // For Koin, you generally want the KSP processor to run for each platform that needs to use the
    // generated Koin modules. However, the universal "ksp()" configuration has performance issues
    // and is deprecated on multiplatform since 1.0.1.
    // androidApp still configures ksp with the normal "ksp()" call in its own build.gradle.kts.

    // This makes KSP process commonMain for metadata, useful if Koin generates common code/modules.
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)

    // Add KSP for each platform if Koin generates platform-specific initializers
    // or if the commonMainMetadata isn't sufficient for platform consumption.
    // The configuration name is usually ksp<TargetName> or ksp<CapitalizedSourceSetName>
    add("kspAndroid", libs.koin.ksp.compiler)
    add("kspDesktop", libs.koin.ksp.compiler)

    // For Apple targets (iOS, macOS, etc.), KSP configurations can be per-target
    add("kspIosX64", libs.koin.ksp.compiler)
    add("kspIosArm64", libs.koin.ksp.compiler)
    add("kspIosSimulatorArm64", libs.koin.ksp.compiler)
}


// Configure KSP tasks to depend on kspCommonMainKotlinMetadata,to be able to use annotations in
// commonMain. This is not done implicitly.
// The specific KSP task names for platforms would be:
//     val platformCompilationKspTaskNames = listOf(
//         "kspDebugKotlinAndroid",
//         "kspReleaseKotlinAndroid",
//         "kspKotlinDesktopApp",
//         "kspKotlinWindows",
//         "kspKotlinMacos",
//         "kspKotlinMacosArm",
//         "kspKotlinLinux",
//         "kspKotlinIosX64",
//         "kspKotlinIosArm64",
//         "kspKotlinIosSimulatorArm64",
//     )
// Since AGP 9.0, kspAndroidMain also exists.
// For less maintenance when adding platforms, we examine if the task name matches a pattern.
// This is done by name matching because accurately identifying these tasks by a common supertype
// (e.g., with tasks.withType<SomeKspTask>()) proved unreliable due to Gradle's task decoration and
// the specific class hierarchy of KSP tasks.
tasks.matching { task ->
    // 1. Ensure the task name starts with "ksp". This is the primary identifier.
    task.name.startsWith("ksp") &&
    // 2. Exclude the common metadata task itself.
    task.name != "kspCommonMainKotlinMetadata" &&
    // 3. Exclude KSP tasks related to classpath setup or internal KSP workings.
    !task.name.contains("ProcessorClasspath", ignoreCase = true) &&
    // 4. A more specific check to ensure it's likely a compilation task.
    //    KSP tasks for compilations usually include the source set name (e.g., Main, Debug, Release).
    //    This helps avoid random ksp-prefixed tasks that aren't related to compilation.
    //    The most common pattern is ksp<SourceSetName> or ksp<Variant>Kotlin<Target>.
    task.name.matches(Regex("ksp(Debug|Release)?(Kotlin)?(Android|Jvm|Ios.*|Desktop|Windows|Macos.*|Linux)?(Main)?"))
}.configureEach {
    // For all KSP tasks that match our criteria, add the dependency.
    project.logger.info("Configuring task '${this.name}' (name match) to depend on 'kspCommonMainKotlinMetadata'")
    dependsOn("kspCommonMainKotlinMetadata")
}

// Define the task to generate the properties file
compose.resources {
    publicResClass = false
    // Use a more friendly import name than photouploader.composeapp.generated.resources
    packageOfResClass = "com.truepineapps.photouploader.resources"
    generateResClass = auto
}

// For debugging the build configuration: `./gradlew printKotlinDetails` shows the details of the
// configuration defined here.
tasks.register("printKotlinDetails") {
    doLast {
        println(">>> Kotlin Source Sets <<<")
        kotlin.sourceSets.forEach {
            println("Name: ${it.name}")
            it.dependsOn.forEach { dep ->
                println("  DependsOn: ${dep.name}")
            }
        }
        println("\n>>> Kotlin Compilations <<<")
        kotlin.targets.forEach { target ->
            target.compilations.forEach { compilation ->
                println("Compilation: ${compilation.name} on Target: ${target.name}")
                println("  Task Name: ${compilation.compileTaskProvider.name}")
                println("  Default Source Set: ${compilation.defaultSourceSet.name}")
                compilation.allKotlinSourceSets.forEach { ss ->
                    println("    AllKotlinSourceSet: ${ss.name}")
                }
            }
        }
        println("\n>>> KSP Related Configurations (if any exist explicitly) <<<")
        project.configurations.filter { it.name.startsWith("ksp") }.forEach {
            println("Configuration: ${it.name}")
            // if (it.isCanBeResolved) {
            //     it.resolvedConfiguration.lenientConfiguration.allModuleDependencies.forEach { dep ->
            //         println("  Dependency: ${dep.moduleGroup}:${dep.moduleName}:${dep.moduleVersion}")
            //     }
            // }
        }
    }
}
