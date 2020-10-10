package eu.musicnova.musicnova.beans.present

import eu.musicnova.musicnova.boot.MusicnovaCommantLineStartPoint

data class InitCommandLineBeanPresent(private val commandLine: MusicnovaCommantLineStartPoint) :
    BeanPresent<MusicnovaCommantLineStartPoint> {
    override fun get(): MusicnovaCommantLineStartPoint = commandLine
}