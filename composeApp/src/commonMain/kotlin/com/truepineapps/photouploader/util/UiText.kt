package com.truepineapps.photouploader.util

import androidx.compose.runtime.Composable
import com.github.mheerwaarden.retreat.util.FormatArgsSerializer
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.allPluralStringResources
import com.truepineapps.photouploader.resources.allStringResources
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Serializable
sealed class UiText {
    open fun isEmpty(): Boolean = false

    /** @return the translated text */
    @Composable
    abstract fun asString(): String
}

@Serializable
open class UiTextString(private val str: String) : UiText() {
    override fun isEmpty(): Boolean = str.isEmpty()

    override fun toString(): String = str

    @Composable
    override fun asString(): String = str
}

@Serializable
object EmptyText : UiTextString("") {
    override fun isEmpty(): Boolean = true
}

@Serializable(with = UiTextResourceSerializer::class)
data class UiTextResource(
    val resource: StringResource,
    val formatArgs: List<Any> = emptyList(),
) : UiText() {

    constructor(resource: StringResource, vararg args: Any) : this(resource, args.toList())

    /** @return the resource key followed by the arguments */
    override fun toString(): String {
        var result = resource.key
        formatArgs.forEach { arg -> result += " '$arg'" }
        return result
    }

    @Composable
    override fun asString(): String {
        val args = formatArgs.map { arg ->
            if (arg is UiText) arg.asString() else arg
        }.toTypedArray()
        return stringResource(resource = resource, *args)
    }
}

object UiTextResourceSerializer : KSerializer<UiTextResource> {
    override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor(UiTextResource::class.qualifiedName ?: "UiTextResource") {
                element(elementName = "resourceKey", descriptor = serialDescriptor<String>())
                element(elementName = "formatArgs", descriptor = FormatArgsSerializer.descriptor)
            }

    override fun serialize(encoder: Encoder, value: UiTextResource) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.resource.key)
            encodeSerializableElement(descriptor, 1, FormatArgsSerializer, value.formatArgs.toTypedArray())
        }
    }

    override fun deserialize(decoder: Decoder): UiTextResource {
        var resourceKey = ""
        var formatArgs: Array<out Any> = emptyArray<Any>()

        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    0 -> resourceKey = decodeStringElement(descriptor, 0)
                    1 -> formatArgs = decodeSerializableElement(descriptor, 1, FormatArgsSerializer)
                    else -> throw SerializationException("Unknown index: $index")
                }
            }
        }

        return UiTextResource(createStringResource(resourceKey), formatArgs.toList())
    }
}

private fun createStringResource(key: String): StringResource {
    // The key is typically "string.resource_name". We need "resource_name".
    val resourceName = key.substringAfterLast('.')
    return Res.allStringResources[resourceName]
        ?: throw SerializationException("StringResource with key '$key' (name: '$resourceName') not found")
}


/**
 * Resource id for a string containing plurals. The count must be in the right place in the
 * [formatArgs]. If the count is the only format argument, the [formatArgs] can be omitted.
 */
@Serializable(with = UiPluralsResourceSerializer::class)
data class UiPluralsResource(
    val resource: PluralStringResource,
    val count: Int,
    val formatArgs: List<Any> = emptyList(),
) : UiText() {
    override fun toString(): String {
        var result = "${resource.key} $count"
        formatArgs.forEach { arg -> result += " $arg" }
        return result
    }

    @Composable
    override fun asString(): String {
        val args = formatArgs.map { arg ->
            if (arg is UiText) arg.asString() else arg
        }.toTypedArray()
        
        return if (args.isEmpty()) {
            pluralStringResource(resource, count, count)
        } else {
            // Use spread operator '*' to pass the vararg arguments individually
            pluralStringResource(resource, count, *args)
        }
    }
}

object UiPluralsResourceSerializer : KSerializer<UiPluralsResource> {
    override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor(
                UiPluralsResource::class.qualifiedName ?: "UiPluralsResource"
            ) {
                element(elementName = "resourceKey", descriptor = serialDescriptor<String>())
                element(elementName = "count", descriptor = serialDescriptor<Int>())
                element(elementName = "formatArgs", descriptor = FormatArgsSerializer.descriptor)
            }

    override fun serialize(encoder: Encoder, value: UiPluralsResource) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.resource.key)
            encodeIntElement(descriptor, 1, value.count)
            encodeSerializableElement(descriptor, 2, FormatArgsSerializer, value.formatArgs.toTypedArray())
        }
    }

    override fun deserialize(decoder: Decoder): UiPluralsResource {
        var resourceKey = ""
        var count = 0
        var args: Array<out Any> = emptyArray<Any>()

        decoder.decodeStructure(descriptor) {
            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    0 -> resourceKey = decodeStringElement(descriptor, 0)
                    1 -> count = decodeIntElement(descriptor, 1)
                    2 -> args = decodeSerializableElement(descriptor, 2, FormatArgsSerializer)
                    else -> throw SerializationException("Unknown index: $index")
                }
            }
        }

        return UiPluralsResource(createPluralStringResource(resourceKey), count, args.toList())
    }
}

private fun createPluralStringResource(key: String): PluralStringResource {
    val resourceName = key.substringAfterLast('.')
    return Res.allPluralStringResources[resourceName]
        ?: throw SerializationException("PluralStringResource with key '$key' (name: '$resourceName') not found")
}
