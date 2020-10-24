package eu.musicnova.musicnova.web.modules

import eu.musicnova.musicnova.module.WebModule
import eu.musicnova.shared.InterPlatformSerializer
import eu.musicnova.shared.UUIDIdentifier
import eu.musicnova.shared.WebTheme
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
import kotlinx.serialization.builtins.serializer
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
                cookie.httpOnly = true
            }
            cookie<UUIDIdentifier>("musicnova-selected-bot") {
                serializer = BotIdentifierSerializer
                cookie.domain = null
                cookie.encoding = CookieEncoding.URI_ENCODING
                cookie.httpOnly = true
            }
        }
    }

    /* using just ordinals of enums instant of there full names is much smaller  */
    private object ThemeOrdinalSerializer : SessionSerializer<WebTheme> {

        override fun deserialize(text: String): WebTheme = WebTheme.values()[text.toInt()]

        override fun serialize(theme: WebTheme): String = theme.ordinal.toString()
    }

    private object BotIdentifierSerializer : SessionSerializer<UUIDIdentifier> {

        override fun deserialize(text: String) =
            InterPlatformSerializer.deserialize(UUIDIdentifier.serializer(), Base64.getDecoder().decode(text))

        override fun serialize(session: UUIDIdentifier): String =
            Base64.getEncoder().encodeToString(InterPlatformSerializer.serialize(UUIDIdentifier.serializer(), session))
    }
}