package com.truepineapps.photouploader.core.util

import co.touchlab.kermit.Logger
import java.io.File

interface JvmEnvironment {
    fun getProperty(key: String): String?
    fun getEnv(key: String): String?
    fun getAvailableProcessors(): Int
    fun getMaxMemory(): Long
    fun readOsRelease(): Map<String, String>
}

class RealJvmEnvironment(private val log: Logger) : JvmEnvironment {
    override fun getProperty(key: String): String? = System.getProperty(key)
    override fun getEnv(key: String): String? = System.getenv(key)
    override fun getAvailableProcessors(): Int = Runtime.getRuntime().availableProcessors()
    override fun getMaxMemory(): Long = Runtime.getRuntime().maxMemory()
    override fun readOsRelease(): Map<String, String> {
        val file = File("/etc/os-release")
        if (!file.exists()) return emptyMap()
        return try {
            file.readLines().filter { it.contains("=") }.associate { line ->
                val (key, value) = line.split("=", limit = 2)
                key to value.trim('"')
            }
        } catch (e: Exception) {
            log.d(e) { "Error reading /etc/os-release" }
            emptyMap()
        }
    }
}
