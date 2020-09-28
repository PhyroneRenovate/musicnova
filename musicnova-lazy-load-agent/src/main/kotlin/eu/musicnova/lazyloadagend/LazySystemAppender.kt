package eu.musicnova.lazyloadagend

import java.io.File
import java.util.concurrent.LinkedBlockingQueue

object LazySystemAppender {
    val agentQueue = LinkedBlockingQueue<File>()

    @JvmStatic
    @JvmName("appendJar")
    fun appendJar(jarFile: File) {
        agentQueue.add(jarFile)
    }
}