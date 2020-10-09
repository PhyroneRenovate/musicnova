package eu.musicnova.musicnova.database.provider

import com.uchuhimo.konf.Config
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import eu.musicnova.musicnova.database.DatabaseConfigSpec
import org.springframework.beans.factory.annotation.Autowired
import javax.sql.DataSource

abstract class HikariDatasourceProvider : DatasourceProvider {
    abstract fun getURl(): String
    open val driverClass: String? = null

    @Autowired
    lateinit var config: Config

    @Autowired
    lateinit var configSpec: DatabaseConfigSpec

    final override fun get(): DataSource {
        val hikariConfig = HikariConfig()
        val url = (config[configSpec.url].takeUnless { url -> url.isNullOrBlank() || url == "null" } ?: getURl())
        hikariConfig.jdbcUrl = url
        hikariConfig.poolName = "musicnova-db-pool"

        val driverClass = this.driverClass
        if (!driverClass.isNullOrBlank()) {
            hikariConfig.driverClassName = driverClass
        }
        val username = config[configSpec.username]
        if (username?.isNotBlank() == true)
            hikariConfig.username = username
        val password = config[configSpec.password]
        if (password?.isNotBlank() == true)
            hikariConfig.password = password

        return HikariDataSource(hikariConfig)
    }

    open fun HikariConfig.override() {}
}