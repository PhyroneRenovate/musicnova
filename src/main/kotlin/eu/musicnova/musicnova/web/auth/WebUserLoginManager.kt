package eu.musicnova.musicnova.web.auth

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import de.phyrone.brig.wrapper.StringArgument
import de.phyrone.brig.wrapper.getArgument
import de.phyrone.brig.wrapper.literal
import de.phyrone.brig.wrapper.runs
import de.vandermeer.asciitable.AsciiTable
import eu.musicnova.musicnova.database.dao.PersistentWebUserData
import eu.musicnova.musicnova.database.dao.WebUserDatabase
import eu.musicnova.musicnova.utils.TerminalCommandDispatcher
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Duration
import javax.annotation.PostConstruct

@Component
class WebUserLoginManager {


    @Autowired
    lateinit var terminalCommandDispatcher: TerminalCommandDispatcher

    @Autowired
    lateinit var database: WebUserDatabase

    operator fun get(username: String): PersistentWebUserData? {
        return database.findByUsername(username)
    }

    fun create(username: String, password: String): PersistentWebUserData {
        val user = PersistentWebUserData(username, password)
        database.save(user)
        return user
    }

    private val userSuggestionCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(3))
            .build<Unit, List<String>>(CacheLoader.from { _ -> database.findAll().map { user -> user.username } })

    @PostConstruct
    fun registerCommands() {
        terminalCommandDispatcher.literal("web") {
            literal("user") {
                literal("list") {
                    runs {
                        val table = AsciiTable()
                        table.addRule()
                        table.addRow("username", "amount of sessions")
                        table.addRule()
                        database.findAll().forEach { user ->
                            table.addRow(user.username, user.sessions.size)
                            table.addRule()
                        }
                        println(table.render())
                    }
                }
                literal("create") {
                    argument("username", StringArgument) {
                        runs {
                            val username = it.getArgument<String>("username")
                            val password = RandomStringUtils.randomAlphabetic(6, 16)
                            create(username, password)
                            println("User Created: $username:$password")
                        }
                        argument("password", StringArgument) {
                            runs {
                                val username = it.getArgument<String>("username")
                                val password = it.getArgument<String>("password")
                                create(username, password)
                                println("User Created: $username:$password")
                            }
                        }
                    }
                }
                argument("user", StringArgument) {
                    suggest {
                        userSuggestionCache.get(Unit).forEach { suggestion -> suggest(suggestion) }
                    }
                    runs {
                        val user = get(it.getArgument("user"))
                        if (user == null) {
                            println("user not found")
                        } else {
                            println(user)
                        }
                    }
                }
            }
        }
    }

}