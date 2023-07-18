package shared

import enigma.components.Reflector
import enigma.components.recorder.StepRecorder
import shared.Util.Companion.validate

/**
 * A scrambler consisting of a reflector and 3 or 4 rotors. No plugboard.
 */

// Default constructor supports
// - both 3 and 4 wheel scramblers
// - scramblers where the actual rotors are place later on in the process
open class BasicScrambler private constructor (
    val rotorPositions: Int,

    val reflector: Reflector,

    // optional 4th rotor
    var leftLeftRotor : AbstractRotor? = null,

    var leftRotor: AbstractRotor? = null,

    var middleRotor : AbstractRotor? = null,

    var rightRotor: AbstractRotor? = null,

    ){
    // constructor for a 4-wheel scrambler
    constructor(reflector: Reflector, leftLeftRotor: AbstractRotor?, leftRotor: AbstractRotor, middleRotor: AbstractRotor, rightRotor: AbstractRotor) : this(4, reflector, leftLeftRotor, leftRotor, middleRotor, rightRotor)

    // constructor for a 3-wheel scrambler
    constructor(reflector: Reflector, leftRotor: AbstractRotor, middleRotor: AbstractRotor, rightRotor: AbstractRotor) : this(3, reflector, null, leftRotor, middleRotor, rightRotor)

    // constructor with a list of rotors must accommodate for both 3 and 4 wheel enigmas
    constructor(reflector: Reflector, rotors: List<AbstractRotor>) : this(rotors.size, reflector, if (rotors.size == 4) rotors[0] else null, rotors[rotors.size-3], rotors[rotors.size-2], rotors[rotors.size-1])

    // constructor without a list of rotors, they will be placed later on
    constructor(rotorPositions: Int, reflector: Reflector) : this(rotorPositions, reflector, null, null, null, null)


    // list of rotors, starting with the left-most rotor
    var rotors = listOf(leftLeftRotor, leftRotor, middleRotor, rightRotor).filterNotNull()

    fun placeDrums(rotors:List<AbstractRotor>) {
        leftLeftRotor = if (rotors.size == 4) rotors[0] else null
        leftRotor = rotors[rotors.size-3]
        middleRotor = rotors[rotors.size-2]
        rightRotor = rotors[rotors.size-1]
        this.rotors = listOf(leftLeftRotor, leftRotor, middleRotor, rightRotor).filterNotNull()
        checkRotors()
    }

    fun checkRotors() {
        check(rotorPositions == rotors.size) {"expected $rotorPositions to be present, but there are ${rotors.size}"}
    }

    // position is 1-based
    // getRotor(1) returns the left-most rotor
    fun getRotor(position: Int) : AbstractRotor {
        // first check if we're ready to go
        checkRotors()

        return rotors[position-1]
    }

    fun encrypt(inputContactId: Int, recorders:MutableList<StepRecorder>?=null) :Int {
        // first check if we're ready to go
        checkRotors()

        // validate input
        //TODO("we need alphabetsize here")
        check(validate(inputContactId, 26)) {"input must number bebe a tween 0 and 25"}

        var contactOffset = inputContactId

        // start with the right-most rotor (last one in the list)
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

        // TODO remove
        check(Util.Companion.validate(contactOffset, 26)) { "$contactOffset should be between 0 and 25"}
        return contactOffset
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
        // first check if we're ready to go
        checkRotors()

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

    /**
     * Prepare for a new messsage to be encoded with the same enigma settings: return all rotors to their starting positions
     */
    internal fun resetRotors() {
        // first check if we're ready to go
        checkRotors()

        rotors.forEach { it.reset() }
    }
}