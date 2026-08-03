/*
 * Copyright (c) 2026 True Pine Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.jk1.license.ModuleData
import com.github.jk1.license.ProjectData
import com.github.jk1.license.render.ReportRenderer
import java.io.File

class NoticesRenderer(
    private val filename: String = "THIRD-PARTY-NOTICES.txt",
    private val outputDirectory: File? = null
) : ReportRenderer {

    // Mapping of known SPDX identifiers to canonical URLs
    private val spdxUrls = mapOf(
        "Apache-2.0" to "https://www.apache.org/licenses/LICENSE-2.0",
        "MIT" to "https://opensource.org/licenses/MIT",
        "BSD-2-Clause" to "https://opensource.org/licenses/BSD-2-Clause",
        "BSD-3-Clause" to "https://opensource.org/licenses/BSD-3-Clause",
        "LGPL-2.1-only" to "https://www.gnu.org/licenses/lgpl-2.1",
        "LGPL-2.1-or-later" to "https://www.gnu.org/licenses/lgpl-2.1",
        "LGPL-2.0-only" to "https://www.gnu.org/licenses/lgpl-2.0",
        "GPL-2.0-only" to "https://www.gnu.org/licenses/gpl-2.0",
        "GPL-3.0-only" to "https://www.gnu.org/licenses/gpl-3.0",
        "EPL-1.0" to "https://www.eclipse.org/legal/epl-v10.html",
        "EPL-2.0" to "https://www.eclipse.org/legal/epl-2.0",
        "MPL-2.0" to "https://www.mozilla.org/en-US/MPL/2.0/",
        "CDDL-1.0" to "https://opensource.org/licenses/CDDL-1.0",
        "Unknown" to ""
    )

    // Data to render an embedded notice: identifier = GAV = group:artifact:version, project URL,
    // and file content
    private data class NoticeData(val identifier: String, val url: String, val content: String)

    override fun render(data: ProjectData) {
        // Use the passed directory or fallback to a default (avoiding data.project)
        val reportDir = outputDirectory ?: File("build/reports/dependency-license")
        val output = File(reportDir, filename)

        if (!reportDir.exists()) reportDir.mkdirs()

        // Each module can have multiple licenses; group them
        // Key: sorted set of license names (e.g. "Apache-2.0 OR LGPL-2.1-or-later")
        val grouped = mutableMapOf<String, MutableList<ModuleData>>()

        // Use a set to track unique GAVs to verify the 240 vs 243 count
        val uniqueModules = mutableSetOf<String>()

        // Map the module to embedded notice data
        val moduleNotices = mutableMapOf<String, NoticeData>()
        // Map GAV to set of copyright lines for MIT
        val mitCopyrights = mutableMapOf<String, MutableSet<String>>()

        println("NoticesRenderer: Retrieving data")
        data.allDependencies.forEach { module ->
            val licenseKey = resolveLicenseKey(module)
            grouped.getOrPut(licenseKey) { mutableListOf() }.add(module)
            val gav = "${module.group}:${module.name}:${module.version}"
            uniqueModules.add(gav)

            val projectUrl = resolveProjectUrl(module)

            // Collect NOTICE file contents and MIT copyrights if they exist
            module.licenseFiles.forEach { file ->
                file.fileDetails.forEach { detail ->
                    val filePath = detail.file ?: return@forEach
                    val f = File(reportDir,filePath)
                    if (!f.exists()) return@forEach
                    val fileName = f.name.uppercase()

                    if (fileName.startsWith("NOTICE")) {
                        val content = f.readText().trim()
                        if (content.isNotBlank()) {
                            moduleNotices[gav] = NoticeData(gav, projectUrl, content)
                        }
                    }

                    if (licenseKey == "MIT" && fileName.startsWith("LICENSE")) {
                        val copyrightLine = extractCopyrights(f.readText())
                        mitCopyrights.getOrPut(gav) { mutableSetOf() }
                            .addAll(copyrightLine)
                    }
                }
            }
        }

        println("NoticesRenderer: Generating output")
        output.bufferedWriter().use { writer ->
            writer.write("PhotoUploader\n")
            writer.write("Copyright 2026 True Pine Apps\n\n")
            writer.write("This product includes software developed at\n")
            writer.write("True Pine Apps (https://www.truepineapps.com/).\n\n")

            writer.write("THIRD-PARTY SOFTWARE NOTICES\n")
            writer.write("=".repeat(60) + "\n\n")

            writer.write("Total Unique Packages: ${uniqueModules.size}\n")
            writer.write("Note: Where packages are listed under combined license headers (e.g., Apache-2.0 OR LGPL),\n" +
                    "True Pine Apps distributes them under the Apache-2.0 license.\n\n")

            grouped.entries.sortedBy { it.key }.forEach { (licenseKey, modules) ->
                val urls = resolveUrls(licenseKey)

                writer.write("-".repeat(60) + "\n")
                writer.write("$licenseKey (${modules.size} package${if (modules.size != 1) "s" else ""})\n")
                urls.forEach { writer.write("$it\n") }
                writer.write("-".repeat(60) + "\n")

                // Deduplicate within the group (some plugins report same module multiple times)
                modules.distinctBy { "${it.group}:${it.name}:${it.version}" }
                    .sortedBy { "${it.group}:${it.name}" }.forEach { module ->
                        val projectUrl = resolveProjectUrl(module)
                        writer.write("  ${module.group}:${module.name}:${module.version}")
                        if (projectUrl.isNotBlank()) writer.write("  ($projectUrl)")
                        writer.write("\n")
                    }
                writer.write("\n")

                // MIT needs explicit license
                if (licenseKey == "MIT") {
                    writer.write("  License text:\n")
                    writer.write("\n")

                    val copyrights = modules.flatMap { module ->
                        val gav = "${module.group}:${module.name}:${module.version}"
                        mitCopyrights[gav] ?: emptySet()
                    }.distinct().sorted()
                    if (copyrights.isNotEmpty()) {
                        copyrights.forEach { writer.write("  $it\n") }
                    }

                    writer.write("\n")
                    writer.write("  Permission is hereby granted, free of charge, to any person obtaining\n")
                    writer.write("  a copy of this software and associated documentation files (the\n")
                    writer.write("  \"Software\"), to deal in the Software without restriction, including\n")
                    writer.write("  without limitation the rights to use, copy, modify, merge, publish,\n")
                    writer.write("  distribute, sublicense, and/or sell copies of the Software, and to\n")
                    writer.write("  permit persons to whom the Software is furnished to do so, subject to\n")
                    writer.write("  the following conditions:\n")
                    writer.write("\n")
                    writer.write("  The above copyright notice and this permission notice shall be\n")
                    writer.write("  included in all copies or substantial portions of the Software.\n")
                    writer.write("\n")
                    writer.write("  THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND,\n")
                    writer.write("  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF\n")
                    writer.write("  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND\n")
                    writer.write("  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS\n")
                    writer.write("  BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN\n")
                    writer.write("  ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN\n")
                    writer.write("  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n")
                    writer.write("  SOFTWARE.\n\n")
                }
            }

            // Additional embedded notices
            if (moduleNotices.isNotEmpty()) {
                writer.write("=".repeat(60) + "\n")
                writer.write("PACKAGE NOTICES\n")
                writer.write("Individual packages that include a NOTICE file with additional\n")
                writer.write("attribution requirements are listed below.\n")
                writer.write("=".repeat(60) + "\n\n")

                moduleNotices.values.sortedBy { it.identifier }.forEach { notice ->
                    writer.write("-".repeat(60) + "\n")
                    writer.write("${notice.identifier}  (${notice.url})\n")
                    writer.write("-".repeat(60) + "\n")
                    writer.write(notice.content)
                    writer.write("\n\n")
                }
            }
        }
    }

    /**
     * Collects all license names of a module and combines them into a single key.
     * For JNA this results in "Apache-2.0 OR LGPL-2.1-or-later".
     */
    private fun resolveLicenseKey(module: ModuleData): String {
        val names = mutableSetOf<String>()

        // Check POMs
        module.poms.forEach { pom ->
            pom.licenses.forEach { if (!it.name.isNullOrBlank()) names.add(it.name) }
        }
        // Check Manifests
        module.manifests.forEach { manifest ->
            if (!manifest.license.isNullOrBlank()) names.add(manifest.license)
        }
        // Check License Files
        module.licenseFiles.forEach { file ->
            file.fileDetails.forEach { if (!it.license.isNullOrBlank()) names.add(it.license) }
        }

        return if (names.isEmpty()) "Unknown" else names.sorted().joinToString(" OR ")
    }

    /**
     * Converts license names to known URLs.
     * For an OR combination, this returns multiple URLs.
     */
    private fun resolveUrls(licenseKey: String): List<String> {
        // Split on " OR " for combined licenses
        return licenseKey.split(" OR ").map { it.trim() }.mapNotNull { name ->
            spdxUrls[name] ?: extractUrlFromName(name)
        }.distinct()
    }

    /**
     * Some license names contain a URL themselves after a dash,
     * such as "Apache License, Version 2.0 - https://www.apache.org/..."
     */
    private fun extractUrlFromName(name: String): String? {
        val urlPattern = Regex("https?://\\S+")
        return urlPattern.find(name)?.value
    }

    private fun resolveProjectUrl(module: ModuleData): String {
        return module.poms.firstOrNull { it.projectUrl.isNotBlank() }?.projectUrl ?: ""
    }

    private fun extractCopyrights(content: String): List<String> {
        return content.lines().filter { it.startsWith("Copyright", ignoreCase = true) }
            .map { it.trim() }.filter { it.isNotBlank() }
    }
}
