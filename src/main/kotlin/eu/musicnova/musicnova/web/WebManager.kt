package eu.musicnova.musicnova.web

import com.uchuhimo.konf.ConfigSpec
import eu.musicnova.musicnova.module.WebModule
import eu.musicnova.musicnova.utils.Konf
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy

@Component
class WebManager {
    private val logger = LoggerFactory.getLogger(WebManager::class.java)

    @Bean
    fun appEnv(appContext: ApplicationContext, config: Konf) = applicationEngineEnvironment {
        logger.info("Configure WebServer...")

        config[WebConfig.hosts].forEach { hostAndPort ->
            connector {
                this.host = hostAndPort.host
                this.port = hostAndPort.port
            }
        }

        appContext.getBeansOfType(WebModule::class.java).values.forEach { webExt ->
            module {
                with(webExt) { invoke() }
            }

        }
    }

    @Bean
    fun ktorServer(appEnv: ApplicationEngineEnvironment) = embeddedServer(Netty, appEnv) {

    }

    @Autowired
    lateinit var engine: ApplicationEngine

    @Bean
    fun onWebModuleStart() = ApplicationRunner {
        logger.info("Starting WebServer...")
        engine.start()
    }

    @Bean
    fun webConfigSpec(): ConfigSpec = WebConfig

    private object WebConfig : ConfigSpec("web") {
        val hosts by optional(listOf(WebHostAndPort("0.0.0.0", 8080)))
    }

    private data class WebHostAndPort(val host: String, val port: Int)

    @PreDestroy
    fun onStop() {
        logger.info("Stopping WebServer...")
        engine.stop(1000, 3000)
    }



}


