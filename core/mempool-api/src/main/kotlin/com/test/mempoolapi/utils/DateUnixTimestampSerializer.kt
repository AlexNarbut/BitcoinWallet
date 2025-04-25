package com.test.mempoolapi.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Date

internal object DateUnixTimestampSerializer : KSerializer<Date?> {
    private val longSerializer = Long.serializer().nullable
    private val stringSerializer = String.serializer().nullable

    override val descriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)

    override fun serialize(
        encoder: Encoder,
        value: Date?
    ) = longSerializer.serialize(encoder, value?.time?.let { it / MILLISECOND_MULTIPLY })

    override fun deserialize(decoder: Decoder): Date? {
        val string = stringSerializer.deserialize(decoder)
        return string?.toLongOrNull()?.let { Date(it * MILLISECOND_MULTIPLY) }

    }

    private const val MILLISECOND_MULTIPLY = 1000
}