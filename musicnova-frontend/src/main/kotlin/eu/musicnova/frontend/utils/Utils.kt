package eu.musicnova.frontend.utils


import eu.musicnova.shared.*
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
import kotlinx.browser.window
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.json.JsonEncoder
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Console
import kotlin.random.Random

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
    return protoBuf.decodeFromByteArray(PageStartData.serializer(), pageStartBytes.encodeToByteArray())
}

suspend fun <RES> getRequest(url: String, responseSerializer: KSerializer<RES>) = suspendCoroutine<RES> { lock ->
    val httpRequest = prepareHttpRequest()
    httpRequest.continueOnResponse(lock, responseSerializer)
    httpRequest.open("GET", url)
    httpRequest.send()
}

suspend fun <REQ, RES> postRequest(
    url: String,
    request: REQ,
    requestSerializer: KSerializer<REQ>,
    responseSerializer: KSerializer<RES>
) = suspendCoroutine<RES> { lock ->
    val httpRequest = prepareHttpRequest()
    httpRequest.continueOnResponse(lock, responseSerializer)
    httpRequest.open("POST", url)
    val postBytes = try {
        protoBuf.encodeToByteArray(requestSerializer, request)
    } catch (e: Throwable) {
        console.error("Request encoding failed", e)
        byteArrayOf()
    }
    httpRequest.send(postBytes)
}

suspend fun <REQ> putRequest(url: String, request: REQ, requestSerializer: KSerializer<REQ>) =
    suspendCoroutine<Unit> { lock ->
        val httpRequest = prepareHttpRequest()
        httpRequest.continueOnResponseWithoutContent(lock)
        httpRequest.open("PUT", url)
        httpRequest.send(protoBuf.encodeToByteArray(requestSerializer, request))
    }

fun XMLHttpRequest.continueOnResponseWithoutContent(lock: Continuation<Unit>) {
    onload = {
        val buffer = response as? ArrayBuffer
        runCatching {
            if (buffer == null) {
                lock.resumeWithException(IllegalStateException("wrong response"))
            } else {
                lock.resume(Unit)
            }
        }.getOrElse {
            lock.resumeWithException(it)
        }
    }
}

private fun <RES> XMLHttpRequest.continueOnResponse(lock: Continuation<RES>, ser: KSerializer<RES>) {
    onload = {
        val buffer = response as? ArrayBuffer
        runCatching {
            if (buffer == null) {
                lock.resumeWithException(IllegalStateException("wrong response"))
            } else {
                val bytes = Uint8Array(buffer).unsafeCast<ByteArray>()
                lock.resume(protoBuf.decodeFromByteArray(ser, bytes))
            }
        }.getOrElse {
            lock.resumeWithException(it)
        }
    }
}


private fun prepareHttpRequest(): XMLHttpRequest {
    val httpRequest = XMLHttpRequest()
    httpRequest.withCredentials = true
    httpRequest.responseType = XMLHttpRequestResponseType.ARRAYBUFFER
    return httpRequest
}

private val styleLink by lazy { document.getElementById(SharedConst.STYLE_LINK_ID) as HTMLLinkElement }
fun setTheme(theme: WebTheme) {
    println("switch to theme ${theme.name} with url ${theme.fullPath}")
    GlobalScope.launch {
        putRequest(
            SharedConst.INTERNAL_SET_THEME_PATH,
            ChangeThemeRequest(theme),
            ChangeThemeRequest.serializer()
        )
    }
    styleLink.href = theme.fullPath
}

fun Console.debug(vararg any: Any?) {
    asDynamic().debug(any)
}

@Suppress("NOTHING_TO_INLINE")
inline fun randomId() = Random.nextDouble(Double.MIN_VALUE, Double.MAX_VALUE).toString()