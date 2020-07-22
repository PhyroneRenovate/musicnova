package eu.musicnova.musicnova.module

import io.ktor.application.Application

interface WebModule {
    operator fun Application.invoke()
}