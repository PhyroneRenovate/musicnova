package eu.musicnova.shared

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

object InterPlatformSerializer {

    fun <DATA> serialize(kSerializer: KSerializer<DATA>,data: DATA): ByteArray = Json.encodeToString(kSerializer,data).encodeToByteArray()

    fun <DATA> deserialize(kSerializer: KSerializer<DATA>, bytes: ByteArray) = Json.decodeFromString(kSerializer,bytes.decodeToString())

}