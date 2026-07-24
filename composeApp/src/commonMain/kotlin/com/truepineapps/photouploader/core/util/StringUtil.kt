package com.truepineapps.photouploader.core.util

/**
 * Collapses multiple whitespace characters (including newlines and tabs)
 * into a single space and trims leading/trailing whitespace.
 */
fun String.normalizeWhitespace() = this.replace(Regex("\\s+"), " ").trim()