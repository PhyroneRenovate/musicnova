package eu.musicnova.musicnova.web.modules

import eu.musicnova.musicnova.module.WebModule
import eu.musicnova.shared.BotIdentifier
import eu.musicnova.shared.WebTheme
import eu.musicnova.shared.protoBuf
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.*
import io.ktor.http.CookieEncoding
import io.ktor.jackson.jackson
import io.ktor.sessions.SessionSerializer
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.websocket.WebSockets
import org.slf4j.event.Level
import org.springframework.stereotype.Component
import java.util.*

@Component
class DefaultFeaturesWebModule : WebModule {
    override fun Application.invoke() {
        install(ContentNegotiation) {
            jackson {
                findAndRegisterModules()
            }
        }
        install(WebSockets)
        install(DefaultHeaders)
        install(CallLogging) {
            level = Level.DEBUG
        }
        install(ConditionalHeaders)
        install(CORS) {

        }
        install(Authentication) {

        }
        install(StatusPages)
        install(Compression) {
            gzip {
                priority = 10.0
                minimumSize(1024)
            }
            deflate {
                priority = 1.0
                minimumSize(512)
            }
        }

        install(Sessions) {
            //TODO("configurable")
            cookie<WebTheme>("musicnova-theme") {
                serializer = ThemeOrdinalSerializer
                cookie.domain = null
                cookie.encoding = CookieEncoding.RAW
                cookie.secure = false
                cookie.httpOnly=true
            }
            cookie<BotIdentifier>("musicnova-selected-bot") {
                serializer = BotIdentifierSerializer
                cookie.domain = null
                cookie.encoding = CookieEncoding.URI_ENCODING
                cookie.secure = false
                cookie.httpOnly=true
            }
        }
    }

    /* using ordinals is much smaller  */
    private object ThemeOrdinalSerializer : SessionSerializer<WebTheme> {

        override fun deserialize(text: String): WebTheme = WebTheme.values()[text.toInt()]

        override fun serialize(theme: WebTheme): String = theme.ordinal.toString()
    }

    private object BotIdentifierSerializer : SessionSerializer<BotIdentifier> {

        override fun deserialize(text: String): BotIdentifier = protoBuf.load(BotIdentifier.serializer(), Base64.getDecoder().decode(text))

        override fun serialize(session: BotIdentifier): String = Base64.getEncoder().encodeToString(protoBuf.dump(BotIdentifier.serializer(), session))
    }
}