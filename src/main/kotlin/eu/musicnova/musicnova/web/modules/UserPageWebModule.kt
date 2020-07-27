package eu.musicnova.musicnova.web.modules

import eu.musicnova.musicnova.module.WebModule
import eu.musicnova.musicnova.web.auth.WebSessionAuthManager
import eu.musicnova.musicnova.web.template.PageTemplate
import eu.musicnova.shared.DashboardPage
import eu.musicnova.shared.LoginStatus
import eu.musicnova.shared.PageStartData
import eu.musicnova.shared.SharedConst
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.html.insert
import io.ktor.html.respondHtml
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelineInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller

@Controller
class UserPageWebModule : WebModule {

    @Autowired
    lateinit var sessionAuthManager: WebSessionAuthManager

    suspend fun PipelineContext<Unit, ApplicationCall>.handlePage(dashboardPage: DashboardPage) {
        val session = with(sessionAuthManager) { getUserSession() }
        val loginStatus = if (session == null) {
            LoginStatus.LOGOUT
        } else {
            LoginStatus.LOGIN
        }
        call.respondHtml { insert(PageTemplate(PageStartData(loginStatus, dashboardPage))) {} }
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