package eu.musicnova.musicnova.web.modules

import eu.musicnova.musicnova.module.WebModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.websocket.WebSockets
import org.springframework.stereotype.Component

@Component
class DefaultFeaturesWebModule : WebModule {
    override fun Application.invoke() {
        install(ContentNegotiation){
            jackson {
                findAndRegisterModules()
            }
        }
        install(WebSockets)
    }
}