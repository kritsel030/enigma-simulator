package enigma.components

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ReflectorTest {

    @Test
    fun encrypt() {
        val reflector = Reflector(ReflectorType.B)

        val output = reflector.encrypt('C')
        assertEquals('U', output)
    }
}