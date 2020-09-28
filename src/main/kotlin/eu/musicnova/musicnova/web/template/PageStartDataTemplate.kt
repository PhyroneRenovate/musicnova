package eu.musicnova.musicnova.web.template

import eu.musicnova.shared.InterPlatformSerializer
import eu.musicnova.shared.PageStartData
import eu.musicnova.shared.SharedConst

import io.ktor.html.Template
import kotlinx.html.HEAD
import kotlinx.html.script
import kotlinx.html.unsafe
import java.util.*

class PageStartDataTemplate(private val startData: PageStartData) : Template<HEAD> {
    override fun HEAD.apply() {
        val startDataBytes = startData.toBytes()
        val baseString = base64Encoder.encodeToString(startDataBytes)
        script { unsafe { +"window.${SharedConst.START_DATA_FIELD}=\"${baseString}\"" } }
    }

    companion object Static {
       private val base64Encoder = Base64.getEncoder()
    }
}