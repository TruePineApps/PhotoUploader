package com.truepineapps.photouploader.core.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
object FormatArgsSerializer : KSerializer<Array<out Any>> {
    override val descriptor: SerialDescriptor =
        ArraySerializer(_root_ide_package_.com.truepineapps.photouploader.core.util.PrimitiveFormatValue.serializer()).descriptor

    override fun serialize(encoder: Encoder, value: Array<out Any>) {
        encoder.encodeSerializableValue(
            ArraySerializer(_root_ide_package_.com.truepineapps.photouploader.core.util.PrimitiveFormatValue.serializer()),
            value.map { arg -> _root_ide_package_.com.truepineapps.photouploader.core.util.PrimitiveFormatValue.toFormatValue(arg) }.toTypedArray()
        )
    }

    override fun deserialize(decoder: Decoder): Array<Any> {
        return decoder.decodeSerializableValue(
            ArraySerializer(_root_ide_package_.com.truepineapps.photouploader.core.util.PrimitiveFormatValue.serializer())
        ).map { value -> value.argValue }.toTypedArray()
    }
}

@Serializable
sealed class PrimitiveFormatValue {
    abstract val argValue: Any

    companion object {
        fun toFormatValue(value: Any): PrimitiveFormatValue {
            return when (value) {
                // Allow UiText arguments to be serialized
                is com.truepineapps.photouploader.core.util.UiText -> _root_ide_package_.com.truepineapps.photouploader.core.util.UiTextFormatValue(
                    value
                )
                is String -> _root_ide_package_.com.truepineapps.photouploader.core.util.StringFormatValue(
                    value
                )
                is Boolean -> _root_ide_package_.com.truepineapps.photouploader.core.util.BooleanFormatValue(
                    value
                )
                is Byte -> _root_ide_package_.com.truepineapps.photouploader.core.util.ByteFormatValue(
                    value
                )
                is Short -> _root_ide_package_.com.truepineapps.photouploader.core.util.ShortFormatValue(
                    value
                )
                is Int -> _root_ide_package_.com.truepineapps.photouploader.core.util.IntFormatValue(
                    value
                )
                is Long -> _root_ide_package_.com.truepineapps.photouploader.core.util.LongFormatValue(
                    value
                )
                is Float -> _root_ide_package_.com.truepineapps.photouploader.core.util.FloatFormatValue(
                    value
                )
                is Double -> _root_ide_package_.com.truepineapps.photouploader.core.util.DoubleFormatValue(
                    value
                )
                is Char -> _root_ide_package_.com.truepineapps.photouploader.core.util.CharFormatValue(
                    value
                )
                else -> _root_ide_package_.com.truepineapps.photouploader.core.util.StringFormatValue(
                    value.toString()
                )
            }
        }
    }
}

@Serializable
data class BooleanFormatValue(override val argValue: Boolean) : com.truepineapps.photouploader.core.util.PrimitiveFormatValue()

@Serializable
data class ByteFormatValue(override val argValue: Byte) : com.truepineapps.photouploader.core.util.PrimitiveFormatValue()

@Serializable
data class ShortFormatValue(override val argValue: Short) : com.truepineapps.photouploader.core.util.PrimitiveFormatValue()

@Serializable
data class IntFormatValue(override val argValue: Int) : com.truepineapps.photouploader.core.util.PrimitiveFormatValue()

@Serializable
data class LongFormatValue(override val argValue: Long) : com.truepineapps.photouploader.core.util.PrimitiveFormatValue()

@Serializable
data class FloatFormatValue(override val argValue: Float) : com.truepineapps.photouploader.core.util.PrimitiveFormatValue()

@Serializable
data class DoubleFormatValue(override val argValue: Double) : com.truepineapps.photouploader.core.util.PrimitiveFormatValue()

@Serializable
data class CharFormatValue(override val argValue: Char) : com.truepineapps.photouploader.core.util.PrimitiveFormatValue()

@Serializable
data class StringFormatValue(override val argValue: String) : com.truepineapps.photouploader.core.util.PrimitiveFormatValue()

@Serializable
data class UiTextFormatValue(override val argValue: com.truepineapps.photouploader.core.util.UiText) : com.truepineapps.photouploader.core.util.PrimitiveFormatValue()