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
val debug by lazy { pageStartData.debug }

private fun loadPageStartData(): PageStartData {
    val pageStartBaseString = window[SharedConst.START_DATA_FIELD] as String
    val pageStartBytes = window.atob(pageStartBaseString).encodeToByteArray()
    return PageStartData.fromBytes(pageStartBytes)
}

suspend fun <RESPONSE> getRequest(url: String, kSerializer: KSerializer<RESPONSE>) =
    InterPlatformSerializer.deserialize(
        kSerializer,
        getRequest(url)
    )

suspend fun getRequest(url: String) = suspendCoroutine<ByteArray> { lock ->
    val httpRequest = prepareHttpRequest()
    httpRequest.continueOnResponse(lock)
    httpRequest.open("GET", url)
    httpRequest.send()
}

suspend fun <REQUEST, RESPONSE> postRequest(
    url: String,
    requestSerializer: KSerializer<REQUEST>,
    responseSerializer: KSerializer<RESPONSE>,
    request: REQUEST
) = InterPlatformSerializer.deserialize(
    responseSerializer,
    postRequest(url, InterPlatformSerializer.serialize(requestSerializer, request))
)

suspend fun postRequest(
    url: String,
    request: ByteArray
) = suspendCoroutine<ByteArray> { lock ->
    val httpRequest = prepareHttpRequest()
    httpRequest.continueOnResponse(lock)
    httpRequest.open("POST", url)
    httpRequest.send(request)
}

suspend fun <REQEST> putRequest(url: String, kSerializer: KSerializer<REQEST>, request: REQEST) =
    putRequest(url, InterPlatformSerializer.serialize(kSerializer, request))

suspend fun putRequest(url: String, request: ByteArray) =
    suspendCoroutine<Unit> { lock ->
        val httpRequest = prepareHttpRequest()
        httpRequest.continueOnResponseWithoutContent(lock)
        httpRequest.open("PUT", url)
        httpRequest.send(request)
    }

fun XMLHttpRequest.continueOnResponseWithoutContent(lock: Continuation<Unit>) {
    onload = {
        val buffer = response as? ArrayBuffer
        runCatching {
            if (buffer == null) {
                lock.resumeWithException(IllegalStateException("wrong response"))
            } else {
                if (debug) {
                    console.info(this, "got expected empty response")
                }
                lock.resume(Unit)
            }
        }.getOrElse {
            lock.resumeWithException(it)
        }
    }
}

fun XMLHttpRequest.continueOnResponse(lock: Continuation<ByteArray>) {
    onload = {
        val buffer = response as? ArrayBuffer
        runCatching {
            if (buffer == null) {
                lock.resumeWithException(IllegalStateException("wrong response"))
            } else {
                val bytes = Uint8Array(buffer).unsafeCast<ByteArray>()
                if (debug) {
                    console.info(
                        this,
                        "recieved packet bytes (lenght: ${bytes.size}bytes)",
                        bytes.joinToString(","),
                        window.btoa(bytes.unsafeCast<String>())
                    )
                }
                lock.resume(bytes)
            }
        }.getOrElse {
            lock.resumeWithException(it)
        }
    }
}


fun prepareHttpRequest(): XMLHttpRequest {
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
            ChangeThemeRequest.serializer(),
            ChangeThemeRequest(theme)
        )
    }
    styleLink.href = theme.fullPath
}

fun Console.debug(vararg any: Any?) {
    asDynamic().debug(any)
}

@Suppress("NOTHING_TO_INLINE")
inline fun randomId() = Random.nextDouble(Double.MIN_VALUE, Double.MAX_VALUE).toString()