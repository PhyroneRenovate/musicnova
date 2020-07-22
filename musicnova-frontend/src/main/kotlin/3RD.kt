package eu.musicnova.frontend

@JsModule("sweetalert2")
@JsNonModule
external object Swal {
    fun fire(any: SwalBuilder)
    fun fire(title: String)
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


}

fun Swal.fire(block: SwalBuilder.() -> Unit) {
    val builder = SwalBuilder()
    block.invoke(builder)
    fire(builder)
}