package eu.musicnova.shared

import kotlinx.serialization.*
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.jvm.JvmOverloads


object SharedConst {
    const val LOGIN_PAGE_PATH = "/login"
    const val VERSION = "0.0.1-SNAPSHOT"
    const val INTERNAL_API_PATH = "/api/internal"
    const val INTERNAL_LOGIN_PATH = "$INTERNAL_API_PATH/login"
    const val INTERNAL_LOGOUT_PATH = "$INTERNAL_API_PATH/logout"
    const val INTERNAL_OTP_PATH = "$INTERNAL_API_PATH/otp"
    const val SOCKET_PATH = "$INTERNAL_API_PATH/event"
    const val START_DATA_FIELD = "pageStartData"
}

@Serializable
data class PageStartData(
        val loginStatus: LoginStatus,
        val dashboardPage: DashboardPage
)

@Serializable
enum class WsPacketID(val serializer: KSerializer<out WsPacket>) {
    CLOSE(WsPacketClose.serializer()),
    SELECT_BOT(WsPacketUpdateSelectedBot.serializer()),
    PLAYER_PLAY_STREAM(WsPacketBotPlayerPlayStream.serializer()),
    PLAYER_UPDATE_VOLUME(WsPacketBotPlayerUpdateVolume.serializer())
}

@Serializable
data class WsPacketHead(
        val packetID: WsPacketID
)

val protoBuf = ProtoBuf(false)

object WsPacketSerializer {

    @JvmOverloads
    fun serialize(packet: WsPacket): ByteArray {
        val packetID = packet.packetID()
        val packetHeadBytes = protoBuf.dump(WsPacketHead.serializer(), WsPacketHead(packetID))
        val packetBodyBytes = packet.toBytes()
        return byteArrayOf(packetHeadBytes.size.toByte()) + packetHeadBytes + packetBodyBytes
    }

    fun deserialize(bytes: ByteArray): WsPacket {
        val offset = bytes.offSet()
        val headBytes = bytes.copyOfRange(1, offset + 1)
        val bodyBytes = bytes.copyOfRange(offset + 1, bytes.size)
        val head = protoBuf.load(WsPacketHead.serializer(), headBytes)
        val packetID = head.packetID
        return protoBuf.load(packetID.serializer, bodyBytes)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun ByteArray.offSet() = first().toInt()

    private fun WsPacket.toBytes() = when (this) {

        is WsPacketClose -> protoBuf.dump(WsPacketClose.serializer(), this)
        is WsPacketBotPlayerUpdateVolume -> protoBuf.dump(WsPacketBotPlayerUpdateVolume.serializer(), this)
        is WsPacketBotPlayerPlayStream -> protoBuf.dump(WsPacketBotPlayerPlayStream.serializer(), this)
        is WsPacketUpdateSelectedBot -> protoBuf.dump(WsPacketUpdateSelectedBot.serializer(), this)
    }

    private fun WsPacket.packetID() = when (this) {
        is WsPacketClose -> WsPacketID.CLOSE
        is WsPacketBotPlayerPlayStream -> WsPacketID.PLAYER_PLAY_STREAM
        is WsPacketBotPlayerUpdateVolume -> WsPacketID.PLAYER_UPDATE_VOLUME
        is WsPacketUpdateSelectedBot -> WsPacketID.SELECT_BOT
    }
}


/* Ws Packets */
@Serializable
sealed class WsPacket

@Serializable
data class WsPacketClose(
        val reason: Reason
) : WsPacket() {
    enum class Reason {
        LOGOUT, BLOCKED, GONE_AWAY, ERROR
    }
}

@Serializable
data class WsPacketBotPlayerPlayStream(
        val url: String
) : WsPacket()

@Serializable
data class WsPacketBotPlayerUpdateVolume(
        val newVolume: Int
) : WsPacket()

@Serializable
data class WsPacketUpdateSelectedBot(
        val bitIdentifier: BotIdentifier?
) : WsPacket()


/* "REST" (its like rest but protobuf instant of json... so its not) packets */

@Serializable
data class PacketLoginRequest(
        val username: String,
        val password: String,
        val otp: String? = null
)

@Serializable
data class PacketLoginResponse(
        val status: LoginStatus
)


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

enum class DashboardPage(val path: String) {
    DASHBOARD("/"), PROFILE("/profile")
}

enum class LoginStatus {
    LOGIN, LOGOUT, BLOCKED, OTP, ERROR
}

@Serializable
class EmptyObject