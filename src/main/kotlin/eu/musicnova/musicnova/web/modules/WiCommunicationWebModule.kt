package eu.musicnova.musicnova.web.modules

import com.google.common.primitives.Bytes
import eu.musicnova.musicnova.database.jpa.PersistentWebUserData
import eu.musicnova.musicnova.module.WebModule
import eu.musicnova.shared.SharedConst
import eu.musicnova.shared.WsPacket
import eu.musicnova.shared.WsPacketHead
import eu.musicnova.shared.WsPacketSerializer
import io.ktor.application.Application
import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.routing
import io.ktor.websocket.webSocket
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class WiCommunicationWebModule : WebModule {

    override fun Application.invoke() {
        routing {

            put(SharedConst.SOCKET_PATH) {

            }
            post(SharedConst.SOCKET_PATH) {

            }
            webSocket(SharedConst.SOCKET_PATH) {
                for (frame in incoming) {
                    if (frame is Frame.Binary) {
                        val bytes = frame.data
                        launch {

                        }
                    }
                }
            }
        }
    }


    interface CommunicationAdapter{
        suspend fun pushEvent(event: WsPacket)
    }

    interface CommunicationSession {

        suspend fun pushEvent(event: WsPacket)
    }

}


