package eu.musicnova.musicnova.web.template

import eu.musicnova.shared.PageStartData
import io.ktor.html.Template
import io.ktor.html.insert
import io.ktor.http.LinkHeader
import kotlinx.html.*


class PageTemplate(private val startData: PageStartData) : Template<HTML> {
    override fun HTML.apply() {
        head {
            script(src = "/assets/3rd/ionicons/ionicons.js") { }
            insert(PageStartDataTemplate(startData)) {}
            script(src = "/assets/js/musicnova.js") {}
            link(rel = LinkHeader.Rel.Stylesheet, href = "/assets/css/default.css") {
                id = "style-link"
            }
        }
        body { }
    }
}