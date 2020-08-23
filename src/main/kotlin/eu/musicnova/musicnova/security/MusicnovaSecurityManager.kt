package eu.musicnova.musicnova.security

import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class MusicnovaSecurityManager : SecurityManager() {

    @PostConstruct
    fun append() {
        //TODO()
        //System.setSecurityManager(this)
    }
}