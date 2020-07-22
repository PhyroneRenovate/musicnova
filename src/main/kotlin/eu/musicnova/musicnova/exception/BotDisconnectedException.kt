package eu.musicnova.musicnova.exception

import java.lang.IllegalStateException

class BotDisconnectedException : IllegalStateException("bot is not connected")