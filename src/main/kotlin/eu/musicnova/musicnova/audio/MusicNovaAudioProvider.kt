package eu.musicnova.musicnova.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import eu.musicnova.musicnova.bot.BotPlayerEventListner
import eu.musicnova.musicnova.database.dao.*
import eu.musicnova.musicnova.utils.Const
import eu.musicnova.musicnova.utils.asnycIOTask
import eu.musicnova.musicnova.utils.loadItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.File
import java.util.*

@Component
class MusicNovaAudioProvider {

    private val logger = LoggerFactory.getLogger(MusicNovaAudioProvider::class.java)

    @Autowired
    lateinit var playerManager: DefaultAudioPlayerManager

    @Autowired
    lateinit var audioControllerDatabase: AudioContollerDatabase

    @Autowired
    @Qualifier(Const.BEAN_AUDIO_TRACK_FOLDER)
    lateinit var localTrackFolder :File

    private val localAudioSource = LocalAudioSourceManager()

    operator fun get(id: UUID, listener: BotPlayerEventListner) = getOrCreate(
            audioControllerDatabase.findById(id)
                    .orElseGet { PersistentAudioControllerData(id, 100) },
            listener)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getOrCreate(dao: PersistentAudioControllerData, listener: BotPlayerEventListner): LavaPlayerAudioController = ProvidedAudioControllerImpl(dao, listener)

    private inner class ProvidedAudioControllerImpl(
        private val data: PersistentAudioControllerData,
        private val listener: BotPlayerEventListner,
        override val lavaPlayer: AudioPlayer = playerManager.createPlayer()
    ) : LavaPlayerAudioController, AudioPlayer by lavaPlayer, AudioEventListener {


        init {
            lavaPlayer.volume = data.volume
            lavaPlayer.addListener(this)
        }

        override fun setVolume(volume: Int) {
            asnycIOTask {
                data.volume = volume
                audioControllerDatabase.save(data)
            }
            lavaPlayer.volume = volume
            listener.onVolumeUpdate()
        }


        override val isPlaying: Boolean
            get() = lavaPlayer.playingTrack != null && !lavaPlayer.isPaused

        override fun togglePlayPause() {
            lavaPlayer.isPaused = !lavaPlayer.isPaused
        }

        override val currentTrack: AudioTrack?
            get() = lavaPlayer.playingTrack

        override fun playTrack(track: AudioTrack?) {
            lavaPlayer.playTrack(track)
            lavaPlayer.isPaused = false
        }

        override suspend fun playStream(url: String): Boolean {
            return try {
                val track = playerManager.loadItem(url)
                if (track != null) {
                    playTrack(track)
                    true
                } else {
                    false
                }
            } catch (e: FriendlyException) {
                logger.warn("Load Track Failed", e)
                false
            }
        }

        override suspend fun playFile(file: File, title: String?): Boolean {
            val audioItem = withContext(Dispatchers.IO) {
                localAudioSource.loadItem(playerManager, AudioReference(file.absolutePath, title))
            }
            return if (audioItem is AudioTrack) {
                playTrack(audioItem)
                true
            } else {
                false
            }
        }

        suspend fun playTrack(track: PersistentAudioTrackData) {
            when (track) {
                is PersistentLocalAudioTrackData -> playFile(File(localTrackFolder, track.file), track.title)
                is PersistentRemoteAudioTrackData -> playStream(track.url)
                else -> throw IllegalArgumentException("unsupported track type: ${track::class.java.name}")
            }
        }

        override fun onEvent(event: AudioEvent) {
            listener.onPlayerContinationUpdate()
            if (event is TrackStartEvent || event is TrackEndEvent) {
                listener.onPlayerTrackUpdate()
            }
        }

    }
}