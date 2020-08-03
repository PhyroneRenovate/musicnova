package eu.musicnova.frontend.thrd

import org.w3c.dom.HTMLElement

//@JsModule("sweetalert2")
@JsModule("sweetalert2/dist/sweetalert2.js")
@JsNonModule
external object Swal {
    fun fire(any: SwalBuilder)
    fun fire(title: String)

    fun getContent(): HTMLElement
    fun isVisible()
    fun getTitle(): String
    fun isLoading():Boolean
    fun showLoading()
    fun hideLoading()
    fun close()
}


class SwalBuilder {
    @JsName("title")
    var title: String? = null

    @JsName("titleText")
    var titleText: String? = null

    @JsName("icon")
    var icon: String? = null

    @JsName("iconHtml")
    var iconHtml: String? = null

    @JsName("html")
    var html: Any? = null

    var text: Any? = null
}

fun Swal.fire(block: SwalBuilder.() -> Unit) {
    val builder = SwalBuilder()
    block.invoke(builder)
    fire(builder)
}


@JsModule("@vizuaalog/bulmajs")
@JsNonModule
external object Bulma {
    val default: BulmaCore

}

external class BulmaCore {
    @JsName("VERSION")
    val VERSION: String
}

@JsModule("@popperjs/core")
@JsNonModule
external fun createPopper(element1: HTMLElement, toolTipElement: HTMLElement, config: PopperConfiguration?)
fun createPopper(element1: HTMLElement, toolTipElement: HTMLElement) = createPopper(element1, toolTipElement, null)

data class PopperConfiguration(
        val placement: String?
)