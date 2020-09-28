@file:JvmName("Main")

package eu.musicnova.musicnova.main

import eu.musicnova.musicnova.MusicnovaApplication
import eu.musicnova.musicnova.boot.MusicnovaApplicationCommandLine
import org.fusesource.jansi.AnsiConsole
import picocli.CommandLine
import java.io.File

fun main(args: Array<String>) {
    AnsiConsole.systemInstall()
    val commandLine = CommandLine(MusicnovaApplicationCommandLine::class.java)
    if (jarName != null) {
        commandLine.commandName = jarName
    }
    commandLine.execute(*args)
}
private val jarName by lazy {
    File(MusicnovaApplication::class.java.protectionDomain.codeSource.location.path)
        .takeIf { file -> file.exists() && !file.isFile }?.name
}