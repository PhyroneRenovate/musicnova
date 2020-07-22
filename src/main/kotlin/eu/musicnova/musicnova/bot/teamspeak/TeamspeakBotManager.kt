package eu.musicnova.musicnova.bot.teamspeak

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.manevolent.ts3j.audio.Microphone
import com.github.manevolent.ts3j.enums.CodecType
import com.github.manevolent.ts3j.event.TS3Listener
import com.github.manevolent.ts3j.event.TextMessageEvent
import com.github.manevolent.ts3j.identity.LocalIdentity
import com.github.manevolent.ts3j.protocol.TS3DNS
import com.github.manevolent.ts3j.protocol.socket.client.LocalTeamspeakClientSocket
import de.phyrone.brig.wrapper.StringArgument
import de.phyrone.brig.wrapper.getArgument
import de.phyrone.brig.wrapper.literal
import de.phyrone.brig.wrapper.runs
import eu.musicnova.musicnova.utils.TerminalCommandDispatcher
import eu.musicnova.musicnova.audio.MusicNovaAudioProvider
import eu.musicnova.musicnova.bot.*
import eu.musicnova.musicnova.database.jpa.PersistentTeamspeakBotData
import eu.musicnova.musicnova.database.jpa.PersistentTeamspeakIdentity
import eu.musicnova.musicnova.database.jpa.TeamspeakBotDatabase
import eu.musicnova.musicnova.utils.BotListenerAdapter
import eu.musicnova.musicnova.utils.ioTask
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URL
import java.util.*
import javax.annotation.PostConstruct

@Component
class TeamspeakBotManager {


    @Autowired
    lateinit var audioProvider: MusicNovaAudioProvider

    @Autowired
    lateinit var teamspeakBotDatabase: TeamspeakBotDatabase

    @Autowired
    lateinit var botManager: BotManager

    @Autowired
    lateinit var idenityManager: TeamspeakIdenityManager


    @Bean
    fun onTeamspeakStart() = ApplicationRunner {
        teamspeakBotDatabase.findAll().forEach { data ->
            botManager.registerBot(TeamspeakBotImpl(data).apply { handleInit(data) })
        }
    }

    private fun TeamspeakBotImpl.handleInit(data: PersistentTeamspeakBotData) {
        if (data.connected) {
            GlobalScope.launch { connect() }
        }
    }

    private var cachedVersionsList: List<TeamspeakClientProtocolVersion>? = null
    fun getVersions() = cachedVersionsList ?: downloadVersionsTable().also { versions ->
        this.cachedVersionsList = versions
    }

    fun clearVersionsCache() {
        cachedVersionsList = null
    }

    @Autowired
    lateinit var terminalCommandDipatcher: TerminalCommandDispatcher

    @PostConstruct
    fun registerCommands() {
        terminalCommandDipatcher.literal("bot") {
            literal("create") {
                literal("teamspeak") {
                    runs {
                        val tsDAO = PersistentTeamspeakBotData().also { dao -> dao.connected = false }
                        teamspeakBotDatabase.save(tsDAO)
                        teamspeakBotDatabase.refresh(tsDAO)
                        botManager.registerBot(TeamspeakBotImpl(tsDAO))
                    }
                    argument("name", StringArgument) {
                        runs {
                            val tsDAO = PersistentTeamspeakBotData(
                            ).also { dao ->
                                dao.connected = false
                                dao.name = it.getArgument("name")
                            }
                            teamspeakBotDatabase.save(tsDAO)
                            teamspeakBotDatabase.refresh(tsDAO)
                            botManager.registerBot(TeamspeakBotImpl(tsDAO))
                        }
                    }

                }
            }
        }
    }

    companion object Static {
        private const val VERSIONS_LINK = "https://raw.githubusercontent.com/ReSpeak/tsdeclarations/master/Versions.csv"
        private val mapper = CsvMapper().also { it.findAndRegisterModules().registerKotlinModule() }
        private val schema = mapper.schemaFor(TeamspeakClientProtocolVersion::class.java)
        private val reader = mapper.readerFor(TeamspeakClientProtocolVersion::class.java).with(schema)

        private fun downloadVersionsTable() = reader.readValues<TeamspeakClientProtocolVersion>(
                URL(VERSIONS_LINK).openStream()
        ).also { /* drop first row (header) */it.nextValue() }.readAll()
    }

    interface IdenityConfigurable {
        var identityData: PersistentTeamspeakIdentity?
    }

