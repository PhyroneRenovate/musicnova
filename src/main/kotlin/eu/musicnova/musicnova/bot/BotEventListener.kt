package eu.musicnova.musicnova.bot

interface BotEventListener {
    fun onStatusChange()
    fun onPlayerContinationUpdate()
    fun onPlayerTrackUpdate()
    fun onVolumeUpdate()
}