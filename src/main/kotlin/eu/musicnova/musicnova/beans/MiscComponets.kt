package eu.musicnova.musicnova.beans

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import eu.musicnova.musicnova.utils.TerminalCommandDispatcher
import org.jline.reader.Completer
import org.jline.reader.Highlighter
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream

@Component
class MiscComponets {
    @Bean fun terminalCommandDispatcher() = TerminalCommandDispatcher()

    @Bean
    fun lavaPlayer() = DefaultAudioPlayerManager().also { defaultAudioPlayerManager ->
        AudioSourceManagers.registerRemoteSources(defaultAudioPlayerManager)
    }

    /*
    @Bean
    fun terminal(): Terminal = TerminalBuilder.builder()
            .system(true)
            .dumb(true)
            .streams(FileInputStream(FileDescriptor.`in`), FileOutputStream(FileDescriptor.out))
            .jansi(true)
            .jna(true)
            .encoding(Charsets.UTF_8)
            .name("Musicnova")
            .nativeSignals(true)
            .build()

    @Bean
    fun lineReader(terminal: Terminal, completer: Completer, highlighter: Highlighter): LineReader = LineReaderBuilder.builder()
            .terminal(terminal)
            .completer(completer)
            .history(DefaultHistory())
            .highlighter(highlighter)
            .appName("Musicnova")
            .build()
    */

    //TODO("implement sentry (+ disable option)")
    //@Bean fun senry():SentryClient = Sentry.init("https://4c3668b7a6b34a1da4feddb0755744e2@sentry.phyrone.de/8")
}