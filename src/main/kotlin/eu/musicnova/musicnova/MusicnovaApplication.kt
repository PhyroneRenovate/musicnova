@file:JvmName("MusicNovaBoot")

package eu.musicnova.musicnova

import eu.musicnova.musicnova.event.AppStartedEvent
import eu.musicnova.musicnova.event.api.EventHandler
import eu.musicnova.musicnova.event.api.EventListener
import eu.musicnova.musicnova.utils.MnRepositoryImpl
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = MnRepositoryImpl::class)
@EnableScheduling
@EnableAsync
@EnableJdbcAuditing
class MusicnovaApplication : EventListener {

    @EventHandler()
    fun onStarted(appStartedEvent: AppStartedEvent){

    }
}

