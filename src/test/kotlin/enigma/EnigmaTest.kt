package enigma

import enigma.components.*
import org.junit.jupiter.api.Test
import shared.RotorType
import kotlin.test.assertEquals

class EnigmaTest {

    @Test
    fun encrypt() {
        val reflector = Reflector(ReflectorType.B)
        val leftRotor = Rotor(RotorType.III, 'B', 'Z')
        val middleRotor = Rotor(RotorType.II, 'C', 'Y' )
        val rightRotor = Rotor(RotorType.I, 'D', 'X')
        val plugboard = Plugboard("AB-CD-EF-GH-IJ-KL", 26)
        val enigma = Enigma(reflector, leftRotor, middleRotor, rightRotor, plugboard)

        val input = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val output = enigma.encryptMessage(input)

        // output confirmed with a test via an on-line enigma simulator (https://cryptii.com/pipes/enigma-machine)
        assertEquals("UYUEXLJBMFIRTDJXMUYUPWOFMD", output)
    }

    /**
     * Example from https://www.lysator.liu.se/~koma/turingbombe/TuringBombeTutorial.pdf
     */
    @Test
    fun encrypt_linkopingBombeSimulatorExample() {
        val leftRotor = Rotor(RotorType.II, 'Y', 'D')
        val middleRotor = Rotor(RotorType.V, 'W', 'K')
        val rightRotor = Rotor(RotorType.III, 'Y', 'X')
        val reflector = Reflector(ReflectorType.B)
        val plugboard = Plugboard("UF-ET-GQ-AD-VN-HM-ZP-LJ-IK-XO", 26)
        val enigma = Enigma(reflector, leftRotor, middleRotor, rightRotor, plugboard)

        val input = "WETTERVORHERSAGE"
        val output = enigma.encryptMessage(input)

        assertEquals("SNMKGGSTZZUGARLV", output)
    }

    @Test
    fun stepRightRotor() {
        val reflector = Reflector(ReflectorType.B)
        // not a single rotor in turnover position
        val leftRotor = Rotor(RotorType.III, 'A', 'A')
        val middleRotor = Rotor(RotorType.II, 'A', 'A')
        val rightRotor = Rotor(RotorType.I, 'A', 'A')
        val plugboard = Plugboard("AB-CD-EF-GH-IJ-KL", 26)
        val enigma = Enigma(reflector, leftRotor, middleRotor, rightRotor, plugboard)

        enigma.stepRotors()

        // only the right rotor should have a different position
        assertEquals(leftRotor.startRingOrientation(), leftRotor.currentRingOrientation())
        assertEquals(middleRotor.startRingOrientation(), middleRotor.currentRingOrientation())
        assertEquals(rightRotor.startRingOrientation() + 1, rightRotor.currentRingOrientation())
    }

    @Test
    fun stepRotors_rightOnly() {
        val reflector = Reflector(ReflectorType.B)
        var leftRotor = Rotor(RotorType.III, 'A', 'A')
        var middleRotor = Rotor(RotorType.II, 'A', 'A')
        // right rotor in turnover position
        var rightRotor = Rotor(RotorType.I, 'Q', 'A')
        val plugboard = Plugboard("AB-CD-EF-GH-IJ-KL", 26)
        val enigma = Enigma(reflector, leftRotor, middleRotor, rightRotor, plugboard)

        enigma.stepRotors()

        // both the right and middle rotor should have a different position
        assertEquals(leftRotor.startRingOrientation(), leftRotor.currentRingOrientation())
        assertEquals(middleRotor.startRingOrientation() + 1, middleRotor.currentRingOrientation())
        assertEquals(rightRotor.startRingOrientation() + 1, rightRotor.currentRingOrientation())
    }

    @Test
    fun stepRotors_all() {
        val reflector = Reflector(ReflectorType.B)
        var leftRotor = Rotor(RotorType.III, 'A', 'A')
        // middle rotor in turnover position
        var middleRotor = Rotor(RotorType.II, 'E', 'A')
        // right rotor in turnover position
        var rightRotor = Rotor(RotorType.I, 'Q', 'A')
        val plugboard = Plugboard("AB-CD-EF-GH-IJ-KL", 26)
        val enigma = Enigma(reflector, leftRotor, middleRotor, rightRotor, plugboard)

        enigma.stepRotors()

        // right and left rotor should have taken a single step, middle rotor two steps
        assertEquals(leftRotor.startRingOrientation() + 1, leftRotor.currentRingOrientation())
        assertEquals(middleRotor.startRingOrientation() + 2, middleRotor.currentRingOrientation())
        assertEquals(rightRotor.startRingOrientation() + 1, rightRotor.currentRingOrientation())
    }

    @Test
    fun reset() {
        val reflector = Reflector(ReflectorType.B)
        var leftRotor = Rotor(RotorType.III, 'A', 'A')
        // middle rotor in turnover position
        var middleRotor = Rotor(RotorType.II, 'E', 'A')
        // right rotor in turnover position
        var rightRotor = Rotor(RotorType.I, 'Q', 'A')
        val plugboard = Plugboard("AB-CD-EF-GH-IJ-KL", 26)
        val enigma = Enigma(reflector, leftRotor, middleRotor, rightRotor, plugboard)

        // encrypt something,
        // because of the used start position, all rotors will have stepped
        // (this is verified in another test method, so we do not need to assert this here)
        enigma.encryptMessage("HELLO")

        // the reset should cause all rotors to have returned to their start positions
        enigma.resetRotors()

        assertEquals(leftRotor.startRingOrientation(), leftRotor.currentRingOrientation())
        assertEquals(middleRotor.startRingOrientation(), middleRotor.currentRingOrientation())
        assertEquals(rightRotor.startRingOrientation(), rightRotor.currentRingOrientation())
    }

}