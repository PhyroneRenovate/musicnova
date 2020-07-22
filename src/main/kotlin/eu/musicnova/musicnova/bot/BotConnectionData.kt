package eu.musicnova.musicnova.bot

import java.net.InetSocketAddress

interface DiscordConnectionData
interface DiscordApiConnectionData : DiscordConnectionData
interface DiscordGuildConnectionData : DiscordConnectionData
interface CombinedDiscordConnectionData : DiscordApiConnectionData, DiscordGuildConnectionData

interface TeamspeakConnectionData {
    val address: InetSocketAddress
    val timeout:Int
}