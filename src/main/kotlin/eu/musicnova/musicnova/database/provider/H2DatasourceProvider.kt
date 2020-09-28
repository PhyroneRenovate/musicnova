package eu.musicnova.musicnova.database.provider

import eu.musicnova.musicnova.utils.Const
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.File
import javax.sql.DataSource

@Component
class H2DatasourceProvider : HikariDatasourceProvider() {

    @Autowired
    @Qualifier(Const.BEAN_DATA_FOLDER)
    lateinit var dataFolder: File

    override fun getURl() = "jdbc:h2:${dataFolder.absolutePath}/musicnova;DB_CLOSE_DELAY=-1"

    override val driverClass: String? = "org.h2.Driver"
    override val name: String = "h2"


}