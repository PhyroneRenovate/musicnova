package eu.musicnova.musicnova.bot


import com.mojang.brigadier.Message
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.*
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.skb.interfaces.document.TableRowStyle
import eu.musicnova.musicnova.utils.TerminalCommandDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.collections.HashSet

@Component
class BotManager {

    private val bots = HashSet<Bot>()

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     *
     * @param includeChildren Boolean resolve children of parentbots (f.e. discord)
     * @return List<Bot> a copy of all bots as a list
     */
    @JvmOverloads
    fun all(includeChildren: Boolean = true): List<Bot> {
        val response = mutableListOf<Bot>()

        bots.sortedBy { bot -> bot.name }.forEach { bot ->
            response += bot
            if ((bot is ParentBot) && includeChildren) {
                bot.children.forEach { childBot ->
                    response.add(childBot)
                }
            }
        }
        return response
    }

    fun registerBot(bot: Bot) {
        bots.add(bot)
    }

    fun unregisterBot(bot: Bot) {
        bots.remove(bot)
    }

    @PreDestroy
    fun onStop() = runBlocking(Dispatchers.Default) {
        logger.info("Stopping Bots...")
        bots.forEach { bot ->
            bot.destroy()
        }
    }

    fun findBot(name: String): Bot? = bots.find { bot -> name.equals(bot.uuid.toString(), true) }
            ?: bots.find { bot -> name.equals(bot.name, true) }

    fun findBot(uuid: UUID, subID: Long?): Bot? {
        val bot = bots.find { bot -> bot.uuid == uuid }
        return if (subID != null) {
            if (bot is ParentBot) {
                bot.getChild(subID)
            } else {
                throw IllegalArgumentException("cant select a childbot from non parent bot")
            }
        } else {
            bot
        }
    }

    private val botNameArg = "UUIDorUsername"
    fun CommandContext<Unit>.getBot() = findBot(getArgument(botNameArg))

    @Autowired
    lateinit var terminalCommandDispatcher: TerminalCommandDispatcher

    @PostConstruct
    fun setupBotCommands() {
        terminalCommandDispatcher.literal("bot") {
            literal("list") {
                runs {
                    val botsTable = AsciiTable()
                    botsTable.addRule()
                    botsTable.addRow("UUID", "Name", "Is Connected")
                    botsTable.addRule()
                    bots.forEach { bot ->
                        botsTable.addRow(bot.uuid, bot.name, bot.isConnected)
                        botsTable.addRule()
                    }
                    println(botsTable.render())
                }
            }
            argument(botNameArg, StringArgument) {
                suggest {
                    bots.forEach { bot ->
                        val name = bot.name
                        if (name == null) {
                            suggest(bot.uuid.toString())
                        } else {
                            suggest(bot.uuid.toString()) { name }
                            suggest(name)
                        }
                    }

                }
                literal("connect") {
                    runs {
                        val bot = it.getBot()
                        bot?.connect()
                    }
                }
                literal("disconnect") {
                    runs {
                        val bot = it.getBot()
                        bot?.disconnect()
                    }
                }
                literal("setName") {
                    argument("name", GreedyStringArgument) {
                        runs {
                            val bot = it.getBot()
                            bot?.name = it.getArgument("name")
                        }
                    }
                }
                literal("set") {

                    argument("property", StringArgument) {
                        suggest {
                            it.getBot()?.suggestTerminalProperties()?.forEach { suggestion -> suggest(suggestion) }
                        }
                        argument("value", GreedyStringArgument) {
                            suggest {
                                it.getBot()?.getTerminalProperty(it.getArgument("property"))?.suggest()?.forEach { suggestion ->
                                    suggest(suggestion)
                                }
                            }
                            runs {
                                it.getBot()?.getTerminalProperty(it.getArgument("property"))?.set(it.getArgument("value"))
                            }
                        }

                    }
                }
                literal("get") {

                    argument("property", StringArgument) {
                        suggest {
                            it.getBot()?.suggestTerminalProperties()?.forEach { suggestion -> suggest(suggestion) }
                        }
                        runs {
                            val value = it.getBot()?.getTerminalProperty(it.getArgument("property"))?.get()
                            println(value)

                        }

                    }
                }
                literal("player") {
                    literal("stop") {
                        runs {
                            when (val bot = it.getBot()) {
                                is MusicBot -> {
                                    bot.audioController.stopTrack()
                                }
                                null -> {
                                    println("bot not found")
                                }
                                else -> {
                                    println("bot is not a musicbot")
                                }
                            }
                        }
                    }
                    literal("pause") {
                        runs {
                            when (val bot = it.getBot()) {
                                is MusicBot -> {
                                    bot.audioController.isPaused = true
                                }
                                null -> {
                                    println("bot not found")
                                }
                                else -> {
                                    println("bot is not a musicbot")
                                }
                            }
                        }
                    }
                    literal("resume") {
                        runs {
                            when (val bot = it.getBot()) {
                                is MusicBot -> {
                                    bot.audioController.isPaused = false
                                }
                                null -> {
                                    println("bot not found")
                                }
                                else -> {
                                    println("bot is not a musicbot")
                                }
                            }
                        }
                    }
                    literal("volume") {
                        runs {
                            when (val bot = it.getBot()) {
                                is MusicBot -> {
                                    println(bot.audioController.volume)
                                }
                                null -> {
                                    println("bot not found")
                                }
                                else -> {
                                    println("bot is not a musicbot")
                                }
                            }
                        }
                        argument("newValue", IntegerArgumentType.integer(0, 100)) {
                            runs {
                                when (val bot = it.getBot()) {
                                    is MusicBot -> {
                                        bot.audioController.volume = it.getArgument("newValue")
                                    }
                                    null -> {
                                        println("bot not found")
                                    }
                                    else -> {
                                        println("bot is not a musicbot")
                                    }
                                }
                            }
                        }
                    }
                    literal("playStream") {
                        argument("url", GreedyStringArgument) {
                            runs {
                                val url = it.getArgument<String>("url")
                                when (val bot = it.getBot()) {
                                    is MusicBot -> {
                                        bot.audioController.playStream(url)
                                    }
                                    null -> {
                                        println("bot not found")
                                    }
                                    else -> {
                                        println("bot is not a musicbot")
                                    }
                                }
                            }
                        }
                    }

                }


            }
        }
    }
}