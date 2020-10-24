package eu.musicnova.shared

import kotlinx.serialization.KSerializer

fun <DATA> InterPlatformSerializer.deserializeList(kSerializer: KSerializer<DATA>, bytes: ByteArray) =
    ByteArrayStacker.unStackBytes(bytes).map { subByteArray -> deserialize(kSerializer, subByteArray) }


fun <DATA> InterPlatformSerializer.serializeList(kSerializer: KSerializer<DATA>, data: List<DATA>) =
    ByteArrayStacker.stackBytes(data.map { subData -> serialize(kSerializer, subData) })