@file:Suppress("")
package eu.musicnova.musicnova.database.dao

import com.github.manevolent.ts3j.identity.LocalIdentity
import com.github.manevolent.ts3j.util.Ts3Crypt
import com.google.common.hash.Hashing
import eu.musicnova.musicnova.bot.teamspeak.TeamspeakBotManager
import eu.musicnova.musicnova.bot.teamspeak.TeamspeakClientProtocolVersion
import eu.musicnova.musicnova.utils.MnRepository
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.springframework.data.jpa.repository.Modifying
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "bot")
@Inheritance(strategy = InheritanceType.JOINED)
open class PersistentBotData(
        open var name: String? = null,
        open var connected: Boolean = true
) {
    @Id
    open var id: UUID = UUID.randomUUID()

}

@Entity
@Table(name = "bot_teamspeak")
data class PersistentTeamspeakBotData(
        var host: String = "",
        var port: Int? = null,
        var hostResolve: TeamspeakBotManager.TeamspeakResoveMode = TeamspeakBotManager.TeamspeakResoveMode.NONE,
        var nickname: String = "TeamspeakBot",
        var timeout: Long = 10000,
        var serverPassword: String? = null,
        var channel: Int? = null,
        var channelPassword: String? = null,
        var hwid: String? = null,
        var serverVersion: TeamspeakClientProtocolVersion? = null,
        @ManyToOne(optional = true)
        var identity: PersistentTeamspeakIdentity? = null
) : PersistentBotData()

@Entity
@Table(name = "teamspeak_identity")
data class PersistentTeamspeakIdentity(
        @Lob var asn: ByteArray,
        @Column(name = "asn_offset")
        var offset: Long,
        var nickname: String? = null,
        @Id var uuid: UUID = UUID.randomUUID()
) {

    constructor(identity: LocalIdentity, nickname: String? = null) : this(identity.toASN(), identity.keyOffset, nickname)

    @OneToMany(mappedBy = "identity")
    var bots: Set<PersistentTeamspeakBotData> = setOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersistentTeamspeakIdentity

        if (!asn.contentEquals(other.asn)) return false
        if (offset != other.offset) return false
        if (nickname != other.nickname) return false
        if (uuid != other.uuid) return false

        return true
    }

    var identity: LocalIdentity
        get() = Ts3Crypt.loadIdentityFromAsn(asn).also { localIdentity ->
            localIdentity.keyOffset = offset
            localIdentity.lastCheckedKeyOffset = offset
        }
        set(value) {
            asn = value.toASN()
            offset = value.keyOffset
        }

    override fun hashCode(): Int {
        var result = asn.contentHashCode()
        result = 31 * result + offset.hashCode()
        result = 31 * result + (nickname?.hashCode() ?: 0)
        result = 31 * result + uuid.hashCode()
        return result
    }


}

@Entity
@Table(name = "audio_controller")
data class PersistentAudioControllerData(
        @Id val uuid: UUID,
        var volume: Int
)

data class PeristentAudioTrackData(
        var name: String
)

@Entity
@Table(name = "web_user")
data class PersistentWebUserData(
        @Column(length = 255, unique = true)
        var username: String,
        @Column(length = 64)
        var password: ByteArray,
        @Column(length = 16, unique = true)
        var salt: ByteArray,
        @Id var userID: UUID = UUID.randomUUID()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersistentWebUserData

        if (username != other.username) return false
        if (!password.contentEquals(other.password)) return false
        if (!salt.contentEquals(other.salt)) return false
        if (userID != other.userID) return false

        return true
    }

    fun checkPassword(password: String) = hashPassword(password, salt).contentEquals(this.password)

    fun setPassword(newPassword: String) {
        val newSalt = newSalt()
        val newPasswordBytes = hashPassword(newPassword, newSalt)
        this.salt = newSalt
        this.password = newPasswordBytes
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + password.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + userID.hashCode()
        return result
    }

    @OneToMany(mappedBy = "webUser")
    var sessions: Set<PersistentWebUserSessionData> = setOf()

    companion object Static {
        private val hashing = Hashing.sha512()
        private val random = SecureRandom.getInstanceStrong()
        operator fun invoke(username: String, password: String): PersistentWebUserData {
            val newSalt = newSalt()
            val hashedPassword = hashPassword(password, newSalt)
            return PersistentWebUserData(username, hashedPassword, newSalt)
        }

        private fun hashPassword(password: String, salt: ByteArray): ByteArray = hashing.newHasher()
                .putString(password, Charsets.UTF_8).putBytes(salt)
                .hash().asBytes()

        private fun newSalt(): ByteArray {
            val newSalt = ByteArray(16)
            random.nextBytes(newSalt)
            return newSalt
        }

    }
}

@Entity
@Table(name = "web_user_session")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
data class PersistentWebUserSessionData(
    @Column(length = 512) @Id val sessionToken: String,
    @ManyToOne var webUser: PersistentWebUserData,
    var lastSeenDate: LocalDateTime,
    var loginDate: LocalDateTime
)

@Entity
@Table(name = "audio_track")
@Inheritance(strategy = InheritanceType.JOINED)
open class PersistentAudioTrackData(
        open val title: String,
        @Id open val uuid: UUID = UUID.randomUUID()
)

@Entity
@Table(name = "audio_track_local")
open class PersistentLocalAudioTrackData(
        title: String,
        open val file: String
) : PersistentAudioTrackData(title)

@Entity
@Table(name = "audio_track_remote")
open class PersistentRemoteAudioTrackData(
        title: String,
        open val url: String
) : PersistentAudioTrackData(title)


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

interface WebUserDatabase : MnRepository<PersistentWebUserData, UUID> {
    fun findByUsername(username: String): PersistentWebUserData?
}

interface AudioContollerDatabase : MnRepository<PersistentAudioControllerData, UUID>

interface BotDatabase : MnRepository<PersistentBotData, UUID>
interface TeamspeakBotDatabase : MnRepository<PersistentTeamspeakBotData, UUID>
interface TeamspeakIdentiyDatabase : MnRepository<PersistentTeamspeakIdentity, UUID> {
    fun findByNickname(nickname: String): PersistentTeamspeakIdentity?

}