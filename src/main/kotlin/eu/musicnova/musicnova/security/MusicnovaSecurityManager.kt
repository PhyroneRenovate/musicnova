package eu.musicnova.musicnova.security

import org.python.util.PythonInterpreter
import org.springframework.stereotype.Component
import java.security.Permission
import javax.annotation.PostConstruct

@Component
class MusicnovaSecurityManager : SecurityManager() {

    @PostConstruct
    fun append() {
        //TODO()
        //System.setSecurityManager(this)
    }
}