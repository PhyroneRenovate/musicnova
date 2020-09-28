@file:JvmName("MusicNovaBoot")

package eu.musicnova.musicnova.boot

import com.github.lalyos.jfiglet.FigletFont
import com.github.lalyos.jfiglet.JFiglet
import com.jcabi.manifests.Manifests
import com.sun.tools.attach.VirtualMachine
import eu.musicnova.musicnova.MusicnovaApplication
import eu.musicnova.musicnova.beans.present.InitCommandLineBeanPresent
import eu.musicnova.musicnova.utils.Const
import eu.musicnova.musicnova.utils.isIDEA
import eu.musicnova.shared.SharedConst
import org.fusesource.jansi.AnsiConsole
import org.slf4j.LoggerFactory
import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.boot.SpringApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import oshi.software.os.OSProcess
import oshi.software.os.OperatingSystem
import picocli.CommandLine
import java.lang.instrument.Instrumentation
import java.util.*
import kotlin.system.exitProcess


@CommandLine.Command(
    name = "MusicNova",
    mixinStandardHelpOptions = true,
    version = ["""Version: ${SharedConst.VERSION}"""]

)
class MusicnovaApplicationCommandLine : Runnable {


    @CommandLine.Option(names = ["--debug"], description = ["enables the debug log"])
    var debug = false

    @CommandLine.Option(names = [Const.ROOT_BYPASS_FLAG])
    var ignoreRoot = false

    @CommandLine.Option(names = ["-c", "--config"])
    var configFileName = "config.yml"

    @CommandLine.Option(names = ["-i", "--interactive"])
    var interactive: Boolean = false


    private fun checkRoot(process: OSProcess) {
        if (!ignoreRoot && isRoot(process)) {
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

