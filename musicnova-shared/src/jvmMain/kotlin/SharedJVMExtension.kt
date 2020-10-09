@file:JvmName("SharedJVMExtKt")

package eu.musicnova.shared

import java.util.*

val UUIDTranslatable.uuid: UUID
    get() = UUID(mostSignificantBits, leastSignificantBits)

object BotIdentifierJVMExt {
    @JvmOverloads
    operator fun invoke(uuid: UUID, subID: Long? = null) = BotIdentifier(uuid.mostSignificantBits, uuid.leastSignificantBits, subID)
}

fun UUID.toUUIDIdentifier() = UUIDIdentifier(mostSignificantBits,leastSignificantBits)