package eu.musicnova.musicnova.web.template

import eu.musicnova.shared.PageStartData
import eu.musicnova.shared.SharedConst
import eu.musicnova.shared.protoBuf
import io.ktor.html.Template
import kotlinx.html.HEAD
import kotlinx.html.script
import kotlinx.html.unsafe
import java.util.*

class PageStartDataTemplate(private val startData: PageStartData) : Template<HEAD> {
    override fun HEAD.apply() {
        val startDataBytes = protoBuf.encodeToByteArray(PageStartData.serializer(), startData)
        val baseString = base64Encoder.encodeToString(startDataBytes)
        script { unsafe { +"window.${SharedConst.START_DATA_FIELD}=\"${baseString}\"" } }
    }

    private companion object Static {
        val base64Encoder = Base64.getEncoder()
    }
}