@file:JvmName("SharedJVMExtKt")

package eu.musicnova.shared

import java.nio.ByteBuffer
import java.util.*

val UUIDIdentifier.uuid: UUID
    get() = uuidFromBytes(this.data)

fun UUID.toUUIDIdentifier(): UUIDIdentifier =
    UUIDIdentifier(toByteArray()) //UUIDIdentifier(mostSignificantBits,leastSignificantBits)

private fun UUID.toByteArray(): ByteArray {
    val buffer = ByteBuffer.wrap(ByteArray(16))
    buffer.putLong(mostSignificantBits)
    buffer.putLong(leastSignificantBits)
    return buffer.array()
}


private fun uuidFromBytes(byteArray: ByteArray): UUID {
    val buffer = ByteBuffer.wrap(byteArray)
    return UUID(buffer.long, buffer.long)
}