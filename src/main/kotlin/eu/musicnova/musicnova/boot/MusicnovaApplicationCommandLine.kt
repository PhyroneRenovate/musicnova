@file:JvmName("MusicNovaBoot")

package eu.musicnova.musicnova.boot

import com.github.lalyos.jfiglet.FigletFont
import com.jcabi.manifests.Manifests
import eu.musicnova.musicnova.MusicnovaApplication
import eu.musicnova.musicnova.utils.isIDEA
import eu.musicnova.shared.SharedConst
import org.fusesource.jansi.AnsiConsole
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBean
import org.springframework.boot.SpringApplication
import org.springframework.boot.runApplication
import oshi.software.os.OperatingSystem
import picocli.CommandLine
import kotlin.system.exitProcess

@CommandLine.Command(
        name = "MusicNova",
        mixinStandardHelpOptions = true,
        version = ["""Version: ${SharedConst.VERSION}"""]

)
object MusicnovaApplicationCommandLine : Runnable {
    private const val ROOT_BYPASS_FLAG = "--imRunningAsRootItIsEvilAndIKnowIt"

    @CommandLine.Option(names = ["--debug"], description = ["enables the debug log"])
    var debug = false

    @CommandLine.Option(names = [ROOT_BYPASS_FLAG])
    var ignoreRoot = false

    @CommandLine.Option(names = ["-c", "--config"])
    var configFileName = "config.yml"

    @CommandLine.Option(names = ["-i", "--interactive"])
    var interactive: Boolean = false


    private fun checkRoot(os: OperatingSystem) {
        if (!ignoreRoot && isRoot(os)) {
            val rootCheckLogger = LoggerFactory.getLogger("RootCheck")
            rootCheckLogger.error("We Detected you running MusicNova as root user this is forbidden by Default\n" + FigletFont.convertOneLine("Root IS Evil"))
            rootCheckLogger.warn("""Event if its not recommended you can bypass the RootCheck with "$ROOT_BYPASS_FLAG"""")
            exitProcess(1)
        }
    }

    private fun isRoot(os: OperatingSystem) = os.getProcess(os.processId)?.userID?.toIntOrNull() == 0

    override fun run() {
        if (debug) {
            setDebugFlags()
        }
        if (isIDEA)
            AnsiConsole.systemUninstall()

        val springAppContext = runApplication<MusicnovaApplication>()
        checkRoot(springAppContext.getBean())

    }

    private fun setDebugFlags() {
        System.setProperty("logging.level.org.springframework", "DEBUG")
        System.setProperty("spring.jpa.show-sql", "true")
    }

}

