@file:Suppress("JpaDataSourceORMInspection")

package eu.musicnova.musicnova.database.dao

import com.github.manevolent.ts3j.identity.LocalIdentity
import com.github.manevolent.ts3j.util.Ts3Crypt
import com.google.common.hash.Hashing
import eu.musicnova.musicnova.bot.teamspeak.TeamspeakBotManager
import eu.musicnova.musicnova.bot.teamspeak.TeamspeakClientProtocolVersion
import eu.musicnova.musicnova.permission.PermissionIdentificationStrategy
import eu.musicnova.musicnova.permission.PersistentPermissionEntryData
import eu.musicnova.musicnova.utils.MnRepository
import lombok.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.springframework.data.jpa.repository.Modifying
import java.io.Serializable
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList


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
    @Lob
    var host: String = "",
    @Column(nullable = true)
    var port: Int? = null,
    @Enumerated(EnumType.ORDINAL)
    var hostResolve: TeamspeakBotManager.TeamspeakResoveMode = TeamspeakBotManager.TeamspeakResoveMode.NONE,
    @Column(nullable = false, length = 16)
    var nickname: String = "TeamspeakBot",
    var timeout: Long = 10000,
    @Column(nullable = true) @Lob
    var serverPassword: String? = null,
    @Column(nullable = true)
    var channel: Int? = null,
    @Column(nullable = true) @Lob
    var channelPassword: String? = null,
    @Column(nullable = true) @Lob
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
    @Column(nullable = true)
    var nickname: String? = null,
    @Id var uuid: UUID = UUID.randomUUID()
) {

    constructor(identity: LocalIdentity, nickname: String? = null) : this(
        identity.toASN(),
        identity.keyOffset,
        nickname
    )

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
@DiscriminatorColumn(name = "type", length = 16)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
open class PersistentAudioTrackData(
    open val title: String,
    @Id open val uuid: UUID = UUID.randomUUID()
) {

    @ManyToMany(mappedBy = "tracks")
    open var playlists: List<PersistentPlaylistData> = listOf()
}

@Entity
@DiscriminatorValue("LOCAL")
open class PersistentLocalAudioTrackData(
    title: String,
    open val file: String,
    @Column(nullable = true)
    open val hash: ByteArray?
) : PersistentAudioTrackData(title)

@Entity
@DiscriminatorValue("REMOTE")
open class PersistentRemoteAudioTrackData(
    title: String,
    open val url: String
) : PersistentAudioTrackData(title)


@Entity
@Table(name = "playlist")
data class PersistentPlaylistData(
    val title: String,
    @ManyToMany()
    @JoinTable(
        name = "playlist_track_ref",
        joinColumns = [JoinColumn(name = "playlist")],
        inverseJoinColumns = [JoinColumn(name = "track")]
    )
    val tracks: List<PersistentAudioTrackData>,
    @Id val id: UUID = UUID.randomUUID()
)

@Entity
@Table(name = "permission_entity")
@DiscriminatorColumn(name = "type", length = 16)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
open class PersistentPermissionEntityData(
    @Id open val id: UUID = UUID.randomUUID()
) {


    @CollectionTable(
        name = "permission_entity_permisions",
        joinColumns = [JoinColumn(name = "entity")],
    )
    @Lob
    @Column(name = "permission")
    @ElementCollection
    @Suppress("JpaAttributeTypeInspection")
    open var permissions: List<PersistentPermissionEntryData> = listOf()

    @ManyToMany(targetEntity = PersistentPermissionGroupEntityData::class)
    @JoinTable(
        name = "permission_group_parent_child_ref",
        joinColumns = [JoinColumn(name = "parent")],
        inverseJoinColumns = [JoinColumn(name = "child")]
    )
    open lateinit var parents: List<PersistentPermissionGroupEntityData>
}

@Entity
@DiscriminatorValue("CLIENT")
open class PersistentPermissionClientEntityData(
    open val platform: String,
    open val identifier: String
) : PersistentPermissionEntityData()

@Entity
@DiscriminatorValue("GROUP")
open class PersistentPermissionGroupEntityData(
    @Column(name = "group_name", unique = true)
    open val name: String,
) : PersistentPermissionEntityData() {

    @ManyToMany(mappedBy = "parents", targetEntity = PersistentPermissionEntityData::class)
    open lateinit var children: List<PersistentPermissionEntityData>
}

