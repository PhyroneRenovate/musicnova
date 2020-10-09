package eu.musicnova.musicnova.beans

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import javax.sql.DataSource


@Component
class DatabaseBeans {
    private val logger = LoggerFactory.getLogger(DatabaseBeans::class.java)

    @Bean
    fun database(
        dataSource: DataSource,
        appContext: ApplicationContext,
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        tables: Array<Table>
    ): Database {
        logger.info("Starting Exposed...")

        return Database.connect(dataSource).also { database ->
            transaction(database) {
                addLogger(Slf4jSqlDebugLogger)
                SchemaUtils.createMissingTablesAndColumns(*tables)
            }
        }
    }
}