package eu.musicnova.musicnova.web.modules

import eu.musicnova.musicnova.boot.MusicnovaCommantLineStartPoint
import eu.musicnova.musicnova.module.WebModule
import eu.musicnova.musicnova.web.auth.WebSessionAuthManager
import eu.musicnova.musicnova.web.template.PageTemplate
import eu.musicnova.shared.*
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.html.insert
import io.ktor.html.respondHtml
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.getOrSet
import io.ktor.sessions.sessions
import io.ktor.util.pipeline.PipelineContext
import kotlinx.html.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller

@Controller
class UserPageWebModule : WebModule {

    @Autowired
    lateinit var sessionAuthManager: WebSessionAuthManager

    @Autowired
    lateinit var commandLine: MusicnovaCommantLineStartPoint

    private val sendPageDebug by lazy { commandLine.debug }

    suspend fun PipelineContext<Unit, ApplicationCall>.handlePage(dashboardPage: PageContent) {
        val session = with(sessionAuthManager) { getUserSession() }
        val loginStatus = if (session == null) {
            LoginStatus.LOGOUT
        } else {
            LoginStatus.LOGIN
        }
        val theme = call.sessions.getOrSet { WebTheme.UNITED }
        call.respondHtml {
            insert(
                PageTemplate(
                    PageStartData(loginStatus, dashboardPage, theme, sendPageDebug),
                    theme
                )
            ) {}
        }
    }

    override fun Application.invoke() {
        routing {
            PageContent.values().forEach { dashboardPage ->
                get(dashboardPage.path) {
                    handlePage(dashboardPage)
                }
            }
            get("api") {
                call.respondHtml {
                    head {}
                    body {
                        ul {
                            li { a("/api/graphQL") { +"/api/graphQL" } }
                            li { a("/api/v1/") { +"/api/v1/" } }
                        }
                    }
                }
            }
        }
    }
}