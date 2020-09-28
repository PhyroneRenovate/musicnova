package eu.musicnova.musicnova.event.api

import eu.musicnova.musicnova.event.api.Event

interface EventApi {
    fun pushEvent(event: Event)
}