package eu.musicnova.frontend

import eu.musicnova.shared.*
import org.khronos.webgl.Int8Array
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.WebSocket
import kotlin.browser.window

fun main() {
    console.log("Started...")
    console.log(pageStartData)
    window.onload = { onLoad() }

}

fun onLoad() {
    val socket = WebSocket("$wsBaseURL${SharedConst.SOCKET_PATH}")
    socket.binaryType = BinaryType.ARRAYBUFFER
    socket.onopen = {
        socket.send(WsPacketSwitchBot(BotIdentifier(32124324323, -3211234113, 23)))
    }
}
