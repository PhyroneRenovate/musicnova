package eu.musicnova.musicnova.web.auth

import com.uchuhimo.konf.ConfigSpec
import de.phyrone.brig.wrapper.literal
import eu.musicnova.musicnova.database.dao.PersistentWebUserData
import eu.musicnova.musicnova.database.dao.PersistentWebUserSessionData
import eu.musicnova.musicnova.database.dao.WebUserDatabase
import eu.musicnova.musicnova.database.dao.WebUserSessionDatabase
import eu.musicnova.musicnova.utils.Konf
import eu.musicnova.musicnova.utils.TerminalCommandDispatcher
import eu.musicnova.musicnova.utils.getOrNull
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.CookieEncoding
import io.ktor.util.pipeline.PipelineContext
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import javax.annotation.PostConstruct

@Component
class WebSessionAuthManager {

    @Autowired
    lateinit var webSessionDatabase: WebUserSessionDatabase

    @Autowired
    lateinit var webUserDatabase: WebUserDatabase

    @Autowired
    @Qualifier(value = "web-session-name")
    lateinit var sessionName: String

    private val cookieEncoding = CookieEncoding.URI_ENCODING

    /* a cache to hold used instances */
    private val weakCache = WeakHashMap<String, PersistentWebUserSessionData>()

    @Suppress("NOTHING_TO_INLINE")
    final inline fun PipelineContext<*, ApplicationCall>.getUserSession() = call.getUserSession()

    fun ApplicationCall.getUserSession(): PersistentWebUserSessionData? {

        val token = request.cookies[sessionName, cookieEncoding]
        return if (token != null) {
            val session = getSession(token)
            if (session == null) {
                response.cookies.appendExpired(token)
            }
            session
        } else null
    }

    private fun PersistentWebUserSessionData.renew() {
        lastSeenDate = LocalDateTime.now()
        webSessionDatabase.save(this)
    }

    fun ApplicationCall.createSession(user: PersistentWebUserData): PersistentWebUserSessionData {
        val session = createSessionObject(user)
        response.cookies.append(sessionName, session.sessionToken, cookieEncoding, path = "/", httpOnly = true)
        return session
    }

    fun getSession(token: String): PersistentWebUserSessionData? = weakCache[token]
            ?: webSessionDatabase.findById(token).getOrNull()?.also { it.renew() }


    fun removeSession(token: String) {
        weakCache.remove(token)
        webSessionDatabase.deleteBySessionToken(token)
    }

    @Autowired
    @Qualifier(value = "max-idle-lifetime")
    var maxIdleLifetime: Long = Long.MAX_VALUE

    @Autowired
    @Qualifier(value = "max-lifetime")
    var maxLifetime: Long = Long.MAX_VALUE

    @Transactional
    @Scheduled(fixedRate = 1000 * 30)
    fun sessionCleanup() {
        val currentDateTime = LocalDateTime.now()
        webSessionDatabase.deleteByLastSeenDateBefore(currentDateTime - Duration.ofSeconds(maxIdleLifetime))
        webSessionDatabase.deleteByLoginDateBefore(currentDateTime - Duration.ofSeconds(maxLifetime))
    }


    @Transactional
    fun createSessionObject(user: PersistentWebUserData): PersistentWebUserSessionData {
        val token = newWebSessionToken()
        val currentDate = LocalDateTime.now()
        val session = PersistentWebUserSessionData(token, user, currentDate, currentDate)
        webSessionDatabase.save(session)
        webUserDatabase.refresh(user)
        return session
    }


    private fun newWebSessionToken() = RandomStringUtils.randomAlphabetic(512)

    @Autowired
    lateinit var config: Konf

    @Autowired
    lateinit var spec: WebSessionManagerSpec

    @Autowired
    lateinit var terminalCommandDispatcher: TerminalCommandDispatcher

    @PostConstruct
    fun registerSessionCOmmands() {
        terminalCommandDispatcher.literal("web") {
            literal("session") {

            }
        }
    }
}

@Component
class WebSessionManagerSpec : ConfigSpec("web.session") {
    val cookieName by optional("musicnova-session", "cookie-name")
    val maxIdleLifeTime by optional(3 * 24 * 60 * 60L, "max-idle-lifetime")
    val maxLifeTime by optional(7 * 24 * 60 * 60L, "max-lifetime")

    @Bean
    @Qualifier(value = "web-session-name")
    fun getSessionTokenName(config: Konf) = config[cookieName]

    @Bean
    @Qualifier(value = "max-idle-lifetime")
    fun getMaxIdleLifeTime(config: Konf) = config[maxIdleLifeTime]

    @Bean
    @Qualifier(value = "max-lifetime")
    fun getMaxTime(config: Konf) = config[maxLifeTime]
}
