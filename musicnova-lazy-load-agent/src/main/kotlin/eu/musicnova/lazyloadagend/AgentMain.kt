package eu.musicnova.lazyloadagend

import eu.musicnova.lazyloadagend.LazySystemAppender.agentQueue
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.lang.instrument.Instrumentation
import java.util.concurrent.LinkedBlockingQueue
import java.util.jar.JarFile
import kotlin.concurrent.thread

object AgentMain {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @JvmStatic
    fun agentmain(args: String?, instrumentation: Instrumentation) {
        logger.info("LazyAgent Started")
        thread(name = "JarAppender") {
            while (true) {
                val file = agentQueue.take()
                try {
                    logger.debug("Prepare append of file $file to classpath")
                    val jarFile = JarFile(file.absolutePath, true)
                    logger.debug("file $file matched as jarfile appending now")
                    instrumentation.appendToSystemClassLoaderSearch(jarFile)
                    logger.debug("file $file appended to classpath")
                } catch (e: Exception) {
                    logger.error("failed to append file $file to classpath", e)
                }
            }
        }
    }
}
