package eu.musicnova.frontend.login

import eu.musicnova.frontend.startMainPage
import eu.musicnova.frontend.thrd.Swal
import eu.musicnova.frontend.utils.Const
import eu.musicnova.frontend.utils.newBody
import eu.musicnova.frontend.utils.postRequest
import eu.musicnova.frontend.utils.setTheme
import eu.musicnova.shared.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.*


fun buildLoginWindow() {
    newBody().append {
        nav(classes = "navbar") {
            div(classes = "navbar-item") {
                +"Test"
            }
            div(classes = "navbar-end") {
                div(classes = "navbar-item has-dropdown is-hoverable") {
                    a(classes = "navbar-link") { +"Themes" }
                    div(classes = "navbar-dropdown is-boxed") {
                        WebTheme.values().forEach { theme ->
                            a(classes = "navbar-item") {
                                +theme.name
                                onClickFunction = {
                                    setTheme(theme)
                                }
                            }

                        }
                    }
                }
            }
        }
        div {
            form(method = FormMethod.post) {

                val emailField = input(classes = "input", type = InputType.text) { required = true }
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
                        setLoginDisabled(false)
                        when (response.status) {
                            LoginStatusResponse.VALID -> startMainPage()
                            LoginStatusResponse.INVALID -> Swal.fire("Login Invalid")
                            LoginStatusResponse.BLOCKED -> Swal.fire("you are blocked")
                        }
                        println(response)
                    }
                }
            }
        }
    }
}
