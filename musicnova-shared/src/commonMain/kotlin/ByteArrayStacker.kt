package eu.musicnova.shared

object ByteArrayStacker {

    fun stackBytes(list: List<ByteArray>): ByteArray {
        var result = byteArrayOf()
        list.forEach { bytes ->
            val size = bytes.size
            if (size > Byte.MAX_VALUE) {
                val header = InterPlatformSerializer.serialize(LargePacketHeader.serializer(), LargePacketHeader(size))
                result += (header.size * -1).toByte()
                result += header
            } else {
                result += size.toByte()
            }
            result += bytes
        }
        return result
    }

    fun unStackBytes(bytes: ByteArray): List<ByteArray> {
        val result = mutableListOf<ByteArray>()
        var offset = 0
        val length = bytes.size
        while (offset < length) {
            val packetOffsetByte = bytes[offset]
            offset++;
            when {
                packetOffsetByte > 0 -> {
                    val size = packetOffsetByte.toInt()
                    result.add(bytes.copyOfRange(offset, offset + size))
                    offset += size
                }
                packetOffsetByte < 0 -> {
                    val largeHeadSize = packetOffsetByte * -1
                    val largePacketHeader = InterPlatformSerializer.deserialize(
                        LargePacketHeader.serializer(),
                        bytes.copyOfRange(offset, offset + largeHeadSize)
                    )
                    offset += largeHeadSize
                    val size = largePacketHeader.size
                    result.add(  bytes.copyOfRange(offset, offset + size))
                    offset += size
                }
                packetOffsetByte == 0.toByte() -> {
                    result.add(byteArrayOf())
                }
            }
        }
        return result
    }
}