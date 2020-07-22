package eu.musicnova.musicnova.beans

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import javax.sql.DataSource


@Component
class ExposedDatabaseComponent {
    private val logger = LoggerFactory.getLogger(ExposedDatabaseComponent::class.java)

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