package eu.musicnova.musicnova.config

import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.Feature
import eu.musicnova.musicnova.boot.MusicnovaApplicationCommandLine
import eu.musicnova.musicnova.utils.ConfigWrapper
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.io.File
import javax.inject.Named

@Component
class ConfigManager {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun config(appContext: ApplicationContext, @Named("main-config") configFile: File?,configSpecList: List<ConfigSpec>) = ConfigWrapper {
        enable(Feature.WRITE_DESCRIPTIONS_AS_COMMENTS)
        configSpecList.forEach { spec ->
            addSpec(spec)
        }
    }.also { configWrapper ->
        logger.info("Loading Config...")
        if (configFile != null) {
            if (configFile.exists()) {
                configWrapper.load(configFile)
            } else if (configFile.parentFile?.exists() == false) {
                configFile.parentFile.mkdirs()
            }
            configWrapper.save(configFile)
        }
    }

    @Bean
    @Named("main-config")
    fun mainConfig(): File? {
        val fileName = MusicnovaApplicationCommandLine.configFileName
        return if (fileName.isBlank() || fileName.equals("none", true)) {
            null
        } else {
            File(fileName)
        }
    }
}