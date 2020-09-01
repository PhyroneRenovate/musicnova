package eu.musicnova.frontend.thrd

import org.w3c.dom.HTMLElement

//@JsModule("sweetalert2")
@JsModule("sweetalert2/dist/sweetalert2.js")
@JsNonModule
external object Swal {
    fun fire(options: SweetAlertOptions)
    fun fire(title: String)

    fun getContent(): HTMLElement
    fun isVisible()
    fun getTitle(): String
    fun isLoading(): Boolean
    fun showLoading()
    fun hideLoading()
    fun close()
}

fun Swal.fire(block: SweetAlertOptions.() -> Unit) {
    val options = SweetAlertOptions()
    block.invoke(options)

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

@Suppress("unused")
class SweetAlertOptions {
    var title: dynamic /* String? | HTMLElement? | JQuery? */ = null

    var titleText: String? = null
    var text: String? = null
    var html: dynamic /* String? | HTMLElement? | JQuery? */ = null
    var icon: String? /* 'success' | 'error' | 'warning' | 'info' | 'question' */ = null
    var iconHtml: String? = null
    var footer: dynamic /* String? | HTMLElement? | JQuery? */ = null
    var backdrop: dynamic /* Boolean? | String? */ = null
    var toast: Boolean? = null
    var target: dynamic /* String? | HTMLElement? */ = null
    var input: String? /* 'text' | 'email' | 'password' | 'number' | 'tel' | 'range' | 'textarea' | 'select' | 'radio' | 'checkbox' | 'file' | 'url' */ =
        null
    var width: dynamic /* Number? | String? */ = null
    var padding: dynamic /* Number? | String? */ = null
    var background: String? = null
    var position: String? /* 'top' | 'top-start' | 'top-end' | 'top-left' | 'top-right' | 'center' | 'center-start' | 'center-end' | 'center-left' | 'center-right' | 'bottom' | 'bottom-start' | 'bottom-end' | 'bottom-left' | 'bottom-right' */ =
        null
    var grow: dynamic /* String | String | String | Boolean? */ = null
    var showClass: dynamic = null
    var hideClass: dynamic = null
    var customClass: dynamic = null
    var timer: Number? = null
    var timerProgressBar: Boolean? = null
    var animation: dynamic /* Boolean? | (() -> Boolean)? */ = null
    var heightAuto: Boolean? = null
    var allowOutsideClick: dynamic /* Boolean? | (() -> Boolean)? */ = null
    var allowEscapeKey: dynamic /* Boolean? | (() -> Boolean)? */ = null
    var allowEnterKey: dynamic /* Boolean? | (() -> Boolean)? */ = null
    var stopKeydownPropagation: Boolean? = null
    var keydownListenerCapture: Boolean? = null
    var showConfirmButton: Boolean? = null
    var showCancelButton: Boolean? = null
    var confirmButtonText: String? = null
    var cancelButtonText: String? = null
    var confirmButtonColor: String? = null
    var cancelButtonColor: String? = null
    var confirmButtonAriaLabel: String? = null
    var cancelButtonAriaLabel: String? = null
    var buttonsStyling: Boolean? = null
    var reverseButtons: Boolean? = null
    var focusConfirm: Boolean? = null
    var focusCancel: Boolean? = null
    var showCloseButton: Boolean? = null
    var closeButtonHtml: String? = null
    var closeButtonAriaLabel: String? = null
    var showLoaderOnConfirm: Boolean? = null
    val preConfirm: ((inputValue: dynamic) -> dynamic)? = null
    var imageUrl: String? = null
    var imageWidth: Number? = null
    var imageHeight: Number? = null
    var imageAlt: String? = null
    var inputPlaceholder: String? = null
    var inputValue: dynamic /* String? | Promise<String>? | `T$0`? */ = null
    var inputOptions: dynamic /* ReadonlyMap<String, String>? | Record<String, Any>? | Promise<dynamic /* ReadonlyMap<String, String> | Record<String, Any> */>? | `T$0`? */ =
        null
    var inputAutoTrim: Boolean? = null
    var inputAttributes: dynamic = null
    val inputValidator: ((inputValue: String) -> dynamic)? = null
    var validationMessage: String? = null
    var progressSteps: Any? = null
    var currentProgressStep: String? = null
    var progressStepsDistance: String? = null
    val onBeforeOpen: ((popup: HTMLElement) -> Unit)? = null
    val onOpen: ((popup: HTMLElement) -> Unit)? = null
    val onRender: ((popup: HTMLElement) -> Unit)? = null
    val onClose: ((popup: HTMLElement) -> Unit)? = null
    val onAfterClose: (() -> Unit)? = null
    val onDestroy: (() -> Unit)? = null
    var scrollbarPadding: Boolean? = null
}