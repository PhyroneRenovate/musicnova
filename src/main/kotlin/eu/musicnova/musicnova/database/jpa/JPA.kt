package eu.musicnova.musicnova.database.jpa

import com.github.manevolent.ts3j.identity.LocalIdentity
import com.github.manevolent.ts3j.util.Ts3Crypt
import eu.musicnova.musicnova.bot.teamspeak.TeamspeakBotManager
import eu.musicnova.musicnova.bot.teamspeak.TeamspeakClientProtocolVersion
import eu.musicnova.musicnova.utils.MnRepository
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
) : PersistentBotData() {


}

@Entity
@Table(name = "teamspeak_identity")
data class PersistentTeamspeakIdentity(
        @Lob var asn: ByteArray,
        @Column("asn_offset")
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
data class PersistentWebUserData(
        var username: String,
        var password: ByteArray,
        val salt: ByteArray,
        @Id var userID: UUID
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

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + password.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + userID.hashCode()
        return result
    }

    @OneToMany(mappedBy = "webUser")
    var sessions: Set<PersistentWebUserSessionData> = setOf()

}

@Entity
data class PersistentWebUserSessionData(
        @Column(length = 512) @Id val sessionToken: String,
        @ManyToOne var webUser: PersistentWebUserData
)

interface WebUserSessionDatabase : MnRepository<PersistentWebUserData, String>
interface WebUserDatabase : MnRepository<PersistentWebUserData, UUID>

interface AudioContollerDatabase : MnRepository<PersistentAudioControllerData, UUID>

interface BotDatabase : MnRepository<PersistentBotData, UUID>
interface TeamspeakBotDatabase : MnRepository<PersistentTeamspeakBotData, UUID>
interface TeamspeakIdentiyDatabase : MnRepository<PersistentTeamspeakIdentity, UUID> {
    fun findByNickname(nickname: String): PersistentTeamspeakIdentity?

}