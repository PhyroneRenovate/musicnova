package eu.musicnova.musicnova.beans

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import eu.musicnova.musicnova.beans.present.InitCommandLineBeanPresent
import eu.musicnova.musicnova.boot.StartConfiguration
import eu.musicnova.musicnova.utils.Const
import eu.musicnova.musicnova.utils.TerminalCommandDispatcher
import io.sentry.Sentry
import io.sentry.SentryClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PreDestroy

@Component
class MiscComponets {
    @Bean
    fun terminalCommandDispatcher() = TerminalCommandDispatcher()

    @Bean
    fun lavaPlayer() = DefaultAudioPlayerManager().also { defaultAudioPlayerManager ->
        AudioSourceManagers.registerRemoteSources(defaultAudioPlayerManager)
    }

    @Bean
    @Suppress("SpringJavaAutowiringInspection", "SpringJavaInjectionPointsAutowiringInspection")
    fun commandLine(@Qualifier(Const.BEAN_NAME_CLI_PRESENT) present: InitCommandLineBeanPresent) = present.get()

    @Bean
    @Qualifier(Const.BEAN_DATA_FOLDER)
    fun dataFolder(cli: StartConfiguration) = cli.dataFolder

    @Bean
    @Qualifier(Const.BEAN_TEMP_FOLDER)
    fun tempFolder(@Qualifier(Const.BEAN_DATA_FOLDER) dataFolder: File) =
        File(dataFolder, "temp").apply { deleteRecursively();mkdirs() }

    @Bean
    @Qualifier(Const.BEAN_AUDIO_TRACK_FOLDER)
    fun audioTrackFolder(@Qualifier(Const.BEAN_DATA_FOLDER) dataFolder: File) = File(dataFolder, "audio")

    @Component
    class MiscBeansEndDestroyer{
        @Autowired
        @Qualifier(Const.BEAN_TEMP_FOLDER)
        lateinit var tempFolder: File

        @PreDestroy
        fun onMiscDestroy() {
            tempFolder.deleteRecursively()
        }
    }
}
