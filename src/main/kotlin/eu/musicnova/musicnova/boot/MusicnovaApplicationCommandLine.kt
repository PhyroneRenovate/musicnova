@file:JvmName("MusicNovaBoot")

package eu.musicnova.musicnova.boot

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import com.github.lalyos.jfiglet.FigletFont
import eu.musicnova.musicnova.MusicnovaApplication
import eu.musicnova.musicnova.beans.present.InitCommandLineBeanPresent
import eu.musicnova.musicnova.utils.Const
import eu.musicnova.musicnova.utils.isIDEA
import eu.musicnova.shared.SharedConst
import io.sentry.Sentry
import org.fusesource.jansi.AnsiConsole
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBean
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import oshi.software.os.OSProcess
import picocli.CommandLine
import kotlin.system.exitProcess


@CommandLine.Command(
    name = "MusicNova",
    mixinStandardHelpOptions = true,
    version = ["""Version: ${SharedConst.VERSION}"""]

)
class MusicnovaApplicationCommandLine : Runnable {


    @CommandLine.Option(
        names = ["--debug"],
        description = ["enables the debug log"]
    )
    var debug = false

    @CommandLine.Option(
        names = [Const.ROOT_BYPASS_FLAG],
        hidden = true
    )
    var allowRoot = false

    @CommandLine.Option(
        names = ["-c", "--config"]
    )
    var configFileName = "config.yml"

    @CommandLine.Option(
        names = ["-i", "--interactive"]
    )
    var interactive: Boolean = false

    @CommandLine.Option(
        names = [Const.DISABLE_SENTRY_FLAG]
    )
    var disableSentry = false


    private fun checkRoot(process: OSProcess) {
        if (!allowRoot && isRoot(process)) {
            val rootCheckLogger = LoggerFactory.getLogger("RootCheck")
            rootCheckLogger.error(
                "We Detected you running MusicNova as root user. this is forbidden by Default\n" + FigletFont.convertOneLine(
                    "Root IS Evil"
                )
            )
            rootCheckLogger.warn("""Event if its not recommended you can bypass the RootCheck with "${Const.ROOT_BYPASS_FLAG}"""")
            exitProcess(1)
        }
    }

    private fun isRoot(process: OSProcess) = process.userID?.toIntOrNull() == 0

    override fun run() {
        if (debug) {
            setDebugFlags()
        }
        if (!disableSentry) {
            println("MusicNova sends error data automatically to sentry.phyrone.de")
            println("if you disagree with that start the bot with '${Const.DISABLE_SENTRY_FLAG}' flag")
            Sentry.init { options ->
                options.dsn = "https://df5feaac76a6489ea3eb116a2dd37191@sentry.phyrone.de/2"
                options.isEnableUncaughtExceptionHandler = true
            }
        } else {
            (LoggerFactory.getILoggerFactory() as? LoggerContext)
                ?.getLogger(Logger.ROOT_LOGGER_NAME)
                ?.detachAppender("SENTRY")
        }

        if (interactive)
            System.setProperty(Const.INTERACTIVE_PROPERTY_FULL_NAME, "true")


        if (isIDEA) AnsiConsole.systemUninstall()

        val app = SpringApplication(MusicnovaApplication::class.java)

        app.setHeadless(true)
        app.setLogStartupInfo(true)
        app.setRegisterShutdownHook(true)
        app.addInitializers(ApplicationContextInitializer<ConfigurableApplicationContext> { context ->
            context.beanFactory.registerSingleton(Const.BEAN_NAME_CLI_PRESENT, InitCommandLineBeanPresent(this))
        })

        val springAppContext = app.run()

        checkRoot(springAppContext.getBean())

    }

    private fun setDebugFlags() {
        System.setProperty("logging.level.org.springframework", "DEBUG")
        System.setProperty("spring.jpa.show-sql", "true")
    }

}

