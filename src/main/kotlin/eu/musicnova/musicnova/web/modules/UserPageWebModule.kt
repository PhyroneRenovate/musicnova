package eu.musicnova.musicnova.web.modules

import eu.musicnova.musicnova.boot.MusicnovaApplicationCommandLine
import eu.musicnova.musicnova.module.WebModule
import eu.musicnova.musicnova.web.auth.WebSessionAuthManager
import eu.musicnova.musicnova.web.template.PageTemplate
import eu.musicnova.shared.*
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.html.insert
import io.ktor.html.respondHtml
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.getOrSet
import io.ktor.sessions.sessions
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelineInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller

@Controller
class UserPageWebModule : WebModule {

    @Autowired
    lateinit var sessionAuthManager: WebSessionAuthManager

    @Autowired
    lateinit var commandLine: MusicnovaApplicationCommandLine

    private val sendPageDebug by lazy { commandLine.debug }

    suspend fun PipelineContext<Unit, ApplicationCall>.handlePage(dashboardPage: DashboardPage) {
        val session = with(sessionAuthManager) { getUserSession() }
        val loginStatus = if (session == null) {
            LoginStatus.LOGOUT
        } else {
            LoginStatus.LOGIN
        }
        val theme = call.sessions.getOrSet { WebTheme.UNITED }
        call.respondHtml { insert(PageTemplate(PageStartData(loginStatus, dashboardPage, theme, sendPageDebug), theme)) {} }
    }

    override fun Application.invoke() {
        routing {
            DashboardPage.values().forEach { dashboardPage ->
                get(dashboardPage.path) {
                    handlePage(dashboardPage)
                }
            }
        }
    }
}