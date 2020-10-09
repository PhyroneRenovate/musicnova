package eu.musicnova.musicnova.database.dao

import eu.musicnova.musicnova.permission.PersistentPermissionEntryData
import eu.musicnova.musicnova.utils.MnRepository
import org.springframework.data.jpa.repository.Modifying
import java.time.LocalDateTime
import java.util.*
import javax.transaction.Transactional

interface PermissionEntityDatabase : MnRepository<PersistentPermissionEntityData, UUID>

interface PermissionClientEntityDatabase : MnRepository<PersistentPermissionClientEntityData, UUID> {
    fun getByPlatformAndIdentifier(platform: String, identifier: String): PersistentPermissionClientEntityData?
}

interface PermissionGroupEntityDatabase : MnRepository<PersistentPermissionGroupEntityData, UUID> {
    fun getByName(name: String): PersistentPermissionGroupEntityData?
    fun existsByName(name: String) : Boolean

    @Transactional
    @Modifying
    fun deleteByName(name: String) : Int
}

interface WebUserSessionDatabase : MnRepository<PersistentWebUserSessionData, String> {
    @Modifying
    fun deleteByLastSeenDateAfter(deadLine: LocalDateTime)

    @Modifying
    fun deleteByLastSeenDateBefore(deadLine: LocalDateTime)

    @Modifying
    fun deleteByLoginDateAfter(deadLine: LocalDateTime)

    @Modifying
    fun deleteByLoginDateBefore(deadLine: LocalDateTime)

    @Modifying
    fun deleteBySessionToken(token: String)
}


interface AudioTrackDatabase : MnRepository<PersistentAudioTrackData, Long> {
    fun findByTitle(name: String): PersistentAudioTrackData?
    fun getAllByOrderByTitleAsc(): List<PersistentAudioTrackData>
}

interface WebUserDatabase : MnRepository<PersistentWebUserData, UUID> {
    fun findByUsername(username: String): PersistentWebUserData?
}

interface AudioContollerDatabase : MnRepository<PersistentAudioControllerData, UUID> {

}

interface BotDatabase : MnRepository<PersistentBotData, UUID>
interface TeamspeakBotDatabase : MnRepository<PersistentTeamspeakBotData, UUID>
interface TeamspeakIdentiyDatabase : MnRepository<PersistentTeamspeakIdentity, UUID> {
    fun findByNickname(nickname: String): PersistentTeamspeakIdentity?

}