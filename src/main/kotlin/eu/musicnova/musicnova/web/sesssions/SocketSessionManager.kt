package eu.musicnova.musicnova.web.sesssions

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import eu.musicnova.musicnova.bot.*
import eu.musicnova.musicnova.database.jpa.PersistentWebUserSessionData
import eu.musicnova.musicnova.utils.serializableIdentifier
import eu.musicnova.musicnova.web.modules.WiCommunicationWebModule
import eu.musicnova.shared.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SocketSessionManager {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var botManager: BotManager

    inner class CommunicationSession(
            private val adapter: WiCommunicationWebModule.CommunicationAdapter,
            private val session: PersistentWebUserSessionData,
            selectBot: BotIdentifier?
    ) {
        private var currentBot: Bot? = selectBot?.toBot()
            set(value) {
                if (field != value) {
                    if (value != null)
                        registerBot(value)
                    field = value
                }
            }

        init {
            val botSnapshot = currentBot
            if (botSnapshot != null) {
                registerBot(botSnapshot)
            }

        }

        var listener: BotEventListener? = null


        private fun registerBot(bot: Bot) {
            GlobalScope.launch {
                adapter.sendPacket(WsPacketUpdateBotInfo(BotData(
                        bot.serializableIdentifier(), bot.name ?: bot.uuid.toString(),
                        bot is ChildBot,
                        bot is MusicBot
                )))
                if (bot is MusicBot) {
                    adapter.sendPacket(WsPacketBotPlayerUpdateVolume(bot.audioController.volume))
                    adapter.sendPacket(WsPacketBotPlayerUpdateIsPlaying(bot.audioController.isPlaying))
                    adapter.sendPacket(WsPacketBotUpdateIsConnected(bot.isConnected))
                    adapter.sendPacket(bot.audioController.currentTrack.toInfoPacket())
                    adapter.sendPacket(WsPacketUpdateSongDurationPosition(bot.audioController.currentTrack?.position
                            ?: 0))
                    updateTrackUpdateSender()
                } else {
                    setITrackUpdateSenderEnabled(false)
                }
            }
            val listener = SocketBotListener()
            bot.addListener(listener)
            this@CommunicationSession.listener = listener
        }

        private inline fun onMusicBot(block: (MusicBot) -> Unit) = currentBot.onMusicBot(block)
        private inline fun Bot?.onMusicBot(block: (MusicBot) -> Unit) {
            if (this is MusicBot) {
                block.invoke(this)
            }
        }

        private fun AudioTrack?.toInfoPacket() = WsPacketUpdateSongInfo(
                this?.info?.title, this?.info?.author,
                this?.info?.length?.takeUnless { this.info?.isStream ?: false }
        )


        private fun BotManager.findBot(identifier: BotIdentifier) = findBot(identifier.uuid, identifier.subID)

        private fun handlePacketPlayStream(packet: WsPacketBotPlayerPlayStream) {
            onMusicBot { bot ->
                GlobalScope.launch { bot.audioController.playStream(packet.url) }
            }
        }

        private suspend fun handlePacketClose(packet: WsPacketClose) {
            adapter.stop()
        }

        fun onAdapterStop() {
            trackUpdateSendJob?.cancel()
        }

        private fun handlePacketBotPlayerUpdateVolume(packet: WsPacketBotPlayerUpdateVolume) {
            assert(packet.newVolume in (0..100))
            val botSnapshot = currentBot
            if (botSnapshot is MusicBot) {
                botSnapshot.audioController.volume = packet.newVolume
            }
        }

        fun handlePacketUpdateSelectedBot(packet: WsPacketUpdateSelectedBot) {
            currentBot = packet.botIdentifier?.toBot()
        }

        @Suppress("NOTHING_TO_INLINE")
        private inline fun BotIdentifier.toBot() = botManager.findBot(uuid, subID)

        fun handlePacketBotPlayerUpdateIsPlaying(packet: WsPacketBotPlayerUpdateIsPlaying) {
            onMusicBot { bot ->
                bot.audioController.togglePlayPause()
            }
        }

        suspend fun onPacket(packet: WsPacket) {
            when (packet) {
                is WsPacketClose -> handlePacketClose(packet)
                is WsPacketBotPlayerPlayStream -> handlePacketPlayStream(packet)
                is WsPacketBotPlayerUpdateVolume -> handlePacketBotPlayerUpdateVolume(packet)
                is WsPacketUpdateSelectedBot -> handlePacketUpdateSelectedBot(packet)
                is WsPacketBotPlayerUpdateIsPlaying -> handlePacketBotPlayerUpdateIsPlaying(packet)
                is WsPacketUpdateSongDurationPosition -> handlePacketBotPlayerUpdateSongDuration(packet)
                is WsPacketBotPlayerStopTrack -> handlePacketBotPlayerStopTrack()
                is WsPacketBotUpdateIsConnected -> handlePacketBotUpdateIsConnected(packet)
                else -> logger.warn("Unhandled Packet: $packet")
            }
        }

        private suspend fun handlePacketBotUpdateIsConnected(packet: WsPacketBotUpdateIsConnected) {
            val bot = currentBot
            if (bot != null) {
                val isBotConnected = bot.isConnected
                val isPacketExpectConnected = packet.isConnected
                when {
                    isBotConnected == isPacketExpectConnected -> return
                    isPacketExpectConnected -> bot.connect()
                    else -> bot.disconnect()
                }
            }
        }

        private fun handlePacketBotPlayerStopTrack() {
            onMusicBot { bot ->
                bot.audioController.stopTrack()
            }
        }

        private fun handlePacketBotPlayerUpdateSongDuration(packetPosition: WsPacketUpdateSongDurationPosition) {
            onMusicBot { bot ->
                bot.audioController.currentTrack?.position = packetPosition.postition
            }
        }

        private var trackUpdateSendJob: Job? = null
        fun updateTrackUpdateSender() {
            onMusicBot { bot ->
                val start = bot.audioController.isPlaying && bot.audioController.currentTrack?.info?.isStream == false
                setITrackUpdateSenderEnabled(start)
            }
        }

        fun setITrackUpdateSenderEnabled(enabled: Boolean) {
            onMusicBot { bot ->
                if (enabled) {
                    val audioController = bot.audioController
                    trackUpdateSendJob?.cancel()
                    trackUpdateSendJob = GlobalScope.launch {
                        while (true) {
                            adapter.sendPacket(WsPacketUpdateSongDurationPosition(audioController.currentTrack?.position
                                    ?: 0))
                            delay(100)
                        }
                    }
                } else {
                    trackUpdateSendJob?.cancel()
                }

            }
        }

        private inner class SocketBotListener : BotEventListener {
            override fun onStatusChanged() {
                val bot = currentBot
                if (bot != null) {
                    GlobalScope.launch { adapter.sendPacket(WsPacketBotUpdateIsConnected(bot.isConnected)) }
                }
            }


            override fun onPlayerContinationUpdate() {
                onMusicBot { bot ->
                    updateTrackUpdateSender()
                    GlobalScope.launch {
                        adapter.sendPacket(WsPacketBotPlayerUpdateIsPlaying(bot.audioController.isPlaying))
                    }
                }
            }


            override fun onPlayerTrackUpdate() {
                onMusicBot { bot ->
                    GlobalScope.launch {
                        adapter.sendPacket(bot.audioController.currentTrack.toInfoPacket())
                    }
                }
            }

            override fun onVolumeUpdate() {
                onMusicBot { bot ->
                    GlobalScope.launch {
                        adapter.sendPacket(WsPacketBotPlayerUpdateVolume(bot.audioController.volume))
                    }
                }
            }
        }
    }


}