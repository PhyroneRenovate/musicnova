package eu.musicnova.frontend.dashboard

import eu.musicnova.frontend.thrd.Swal
import eu.musicnova.frontend.thrd.fire
import eu.musicnova.frontend.utils.*
import eu.musicnova.shared.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.js.*
import kotlinx.html.role
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.*
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
                if (pageStartData.debug) {
                    console.info("received packet with ${packetBytes.size} bytes", packet)
                }
                handleIncommingPacket(packet)
            }
            Unit
        }

    }

    private fun handleIncommingPacket(packet: WsPacket) {
        when (packet) {
            is WsPacketBotPlayerUpdateVolume -> handleIncommingVolumeUpdate(packet)
            is WsPacketUpdateSongInfo -> handleSongInfoUpdate(packet)
            is WsPacketBotPlayerUpdateIsPlaying -> handlePlayerIsPlaying(packet)
            is WsPacketUpdateBotInfo -> handleBotUpdatePacket(packet)
            is WsPacketUpdateSongDurationPosition -> handleUpdateSongDuration(packet)
            else -> console.error("unhandled packet", packet)
        }
    }

    private fun handleUpdateSongDuration(packetPosition: WsPacketUpdateSongDurationPosition) {
        if (!volumeDurationSliderUpdateLock)
            playerDurationSlider.value = packetPosition.postition.toString()
    }

    private fun handleBotUpdatePacket(packet: WsPacketUpdateBotInfo) {
        val bot = packet.data
        if (bot == null) {
            playerSetEnabled(false)
        } else {
            playerSetEnabled(bot.isMusicBot)
        }
    }


    private fun handlePlayerIsPlaying(packet: WsPacketBotPlayerUpdateIsPlaying) {
        val playing = packet.isPlaying
        if (playing != null) {
            if(playing){
                playPauseIcon.classList.replace(IS_PLAYING_FAS, IS_PAUSED_FAS)
            }else{
                playPauseIcon.classList.replace(IS_PAUSED_FAS, IS_PLAYING_FAS)
            }

        }
    }



    fun handleIncommingVolumeUpdate(packet: WsPacketBotPlayerUpdateVolume) {
        if (!volumeSliderUpdateLock) playerVolumeSlider.value = packet.newVolume.toString()
    }

    fun handleSongInfoUpdate(packet: WsPacketUpdateSongInfo) {
        val title = packet.title
        playerDurationSlider.value = "0"
        if (title != null) {
            val maxLenght = packet.length
            if (maxLenght != null) {
                playerDurationSlider.max = maxLenght.toString()
                playerDurationSlider.disabled = false
            } else {
                playerDurationSlider.max = "0"
                playerDurationSlider.disabled = true
            }
        } else {
            playerDurationSlider.max = "0"
            playerDurationSlider.disabled = true
        }
    }

    fun sendPacket(packet: WsPacket) {
        val socket = this.socket
        if (socket == null) {
            packetSendQueue.add(packet)
        } else {
            val packetBytes = WsPacketSerializer.serialize(packet)
            if (pageStartData.debug) console.info("send packet with ${packetBytes.size} bytes", packet)
            socket.send(packetBytes)
        }
    }


    private fun selectBot(identifier: BotIdentifier) {
        GlobalScope.launch { postRequest(SharedConst.INTERNAL_SET_SELECT_COOkIE, identifier, BotIdentifier.serializer(), EmptyObject.serializer()) }
        sendPacket(WsPacketUpdateSelectedBot(identifier))

    }

    fun openBotSelect() {
        Swal.fire {
            title = "Select Bot"
        }
        Swal.showLoading()
        GlobalScope.launch {
            try {
                val response = postRequest(SharedConst.INTERNAL_GET_BOTS_REQUEST, EmptyObject(), EmptyObject.serializer(), PacketBotsResponse.serializer())
                Swal.hideLoading()
                Swal.getContent().append {
                    div("swal-scrool-box") {
                        ul {
                            response.bots.forEach { botData ->
                                li {
                                    button(classes = "button is-fullwidth") {
                                        +botData.name
                                        onClickFunction = {
                                            selectBot(botData.identifier)
                                            Swal.close()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Swal.fire("ERROR")
            }

        }

    }

    fun playerSetEnabled(enabled: Boolean) {
        val disabled = !enabled
        playerVolumeSlider.disabled = disabled
        playerPlayPauseButton.disabled = disabled
        playerStopButton.disabled = disabled
    }

    private lateinit var playerVolumeSlider: HTMLInputElement
    private lateinit var playerPlayPauseButton: HTMLButtonElement
    private lateinit var playerStopButton: HTMLButtonElement
    private lateinit var playerDurationSlider: HTMLInputElement
    private lateinit var playerDurationtimeSpan: HTMLSpanElement
    private lateinit var playPauseIcon: HTMLElement
    private var volumeDurationSliderUpdateLock = false
    private var volumeSliderUpdateLock = false


    private fun buildPage() {
        newBody().append {
            div("sticky-top") {
                nav(classes = "navbar is-primary") {
                    role = "navigation"
                    div(classes = "navbar-item") {
                        a {
                            +"Select Bot"
                            onClickFunction = {
                                openBotSelect()
                            }
                        }
                    }
                }
            }
            div("container is-fluid") {
                div("page-content") {
                    repeat(1000) {
                        p { +"$it" }
                    }
                }
            }
            div("foot-spacer") { }
            div("footer-fix") {
                footer("footer") {
                    div("columns") {
                        button(classes = "button") { disabled = true;i("fas fa-backward") {} }
                        playerPlayPauseButton = button(classes = "button") {
                            disabled = true
                            playPauseIcon = i("fas fa-play") {}
                            onClickFunction = {
                                sendPacket(WsPacketBotPlayerUpdateIsPlaying())
                            }
                        }
                        playerStopButton = button(classes = "button") { disabled = true; i("fas fa-stop") { } }
                        button(classes = "button") { disabled = true;i("fas fa-forward") { } }
                    }

                    div("foot-lineone-end") {
                        playerVolumeSlider = input(classes = "slider is-fullwidth is-info is-circle") {
                            disabled = true
                            type = InputType.range
                            disabled
                            min = "0"
                            max = "100"
                            value = "0"
                            onChangeFunction = {
                                volumeSliderUpdateLock = false
                                val newVolume = playerVolumeSlider.value.toIntOrNull()
                                if (newVolume != null) GlobalScope.launch { sendPacket(WsPacketBotPlayerUpdateVolume(newVolume)) }
                            }

                            onInputFunction = {
                                volumeSliderUpdateLock = true
                            }
                        }


                    }
                    div("columns") {
                        playerDurationSlider = input(classes = "slider is-fullwidth is-success") {
                            disabled = true
                            type = InputType.range
                            onChangeFunction = {
                                volumeDurationSliderUpdateLock = false
                                val newDuration = playerDurationSlider.value.toLongOrNull()
                                if (newDuration != null) GlobalScope.launch { sendPacket(WsPacketUpdateSongDurationPosition(newDuration)) }
                            }
                            onInputFunction = {
                                volumeDurationSliderUpdateLock = true
                            }
                        }
                        playerDurationtimeSpan = span { +"0:00/0:00" }
                    }
                }
            }
        }

    }


    suspend fun start() {
        buildPage()
        socket = openSocket()
    }

    companion object Static{
        private const val IS_PLAYING_FAS = "fa-play"
        private const val IS_PAUSED_FAS = "fa-pause"
    }
}