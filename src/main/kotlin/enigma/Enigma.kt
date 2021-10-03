package enigma

import enigma.components.Reflector
import enigma.components.Rotor
import enigma.components.recorder.StepRecorder
import enigma.util.Util.Companion.validate
import java.lang.IllegalArgumentException

/**
 * When creating an Enigma instance, list the rotors in the order as they would physically appear in an Enigma
 * machine from left to right.
 */
class Enigma (
    val reflector: Reflector,

    // left most rotor (position 1 in Enigma terms)
    val rotor1: Rotor,

    // middle rotor (position 2 in Enigma terms)
    val rotor2: Rotor,

    // right most rotor (position 3 in Enigma terms)
    val rotor3: Rotor
    ){

    fun encrypt(input:String) : String {
        // validate input
        if (!validate(input)) {
            throw IllegalArgumentException("input may only contain characters between A and Z (capital!)")
        }

        // reset all rotors to their starting positions
        resetRotors()

        // do magic
        var buf = StringBuffer()
        for (character in input) {
            buf.append(encrypt(character))
        }
        return buf.toString()
    }

    fun encrypt(input: Char, recorders:MutableList<StepRecorder>?=null) :Char {
        // validate input
        if (!validate(input)) {
            throw IllegalArgumentException("input must be character between A and Z (capital!)")
        }

        // Before a pressed key is encoded by the Enigma, it first triggers the mechanical rotor stepping mechanism
        stepRotors()

        // Follow the signal through the rotors from right to left (order: rotor3, rotor2, rotor1)
        val input2 = rotor3.encryptContactRightToLeft(input, recorders)
        val input3 = rotor2.encryptContactRightToLeft(input2, recorders)
        val inputReflector = rotor1.encryptContactRightToLeft(input3, recorders)

        // Reflector
        val input4 = reflector.encryptContact(inputReflector, recorders)

        // Follow the signal through the rotors from left to right (order: rotor1, rotor2, rotor3)
        val input5 = rotor1.encryptContactLeftToRight(input4, recorders)
        val input6 = rotor2.encryptContactLeftToRight(input5, recorders)
        val output = rotor3.encryptContactLeftToRight(input6, recorders)

        return output
    }

    /**
     * Every key pressed causes one or more rotors to step by one twenty-sixth of a full rotation,
     * before the electrical connection is made to encrypt the pressed key.
     *
     * https://en.wikipedia.org/wiki/Enigma_machine#Stepping
     */
    private fun stepRotors() {
        //The right-most rotor (rotor3) steps with every key press.
        val stepMiddle = rotor3.stepRotor()
        if (stepMiddle) {
            // The middle rotor (rotor2) only steps every 26th key presses.
            // It steps when the rotor to its right reaches its turnover position.
            val stepLeft = rotor2.stepRotor()
            if (stepLeft) {
                // The left-most rotor (rotor1) only steps every 26x26 key presses.
                // It too steps when the rotor to its right reaches its turnover position.
                rotor1.stepRotor()
                // Due to the mechanical design of the stepping mechanism, when a step of the middle-rotor
                // causes the left-most rotor to step, the left-most rotor causes the middle rotor to step an additional step.
                rotor2.stepRotor()
            }
        }
    }

    /**
     * Prepare for a new messsage to be encoded: return all rotors to their starting positions
     */
    private fun resetRotors() {
        rotor1.reset()
        rotor2.reset()
        rotor3.reset()
    }
}