package enigma

import enigma.components.Plugboard
import enigma.components.Reflector
import enigma.components.Rotor
import enigma.components.recorder.StepRecorder
import shared.Util.Companion.validate
import shared.BasicScrambler
import shared.Util
import java.lang.IllegalArgumentException

/**
 * When creating an Enigma instance, list the rotors in the order as they would physically appear in an Enigma
 * machine from left to right.
 */

// Default constructor supports a 4-wheel enigma
class Enigma (
    reflector: Reflector,

    rotors: List<Rotor>,

    val plugboard: Plugboard
    ) : BasicScrambler("enigma", reflector, rotors){

    // constructor for a 3-wheel enigma
    constructor(reflector: Reflector, leftRotor: Rotor, middleRotor: Rotor, rightRotor: Rotor, plugboard: Plugboard) : this(reflector, listOf(leftRotor, middleRotor, rightRotor), plugboard)

    // constructor for a 4-wheel enigma
    constructor(reflector: Reflector, leftLeftRotor: Rotor, leftRotor: Rotor, middleRotor: Rotor, rightRotor: Rotor, plugboard: Plugboard) : this(reflector, listOf(leftLeftRotor, leftRotor, middleRotor, rightRotor), plugboard)


    fun encryptMessage(input:String) : String {
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
            buf.append(encryptSingleLetter(character, true))
            pos++
        }
        return buf.toString()
    }

    fun encryptSingleLetter(inputChar: Char, stepRotors: Boolean?=true, recorders:MutableList<StepRecorder>?=null) :Char {
        // validate input
        if (!validate(inputChar)) {
            throw IllegalArgumentException("input must be character between A and Z (capital!)")
        }

        // Before a pressed key is encoded by the Enigma, it first triggers the mechanical rotor stepping mechanism
        if (stepRotors == true) {
            stepRotors()
        }

        // Plugboard on the way in
        var pbOut = plugboard.encrypt(inputChar, recorders)

        // rotors, reflector and rotors again
        val pbContactIn = encrypt(Util.toInt(pbOut))

        // Plugboard on the way out
        val pbIn = 'A'.plus(pbContactIn)
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
        val turnOverToMiddle = rightRotor!!.stepRotor()
        if (turnOverToMiddle) {
            val turnOverToLeft = middleRotor!!.stepRotor()
            if (turnOverToLeft) {
                leftRotor!!.stepRotor()
                // Due to the mechanical design of the stepping mechanism, when a step of the middle rotor
                // causes the left rotor to step, the left rotor causes the middle rotor to step an additional step.
                middleRotor!!.stepRotor()
            }
        }
    }
}