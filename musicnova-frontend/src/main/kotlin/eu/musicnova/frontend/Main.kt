package eu.musicnova.frontend

import eu.musicnova.frontend.dashboard.DashboardSession
import eu.musicnova.frontend.login.buildLoginWindow
import eu.musicnova.frontend.externals.Bulma
import eu.musicnova.frontend.externals.Dropzone
import eu.musicnova.frontend.externals.Swal
import eu.musicnova.frontend.externals.fire
import eu.musicnova.frontend.utils.pageStartData
import eu.musicnova.shared.LoginStatus
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


fun main() {
    console.log("Started...")
    console.dir(pageStartData)
    console.log("BulmaJS Version: ${Bulma.default.VERSION}")
    configureExternals()
    window.onload = { onLoad() }
}

fun startMainPage() {
    GlobalScope.launch { DashboardSession().start() }
}

fun onLoad() {

    when (pageStartData.loginStatus) {
        LoginStatus.LOGOUT, LoginStatus.OTP -> buildLoginWindow()
        LoginStatus.BLOCKED -> Swal.fire {
            title = "You Are Blocked"
            icon = "error"
        }
        LoginStatus.LOGIN -> startMainPage()
        LoginStatus.ERROR -> Swal.fire {
            title = "Error"
            icon = "error"
        }
    }
}

fun configureExternals() {
    Dropzone.autoDiscover = false
}
