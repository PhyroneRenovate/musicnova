package eu.musicnova.musicnova.beans.present

import eu.musicnova.musicnova.boot.MusicnovaApplicationCommandLine

data class InitCommandLineBeanPresent(private val commandLine: MusicnovaApplicationCommandLine) :
    BeanPresent<MusicnovaApplicationCommandLine> {
    override fun get(): MusicnovaApplicationCommandLine = commandLine
}