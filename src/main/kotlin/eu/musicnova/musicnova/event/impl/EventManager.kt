package eu.musicnova.musicnova.event.impl

import eu.musicnova.musicnova.event.api.Event
import eu.musicnova.musicnova.event.api.EventApi
import eu.musicnova.musicnova.event.api.EventListener
import eu.musicnova.musicnova.utils.Const
import org.greenrobot.eventbus.EventBus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.annotation.PreDestroy

@Component
class EventManager: EventApi {


    @Bean
    fun eventBus(
        @Qualifier(Const.BEAN_EXEC_ASYNC_EVENT) exec: ExecutorService
    ):EventBus = EventBus.builder()
        .executorService(exec)
        .eventInheritance(true).build().also { eventBus ->
            listeners.forEach { listener -> eventBus.register(listener) }
        }

    @Autowired
    lateinit var listeners: Set<EventListener>

    @Autowired
    lateinit var eventBus: EventBus

    @PreDestroy
    fun onEventBusStop() {
        listeners.forEach { listener -> eventBus.unregister(listener) }
    }


    @Bean
    @Qualifier(Const.BEAN_EXEC_ASYNC_EVENT)
    fun asyncEventExecutorService(): ExecutorService = Executors.newCachedThreadPool()

    override fun pushEvent(event: Event) {
        eventBus.post(event)
    }
}