package eu.musicnova.frontend.dashboard

import eu.musicnova.frontend.externals.Dropzone
import eu.musicnova.frontend.externals.Swal
import eu.musicnova.frontend.externals.fire
import eu.musicnova.frontend.utils.*
import eu.musicnova.shared.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.TagConsumer
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.*
import kotlinx.html.js.a
import kotlinx.html.js.button
import kotlinx.html.js.div
import kotlinx.html.js.i
import kotlinx.html.js.input
import kotlinx.html.js.label
import kotlinx.html.js.li
import kotlinx.html.js.p
import kotlinx.html.js.span
import kotlinx.html.js.ul
import kotlinx.html.role
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.*
import org.w3c.dom.events.Event
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class DashboardSession {


    var socket: WebSocket? = null
    private val packetSendQueue = mutableListOf<WsPacket>()
    private lateinit var playerVolumeSlider: HTMLInputElement
    private lateinit var playerPlayPauseButton: HTMLButtonElement
    private lateinit var playerStopButton: HTMLButtonElement
    private lateinit var playerDurationSlider: HTMLInputElement
    private lateinit var playerMaxTimeLabelSpan: HTMLSpanElement
    private lateinit var playerCurrentTimeLabelSpan: HTMLSpanElement
    private lateinit var playPauseIcon: HTMLElement
    private lateinit var botConnectedSwitch: HTMLInputElement
    private lateinit var contentDIV: HTMLDivElement
    private var volumeDurationSliderUpdateLock = false
    private var volumeSliderUpdateLock = false
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
            handleSocketClose(it)
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
            is WsPacketBotUpdateIsConnected -> handleBotUpdateIsConnected(packet)
            else -> console.error("unhandled packet", packet)
        }
    }

    private fun handleBotUpdateIsConnected(packet: WsPacketBotUpdateIsConnected) {
        botConnectedSwitch.checked = packet.isConnected
    }

    private fun handleUpdateSongDuration(packetPosition: WsPacketUpdateSongDurationPosition) {
        if (!volumeDurationSliderUpdateLock) {
            setPositionSliderValue(packetPosition.postition)
        }
    }

    private fun millisToHumantTime(millis: Long): String {
        val totalSec = millis / 1000
        val totalMins = totalSec / 60
        val hours = totalMins / 60
        val mins = totalMins - (hours * 60)
        val secs = totalSec - (totalMins * 60)
        return "${if (hours > 0) "$hours:${if (mins < 10) "0" else ""}" else ""}$mins:${if (secs < 10) "0" else ""}$secs"
    }

    private fun setPositionLabelValue(position: Long) {
        playerCurrentTimeLabelSpan.innerText = millisToHumantTime(position)
    }

    private fun setPositionSliderValue(position: Long) {
        if (!volumeDurationSliderUpdateLock) {
            setPositionLabelValue(position)
            playerDurationSlider.value = position.toString()

        }
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
            if (playing) {
                playPauseIcon.classList.replace(IS_PLAYING_FAS, IS_PAUSED_FAS)
            } else {
                playPauseIcon.classList.replace(IS_PAUSED_FAS, IS_PLAYING_FAS)
            }

        }
    }

    private fun handleSocketClose(event: Event) {
        console.log("socked closed", event)
        window.location.reload()
    }

    fun handleIncommingVolumeUpdate(packet: WsPacketBotPlayerUpdateVolume) {
        if (!volumeSliderUpdateLock) playerVolumeSlider.value = packet.newVolume.toString()
    }

    fun handleSongInfoUpdate(packet: WsPacketUpdateSongInfo) {
        val title = packet.title
        playerDurationSlider.value = "0"
        val maxLenght = packet.length
        if (title != null && maxLenght != null) {
            playerDurationSlider.max = maxLenght.toString()
            playerMaxTimeLabelSpan.innerText = millisToHumantTime(maxLenght)
            playerDurationSlider.disabled = false
        } else {
            playerDurationSlider.max = "0"
            playerCurrentTimeLabelSpan.innerText = millisToHumantTime(0)
            playerMaxTimeLabelSpan.innerText = millisToHumantTime(0)
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

    private fun selectBot(identifier: UUIDIdentifier) {
        GlobalScope.launch {
            putRequest(
                SharedConst.INTERNAL_SET_SELECT_COOkIE,
                UUIDIdentifier.serializer(),
                identifier
            )
        }
        sendPacket(WsPacketUpdateSelectedBot(identifier))

    }

    private fun openBotSelect() {

        Swal.fire {
            title = "Select Bot"

        }
        Swal.showLoading()
        GlobalScope.launch {
            try {
                val bots = InterPlatformSerializer.deserializeList(
                    BotData.serializer(),
                    getRequest(SharedConst.INTERNAL_GET_BOTS_REQUEST)
                )
                Swal.hideLoading()
                Swal.getContent().append {
                    div("swal-scrool-box") {
                        ul {
                            bots.forEach { botData ->
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
            } catch (error: Throwable) {
                console.error(error.stackTraceToString(), error)
                Swal.hideLoading()
                Swal.fire {
                    title = "Get Bots Failed"
                    icon = "error"
                    text = error.stackTraceToString()
                }
            }

        }

    }

    fun playerSetEnabled(enabled: Boolean) {
        val disabled = !enabled
        playerVolumeSlider.disabled = disabled
        playerPlayPauseButton.disabled = disabled
        playerStopButton.disabled = disabled
    }

    fun changePageContent(page: PageContent) {
        val title = page.title
        if (title != null)
            document.title = title
        GlobalScope.launch { setPageContentBody(page) }
    }

    private fun TagConsumer<HTMLElement>.appendTopMenu() {
        nav("navbar is-link") {
            role = "navigation"
            div("navbar-brand") {
                a(classes = "navbar-item") {
                    onClickFunction = {
                        changePageContent(PageContent.DASHBOARD)

                    }
                    img(src = "https://bulma.io/images/bulma-logo.png") { }
                }
            }
            div("navbar-item") {
                a {
                    +"Select Bot"
                    onClickFunction = {
                        openBotSelect()
                    }
                }
            }
            div("navbar-item") {
                a {
                    +"Tracks"
                    onClickFunction = {
                        changePageContent(PageContent.TRACKS)
                    }
                }
            }
            div("navbar-end") {
                div("navbar-item") {
                    div("field") {
                        val randomID = randomId()
                        botConnectedSwitch = input(classes = "switch is-info") {
                            type = InputType.checkBox
                            id = randomID
                            onChangeFunction = { sendPacket(WsPacketBotUpdateIsConnected(botConnectedSwitch.checked)) }
                        }
                        label {
                            htmlFor = randomID
                            +"Connected"
                        }
                    }
                }
            }
        }
    }

    private fun TagConsumer<HTMLElement>.appendFootPlayer() {
        footer("footer") {
            div("columns") {
                button(classes = "button") {
                    disabled = true
                    i("fas fa-backward") {}
                }
                playerPlayPauseButton = button(classes = "button") {
                    disabled = true
                    playPauseIcon = i("fas fa-play") {}
                    onClickFunction = {
                        sendPacket(WsPacketBotPlayerUpdateIsPlaying())
                    }
                }
                playerStopButton = button(classes = "button") {
                    disabled = true
                    i("fas fa-stop") { }
                    onClickFunction = {
                        sendPacket(WsPacketBotPlayerStopTrack)
                    }
                }
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
                        if (newDuration != null) GlobalScope.launch {
                            sendPacket(
                                WsPacketUpdateSongDurationPosition(
                                    newDuration
                                )
                            )
                        }
                    }
                    onInputFunction = {
                        volumeDurationSliderUpdateLock = true
                        setPositionLabelValue(playerDurationSlider.value.toLong())
                    }
                }
                span {
                    playerCurrentTimeLabelSpan = span { +"0:00" }
                    +"/"
                    playerMaxTimeLabelSpan = span { +"0:00" }
                }
            }
        }
    }

    private suspend fun setPageContentBody(dashboardPage: PageContent) {
        when (dashboardPage) {
            PageContent.DASHBOARD -> setDashboardContentContent()
            PageContent.TRACKS -> setTracksContent()
            PageContent.PROFILE -> TODO()
        }
    }

    private fun setDashboardContentContent() {

    }

    private suspend fun setTracksContent() {
        val tracks = InterPlatformSerializer.deserializeList(
            AudioTrackData.serializer(),
            getRequest(SharedConst.INTERNAL_GET_TRACKS_PATH)
        )
        contentDIV.append {
            article("panel") {
                p("panel-heading") { +"Tracks" }
                div("panel-block") {
                    p("control has-icons-left") {
                        input(classes = "input is-primary") {
                            type = InputType.text
                        }
                        span("icon is-left") {
                            i("fas fa-search") { }
                        }
                    }
                }
                tracks.forEach { track ->
                    a(classes = "panel-block") {
                        span("panel-icon") {
                            i(classes = "fas fa-book") { }
                        }
                        +track.title
                    }
                }
            }
            val upload = div("dropzone") {
                id = randomId()
            }

            // @formatter:off
            val drop = Dropzone(upload, js("""{
                url: "${SharedConst.INTERNAL_FILE_UPLOAD}",
                uploadMultiple: true,
                maxFilesize: 5
            }"""))
            // @formatter:on
            drop.enable()
            console.log(drop)

        }

    }

    private fun buildPage() {
        newBody().append {
            div("sticky-top") {
                appendTopMenu()

            }
            div("container is-fluid") {
                contentDIV = div("page-content") {}
            }
            div("foot-spacer") { }
            div("footer-fix") {
                appendFootPlayer()
            }
        }

    }

    suspend fun start() {
        buildPage()
        socket = openSocket()
        setPageContentBody(pageStartData.dashboardPage)
    }

    companion object Static {
        private const val IS_PLAYING_FAS = "fa-play"
        private const val IS_PAUSED_FAS = "fa-pause"
    }
}