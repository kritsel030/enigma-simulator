package shared

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UtilTest {

    @Test
    fun normalize() {
        val result = Util.normalize(-1, 26)
        assertEquals(25, result)
    }

    @Test
    fun normalize2() {
        val result = Util.normalize(-27, 26)
        assertEquals(25, result)
    }
}