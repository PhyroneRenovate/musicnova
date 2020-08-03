package eu.musicnova.musicnova.bot

import eu.musicnova.musicnova.audio.AudioController
import org.python.tests.Child
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

interface ParentBot : Bot {
    val children: Set<ChildBot>
    fun getChild(id: Long): ChildBot?
}

interface ChildBot : Bot {
    val childID: Long?
    val parentBot: ParentBot
}


interface MusicBot : Bot {
    val audioController: AudioController
}

interface DiscordBot : Bot


interface DiscordMultiGuildBot : DiscordBot, ParentBot {
    override val children: Set<DiscordGuildSubBot>
    override fun getChild(id: Long): DiscordGuildSubBot?
}

interface DiscordGuildSubBot : DiscordGuildBot<DiscordGuildConnectionData>, ChildBot

interface DiscordSingleGuildBot : DiscordGuildBot<CombinedDiscordConnectionData>

interface DiscordGuildBot<T : DiscordGuildConnectionData> : MusicBot

interface TeamspeakBot : MusicBot