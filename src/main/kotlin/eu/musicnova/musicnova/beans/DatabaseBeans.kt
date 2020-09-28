package eu.musicnova.musicnova.beans

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.sql.DataSource


@Component
class DatabaseBeans {
    private val logger = LoggerFactory.getLogger(DatabaseBeans::class.java)

    @Bean
    fun database(dataSource: DataSource, appContext: ApplicationContext): Database {
        logger.info("Starting Exposed...")
        return Database.connect(dataSource).also { database ->
            transaction(database) { addLogger(Slf4jSqlDebugLogger) }
            transaction(database) {

                SchemaUtils.createMissingTablesAndColumns(*appContext.getBeansOfType(Table::class.java).values.toTypedArray())
            }
        }
    }
}