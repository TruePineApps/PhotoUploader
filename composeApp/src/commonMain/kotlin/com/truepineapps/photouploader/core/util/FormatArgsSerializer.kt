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
        ArraySerializer(PrimitiveFormatValue.serializer()).descriptor

    override fun serialize(encoder: Encoder, value: Array<out Any>) {
        encoder.encodeSerializableValue(
            ArraySerializer(PrimitiveFormatValue.serializer()),
            value.map { arg -> PrimitiveFormatValue.toFormatValue(arg) }.toTypedArray()
        )
    }

    override fun deserialize(decoder: Decoder): Array<Any> {
        return decoder.decodeSerializableValue(
            ArraySerializer(PrimitiveFormatValue.serializer())
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
                is UiText -> UiTextFormatValue(value)
                is String -> StringFormatValue(value)
                is Boolean -> BooleanFormatValue(value)
                is Byte -> ByteFormatValue(value)
                is Short -> ShortFormatValue(value)
                is Int -> IntFormatValue(value)
                is Long -> LongFormatValue(value)
                is Float -> FloatFormatValue(value)
                is Double -> DoubleFormatValue(value)
                is Char -> CharFormatValue(value)
                else -> StringFormatValue(value.toString())
            }
        }
    }
}

@Serializable
data class BooleanFormatValue(override val argValue: Boolean) : PrimitiveFormatValue()

@Serializable
data class ByteFormatValue(override val argValue: Byte) : PrimitiveFormatValue()

@Serializable
data class ShortFormatValue(override val argValue: Short) : PrimitiveFormatValue()

@Serializable
data class IntFormatValue(override val argValue: Int) : PrimitiveFormatValue()

@Serializable
data class LongFormatValue(override val argValue: Long) : PrimitiveFormatValue()

@Serializable
data class FloatFormatValue(override val argValue: Float) : PrimitiveFormatValue()

@Serializable
data class DoubleFormatValue(override val argValue: Double) : PrimitiveFormatValue()

@Serializable
data class CharFormatValue(override val argValue: Char) : PrimitiveFormatValue()

@Serializable
data class StringFormatValue(override val argValue: String) : PrimitiveFormatValue()

@Serializable
data class UiTextFormatValue(override val argValue: UiText) : PrimitiveFormatValue()