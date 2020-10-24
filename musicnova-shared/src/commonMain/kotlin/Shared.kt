@file:JvmName("InterPlatformShared")

package eu.musicnova.shared

import kotlinx.serialization.*
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads


object SharedConst {

    const val LOGIN_PAGE_PATH = "/login"
    const val VERSION = "0.0.1-SNAPSHOT"
    const val INTERNAL_API_PATH = "/api/internal"
    const val INTERNAL_LOGIN_PATH = "$INTERNAL_API_PATH/login"
    const val INTERNAL_LOGOUT_PATH = "$INTERNAL_API_PATH/logout"
    const val INTERNAL_OTP_PATH = "$INTERNAL_API_PATH/otp"
    const val INTERNAL_FILE_UPLOAD = "$INTERNAL_API_PATH/upload"
    const val INTERNAL_SET_SELECT_COOkIE = "$INTERNAL_API_PATH/setBotSelectCookie"
    const val INTERNAL_GET_TRACKS_PATH = "$INTERNAL_API_PATH/getTracks"

    const val INTERNAL_SET_THEME_PATH = "$INTERNAL_LOGIN_PATH/setTheme"
    const val INTERNAL_GET_BOTS_REQUEST = "$INTERNAL_API_PATH/getBots"
    const val SOCKET_PATH = "$INTERNAL_API_PATH/event"
    const val START_DATA_FIELD = "pageStartData"
    const val STYLE_LINK_ID = "style-link"
}

@Serializable
data class PageStartData(
    val loginStatus: LoginStatus,
    val dashboardPage: PageContent,
    val theme: WebTheme,
    val debug: Boolean = false
) {

    fun toBytes() = protoBuf.encodeToByteArray(serializer(), this)

    companion object Static {
        private val protoBuf = ProtoBuf { encodeDefaults = false }
        fun fromBytes(bytes: ByteArray) = protoBuf.decodeFromByteArray(serializer(), bytes)
    }
}

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
    PLAYER_STOP_TRACK(WsPacketBotPlayerStopTrack.serializer()),
    PLAYER_PLAY_TRACK(WsPacketBotPlayTrack.serializer())
}


@Serializable
data class WsPacketHead(
    val packetID: WsPacketID
)

object WsPacketSerializer {

    private val protoBuf = ProtoBuf {
        this.encodeDefaults = false
    }

    @JvmOverloads
    fun serialize(packet: WsPacket): ByteArray {
        val packetID = packet.packetID()
        val packetHeadBytes = protoBuf.encodeToByteArray(WsPacketHead(packetID))
        val packetBodyBytes = packet.toBytes()
        return byteArrayOf(packetHeadBytes.size.toByte()) + packetHeadBytes + packetBodyBytes
    }

    fun deserialize(bytes: ByteArray): WsPacket {
        val offset = bytes.offSet()
        val headBytes = bytes.copyOfRange(1, offset + 1)
        val bodyBytes = bytes.copyOfRange(offset + 1, bytes.size)
        val head = protoBuf.decodeFromByteArray<WsPacketHead>(headBytes)
        val packetID = head.packetID
        return protoBuf.decodeFromByteArray(packetID.serializer, bodyBytes)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun ByteArray.offSet() = first().toInt()

    private fun WsPacket.toBytes() = when (this) {
        is WsPacketClose -> protoBuf.encodeToByteArray(this)
        is WsPacketBotPlayerUpdateVolume -> protoBuf.encodeToByteArray(WsPacketBotPlayerUpdateVolume.serializer(), this)
        is WsPacketBotPlayerPlayStream -> protoBuf.encodeToByteArray(WsPacketBotPlayerPlayStream.serializer(), this)
        is WsPacketUpdateSelectedBot -> protoBuf.encodeToByteArray(WsPacketUpdateSelectedBot.serializer(), this)
        is WsPacketBotPlayerUpdateIsPlaying -> protoBuf.encodeToByteArray(
            WsPacketBotPlayerUpdateIsPlaying.serializer(),
            this
        )
        is WsPacketUpdateSongInfo -> protoBuf.encodeToByteArray(WsPacketUpdateSongInfo.serializer(), this)
        is WsPacketBotUpdateIsConnected -> protoBuf.encodeToByteArray(WsPacketBotUpdateIsConnected.serializer(), this)
        is WsPacketUpdateBotInfo -> protoBuf.encodeToByteArray(WsPacketUpdateBotInfo.serializer(), this)
        is WsPacketUpdateSongDurationPosition -> protoBuf.encodeToByteArray(
            WsPacketUpdateSongDurationPosition.serializer(),
            this
        )
        is WsPacketBotPlayerStopTrack -> protoBuf.encodeToByteArray(serializer(), this)
        is WsPacketBotPlayTrack -> protoBuf.encodeToByteArray(serializer(), this)
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
        is WsPacketBotPlayTrack -> WsPacketID.PLAYER_PLAY_TRACK
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
    val botIdentifier: UUIDIdentifier?
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
data class WsPacketBotPlayTrack(
    val track: UUIDIdentifier
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

/* Data container */

@Serializable
data class LargePacketHeader(
    val size: Int
)

@Serializable
data class AudioTrackData(
    val identifier: UUIDIdentifier,
    val title: String
)

@Serializable
data class BotData(
    val identifier: UUIDIdentifier,
    val name: String,
    val isMusicBot: Boolean = true
)

@Serializable
data class ContentInfomation(
    val contentIdentifier: String,
    val title: String? = null,
    val path: String? = null
)

@Serializable
data class UUIDIdentifier(
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UUIDIdentifier

        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}


enum class PageContent(val path: String, val title: String? = null) {
    DASHBOARD("/", "Dashboard"), PROFILE("/profile", "Profile"), TRACKS("/tracks")
}

enum class LoginStatus {
    LOGIN, LOGOUT, BLOCKED, OTP, ERROR
}

enum class LoginStatusResponse {
    VALID, INVALID, BLOCKED
}
