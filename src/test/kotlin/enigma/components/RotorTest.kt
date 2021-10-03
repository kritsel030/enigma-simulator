package enigma.components

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Expected results determine with
 * http://people.physik.hu-berlin.de/~palloks/js/enigma/enigma-u_v26_en.html
 */
class RotorTest {

    //********************************************************************
    // encryption

    @Test
    fun encryptRightToLeftBasic() {
        val rotor = Rotor(RotorType.I, 'A', 1)
        val output = rotor.encryptRightToLeft('B')

        assertEquals('K', output, "B expected to be translated to K")
    }

    @Test
    fun encryptLeftToRightBasic() {
        val rotor = Rotor(RotorType.I, 'A', 1)
        val output = rotor.encryptLeftToRight('K')

        assertEquals('B', output, "K expected to be translated to B")
    }

    @Test
    fun encryptRightToLeft_NonBasicStartPosition() {
        val rotor = Rotor(RotorType.I, 'S', 1)
        val output = rotor.encryptRightToLeft('B')

        assertEquals('X', output, "B expected to be translated to X")
    }

    @Test
    fun encryptLeftToRight_NonBasicStartPosition() {
        val rotor = Rotor(RotorType.I, 'S', 1)
        val output = rotor.encryptLeftToRight('X')

        assertEquals('B', output, "X expected to be translated to B")
    }

    @Test
    fun encryptRightToLeft_NonBasicRingSetting() {
        val rotor = Rotor(RotorType.I, 'A', 6)
        val output = rotor.encryptRightToLeft('B')

        assertEquals('G', output, "B expected to be translated to G")
    }

    @Test
    fun encryptLeftToRight_NonBasicRingSetting() {
        val rotor = Rotor(RotorType.I, 'A', 6)
        val output = rotor.encryptLeftToRight('G')

        assertEquals('B', output, "G expected to be translated to B")
    }

    @Test
    fun encryptRightToLeft_NonBasicStartPosition_NonBasicRingSetting() {
        val rotor = Rotor(RotorType.I, 'S', 6)
        val output = rotor.encryptRightToLeft('B')

        assertEquals('L', output, "B expected to be translated to L")
    }

    @Test
    fun encryptLeftToRight_NonBasicStartPosition_NonBasicRingSetting() {
        val rotor = Rotor(RotorType.I, 'S', 6)
        val output = rotor.encryptLeftToRight('L')

        assertEquals('B', output, "L expected to be translated to B")
    }

    //********************************************************************
    // rotor stepping

    @Test
    fun testRotorStepWithoutTurnover() {
        val rotor = Rotor(RotorType.I, 'A', 1)
        val stepNeighbour = rotor.stepRotor()

        assertEquals('B', rotor.currentPosition, "rotor position should have moved from A to B")
        assertFalse(stepNeighbour, "step of rotor type ${rotor.rotorType} from 'A' to 'B' should not move its neighbour")
    }

    @Test
    fun testRotorStepWithTurnover() {
        val rotor = Rotor(RotorType.I, 'Q', 1)
        val stepNeighbour = rotor.stepRotor()

        assertEquals('R', rotor.currentPosition, "rotor position should have moved from Q to R")
        assertTrue(stepNeighbour, "step of rotor type ${rotor.rotorType} from 'Q' to 'R' should move its neighbour")
    }

    @Test
    fun testResetRotor() {
        val rotor = Rotor(RotorType.I, 'Q', 1)
        rotor.stepRotor()
        rotor.stepRotor()

        rotor.reset()

        assertEquals('Q', rotor.currentPosition, "rotor position should have returned back to its startposition Q")
    }
}