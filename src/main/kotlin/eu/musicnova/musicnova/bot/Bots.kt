package eu.musicnova.musicnova.bot

import eu.musicnova.musicnova.audio.AudioController
import java.util.*

interface Bot : TerminalConfigurable {
    val uuid: UUID
    var name: String?

    val isConnected: Boolean
    suspend fun connect()
    suspend fun disconnect()

    fun addListener(listener: BotEventListener)

    suspend fun destroy()
    suspend fun delete()
}

interface TerminalConfigurable {

    fun getTerminalProperty(property: String): Property?
    fun suggestTerminalProperties(): List<String>
    interface Property {
        fun set(value: String)
        fun get(): String
        fun suggest(): List<String>
    }
}





interface MusicBot : Bot {
    val audioController: AudioController
}

interface DiscordBot : MusicBot

interface TeamspeakBot : MusicBot