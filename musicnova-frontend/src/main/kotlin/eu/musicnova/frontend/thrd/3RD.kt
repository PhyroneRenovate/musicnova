package eu.musicnova.frontend.thrd

import org.w3c.dom.HTMLElement

//@JsModule("sweetalert2")
@JsModule("sweetalert2/dist/sweetalert2.js")
@JsNonModule
external object Swal {
    fun fire(options: SweetAlertOptions<*, *>)
    fun fire(title: String)

    fun getContent(): HTMLElement
    fun isVisible()
    fun getTitle(): String
    fun isLoading(): Boolean
    fun showLoading()
    fun hideLoading()
    fun close()
}

fun Swal.fire(block: SweetAlertOptions<Any, Any>.() -> Unit) {
    val options = object : SweetAlertOptions<Any, Any> {}
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

external interface SweetAlertOptions<PreConfirmResult, PreConfirmCallbackValue> {
    var title: dynamic /* String? | HTMLElement? | JQuery? */
        get() = definedExternally
        set(value) = definedExternally
    var titleText: String?
        get() = definedExternally
        set(value) = definedExternally
    var text: String?
        get() = definedExternally
        set(value) = definedExternally
    var html: dynamic /* String? | HTMLElement? | JQuery? */
        get() = definedExternally
        set(value) = definedExternally
    var icon: String? /* 'success' | 'error' | 'warning' | 'info' | 'question' */
        get() = definedExternally
        set(value) = definedExternally
    var iconHtml: String?
        get() = definedExternally
        set(value) = definedExternally
    var footer: dynamic /* String? | HTMLElement? | JQuery? */
        get() = definedExternally
        set(value) = definedExternally
    var backdrop: dynamic /* Boolean? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var toast: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var target: dynamic /* String? | HTMLElement? */
        get() = definedExternally
        set(value) = definedExternally
    var input: String? /* 'text' | 'email' | 'password' | 'number' | 'tel' | 'range' | 'textarea' | 'select' | 'radio' | 'checkbox' | 'file' | 'url' */
        get() = definedExternally
        set(value) = definedExternally
    var width: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var padding: dynamic /* Number? | String? */
        get() = definedExternally
        set(value) = definedExternally
    var background: String?
        get() = definedExternally
        set(value) = definedExternally
    var position: String? /* 'top' | 'top-start' | 'top-end' | 'top-left' | 'top-right' | 'center' | 'center-start' | 'center-end' | 'center-left' | 'center-right' | 'bottom' | 'bottom-start' | 'bottom-end' | 'bottom-left' | 'bottom-right' */
        get() = definedExternally
        set(value) = definedExternally
    var grow: dynamic /* String | String | String | Boolean? */
        get() = definedExternally
        set(value) = definedExternally
    var showClass: dynamic
        get() = definedExternally
        set(value) = definedExternally
    var hideClass: dynamic
        get() = definedExternally
        set(value) = definedExternally
    var customClass: dynamic
        get() = definedExternally
        set(value) = definedExternally
    var timer: Number?
        get() = definedExternally
        set(value) = definedExternally
    var timerProgressBar: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var animation: dynamic /* Boolean? | (() -> Boolean)? */
        get() = definedExternally
        set(value) = definedExternally
    var heightAuto: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var allowOutsideClick: dynamic /* Boolean? | (() -> Boolean)? */
        get() = definedExternally
        set(value) = definedExternally
    var allowEscapeKey: dynamic /* Boolean? | (() -> Boolean)? */
        get() = definedExternally
        set(value) = definedExternally
    var allowEnterKey: dynamic /* Boolean? | (() -> Boolean)? */
        get() = definedExternally
        set(value) = definedExternally
    var stopKeydownPropagation: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var keydownListenerCapture: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var showConfirmButton: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var showCancelButton: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var confirmButtonText: String?
        get() = definedExternally
        set(value) = definedExternally
    var cancelButtonText: String?
        get() = definedExternally
        set(value) = definedExternally
    var confirmButtonColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var cancelButtonColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var confirmButtonAriaLabel: String?
        get() = definedExternally
        set(value) = definedExternally
    var cancelButtonAriaLabel: String?
        get() = definedExternally
        set(value) = definedExternally
    var buttonsStyling: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var reverseButtons: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var focusConfirm: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var focusCancel: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var showCloseButton: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var closeButtonHtml: String?
        get() = definedExternally
        set(value) = definedExternally
    var closeButtonAriaLabel: String?
        get() = definedExternally
        set(value) = definedExternally
    var showLoaderOnConfirm: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    val preConfirm: ((inputValue: PreConfirmCallbackValue) -> PreConfirmResult)?
        get() = definedExternally
    var imageUrl: String?
        get() = definedExternally
        set(value) = definedExternally
    var imageWidth: Number?
        get() = definedExternally
        set(value) = definedExternally
    var imageHeight: Number?
        get() = definedExternally
        set(value) = definedExternally
    var imageAlt: String?
        get() = definedExternally
        set(value) = definedExternally
    var inputPlaceholder: String?
        get() = definedExternally
        set(value) = definedExternally
    var inputValue: dynamic /* String? | Promise<String>? | `T$0`? */
        get() = definedExternally
        set(value) = definedExternally
    var inputOptions: dynamic /* ReadonlyMap<String, String>? | Record<String, Any>? | Promise<dynamic /* ReadonlyMap<String, String> | Record<String, Any> */>? | `T$0`? */
        get() = definedExternally
        set(value) = definedExternally
    var inputAutoTrim: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var inputAttributes: dynamic
        get() = definedExternally
        set(value) = definedExternally
    val inputValidator: ((inputValue: String) -> dynamic)?
        get() = definedExternally
    var validationMessage: String?
        get() = definedExternally
        set(value) = definedExternally
    var progressSteps: Any?
        get() = definedExternally
        set(value) = definedExternally
    var currentProgressStep: String?
        get() = definedExternally
        set(value) = definedExternally
    var progressStepsDistance: String?
        get() = definedExternally
        set(value) = definedExternally
    val onBeforeOpen: ((popup: HTMLElement) -> Unit)?
        get() = definedExternally
    val onOpen: ((popup: HTMLElement) -> Unit)?
        get() = definedExternally
    val onRender: ((popup: HTMLElement) -> Unit)?
        get() = definedExternally
    val onClose: ((popup: HTMLElement) -> Unit)?
        get() = definedExternally
    val onAfterClose: (() -> Unit)?
        get() = definedExternally
    val onDestroy: (() -> Unit)?
        get() = definedExternally
    var scrollbarPadding: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}