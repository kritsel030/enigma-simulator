package enigma

import enigma.components.Plugboard
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

    // starts with left-most (slowest) rotor
    val rotors : List<Rotor>,

    val plugboard: Plugboard
    ){

    // position is 1-based
    fun getRotor(position: Int) : Rotor {
        return rotors[position-1]
    }

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

    fun encrypt(input: Char, stepRotors: Boolean?=true, recorders:MutableList<StepRecorder>?=null) :Char {
        // validate input
        if (!validate(input)) {
            throw IllegalArgumentException("input must be character between A and Z (capital!)")
        }

        // Before a pressed key is encoded by the Enigma, it first triggers the mechanical rotor stepping mechanism
        if (stepRotors == true) {
            stepRotors()
        }

        // Plugboard
        var nextElementInput = plugboard.encrypt(input, recorders)

        // start with the right-most rotor (last one in the list)
        for (rotor in rotors.reversed()) {
            nextElementInput = rotor.encryptRightToLeft(nextElementInput, recorders)
        }

        // Reflector
        nextElementInput = reflector.encrypt(nextElementInput, recorders)

        // now start with the left-most rotor (first one in the list)
        for (rotor in rotors) {
            nextElementInput = rotor.encryptLeftToRight(nextElementInput, recorders)
        }

        // Plugboard
        val output = plugboard.encrypt(nextElementInput, recorders)

        return output
    }

    /**
     * Every key pressed causes one or more rotors to step by one twenty-sixth of a full rotation,
     * before the electrical connection is made to encrypt the pressed key.
     *
     * For a 3-rotor enigma:
     * - the right-most rotor (position 3) steps with every key press.
     * - the middle rotor (position 2) steps when the rotor to its right reaches its turnover position.
     * - the left-most rotor (position 2) steps when the rotor to its right reaches its turnover position;
     *   due to the mechanical design of the stepping mechanism, when a step of the middle-rotor
     *    causes the left-most rotor to step, the left-most rotor causes the middle rotor to step an additional step.
     *
     * https://en.wikipedia.org/wiki/Enigma_machine#Stepping
     */
    internal fun stepRotors() {
        stepRotor(rotors.size)
    }

    /**
     * Step the rotor in a specific position.
     * Return true when this rotor's step should trigger a turnover to the rotor to it's left
     * indicating that that rotor should step as well
     */
    private fun stepRotor(position: Int) : Boolean {
        val turnOver = getRotor(position).stepRotor()
        // In a 3-rotor enigma:
        // Due to the mechanical design of the stepping mechanism, when a step of the middle-rotor
        // causes the left-most rotor to step, the left-most rotor causes the middle rotor to step an additional step.
        if (rotors.size == 3) {
            if (position == 1) {
                getRotor(2).stepRotor()
            }
        }

        // if turnover: proceed to the rotor to the left (unless we've already reached the left-most rotor)
        if (turnOver && position != 1) {
            return stepRotor(position-1)
        }
        return false
    }

    /**
     * Prepare for a new messsage to be encoded: return all rotors to their starting positions
     */
    internal fun resetRotors() {
        rotors.forEach { it.reset() }
    }
}