package enigma

import enigma.components.Plugboard
import enigma.components.Reflector
import enigma.components.recorder.StepRecorder
import shared.Util.Companion.validate
import shared.AbstractRotor
import java.lang.IllegalArgumentException

/**
 * When creating an Enigma instance, list the rotors in the order as they would physically appear in an Enigma
 * machine from left to right.
 */

// Default constructor supports a 4-wheel enigma
class EnigmaOld (
    val reflector: Reflector,

    // optional 4th rotor
    val leftLeftRotor : AbstractRotor?,

    val leftRotor: AbstractRotor,

    val middleRotor : AbstractRotor,

    val rightRotor: AbstractRotor,

    val plugboard: Plugboard
    ){

    // constructor for a 3-wheel enigma
    constructor(reflector: Reflector, leftRotor: AbstractRotor, middleRotor: AbstractRotor, rightRotor: AbstractRotor, plugboard: Plugboard) : this(reflector, null, leftRotor, middleRotor, rightRotor, plugboard)

    // constructor with a list of rotors must accommodate for both 3 and 4 wheel enigmas
    constructor(reflector: Reflector, rotors: List<AbstractRotor>, plugboard: Plugboard) : this(reflector, if (rotors.size == 4) rotors[0] else null, rotors[rotors.size-3], rotors[rotors.size-2], rotors[rotors.size-1], plugboard )

    // list of rotors, starting with the left-most rotor
    val rotors = listOf(leftLeftRotor, leftRotor, middleRotor, rightRotor).filterNotNull()

    // position is 1-based
    // getRotor(1) returns the left-most rotor
    fun getRotor(position: Int) : AbstractRotor {
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
        var pos = 0
        for (character in input) {
            buf.append(encrypt(character, true))
            pos++
        }
        return buf.toString()
    }

    fun encrypt(inputChar: Char, stepRotors: Boolean?=true, recorders:MutableList<StepRecorder>?=null) :Char {
        // validate input
        if (!validate(inputChar)) {
            throw IllegalArgumentException("input must be character between A and Z (capital!)")
        }

        // Before a pressed key is encoded by the Enigma, it first triggers the mechanical rotor stepping mechanism
        if (stepRotors == true) {
            stepRotors()
        }

        // Plugboard
        var pbOut = plugboard.encrypt(inputChar, recorders)

        // start with the right-most rotor (last one in the list)
        var contactOffset = pbOut.code - 'A'.code
        for (rotor in rotors.reversed()) {
            contactOffset = rotor.encryptRightToLeft(contactOffset, recorders)
        }

        // Reflector
        val reflectorInput = 'A'.plus(contactOffset)
        val reflectorOutput = reflector.encrypt(reflectorInput, recorders)

        // now start with the left-most rotor (first one in the list)
        contactOffset = reflectorOutput.code - 'A'.code
        for (rotor in rotors) {
            contactOffset = rotor.encryptLeftToRight(contactOffset, recorders)
        }

        // Plugboard
        val pbIn = 'A'.plus(contactOffset)
        val output = plugboard.encrypt(pbIn, recorders)

        return output
    }


    /**
     * Every key pressed causes one or more rotors to step by one twenty-sixth of a full rotation,
     * before the electrical connection is made to encrypt the pressed key.
     *
     * Procedure
     * - the right rotor steps with every key press.
     * - the middle rotor steps when the rotor to its right reaches its turnover position.
     * - the lef rotor steps when the rotor to its right reaches its turnover position;
     *   due to the mechanical design of the stepping mechanism, when a step of the middle-rotor
     *    causes the left-most rotor to step, the left-most rotor causes the middle rotor to step an additional step.
     * - the leftLeft rotor (4th rotor in a 4-wheel enigma) is stationary, it never steps
     *
     * https://en.wikipedia.org/wiki/Enigma_machine#Stepping
     */
    internal fun stepRotors() {
        val turnOverToMiddle = rightRotor.stepRotor()
        if (turnOverToMiddle) {
            val turnOverToLeft = middleRotor.stepRotor()
            if (turnOverToLeft) {
                leftRotor.stepRotor()
                // Due to the mechanical design of the stepping mechanism, when a step of the middle rotor
                // causes the left rotor to step, the left rotor causes the middle rotor to step an additional step.
                middleRotor.stepRotor()
            }
        }
    }

    /**
     * Prepare for a new messsage to be encoded with the same enigma settings: return all rotors to their starting positions
     */
    internal fun resetRotors() {
        rotors.forEach { it.reset() }
    }
}