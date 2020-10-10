package eu.musicnova.musicnova.boot

import java.io.File

interface StartConfiguration {
    val debug: Boolean
    val allowRoot: Boolean
    val configFileName: String
    val interactive: Boolean
    val disableSentry: Boolean
    val dataFolder: File
}