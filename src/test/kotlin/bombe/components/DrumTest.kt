package bombe.components

import enigma.components.Rotor
import shared.RotorType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DrumTest {

    @Test
    fun constructor_Z_implicit() {
        val rotor = Rotor(RotorType.I, 'Y', 'Z')
        val drum = Drum(DrumType.I)
        assertEquals(rotor.encryptRightToLeft(10), drum.encryptRightToLeft(10))
    }

    @Test
    fun constructor_Z_explicit() {
        val rotor = Rotor(RotorType.I, 'Y', 'Z')
        val drum = Drum(DrumType.I, 'Z')
        assertEquals(rotor.encryptRightToLeft(10), drum.encryptRightToLeft(10))
    }

    @Test
    fun constructor_K() {
        val rotor = Rotor(RotorType.I, 'K', 'L')
        val drum = Drum(DrumType.I, 'L')
        assertEquals(rotor.encryptRightToLeft(10), drum.encryptRightToLeft(10))
    }
}