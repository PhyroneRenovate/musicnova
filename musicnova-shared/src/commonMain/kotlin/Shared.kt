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
    const val INTERNAL_SET_SELECT_COOkIE = "$INTERNAL_API_PATH/setBotSelectCookie"

    const val INTERNAL_SET_THEME_PATH = "$INTERNAL_LOGIN_PATH/setTheme"
    const val INTERNAL_GET_BOTS_REQUEST = "$INTERNAL_API_PATH/getBots"
    const val SOCKET_PATH = "$INTERNAL_API_PATH/event"
    const val START_DATA_FIELD = "pageStartData"
    const val STYLE_LINK_ID = "style-link"
}

val protoBuf = ProtoBuf(false)

@Serializable
data class PageStartData(
        val loginStatus: LoginStatus,
        val dashboardPage: DashboardPage,
        val theme: WebTheme,
        val debug: Boolean = false
)

@Serializable
enum class WsPacketID(val serializer: KSerializer<out WsPacket>) {
    CLOSE(WsPacketClose.serializer()),
    SELECT_BOT(WsPacketUpdateSelectedBot.serializer()),
    UPDATE_IS_CONNECTED(WsPacketBotUpdateIsConnected.serializer()),
    PLAYER_PLAY_STREAM(WsPacketBotPlayerPlayStream.serializer()),
    PLAYER_UPDATE_VOLUME(WsPacketBotPlayerUpdateVolume.serializer()),
    PLAYER_UPDATE_IS_PLAYING(WsPacketBotPlayerUpdateIsPlaying.serializer()),
    PLAYER_UPDATE_SONG_INFO(WsPacketUpdateSongInfo.serializer()),
    PLAYER_UPDATE_SONG_DURATION(WsPacketUpdateSongDurationPosition.serializer()),
    UPDATE_BOT_INFO(WsPacketUpdateBotInfo.serializer()),
    PLAYER_STOP_TRACK(WsPacketBotPlayerStopTrack.serializer())
}

@Serializable
data class WsPacketHead(
        val packetID: WsPacketID
)

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
        is WsPacketBotPlayerUpdateIsPlaying -> protoBuf.dump(WsPacketBotPlayerUpdateIsPlaying.serializer(), this)
        is WsPacketUpdateSongInfo -> protoBuf.dump(WsPacketUpdateSongInfo.serializer(), this)
        is WsPacketBotUpdateIsConnected -> protoBuf.dump(WsPacketBotUpdateIsConnected.serializer(), this)
        is WsPacketUpdateBotInfo -> protoBuf.dump(WsPacketUpdateBotInfo.serializer(), this)
        is WsPacketUpdateSongDurationPosition -> protoBuf.dump(WsPacketUpdateSongDurationPosition.serializer(), this)
        is WsPacketBotPlayerStopTrack -> protoBuf.dump(serializer(), this)
    }

    private fun WsPacket.packetID() = when (this) {
        is WsPacketClose -> WsPacketID.CLOSE
        is WsPacketBotPlayerPlayStream -> WsPacketID.PLAYER_PLAY_STREAM
        is WsPacketBotPlayerUpdateVolume -> WsPacketID.PLAYER_UPDATE_VOLUME
        is WsPacketUpdateSelectedBot -> WsPacketID.SELECT_BOT
        is WsPacketBotPlayerUpdateIsPlaying -> WsPacketID.PLAYER_UPDATE_IS_PLAYING
        is WsPacketUpdateSongInfo -> WsPacketID.PLAYER_UPDATE_SONG_INFO
        is WsPacketBotUpdateIsConnected -> WsPacketID.UPDATE_IS_CONNECTED
        is WsPacketUpdateBotInfo -> WsPacketID.UPDATE_BOT_INFO
        is WsPacketUpdateSongDurationPosition -> WsPacketID.PLAYER_UPDATE_SONG_DURATION
        is WsPacketBotPlayerStopTrack -> WsPacketID.PLAYER_STOP_TRACK
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
data class WsPacketUpdateSelectedBot(
        val botIdentifier: BotIdentifier?
) : WsPacket()

@Serializable
data class WsPacketBotUpdateIsConnected(
        val isConnected: Boolean
) : WsPacket()

@Serializable
data class WsPacketBotPlayerPlayStream(
        val url: String
) : WsPacket()

@Serializable
data class WsPacketBotPlayerUpdateVolume(
        val newVolume: Int
) : WsPacket()

@Serializable
data class WsPacketUpdateSongDurationPosition(
        val postition: Long
) : WsPacket()

@Serializable
data class WsPacketBotPlayerUpdateIsPlaying(
        val isPlaying: Boolean? = null
) : WsPacket()

@Serializable
data class WsPacketUpdateSongInfo(
        //title of track or null if no track is playing
        val title: String? = null,
        val author: String? = null,
        //lenght of the song in millis or null if stream
        val length: Long? = null
) : WsPacket()

@Serializable
data class WsPacketUpdateBotInfo(
        val data: BotData? = null
) : WsPacket()

@Serializable
object WsPacketBotPlayerStopTrack : WsPacket()


/* "REST" (its like rest but protobuf instant of json... so its not) packets */

@Serializable
data class PacketLoginRequest(
        val username: String,
        val password: String,
        val otp: String? = null
)

@Serializable
data class ChangeThemeRequest(
        val newTheme: WebTheme
)

@Serializable
data class PacketLoginResponse(
        val status: LoginStatusResponse
)

@Serializable
data class PacketBotsResponse(
        val bots: List<BotData> = listOf()
)


/* Data container */

@Serializable
data class BotData(
        val identifier: BotIdentifier,
        val name: String,
        val isChildBot: Boolean,
        val isMusicBot: Boolean
)

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

enum class LoginStatusResponse {
    VALID, INVALID, BLOCKED
}

@Serializable
class EmptyObject