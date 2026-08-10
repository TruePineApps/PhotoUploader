import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.provider.Property

/**
 * A task that synchronizes versioning information into documentation files (e.g., INSTALL.md).
 * It reads from a template file and writes to an output file, replacing placeholders
 * like ${version} and ${appLabel} with actual values from the build system.
 */
abstract class SyncInstallDocTask : DefaultTask() {

    @get:InputFile
    abstract val templateFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val appName: Property<String>

    @get:Input
    abstract val appLabel: Property<String>

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val majorMinor: Property<String>

    @TaskAction
    fun sync() {
        val template = templateFile.get().asFile
        if (!template.exists()) {
            println("Error: Template file ${template.absolutePath} not found.")
            return
        }

        var content = template.readText()
        
        // Perform replacements
        content = content.replace("\${appName}", appName.get())
        content = content.replace("\${appLabel}", appLabel.get())
        content = content.replace("\${version}", version.get())
        content = content.replace("\${majorMinor}", majorMinor.get())

        val output = outputFile.get().asFile
        output.writeText(content)
        println("Synced version info to ${output.absolutePath}")
    }
}
