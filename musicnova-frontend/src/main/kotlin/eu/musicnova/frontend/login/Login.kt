package eu.musicnova.frontend.login

import eu.musicnova.frontend.utils.Const
import eu.musicnova.frontend.utils.newBody
import eu.musicnova.frontend.utils.postRequest
import eu.musicnova.shared.PacketLoginRequest
import eu.musicnova.shared.PacketLoginResponse
import eu.musicnova.shared.SharedConst
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.js.*
import kotlinx.html.p
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFormElement
import kotlin.browser.document

fun buildLoginWindow() {
    newBody().append {
        div {
            form(method = FormMethod.post) {

                val emailField = input(classes = "input", type = InputType.email) { required = true }
                val passwordField = input(classes = "input", type = InputType.password) { required = true }
                val loginBTN = button(classes = "button is-primary is-light") {
                    i(classes = "fas fa-sign-in-alt") { }
                    p { +Const.LOGIN_BUTTON_TEXT }
                }

                fun setLoginDisabled(disabled: Boolean) {
                    loginBTN.disabled = disabled
                    emailField.disabled = disabled
                    passwordField.disabled = disabled
                    if (disabled) {
                        loginBTN.classList.add(Const.IS_LOADING_CLASS)

                    } else {
                        loginBTN.classList.remove(Const.IS_LOADING_CLASS)
                    }
                }

                onSubmitFunction = {
                    it.preventDefault()
                    GlobalScope.launch {
                        setLoginDisabled(true)
                        val response = postRequest(
                                SharedConst.INTERNAL_LOGIN_PATH,
                                PacketLoginRequest(emailField.value, passwordField.value),
                                PacketLoginRequest.serializer(),
                                PacketLoginResponse.serializer()
                        )
                        delay(5000)
                        setLoginDisabled(false)
                        println(response)
                    }
                }
            }
        }
    }
}
