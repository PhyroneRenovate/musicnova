package eu.musicnova.musicnova.database.provider

import java.util.function.Supplier
import javax.sql.DataSource

interface DatasourceProvider : Supplier<DataSource> {
    val name: String

    val alias: Set<String>
         get() = setOf()
}