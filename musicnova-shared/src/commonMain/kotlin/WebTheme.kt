package eu.musicnova.shared

import kotlinx.serialization.Serializable

enum class WebTheme(fileName: String) {
    DEFAULT("default"),
    UNITED("united"),
    LUMEN("lumen"),
    CERULEAN("cerulean"),
    COSMO("cosmo");

    val fullPath = "/assets/css/$fileName.css"
}