package enigma

import enigma.components.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EnigmaTest {

    @Test
    fun encrypt() {
        val reflector = Reflector(ReflectorType.B)
        var leftRotor = Rotor(RotorType.III, 'B', 26)
        var middleRotor = Rotor(RotorType.II, 'C', 25)
        var rightRotor = Rotor(RotorType.I, 'D', 24)
        val plugboard = Plugboard("AB-CD-EF-GH-IJ-KL")
        val enigma = Enigma(reflector, listOf(leftRotor, middleRotor, rightRotor), plugboard)

        val input = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var output = enigma.encrypt(input)

        assertEquals("UYUEXLJBMFIRTDJXMUYUPWOFMD", output)
    }

    @Test
    fun stepRightRotor() {
        val reflector = Reflector(ReflectorType.B)
        // not a single rotor in turnover position
        var leftRotor = Rotor(RotorType.III, 'A', 1)
        var middleRotor = Rotor(RotorType.II, 'A', 1)
        var rightRotor = Rotor(RotorType.I, 'A', 1)
        val plugboard = Plugboard("AB-CD-EF-GH-IJ-KL")
        val enigma = Enigma(reflector, listOf(leftRotor, middleRotor, rightRotor), plugboard)

        enigma.stepRotors()

        // only the right rotor should have a different position
        assertEquals(leftRotor.startPosition, leftRotor.currentPosition)
        assertEquals(leftRotor.startPosition, middleRotor.currentPosition)
        assertEquals(leftRotor.startPosition + 1, rightRotor.currentPosition)
    }

    @Test
    fun stepRightAndMiddleRotor() {
        val reflector = Reflector(ReflectorType.B)
        var leftRotor = Rotor(RotorType.III, 'A', 1)
        var middleRotor = Rotor(RotorType.II, 'A', 1)
        // right rotor in turnover position
        var rightRotor = Rotor(RotorType.I, 'Q', 1)
        val plugboard = Plugboard("AB-CD-EF-GH-IJ-KL")
        val enigma = Enigma(reflector, listOf(leftRotor, middleRotor, rightRotor), plugboard)

        enigma.stepRotors()

        // both the right and middle rotor should have a different position
        assertEquals(leftRotor.startPosition, leftRotor.currentPosition)
        assertEquals(middleRotor.startPosition + 1, middleRotor.currentPosition)
        assertEquals(rightRotor.startPosition + 1, rightRotor.currentPosition)
    }

    @Test
    fun stepAllRotors() {
        val reflector = Reflector(ReflectorType.B)
        var leftRotor = Rotor(RotorType.III, 'A', 1)
        // middle rotor in turnover position
        var middleRotor = Rotor(RotorType.II, 'E', 1)
        // right rotor in turnover position
        var rightRotor = Rotor(RotorType.I, 'Q', 1)
        val plugboard = Plugboard("AB-CD-EF-GH-IJ-KL")
        val enigma = Enigma(reflector, listOf(leftRotor, middleRotor, rightRotor), plugboard)

        enigma.stepRotors()

        // right and left rotor should have taken a single step, middle rotor two steps
        assertEquals(leftRotor.startPosition + 1, leftRotor.currentPosition)
        assertEquals(middleRotor.startPosition + 2, middleRotor.currentPosition)
        assertEquals(rightRotor.startPosition + 1, rightRotor.currentPosition)
    }

    @Test
    fun reset() {
        val reflector = Reflector(ReflectorType.B)
        var leftRotor = Rotor(RotorType.III, 'A', 1)
        // middle rotor in turnover position
        var middleRotor = Rotor(RotorType.II, 'E', 1)
        // right rotor in turnover position
        var rightRotor = Rotor(RotorType.I, 'Q', 1)
        val plugboard = Plugboard("AB-CD-EF-GH-IJ-KL")
        val enigma = Enigma(reflector, listOf(leftRotor, middleRotor, rightRotor), plugboard)

        // encrypt something,
        // because of the used start position, all rotors will have stepped
        // (this is verified in another test method, so we do not need to assert this here)
        enigma.encrypt("HELLO")

        // the reset should cause all rotors to have returned to their start positions
        enigma.resetRotors()

        assertEquals(leftRotor.startPosition, leftRotor.currentPosition)
        assertEquals(middleRotor.startPosition, middleRotor.currentPosition)
        assertEquals(rightRotor.startPosition, rightRotor.currentPosition)
    }

}