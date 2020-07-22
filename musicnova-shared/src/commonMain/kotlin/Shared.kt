package eu.musicnova.shared

import kotlinx.serialization.*
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.jvm.JvmOverloads


object SharedConst {
    const val VERSION = "0.0.1-SNAPSHOT"
    const val INTERNAL_API_PATH = "/api/internal"
    const val SOCKET_PATH = "$INTERNAL_API_PATH/webClient"
    const val START_DATA_FIELD = "pageStartData"
}

@Serializable
data class PageStartData(
        val loginStatus: LoginStatus,
        val startContent: ContentInfomation? = null
)

@Serializable
enum class WsPacketID(val serializer: KSerializer<out WsPacket>) {
    LOGIN(WsPacketLogin.serializer()),
    LOGOUT(WsPacketLogout.serializer()),
    SWITCH_BOT(WsPacketSwitchBot.serializer())
}

@Serializable
data class WsPacketHead(
        val packetID: WsPacketID,
        val requestID: Long? = null
)

val protoBuf = ProtoBuf(false)

object WsPacketSerializer {

    @JvmOverloads
    fun serialize(packet: WsPacket, requestID: Long? = null): ByteArray {
        val packetID = packet.packetID()
        val packetHeadBytes = protoBuf.dump(WsPacketHead.serializer(), WsPacketHead(packetID, requestID))
        val packetBodyBytes = packet.toBytes()
        return byteArrayOf(packetHeadBytes.size.toByte()) + packetHeadBytes + packetBodyBytes
    }

    fun deserialize(bytes: ByteArray): Pair<WsPacketHead, WsPacket> {
        val offset = bytes.offSet()
        val headBytes = bytes.copyOfRange(1, offset + 1)
        val bodyBytes = bytes.copyOfRange(offset + 1, bytes.size)
        val head = protoBuf.load(WsPacketHead.serializer(), headBytes)
        val packetID = head.packetID
        val packet = protoBuf.load(packetID.serializer, bodyBytes)
        return Pair(head, packet)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun ByteArray.offSet() = first().toInt()

    private fun WsPacket.toBytes() = when (this) {
        is WsPacketLogin -> protoBuf.dump(WsPacketLogin.serializer(), this)
        is WsPacketLogout -> protoBuf.dump(serializer(), this)
        is WsPacketSwitchBot -> protoBuf.dump(WsPacketSwitchBot.serializer(), this)
    }

    private fun WsPacket.packetID() = when (this) {
        is WsPacketLogin -> WsPacketID.LOGIN
        is WsPacketLogout -> WsPacketID.LOGOUT
        is WsPacketSwitchBot -> WsPacketID.SWITCH_BOT
    }
}


/* Ws Packets */
@Serializable
sealed class WsPacket

@Serializable
data class WsPacketSwitchBot(
        val bot: BotIdentifier
) : WsPacket()

@Serializable
data class WsPacketLogin(
        val email: String,
        val password: String,
        val keepLogin: Boolean = false
) : WsPacket()

@Serializable
object WsPacketLogout : WsPacket() {
    override fun toString(): String = "WsPacketLogout()"
}

/* Data container */

@Serializable
data class ContentInfomation(
        val contentIdentifier: String,
        val title: String? = null,
        val path: String? = null
)

@Serializable
data class BotIdentifier(
        val mostSignificantBits: Long,
        val leastSignificantBits: Long,
        val subID: Long? = null
)

enum class LoginStatus {
    LOGOUT, LOGIN, OTP, BLOCKED,
}
