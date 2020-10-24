package eu.musicnova.musicnova.audio

import java.io.File

interface AudioFileStorage {
    operator fun get(id: String): File
    operator fun set(id: String, file: File)
}