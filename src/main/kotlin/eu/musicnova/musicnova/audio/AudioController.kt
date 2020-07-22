package eu.musicnova.musicnova.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import java.io.File

interface LavaPlayerAudioController : AudioController {
    val lavaPlayer: AudioPlayer
}

interface AudioController :AudioPlayer{

    val isPlaying: Boolean
    suspend fun playStream(url: String): Boolean
    suspend fun playFile(file: File, title: String?): Boolean

}