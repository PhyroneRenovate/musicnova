package eu.musicnova.musicnova.bot

interface BotPlayerEventListner {
    fun onPlayerContinationUpdate()
    fun onPlayerTrackUpdate()
    fun onVolumeUpdate()
}

interface BotEventListener : BotPlayerEventListner {
    fun onStatusChange()

}