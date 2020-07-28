package eu.musicnova.frontend.dashboard

import eu.musicnova.frontend.thrd.PopperConfiguration
import eu.musicnova.frontend.thrd.Swal
import eu.musicnova.frontend.thrd.createPopper
import eu.musicnova.frontend.thrd.fire
import eu.musicnova.frontend.utils.newBody
import eu.musicnova.frontend.utils.send
import eu.musicnova.frontend.utils.wsBaseURL
import eu.musicnova.shared.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.dom.append
import kotlinx.html.h1
import kotlinx.html.js.a
import kotlinx.html.js.div
import kotlinx.html.js.nav
import kotlinx.html.js.onClickFunction
import kotlinx.html.li
import kotlinx.html.role
import kotlinx.html.ul
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.WebSocket
import kotlin.browser.document
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class DashboardSession {
    var socket: WebSocket? = null

    private val packetSendQueue = mutableListOf<WsPacket>()

    private fun sendQueuedPackets() {
        val iterator = packetSendQueue.iterator()
        while (iterator.hasNext()) {
            sendPacket(iterator.next())
        }
    }

    suspend fun openSocket() = suspendCoroutine<WebSocket> { lock ->
        val socket = WebSocket("$wsBaseURL${SharedConst.SOCKET_PATH}")
        socket.binaryType = BinaryType.ARRAYBUFFER
        socket.onerror = {
            lock.resumeWithException(IllegalStateException("websocket failed"))
        }
        socket.onopen = {
            sendQueuedPackets()
            lock.resume(socket)
            //socket.send(WsPacketSwitchBot(BotIdentifier(32124324323, -3211234113, 23)))
        }
        socket.onclose = {
            console.log("socked closed", it)
        }
        socket.onmessage = {
            val packetBuffer = it.data as? ArrayBuffer
            if (packetBuffer != null) {
                val packetBytes = Int8Array(packetBuffer).unsafeCast<ByteArray>()
                val packet = WsPacketSerializer.deserialize(packetBytes)
                handleIncommingPacket(packet)
            }
            Unit
        }

    }

    private fun handleIncommingPacket(packet: WsPacket) {

    }

    fun sendPacket(packet: WsPacket) {
        val socket = this.socket
        if (socket == null) {
            packetSendQueue.add(packet)
        } else {
            socket.send(packet)
        }
    }

    fun openBotSelect() {
        Swal.fire {
            title = "Select Bot"
        }
        Swal.getContent().append {
            div("swal-scrool-box") {
                ul {
                    repeat(50) {
                        li {
                            a {
                                +"$it"
                            }
                        }
                    }
                }
            }
        }
    }

    private fun buildPage() {
        newBody().append {

            nav(classes = "navbar") {

                div(classes = "navbar-item") {

                    val btn = a {
                        +"Select Bot"
                        onClickFunction = {
                            openBotSelect()
                        }
                    }
                }
            }
        }
    }

    suspend fun start() {
        GlobalScope.launch { buildPage() }
        socket = openSocket()
    }
}