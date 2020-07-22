package eu.musicnova.musicnova.bot.teamspeak

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.io.Serializable

@JsonPropertyOrder("version", "platform", "hash")
data class TeamspeakClientProtocolVersion(
        val version: String,
        val platform: String,
        val hash: String
) : Serializable