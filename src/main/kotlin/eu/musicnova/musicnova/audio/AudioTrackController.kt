package eu.musicnova.musicnova.audio

import eu.musicnova.musicnova.database.dao.AudioTrackDatabase
import eu.musicnova.musicnova.database.dao.LocalAudioTrackDatabase
import eu.musicnova.musicnova.utils.Const
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.File

@Component
class AudioTrackController(
    @Qualifier(Const.BEAN_AUDIO_TRACK_FOLDER)
    val audioTrackFolder: File,
    val audioTrackDatabase: AudioTrackDatabase,
    val localAudioTrackDatabase: LocalAudioTrackDatabase
) {

    fun localTrackFile(identifier: String) = File(audioTrackFolder, identifier)

    fun allTracks() = audioTrackDatabase.getAllByOrderByTitleAsc()

    fun addFile(identifier: String, file: File){

    }
}