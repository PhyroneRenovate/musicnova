package eu.musicnova.frontend

import eu.musicnova.frontend.dashboard.DashboardSession
import eu.musicnova.frontend.login.buildLoginWindow
import eu.musicnova.frontend.thrd.Swal
import eu.musicnova.frontend.thrd.fire
import eu.musicnova.frontend.utils.pageStartData
import eu.musicnova.frontend.utils.send
import eu.musicnova.frontend.utils.wsBaseURL
import eu.musicnova.shared.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.*
import kotlin.browser.window

fun main() {
    console.log("Started...")
    console.dir(pageStartData)
    window.onload = { onLoad() }

}

fun onLoad() {

    when (pageStartData.loginStatus) {
        LoginStatus.LOGOUT, LoginStatus.OTP -> buildLoginWindow()
        LoginStatus.BLOCKED -> Swal.fire {
            title = "You Are Blocked"
            icon = "error"
        }
        LoginStatus.LOGIN -> GlobalScope.launch { DashboardSession().start() }
        LoginStatus.ERROR -> Swal.fire {
            title = "Error"
            icon = "error"
        }
    }
}

