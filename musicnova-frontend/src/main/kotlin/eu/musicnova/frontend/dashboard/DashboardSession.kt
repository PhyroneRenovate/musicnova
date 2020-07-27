package eu.musicnova.frontend.dashboard

import eu.musicnova.frontend.utils.newBody
import eu.musicnova.frontend.utils.send
import eu.musicnova.frontend.utils.wsBaseURL
import eu.musicnova.shared.*
import kotlinx.html.dom.append
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
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

    private fun buildPage() {
        newBody().append {

        }
    }

    suspend fun start() {
        socket = openSocket()
        buildPage()
    }
}