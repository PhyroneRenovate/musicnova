package eu.musicnova.musicnova.web.modules

import com.google.common.io.BaseEncoding
import eu.musicnova.musicnova.audio.AudioTrackController
import eu.musicnova.musicnova.bot.BotManager
import eu.musicnova.musicnova.bot.MusicBot
import eu.musicnova.musicnova.database.dao.PersistentWebUserSessionData
import eu.musicnova.musicnova.module.WebModule
import eu.musicnova.musicnova.utils.htmlContentType
import eu.musicnova.musicnova.web.auth.WebSessionAuthManager
import eu.musicnova.musicnova.web.auth.WebUserLoginManager
import eu.musicnova.musicnova.web.sesssions.SocketSessionManager
import eu.musicnova.shared.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.websocket.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import lombok.extern.slf4j.Slf4j
import org.apache.commons.io.IOUtils
import org.apache.commons.io.output.NullOutputStream
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.nio.file.Files

@Component
class WiCommunicationWebModule(
    val webSessionAuthManager: WebSessionAuthManager,
    val webUserAuthManager: WebUserLoginManager,
    val aBotManager: BotManager,
    val sessionManager: SocketSessionManager,
    val trackController: AudioTrackController
) : WebModule {


    private val logger = LoggerFactory.getLogger(this::class.java)
    override fun Application.invoke() {
        routing {
            post(SharedConst.INTERNAL_LOGIN_PATH) {
                val requestBytes = call.receive<ByteArray>()
                val request = InterPlatformSerializer.deserialize(PacketLoginRequest.serializer(), requestBytes)
                val user = webUserAuthManager[request.username]

                val response = if (user?.checkPassword(request.password) == true) {
                    with(webSessionAuthManager) { call.createSession(user) }
                    PacketLoginResponse(LoginStatusResponse.VALID)
                } else {
                    PacketLoginResponse(LoginStatusResponse.INVALID)
                }

                call.respondBytes(
                    InterPlatformSerializer.serialize(PacketLoginResponse.serializer(), response),
                    InterPlatformSerializer.htmlContentType
                )
            }

            put(SharedConst.INTERNAL_SET_THEME_PATH) {
                val request = InterPlatformSerializer.deserialize(ChangeThemeRequest.serializer(), call.receive())
                call.sessions.set(request.newTheme)
                call.respondBytes(byteArrayOf(), InterPlatformSerializer.htmlContentType)
            }

            get(SharedConst.INTERNAL_GET_BOTS_REQUEST) {

                val response = aBotManager.all().map { bot ->

                    BotData(
                        bot.uuid.toUUIDIdentifier(), bot.name
                            ?: bot.uuid.toString(),
                        bot is MusicBot
                    )
                }
                val responseBytes = InterPlatformSerializer.serializeList(
                    BotData.serializer(), response
                )
                logger.debug(
                    "respond bot request $response " +
                            "-> ${
                                BaseEncoding.base64().encode(responseBytes)
                            } (${responseBytes.size} bytes) [${responseBytes.joinToString(",")}]"
                )

                call.respondBytes(responseBytes, InterPlatformSerializer.htmlContentType)
            }

            put(SharedConst.INTERNAL_SET_SELECT_COOkIE) {
                call.sessions.set(InterPlatformSerializer.deserialize(UUIDIdentifier.serializer(), call.receive()))
                call.respondBytes(byteArrayOf(), ContentType.Application.ProtoBuf)
            }

            post(SharedConst.SOCKET_PATH) {
                TODO("implement")
            }

            post(SharedConst.INTERNAL_FILE_UPLOAD) {

                val inStream = call.receiveStream()
                println(call.request.headers.names())
                IOUtils.copy(inStream, NullOutputStream())
                call.respond(HttpStatusCode.Accepted, "ok")
            }

            get(SharedConst.INTERNAL_GET_TRACKS_PATH) {
                val bytes = InterPlatformSerializer.serializeList(
                    AudioTrackData.serializer(),
                    trackController.allTracks()
                        .map { trackDAO -> AudioTrackData(trackDAO.uuid.toUUIDIdentifier(), trackDAO.title) }
                )
                call.respondBytes(bytes, ContentType.Application.ProtoBuf)
            }

            webSocket(SharedConst.SOCKET_PATH) {
                with(webSessionAuthManager) {
                    val session = call.getUserSession()
                    if (session != null) {
                        WebSocketCommunicationAdapter(this@webSocket, session, call.sessions.get()).start()
                    } else {
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "login first"))
                    }
                }
            }
        }
    }


    interface CommunicationAdapter {
        suspend fun start()
        suspend fun stop()
        suspend fun sendPacket(event: WsPacket)
    }

    inner class WebSocketCommunicationAdapter(
        private val webSocket: DefaultWebSocketSession,
        loginSession: PersistentWebUserSessionData,
        selectedBot: UUIDIdentifier?
    ) : CommunicationAdapter {
        private val session: SocketSessionManager.CommunicationSession =
            sessionManager.CommunicationSession(this, loginSession, selectedBot)

        override suspend fun start() {
            coroutineScope {
                launch { webSocket.listener() }

            }
        }

        private suspend fun DefaultWebSocketSession.listener() {
            for (frame in incoming) {
                if (frame is Frame.Binary) {
                    val bytes = frame.data
                    launch {
                        runCatching { handlePacket(bytes) }.getOrElse {
                            logger.warn("handling websocket packet failed", it)
                        }
                    }
                }
            }
            session.onAdapterStop()
        }

        private suspend fun handlePacket(packetBytes: ByteArray) {
            val packet = WsPacketSerializer.deserialize(packetBytes)
            session.onPacket(packet)
        }


        override suspend fun stop() {
            webSocket.close()
        }

        override suspend fun sendPacket(event: WsPacket) {
            webSocket.send(WsPacketSerializer.serialize(event))
        }
    }

    @Autowired
    lateinit var botManager: BotManager


}


