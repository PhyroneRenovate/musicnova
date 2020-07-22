package eu.musicnova.frontend


import eu.musicnova.shared.*
import org.khronos.webgl.Int8Array
import org.w3c.dom.WebSocket
import org.w3c.dom.get
import kotlin.browser.window

val baseURL by lazy {
    val loc = window.location
    console.info("Location", loc)
    val baseURL = "${loc.protocol}//${loc.hostname}:${loc.port}/"
    console.info("BaseURL", baseURL)
    return@lazy baseURL
}
val wsBaseURL by lazy {
    val loc = window.location
    val proto = if (loc.protocol.equals("https:", true)) "wss" else "ws"
    return@lazy "$proto://${loc.hostname}:${loc.port}/"
}

fun WebSocket.send(byteArray: ByteArray) {
    send(byteArray.unsafeCast<Int8Array>())
}

fun WebSocket.send(packet: WsPacket) {
    send(WsPacketSerializer.serialize(packet))
}

val pageStartData by lazy { loadPageStartData() }

@OptIn(ExperimentalStdlibApi::class)
private fun loadPageStartData(): PageStartData {
    val pageStartBaseString = window[SharedConst.START_DATA_FIELD] as String
    val pageStartBytes = window.atob(pageStartBaseString)
    return protoBuf.load(PageStartData.serializer(), pageStartBytes.encodeToByteArray())
}