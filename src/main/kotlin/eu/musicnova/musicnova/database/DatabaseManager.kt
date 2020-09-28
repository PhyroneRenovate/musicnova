package eu.musicnova.musicnova.database

import com.uchuhimo.konf.Config
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import eu.musicnova.musicnova.database.provider.DatasourceProvider
import eu.musicnova.musicnova.database.provider.H2DatasourceProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.lang.RuntimeException
import javax.sql.DataSource

@Component
class DatabaseManager {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var config: Config

    @Autowired
    lateinit var configSpec: DatabaseConfigSpec

    @Bean
    fun datasource(
        providers: Set<DatasourceProvider>
    ): DataSource {
        val type = config[configSpec.type]
        return (providers.find { provider ->
            provider.name.equals(
                type,
                true
            ) || provider.alias.any { alias -> alias.equals(type, true) }
        } ?: throw RuntimeException("database with type \"$type\" not found")).get()
    }
}