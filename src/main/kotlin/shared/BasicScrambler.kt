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
    val label: String,

    val rotorPositions: Int,

    var reflector: Reflector?,

    // optional 4th rotor
    var leftLeftRotor : AbstractRotor? = null,

    var leftRotor: AbstractRotor? = null,

    var middleRotor : AbstractRotor? = null,

    var rightRotor: AbstractRotor? = null,

    ){
    // constructor for a 4-wheel scrambler
    constructor(label: String, reflector: Reflector, leftLeftRotor: AbstractRotor?, leftRotor: AbstractRotor, middleRotor: AbstractRotor, rightRotor: AbstractRotor) : this(label, 4, reflector, leftLeftRotor, leftRotor, middleRotor, rightRotor)

    // constructor for a 3-wheel scrambler
    constructor(label: String, reflector: Reflector, leftRotor: AbstractRotor, middleRotor: AbstractRotor, rightRotor: AbstractRotor) : this(label, 3, reflector, null, leftRotor, middleRotor, rightRotor)

    // constructor with a list of rotors must accommodate for both 3 and 4 wheel enigmas
    constructor(label: String, reflector: Reflector, rotors: List<AbstractRotor>) : this(label, rotors.size, reflector, if (rotors.size == 4) rotors[0] else null, rotors[rotors.size-3], rotors[rotors.size-2], rotors[rotors.size-1])

    // constructor without a list of rotors, they will be placed later on
    constructor(label: String, rotorPositions: Int, reflector: Reflector?) : this(label, rotorPositions, reflector, null, null, null, null)


    // list of rotors, starting with the left-most rotor
    var rotors = listOf(leftLeftRotor, leftRotor, middleRotor, rightRotor).filterNotNull()

    fun placeRotors(rotors:List<AbstractRotor>) {
        leftLeftRotor = if (rotors.size == 4) rotors[0] else null
        leftRotor = rotors[rotors.size-3]
        middleRotor = rotors[rotors.size-2]
        rightRotor = rotors[rotors.size-1]
        this.rotors = listOf(leftLeftRotor, leftRotor, middleRotor, rightRotor).filterNotNull()
        checkRotorsAndReflector()
    }

    fun checkRotorsAndReflector() {
        check(rotorPositions == rotors.size) {"[$label] expected $rotorPositions rotors to be placed, but there are only ${rotors.size}"}
        check (reflector != null) {"[$label] scrambler has no reflector"}
        check( reflector!!.reflectorType.alphabetsize == leftRotor!!.getRotorType().alphabetsize) {"left rotor's alphabet size (${leftRotor!!.getRotorType().alphabetsize}) does not match the reflector's alphabbet size (${reflector!!.reflectorType.alphabetsize})"}
        check( reflector!!.reflectorType.alphabetsize == middleRotor!!.getRotorType().alphabetsize) {"middle rotor's alphabet size (${middleRotor!!.getRotorType().alphabetsize}) does not match the reflector's alphabbet size (${reflector!!.reflectorType.alphabetsize})"}
        check( reflector!!.reflectorType.alphabetsize == rightRotor!!.getRotorType().alphabetsize) {"right rotor's alphabet size (${rightRotor!!.getRotorType().alphabetsize}) does not match the reflector's alphabbet size (${reflector!!.reflectorType.alphabetsize})"}
    }

    fun getAlphabetSize () : Int{
        return leftRotor?.getRotorType()?.alphabetsize!!
    }

    // position is 1-based
    // getRotor(1) returns the left-most rotor
    fun getRotor(position: Int) : AbstractRotor? {
        // first check if we're ready to go
        //checkRotors()

        return rotors.getOrNull(position-1)
    }

    fun encrypt(inputContactId: Int, recorders:MutableList<StepRecorder>?=null) :Int {
        // first check if we're ready to go
        checkRotorsAndReflector()

        // validate input
        //TODO("we need alphabetsize here")
        //check(validate(inputContactId, 26)) {"input must number between 0 and 25"}
        check(validate(inputContactId, getAlphabetSize()!!)) {"input must number between 0 and ${getAlphabetSize()!!}-1"}

        var contactOffset = inputContactId

        // start with the right-most rotor (last one in the list)
        for (rotor in rotors.reversed()) {
            contactOffset = rotor.encryptRightToLeft(contactOffset, recorders)
        }

        // Reflector
        val reflectorInput = 'A'.plus(contactOffset)
        val reflectorOutput = reflector!!.encrypt(reflectorInput, recorders)

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
     * Prepare for a new messsage to be encoded with the same enigma settings: return all rotors to their starting positions
     */
    internal fun resetRotors() {
        // first check if we're ready to go
        checkRotorsAndReflector()

        rotors.forEach { it.reset() }
    }
}