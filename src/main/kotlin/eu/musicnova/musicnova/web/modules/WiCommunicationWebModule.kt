package eu.musicnova.musicnova.web.modules

import eu.musicnova.musicnova.bot.Bot
import eu.musicnova.musicnova.bot.BotManager
import eu.musicnova.musicnova.database.jpa.PersistentWebUserSessionData
import eu.musicnova.musicnova.module.WebModule
import eu.musicnova.musicnova.web.auth.WebSessionAuthManager
import eu.musicnova.musicnova.web.auth.WebUserLoginManager
import eu.musicnova.shared.*
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.*
import io.ktor.request.receive
import io.ktor.response.respondBytes
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.ktor.websocket.webSocket
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class WiCommunicationWebModule : WebModule {

    @Autowired
    lateinit var webSessionAuthManager: WebSessionAuthManager

    @Autowired
    lateinit var webUserAuthManager: WebUserLoginManager

    private val logger = LoggerFactory.getLogger(this::class.java)
    override fun Application.invoke() {
        routing {
            post(SharedConst.INTERNAL_LOGIN_PATH) {
                val requestBytes = call.receive<ByteArray>()
                val request = protoBuf.load(PacketLoginRequest.serializer(), requestBytes)
                val user = webUserAuthManager[request.username]

                val response = if (user?.checkPassword(request.password) == true) {
                    with(webSessionAuthManager) { call.createSession(user) }
                    PacketLoginResponse(LoginStatusResponse.VALID)
                } else {
                    PacketLoginResponse(LoginStatusResponse.INVALID)
                }

                call.respondBytes(protoBuf.dump(PacketLoginResponse.serializer(), response), ContentType.Application.ProtoBuf)
            }
            post(SharedConst.INTERNAL_SET_THEME_PATH) {
                val request = protoBuf.load(ChangeThemeRequest.serializer(), call.receive())
                call.sessions.set(request.newTheme)
                call.respondBytes(protoBuf.dump(EmptyObject.serializer(), EmptyObject()), ContentType.Application.ProtoBuf)
            }

            post(SharedConst.SOCKET_PATH) {
                TODO("implement")
            }
            webSocket(SharedConst.SOCKET_PATH) {
                with(webSessionAuthManager) {

                    val session = call.getUserSession()
                    if (session != null) {
                        WebSocketCommunicationAdapter(this@webSocket, session).start()
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
            private val loginSession: PersistentWebUserSessionData
    ) : CommunicationAdapter {
        private val session: CommunicationSession = CommunicationSession(this, loginSession)
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

    inner class CommunicationSession(
            private val adapter: CommunicationAdapter,
            private val session: PersistentWebUserSessionData
    ) {

        private var currentBot: Bot? = null

        private fun BotManager.findBot(identifier: BotIdentifier) = findBot(identifier.uuid, identifier.subID)
        private fun handleSwitchBot(packet: WsPacketUpdateSelectedBot) {
            val identifier = packet.botIdentifier
            if (identifier != null) {
                currentBot = botManager.findBot(identifier)
            }
        }

        private suspend fun handlePacketClose(packet: WsPacketClose) {
            adapter.stop()
        }

        suspend fun onPacket(packet: WsPacket) {
            when (packet) {
                is WsPacketClose -> handlePacketClose(packet)
                is WsPacketBotPlayerPlayStream -> TODO()
                is WsPacketBotPlayerUpdateVolume -> TODO()
                is WsPacketUpdateSelectedBot -> TODO()
            }
        }
    }

}


