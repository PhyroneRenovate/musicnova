package eu.musicnova.musicnova.misc

import eu.musicnova.musicnova.utils.Const
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import oshi.software.os.OSProcess
import oshi.software.os.OperatingSystem
import java.io.File
import java.lang.IllegalStateException
import java.lang.reflect.Method
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions

@Component
class LazyAgentLauncher {


    @Bean
    fun startAgent(
        operatingSystem: OperatingSystem,
        process: OSProcess,
        @Qualifier(Const.BEAN_FILE_LAZY_AGENT) agentFile: File
    ): Session {
        val processID = operatingSystem.processId.toString()

        val jvmCMD = process.commandLine.split(" ").first()
        val commandList = listOf("java", jvmCMD)

        for (cmd in commandList) {
            val response = ProcessBuilder(
                cmd,
                "-cp",
                agentFile.absolutePath,
                "eu.musicnova.lazyloadagend.AgentAppender",
                processID,
                agentFile.absolutePath
            ).redirectError(ProcessBuilder.Redirect.INHERIT).redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start().waitFor()
            if (response == 0) {
                return SessionImpl()
            }
        }

        throw IllegalStateException("agent could not be appended")


    }

    @Bean
    fun agentTest(session: Session) = CommandLineRunner {
        session.appendJar(File("test.jar"))
    }

    @Bean
    @Qualifier(Const.BEAN_FILE_LAZY_AGENT)
    fun lazyAgentFile(
        @Value("classpath:/files/agent.jar")
        agentResource: Resource,
        @Qualifier(Const.BEAN_TEMP_FOLDER)
        tempFolder: File
    ) = File(tempFolder,"lazy-agent.jar").also { file ->
        val agentInStream = agentResource.inputStream
        Files.copy(agentInStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    interface Session {
        fun appendJar(file: File)
    }

    private class SessionImpl : Session {
        private val agentClass by lazy {
            Class.forName(
                "eu.musicnova.lazyloadagend.LazySystemAppender",
                true,
                ClassLoader.getSystemClassLoader()
            )
        }
        private val agentFun by lazy {
            agentClass.getDeclaredMethod("appendJar", File::class.java).also { it.isAccessible = true }
        }

        override fun appendJar(file: File) {
            agentFun.invoke(null, file)
        }
    }
}