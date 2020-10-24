package eu.musicnova.shared

import kotlinx.serialization.KSerializer
import kotlinx.serialization.protobuf.ProtoBuf

object InterPlatformSerializer {

    private val proto = ProtoBuf {
        encodeDefaults = false
    }

    fun <DATA> serialize(kSerializer: KSerializer<DATA>, data: DATA): ByteArray =
        proto.encodeToByteArray(kSerializer, data)

    fun <DATA> deserialize(kSerializer: KSerializer<DATA>, bytes: ByteArray) =
        proto.decodeFromByteArray(kSerializer, bytes)




}