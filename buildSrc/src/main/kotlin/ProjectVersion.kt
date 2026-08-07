import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

private const val MAX_BUILD_NUMBER = "65535"

/**
 * Central utility for calculating project versions and labels based on human-maintained
 * parts in `libs.versions.toml` and automated build numbers from the CI environment.
 * This class is registered as a Gradle Extension, allowing it to be instantiated once
 * at the start of the build and then accessed as fixed metadata by all subprojects.
 *
 * This utility ensures a "Build-Driven" strategy:
 * 1. Technical fields (Windows MSI, macOS DMG, Android versionCode) use a strictly numeric version.
 * 2. User-facing labels (App Name, About Screen) include the release stage (e.g., "Beta 01").
 * 3. Side-by-side installation is supported by suffixing the Application ID based on the stage.
 */
open class ProjectVersion(project: Project) {
    private val catalog: VersionCatalog = project.extensions
        .getByType<VersionCatalogsExtension>()
        .named("libs")

    /**
     * The internal build number, incremented automatically by the CI system.
     * Defaults to MAX_BUILD_NUMBER for local builds to be easily recognizable as a dummy value.
     */
    val buildNumber: String = System.getenv("BUILD_NUMBER")
        ?: System.getenv("GITHUB_RUN_NUMBER")
        ?: MAX_BUILD_NUMBER

    /**
     * The stable application name (e.g., "PhotoUploader").
     */
    val appName: String = project.rootProject.name

    /**
     * The human-maintained major version number.
     */
    val appMajor: String = catalog.findVersion("cfg-appMajor").get().requiredVersion

    /**
     * The human-maintained minor version number.
     */
    val appMinor: String = catalog.findVersion("cfg-appMinor").get().requiredVersion

    /**
     * The human-maintained release stage (e.g., "Beta 1").
     */
    val appStage: String = catalog.findVersion("cfg-appStage").get().requiredVersion

    /**
     * The technical version name, typically in the format `major.minor.build`.
     * Provides full traceability back to the automated build.
     */
    val numericVersion: String = "$appMajor.$appMinor.$buildNumber"

    /**
     * Concise label for the OS (Launcher, Taskbar).
     * Avoids version numbers to prevent truncation and stable installation paths.
     */
    val appLabel: String = if (appStage.isNotEmpty()) "$appName Beta" else appName

    /**
     * Unique application identifier.
     * Uses a stable '.beta' suffix for all non-production stages to ensure
     * that new beta versions upgrade previous ones.
     */
    val appId: String = run {
        val baseId = catalog.findVersion("cfg-appId").get().requiredVersion
        if (appStage.isNotEmpty()) "$baseId.beta" else baseId
    }
}
