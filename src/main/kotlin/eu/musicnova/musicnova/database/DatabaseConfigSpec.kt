package eu.musicnova.musicnova.database

import com.uchuhimo.konf.ConfigSpec
import org.springframework.stereotype.Component

@Component("config_spec_database")
class DatabaseConfigSpec : ConfigSpec("database") {
    val type by optional("h2", "type")
    val url by optional("", "url", "jdbc url of database\nuse this for replacing the default one (advanced usage)")
    val hosts by optional(listOf("localhost"), "hosts")
    val database by optional("musicnova", "db")
    val username by optional("", "auth.username")
    val password by optional("", "auth.password")
}