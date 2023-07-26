package bombe.components

import enigma.components.Rotor
import shared.RotorType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DrumTest {

    @Test
    fun constructor_I_a() {
        val rotor = Rotor(RotorType.I, 'Y', 'Z')
        val drum = Drum(DrumType.I, 'Z')
        assertEquals(rotor.encryptRightToLeft(10), drum.encryptRightToLeft(10))
    }

    @Test
    fun constructor_I_b() {
        val rotor = Rotor(RotorType.I, 'Z', 'A')
        val drum = Drum(DrumType.I, 'Z')
        assertEquals(rotor.encryptRightToLeft(10), drum.encryptRightToLeft(10))
    }

    @Test
    fun constructor_V_a() {
        val rotor = Rotor(RotorType.V, 'W', 'Z')
        val drum = Drum(DrumType.V, 'Z')
        assertEquals(rotor.encryptRightToLeft(10), drum.encryptRightToLeft(10))
    }

    @Test
    fun constructor_V_b() {
        val rotor = Rotor(RotorType.V, 'Z', 'C')
        val drum = Drum(DrumType.V, 'Z')
        assertEquals(rotor.encryptRightToLeft(10), drum.encryptRightToLeft(10))
    }

}