package eu.musicnova.musicnova.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioReference
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import eu.musicnova.musicnova.database.jpa.AudioContollerDatabase
import eu.musicnova.musicnova.database.jpa.PersistentAudioControllerData
import eu.musicnova.musicnova.utils.asnycIOTask
import eu.musicnova.musicnova.utils.loadItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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

    private val localAudioSource = LocalAudioSourceManager()

    operator fun get(id: UUID) = getOrCreate(audioControllerDatabase.findById(id).orElseGet { PersistentAudioControllerData(id, 100) })

    private fun getOrCreate(dao: PersistentAudioControllerData): LavaPlayerAudioController = ProvidedAudioControllerImpl(dao)

    private inner class ProvidedAudioControllerImpl(private val data: PersistentAudioControllerData, override val lavaPlayer: AudioPlayer = playerManager.createPlayer()) : LavaPlayerAudioController, AudioPlayer by lavaPlayer {


        init {
            lavaPlayer.volume = data.volume
        }

        override fun setVolume(volume: Int) {
            asnycIOTask {
                data.volume = volume
                audioControllerDatabase.save(data)
            }
            lavaPlayer.volume = volume
        }

        override val isPlaying: Boolean
            get() = lavaPlayer.playingTrack != null || !lavaPlayer.isPaused

        override suspend fun playStream(url: String): Boolean {
            return try {
                val track = playerManager.loadItem(url)
                if (track != null) {
                    lavaPlayer.playTrack(track)
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
                lavaPlayer.playTrack(audioItem)
                true
            } else {
                false
            }
        }
    }
}