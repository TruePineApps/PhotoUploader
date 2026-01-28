package com.truepineapps.photouploader.log

import co.touchlab.kermit.Message
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import com.truepineapps.photouploader.util.DEFAULT_TIMESTAMP_FORMAT
import com.truepineapps.photouploader.util.now

object TimestampMessageFormatter : MessageStringFormatter {
    override fun formatMessage(severity: Severity?, tag: Tag?, message: Message): String {
        val timestampString = DEFAULT_TIMESTAMP_FORMAT.format(now())
        // Super implementation prefixed with timestamp
        val sb = StringBuilder(timestampString).append(" ")
        if (severity != null) sb.append(formatSeverity(severity)).append(" ")
        if (tag != null && tag.tag.isNotEmpty()) sb.append(formatTag(tag)).append(" ")
        sb.append(message.message)
        return sb.toString()
    }
}
