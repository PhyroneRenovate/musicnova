package eu.musicnova.shared

interface UUIDTranslatable {
    val mostSignificantBits: Long
    val leastSignificantBits: Long
}