package enigma.components

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PlugboardTest {

    @Test
    fun encryptUnmappedLetter() {
        val plugboard = Plugboard("AB-CD-EF-GH-IJ-KL")
        val output = plugboard.encrypt('X')

        assertEquals('X', output, "plugboard should not replace 'X'")
    }

    @Test
    fun encryptMappedLetterOneWay() {
        val plugboard = Plugboard("AB-CD-EF-GH-IJ-KL")
        val output = plugboard.encrypt('A')

        assertEquals('B', output, "plugboard should map 'A' to 'B'")
    }

    @Test
    fun encryptMappedLetterTheOtherWay() {
        val plugboard = Plugboard("AB-CD-EF-GH-IJ-KL")
        val output = plugboard.encrypt('B')

        assertEquals('A', output, "plugboard should map 'B' to 'A'")
    }
}