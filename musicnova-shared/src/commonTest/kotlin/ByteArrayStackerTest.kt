import eu.musicnova.shared.ByteArrayStacker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue


class ByteArrayStackerTest {

    @Test
    fun testStacker() {
        val byteArrayList = listOf(
            byteArrayOf(42, 123, 121, 122),
            byteArrayOf(31, 22, 11, 33, -128), ByteArray(500)
        )
        val stackedBytes = ByteArrayStacker.stackBytes(byteArrayList)
        println(stackedBytes.joinToString(","))
        val reUnStacked = ByteArrayStacker.unStackBytes(stackedBytes)
        assertEquals(reUnStacked.size, byteArrayList.size)
        byteArrayList.forEachIndexed { index, bytes ->
            assertTrue(byteArrayList[index].contentEquals(bytes))
        }
    }
}