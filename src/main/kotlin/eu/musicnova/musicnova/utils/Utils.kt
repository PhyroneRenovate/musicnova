@file:JvmName("Utils")

package eu.musicnova.musicnova.utils

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import eu.musicnova.musicnova.MusicnovaApplication
import eu.musicnova.musicnova.bot.Bot
import eu.musicnova.musicnova.bot.BotEventListener
import eu.musicnova.musicnova.bot.ChildBot
import eu.musicnova.shared.BotIdentifier
import eu.musicnova.shared.BotIdentifierJVMExt
import kotlinx.coroutines.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.core.EntityInformation
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.*
import javax.persistence.EntityManager
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend inline fun <reified T> ioTask(noinline block: () -> T) = withContext(Dispatchers.IO) { block.invoke() }
inline fun <reified T> asnycIOTask(noinline block: () -> T) {
    CoroutineScope(Dispatchers.IO).async { block.invoke() }
}

inline fun <reified T> asnycIODeferedTask(noinline block: () -> T) = CoroutineScope(Dispatchers.IO).async { block.invoke() }


inline fun <reified T> Optional<T>.getOrNull(): T? = orElseGet { null }

val isIDEA by lazy { MusicnovaApplication::class.java.classLoader.getResource("/META-INF/MANIFEST.MF") == null }

@Throws(FriendlyException::class)
suspend fun AudioPlayerManager.loadItem(identifier: String) = suspendCoroutine<AudioTrack?> { lock ->
    loadItem(identifier, object : AudioLoadResultHandler {
        override fun loadFailed(exception: FriendlyException?) {
            if (exception == null) {
                lock.resume(null)
            } else {
                lock.resumeWithException(exception)
            }
        }

        override fun trackLoaded(track: AudioTrack?) {
            lock.resume(track)
        }

        override fun noMatches() {
            lock.resume(null)
        }

        override fun playlistLoaded(playlist: AudioPlaylist?) {
            lock.resume(playlist?.selectedTrack ?: playlist?.tracks?.firstOrNull())
        }
    })
}

class BotListenerAdapter : BotEventListener {

    private val listenerList = ArrayList<WeakReference<BotEventListener>>()

    fun addListener(listener: BotEventListener) {
        listenerList.add(WeakReference(listener))
    }

    private fun MutableList<WeakReference<BotEventListener>>.cleanUP() {
        removeIf { ref -> ref.get() == null }
    }

    private fun MutableList<WeakReference<BotEventListener>>.forEachExisting(block: BotEventListener.() -> Unit) {
        cleanUP()
        forEach { ref ->
            val listener = ref.get()
            if (listener != null) {
                runCatching { block.invoke(listener) }
            }
        }
    }

    override fun onStatusChange() {
        listenerList.forEachExisting { onStatusChange() }
    }

    override fun onPlayerContinationUpdate() {
        listenerList.forEachExisting { onPlayerContinationUpdate() }
    }

    override fun onPlayerTrackUpdate() {
        listenerList.forEachExisting { onPlayerTrackUpdate() }
    }

    override fun onVolumeUpdate() {
        listenerList.forEachExisting { onVolumeUpdate() }
    }
}

@NoRepositoryBean
interface MnRepository<T, ID> : JpaRepository<T, ID> {
    fun refresh(entity: T)
    fun merge(entity: T): T
}


class MnRepositoryImpl<T, ID : Serializable>(
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        entityInformation: JpaEntityInformation<T, ID>,
        val entityManager: EntityManager
) : SimpleJpaRepository<T, ID>(entityInformation, entityManager), MnRepository<T, ID> {
    @Transactional
    override fun refresh(entity: T) {
        this.entityManager.refresh(this.entityManager.merge(entity))
        // entityManager.refresh(entity)
    }

    override fun merge(entity: T): T = entityManager.merge(entity)

}

class StringLineOutputStream(private val block: (String) -> Unit) : ByteArrayOutputStream() {

    var lineCache: String = ""
    override fun flush() {
        lineCache += toString(Charsets.UTF_8)
        handleLines()
        reset()
    }

    private fun handleLines() {
        val lines = lineCache.split("\n", System.lineSeparator())
        if (lines.size > 1) {
            lines.subList(0, lines.size - 1).forEach {
                block.invoke(it)
            }
            lineCache = lines.last()
        }
    }

}

inline fun <reified T> Any?.cast(): T? = this as? T

 fun Bot.serializableIdentifier() = BotIdentifierJVMExt(this.uuid, this.cast<ChildBot>()?.childID)