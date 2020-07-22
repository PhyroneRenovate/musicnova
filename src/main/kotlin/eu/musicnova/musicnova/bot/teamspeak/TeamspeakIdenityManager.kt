package eu.musicnova.musicnova.bot.teamspeak

import com.github.manevolent.ts3j.identity.LocalIdentity
import com.github.manevolent.ts3j.util.Ts3Crypt
import com.google.common.cache.CacheBuilder
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.*
import de.vandermeer.asciitable.AsciiTable
import eu.musicnova.musicnova.bot.TerminalConfigurable
import eu.musicnova.musicnova.database.jpa.PersistentTeamspeakIdentity
import eu.musicnova.musicnova.database.jpa.TeamspeakIdentiyDatabase
import eu.musicnova.musicnova.utils.TerminalCommandDispatcher
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarStyle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import javax.annotation.PostConstruct

@Component
class TeamspeakIdenityManager {

    @Autowired
    lateinit var terminalCommandDispatcher: TerminalCommandDispatcher

    @Autowired
    lateinit var identityDatabase: TeamspeakIdentiyDatabase

    private val identityProperties = WeakHashMap<TeamspeakBotManager.IdenityConfigurable, TsBotIdenityProperty>()
     fun getProperty(bot: TeamspeakBotManager.IdenityConfigurable) = identityProperties
            .getOrPut(bot) { TsBotIdenityProperty(bot) }

    inner class TsBotIdenityProperty(val bot: TeamspeakBotManager.IdenityConfigurable) : TerminalConfigurable.Property {
        override fun set(value: String) {
            if (value.equals("null", true)) {
                bot.identityData = null
            } else {
                bot.identityData = (getIdentity(value)
                        ?: error("identity not found"))
            }
        }

        override fun get(): String = bot.identityData?.uuid?.toString() ?: "null"

        override fun suggest(): List<String> {
            val suggestions = suggestIdenties()
            return suggestions.mapNotNull { it.nickname }+suggestions.map { it.uuid.toString() }
        }
    }



    private fun generateInSteps(level: Int, block: (Int) -> Unit): LocalIdentity {
        val identity = LocalIdentity.generateNew(1)
        identity.improveInSteps(level, block)
        return identity
    }

    private val suggestioncache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofSeconds(3)).expireAfterWrite(Duration.ofSeconds(30)).build<Unit, List<PersistentTeamspeakIdentity>>()
    private fun LocalIdentity.improveInSteps(level: Int, block: (Int) -> Unit) {
        for (targetLevel in (1..level)) {
            if (targetLevel <= securityLevel) continue
            improveSecurity(targetLevel)
            block.invoke(securityLevel)
        }
    }

    private fun getIdentity(name: String) = runCatching { identityDatabase.getOne(UUID.fromString(name)) }
            .getOrElse { runCatching { identityDatabase.findByNickname(name) }.getOrNull() }

    private val NAME_ARG = "UUIDorName"
    private fun suggestIdenties() = suggestioncache.get(Unit) { identityDatabase.findAll() }
    fun CommandContext<Unit>.getIdentity() = getIdentity(getArgument(NAME_ARG))

    @PostConstruct
    fun onCreated() {
        terminalCommandDispatcher.literal("teamspeak") {
            literal("identity") {
                literal("list") {
                    runs {
                        val table = AsciiTable()
                        table.addRule()
                        table.addRow("id", "nickname", "public key", "security level")
                        table.addRule()
                        identityDatabase.findAll().forEach { data ->
                            val identity = data.identity
                            table.addRow(
                                    data.uuid, data.nickname ?: "null",
                                    identity.publicKeyString, identity.securityLevel
                            )
                            table.addRule()
                        }
                        println(table.render())
                    }
                }
                literal("generate") {
                    fun generateIdentityTerm(targetLevel: Int, name: String?) {
                        val progressBar = ProgressBar("Generate Idenity", targetLevel.toLong(), ProgressBarStyle.COLORFUL_UNICODE_BLOCK)
                        val tsIdentity = generateInSteps(targetLevel) { reachedLevel ->
                            progressBar.stepTo(reachedLevel.toLong())
                        }
                        val identityData = PersistentTeamspeakIdentity(tsIdentity, name)
                        progressBar.close()
                        identityDatabase.save(identityData)
                        suggestioncache.invalidateAll()
                        val table = AsciiTable()
                        table.addRule()
                        table.addRow("uuid", "nickname", "public key", "security level")
                        table.addRule()
                        table.addRow(identityData.uuid, identityData.nickname
                                ?: "null", tsIdentity.publicKeyString, tsIdentity.securityLevel)
                        table.addRule()
                        println(table.render())
                    }

                    runs {
                        generateIdentityTerm(8, null)

                    }
                    argument("level", IntegerArgumentType.integer(1)) {
                        runs {
                            generateIdentityTerm(it.getArgument("level"), null)
                        }
                        argument("name", GreedyStringArgument) {
                            runs {
                                generateIdentityTerm(it.getArgument("level"), it.getArgument("name"))
                            }
                        }
                    }
                }
                argument(NAME_ARG, StringArgument) {
                    suggest {
                        suggestIdenties().forEach { identityData ->
                            val nick = identityData.nickname
                            if (nick == null) {
                                suggest(identityData.uuid.toString())
                            } else {
                                suggest(nick)
                                suggest(identityData.uuid.toString()) { nick }
                            }
                        }
                    }

                    literal("setName") {
                        argument("name", GreedyStringArgument) {
                            runs {
                                val identity = it.getIdentity()
                                val name = it.getArgument<String>("name")
                                if (identity == null) {
                                    println("identity not found")
                                } else {
                                    identity.nickname = name
                                    identityDatabase.save(identity)
                                }
                            }
                        }
                    }
                    literal("improve") {
                        argument("targetLevel", IntegerArgumentType.integer(1)) {
                            runs {
                                val identitydata = it.getIdentity()
                                val targetLevel: Int = it.getArgument("targetLevel")
                                if (identitydata == null) {
                                    println("identity not found")
                                } else {
                                    val progressBar = ProgressBar("Improving", 1, ProgressBarStyle.COLORFUL_UNICODE_BLOCK)
                                    progressBar.extraMessage = "Loading"
                                    val localTsIdentity = identitydata.identity
                                    progressBar.step()
                                    val startLevel = localTsIdentity.securityLevel
                                    progressBar.maxHint((startLevel - targetLevel + 1).toLong())
                                    progressBar.extraMessage = ""
                                    localTsIdentity.improveInSteps(targetLevel) { reachedLevel ->

                                        progressBar.stepTo((reachedLevel-startLevel  + 1).toLong())
                                        progressBar.extraMessage = "Saving"
                                        identitydata.identity = localTsIdentity
                                        identityDatabase.save(identitydata)
                                        progressBar.extraMessage = ""
                                    }
                                    progressBar.close()
                                    suggestioncache.invalidateAll()
                                }
                            }
                        }
                    }
                    literal("delete") {
                        runs {
                            val identity = it.getIdentity()
                            if (identity != null) {
                                if (identity.bots.isNotEmpty()) {
                                    println("cant delete identity (bot still using it)")
                                    println("bots:")
                                    identity.bots.forEach { botData ->
                                        println(botData)
                                    }
                                } else {
                                    identityDatabase.delete(identity)
                                    suggestioncache.invalidateAll()
                                }
                            }
                        }
                    }

                }

            }
        }
    }
}