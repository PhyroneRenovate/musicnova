import eu.musicnova.shared.*
import kotlin.test.Test
import kotlin.test.assertEquals

class PacketTests {
    @Test
    fun testPacketBotData() {
        val exampleBotDataPacket = BotData(
            UUIDIdentifier(ByteArray(16) { (Byte.MIN_VALUE..Byte.MAX_VALUE).random().toByte() }),
            "test-bot",
            false
        )
        val bytes = InterPlatformSerializer.serialize(BotData.serializer(), exampleBotDataPacket)
        println(bytes.joinToString(","))
        val reAssambledExampleBotDataPacket = InterPlatformSerializer.deserialize(BotData.serializer(), bytes)
        assertEquals(exampleBotDataPacket, reAssambledExampleBotDataPacket)
    }

    @Test
    fun testPacketBotDataList() {
        val exampleBotDataPacket = listOf(
            BotData(
                UUIDIdentifier(ByteArray(16) { (Byte.MIN_VALUE..Byte.MAX_VALUE).random().toByte() }),
                "test-bot",
                false
            ),
            BotData(
                UUIDIdentifier(ByteArray(16) { (Byte.MIN_VALUE..Byte.MAX_VALUE).random().toByte() }),
                "test-bot",
                true
            )
        )

        val bytes = InterPlatformSerializer.serializeList(BotData.serializer(), exampleBotDataPacket)
        println(bytes.joinToString(","))
        val reAssambledExampleBotDataPacket = InterPlatformSerializer.deserializeList(BotData.serializer(), bytes)
        assertEquals(exampleBotDataPacket, reAssambledExampleBotDataPacket)
    }
}