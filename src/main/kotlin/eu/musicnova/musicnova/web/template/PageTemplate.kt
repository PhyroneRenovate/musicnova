package eu.musicnova.musicnova.web.template

import eu.musicnova.shared.PageStartData
import eu.musicnova.shared.SharedConst
import eu.musicnova.shared.WebTheme
import io.ktor.html.Template
import io.ktor.html.insert
import io.ktor.http.LinkHeader
import kotlinx.html.*


class PageTemplate(
        private val startData: PageStartData,
        private val theme: WebTheme
) : Template<HTML> {
    override fun HTML.apply() {
        head {
            title("MusicNova")
            script(src = "/assets/3rd/ionicons/ionicons/ionicons.esm.js", type = "module") { }
            script(src = "/assets/3rd/ionicons/ionicons/ionicons.js") { attributes["nomodule"] = "" }
            insert(PageStartDataTemplate(startData)) {}
            script(src = "/assets/js/musicnova.js") {}
            link(rel = LinkHeader.Rel.Stylesheet, href = theme.fullPath) {
                id = SharedConst.STYLE_LINK_ID
            }
        }
        body {
            h1 { +"Loading Page" }
            h2 { +"please wait..." }
            div(classes = "sk-circle") {
                repeat(12) { div(classes = "sk-circle-dot") { } }
            }
            noScript { p { +"You Have to enable JavaScript to use this page" } }
        }
    }
}