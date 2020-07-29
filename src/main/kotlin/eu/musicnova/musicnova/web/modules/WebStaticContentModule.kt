package eu.musicnova.musicnova.web.modules

import eu.musicnova.musicnova.module.WebModule
import eu.musicnova.musicnova.utils.Const.BEAN_IS_BOOT_JAR
import io.ktor.application.Application
import io.ktor.http.content.resource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.route
import io.ktor.routing.routing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class WebStaticContentModule : WebModule {

    @Autowired
    @Qualifier(BEAN_IS_BOOT_JAR)
    var isBootJar: Boolean = false

    override fun Application.invoke() {
        routing {
            static {
                if (isBootJar)
                    resource("favicon.ico", "$BOOT_JAR_PREFIX/web/favicon.ico")
                else
                    resource("favicon.ico", "/web/favicon.ico")
                route("assets") {
                    if (isBootJar) {
                        resources("$BOOT_JAR_PREFIX/web/assets/")
                    } else {
                        resources("/web/assets")
                    }
                }
            }
        }
    }

    companion object Static {
        private const val BOOT_JAR_PREFIX = "/BOOT-INF/classes/"
    }
}