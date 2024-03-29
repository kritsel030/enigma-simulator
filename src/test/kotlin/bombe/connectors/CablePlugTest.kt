package bombe.connectors

import bombe.Bombe
import enigma.components.ReflectorType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CablePlugTest {

    @Test
    fun getOppositePlug() {
        val bombe = Bombe(3, 1, 1, 1, 3, 1, ReflectorType.B)

        val cable = bombe.createCable()
        assertEquals(cable.rightPlug, cable.leftPlug.getOppositePlug())
        assertEquals(cable.leftPlug, cable.rightPlug.getOppositePlug())
    }
}
