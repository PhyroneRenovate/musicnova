package eu.musicnova.musicnova.terminal


import com.mojang.brigadier.exceptions.CommandSyntaxException
import de.phyrone.brig.wrapper.literal
import de.phyrone.brig.wrapper.runs
import eu.musicnova.musicnova.boot.MusicnovaApplicationCommandLine
import eu.musicnova.musicnova.utils.TerminalCommandDispatcher
import eu.musicnova.musicnova.utils.StringLineOutputStream
import org.fusesource.jansi.AnsiConsole
import org.jline.reader.*
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.utils.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.io.*
import java.util.regex.Pattern
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.concurrent.thread
import kotlin.system.exitProcess

@Component
class TerminalManager {


    private var terminalThread: Thread? = null

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun startTerminal(
            commandDispatcher: TerminalCommandDispatcher,
            completer: Completer,
            highlighter: Highlighter
    ) = ApplicationRunner {
        if (MusicnovaApplicationCommandLine.interactive) {
            val terminal = TerminalBuilder.builder()
                    .system(true)
                    .dumb(true)
                    .streams(FileInputStream(FileDescriptor.`in`), FileOutputStream(FileDescriptor.out))
                    .jansi(true)
                    .jna(true)
                    .encoding(Charsets.UTF_8)
                    .name("Musicnova")
                    .nativeSignals(true)
                    .build()
            this.terminal = terminal

            val lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .history(DefaultHistory())
                    .highlighter(highlighter)
                    .appName("Musicnova")
                    .build()

            overrideSystemOut(lineReader)
            terminalThread = thread(name = "TerminalReader") {
                logger.debug("Terminal: $terminal")
                logger.debug("LineReader: $lineReader")
                logger.info("Starting Terminal...")
                while (true) {
                    try {
                        val line = lineReader.readLine("musicnova> ").trimEnd()
                        handleTerminalLine(line, commandDispatcher)
                    } catch (e: UserInterruptException) {
                        exitProcess(0)
                    } catch (e: EndOfFileException) {
                        exitProcess(0)
                    } catch (e: CommandSyntaxException) {
                        lineReader.printAbove(e.localizedMessage)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun handleTerminalLine(line: String, commandDispatcher: TerminalCommandDispatcher) {
        val parsed = commandDispatcher.parse(line, Unit)
        commandDispatcher.execute(parsed)
    }

    private var sysOut: PrintStream? = null
    private fun overrideSystemOut(lineReader: LineReader) {
        sysOut = System.out
        System.setOut(PrintStream(StringLineOutputStream { line ->
            if (lineReader.isReading) lineReader.printAbove(AttributedString.fromAnsi(line))
            else AnsiConsole.out.println(line)
        }, true))
    }

    private var terminal: Terminal? = null

    @PreDestroy
    private fun onTerminalStop() {
        val sOut = sysOut
        if (sOut != null) {
            System.setOut(sOut)
        }
        terminalThread?.interrupt()
        terminal?.close()
    }

    @Autowired
    lateinit var terminalCommandDispatcher: TerminalCommandDispatcher

    @PostConstruct
    fun initDefaultTerminalCommands() {
        terminalCommandDispatcher.literal("exit") {
            runs {
                exitProcess(0)
            }
        }
        terminalCommandDispatcher.literal("clear") {
            runs {
                terminal?.puts(InfoCmp.Capability.clear_screen)
            }
        }
        terminalCommandDispatcher.literal("help") {
            runs {
                val usage = terminalCommandDispatcher.getSmartUsage(terminalCommandDispatcher.root, this)
                usage.forEach { (_, line) ->
                    println(line)
                }
            }
        }
    }
}


@Component
class TerminalCompleter : Completer, Highlighter {

    @Autowired
    lateinit var dispatcher: TerminalCommandDispatcher

    override fun complete(reader: LineReader, line: ParsedLine, candidates: MutableList<Candidate>) {
        val parsed = dispatcher.parse(line.line(), Unit)
        val suggestions = dispatcher.getCompletionSuggestions(parsed, line.cursor()).get().list.map { suggestion ->
            Candidate(suggestion.text, suggestion.text, null, suggestion?.tooltip?.string, null, null, true)
        }
        candidates.addAll(suggestions)
    }

    override fun highlight(reader: LineReader, buffer: String): AttributedString {

        val splited = buffer.split(" ")
        val latestWord = splited.lastOrNull()?.takeUnless { it.isBlank() }
        val parsed = dispatcher.parse(buffer.trimEnd(), Unit)

        if (latestWord != null) {
            runCatching {
                val suggest = dispatcher.getCompletionSuggestions(parsed)
                        .get().list.firstOrNull { it.text.startsWith(latestWord, true) }?.text

                if (suggest != null) {
                    val partedSuggest = suggest.substring(latestWord.length)
                    val preWorlds = splited.subList(0, splited.size - 1).joinToString(" ")

                    return AttributedStringBuilder()
                            .append(AttributedString(preWorlds, AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN)))
                            .append(AttributedString(if (preWorlds.isEmpty()) "" else " "))
                            .append(AttributedString(latestWord, AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE)))
                            .append(AttributedString(partedSuggest, AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN)))
                            .toAttributedString()
                }
            }
        }

        return if (/*parsed.context.nodes.isEmpty() */parsed.reader.canRead())
            AttributedString(buffer, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
        else AttributedString(buffer, AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
    }

    override fun setErrorPattern(errorPattern: Pattern?) {

    }

    override fun setErrorIndex(errorIndex: Int) {

    }
}