    private inner class TeamspeakBotImpl(
            private var dao: PersistentTeamspeakBotData
    ) : TeamspeakBot, Microphone, TS3Listener, IdenityConfigurable {

        override val uuid: UUID
            get() = dao.id
        override var name: String?
            get() = dao.name
            set(value) {
                dao.name = value
                teamspeakBotDatabase.save(dao)
            }
        override val isConnected: Boolean
            get() = tsSocket?.isConnected == true

        private var tsSocket: LocalTeamspeakClientSocket? = null

        override fun onTextMessage(e: TextMessageEvent) {
            println("${e.targetClientId} -> ${e.message}")
        }

        private fun newSocket() =
                LocalTeamspeakClientSocket().also { socket ->
                    socket.microphone = this
                    socket.setIdentity(dao.identity?.identity ?: LocalIdentity.generateNew(20))
                    socket.nickname = dao.nickname
                    dao.hwid?.also { socket.hwid = it }
                }

        override suspend fun connect() {
            teamspeakBotDatabase.refresh(dao)
            dao.connected = true


            val tsSocket = newSocket()
            val address = resolveAddress(dao.hostResolve, dao.host, dao.port ?: 9987)
            ioTask { tsSocket.connect(address, dao.serverPassword, dao.timeout) }
            teamspeakBotDatabase.save(dao)
            ioTask { tsSocket.subscribeAll() }
            dao.channel?.also {
                ioTask { tsSocket.joinChannel(it, dao.channelPassword) }
            }

            this.tsSocket = tsSocket
        }

        fun resolveAddress(resoveMode: TeamspeakResoveMode, host: String, port: Int): InetSocketAddress {
            return when (resoveMode) {
                TeamspeakResoveMode.NONE -> InetSocketAddress(host, port)
                TeamspeakResoveMode.SRV -> TS3DNS.lookup(host).firstOrNull() ?: InetSocketAddress(host, port)
            }
        }

        override suspend fun disconnect() {
            disconnect(false)
        }

        private suspend fun disconnect(shutdown: Boolean) {
            if (!shutdown) {
                teamspeakBotDatabase.refresh(dao)
                dao.connected = false
                teamspeakBotDatabase.save(dao)
            }
            ioTask { tsSocket?.disconnect() }
            tsSocket = null
        }

        private val listenerAdapter = BotListenerAdapter()
        override fun addListener(listener: BotEventListener) {
            listenerAdapter.addListener(listener)
        }

        override suspend fun destroy() {
            disconnect(true)
            audioPlayer.destroy()
        }

        override suspend fun delete() {
            destroy()
            teamspeakBotDatabase.delete(dao)
        }


        override fun suggestTerminalProperties(): List<String> = tsProperties.keys.toList()

        override fun getTerminalProperty(property: String): TerminalConfigurable.Property? = tsProperties[property.toLowerCase()]

        private val tsProperties = mapOf(
                "host" to HostProperty(),
                "nickname" to NicknameProperty(),
                "identity" to idenityManager.getProperty(this)
        )

        inner class NicknameProperty : TerminalConfigurable.Property {
            override fun set(value: String) {
                dao.nickname = value
                teamspeakBotDatabase.save(dao)
                tsSocket?.nickname = value
            }

            override fun get(): String = tsSocket?.nickname ?: dao.nickname


            override fun suggest(): List<String> = listOf()
        }

        inner class HostProperty : TerminalConfigurable.Property {
            override fun set(value: String) {
                dao.host = value
                teamspeakBotDatabase.save(dao)
            }

            override fun get(): String = dao.host


            override fun suggest(): List<String> = listOf()
        }

        override val audioController by lazy { audioProvider[uuid] }
        private val audioPlayer by lazy {
            audioController.lavaPlayer.also { player ->
                // Prevent Opus AudioFrame Passthrough due to TeamSpeak Packet Size limit (f.e. youtube)
                player.setFilterFactory { _, _, _ -> listOf() }
            }
        }

        override fun getCodec(): CodecType = CodecType.OPUS_MUSIC

        private var audioFrame: ByteArray? = null


        override fun isMuted() = false
        override fun isReady(): Boolean {
            audioFrame = audioPlayer.provide()?.data
            return audioFrame?.size ?: 0 > 0
        }

        override fun provide() = audioFrame
        override var identityData: PersistentTeamspeakIdentity?
            get() {
                return dao.identity
            }
            set(value) {
                dao.identity = value
                teamspeakBotDatabase.save(dao)
            }
    }

    enum class TeamspeakResoveMode {
        NONE, SRV
    }
}