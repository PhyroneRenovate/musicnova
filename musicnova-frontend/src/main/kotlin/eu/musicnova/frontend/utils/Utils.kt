package eu.musicnova.frontend.utils


import eu.musicnova.shared.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLLinkElement
import org.w3c.dom.WebSocket
import org.w3c.dom.get
import org.w3c.xhr.ARRAYBUFFER
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import kotlin.browser.document
import kotlin.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Console

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

@Suppress("NOTHING_TO_INLINE")
inline fun newBody() = (document.body?.also { it.innerHTML = "" }
        ?: document.createElement("body").also { document.body = it.unsafeCast<HTMLElement>() })

val pageStartData by lazy { loadPageStartData() }

private fun loadPageStartData(): PageStartData {
    val pageStartBaseString = window[SharedConst.START_DATA_FIELD] as String
    val pageStartBytes = window.atob(pageStartBaseString)
    @OptIn(ExperimentalStdlibApi::class)
    return protoBuf.load(PageStartData.serializer(), pageStartBytes.encodeToByteArray())
}

suspend inline fun <reified REQ, reified RES> postRequest(url: String, request: REQ, requestSerializer: KSerializer<REQ>, responseSerializer: KSerializer<RES>) = suspendCoroutine<RES> { lock ->
    val httpRequest = XMLHttpRequest()
    httpRequest.withCredentials = true
    httpRequest.responseType = XMLHttpRequestResponseType.ARRAYBUFFER
    httpRequest.onload = {
        val buffer = httpRequest.response as? ArrayBuffer
        runCatching {
            if (buffer == null) {
                lock.resumeWithException(IllegalStateException("wrong response"))
            } else {
                val bytes = Uint8Array(buffer).unsafeCast<ByteArray>()
                lock.resume(protoBuf.load(responseSerializer, bytes))
            }
        }.getOrElse {
            lock.resumeWithException(it)
        }

    }
    httpRequest.open("POST", url)

    httpRequest.send(protoBuf.dump(requestSerializer, request))
}

private val styleLink by lazy { document.getElementById(SharedConst.STYLE_LINK_ID) as HTMLLinkElement }
fun setTheme(theme: WebTheme) {
    println("switch to theme ${theme.name} with url ${theme.fullPath}")
    GlobalScope.launch { postRequest(SharedConst.INTERNAL_SET_THEME_PATH, ChangeThemeRequest(theme), ChangeThemeRequest.serializer(), EmptyObject.serializer()) }
    styleLink.href = theme.fullPath
}

fun Console.debug(vararg any: Any?) {
    asDynamic().debug(any)
}