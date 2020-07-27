@file:JvmName("MusicNovaBoot")

package eu.musicnova.musicnova


import eu.musicnova.musicnova.boot.MusicnovaApplicationCommandLine
import eu.musicnova.musicnova.utils.MnRepositoryImpl
import org.fusesource.jansi.AnsiConsole
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import picocli.CommandLine
import java.io.File

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = MnRepositoryImpl::class)
@EnableScheduling
class MusicnovaApplication {

    companion object Static {
        @JvmStatic
        fun main(args: Array<String>) {
            AnsiConsole.systemInstall()
            val commandLine = CommandLine(MusicnovaApplicationCommandLine)
            if (jarName != null) {
                commandLine.commandName = jarName
            }
            commandLine.execute(*args)
        }

        private val jarName by lazy {
            File(MusicnovaApplication::class.java.protectionDomain.codeSource.location.path)
                    .takeIf { file -> file.exists() && !file.isFile }?.name
        }
    }
}

