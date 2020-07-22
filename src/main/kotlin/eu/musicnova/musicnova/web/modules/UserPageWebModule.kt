package eu.musicnova.musicnova.web.modules

import eu.musicnova.musicnova.module.WebModule
import eu.musicnova.musicnova.web.template.PageStartDataTemplate
import eu.musicnova.musicnova.web.template.PageTemplate
import eu.musicnova.shared.LoginStatus
import eu.musicnova.shared.PageStartData
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.html.insert
import io.ktor.html.respondHtml
import io.ktor.http.LinkHeader
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.html.*
import org.springframework.stereotype.Controller

@Controller
class UserPageWebModule : WebModule {
    override fun Application.invoke() {
        routing {
            get("/") {
                call.respondHtml {
                    insert(PageTemplate(PageStartData(LoginStatus.LOGIN))) {}
                }
            }
        }
    }
}