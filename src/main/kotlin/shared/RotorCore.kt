package shared

import enigma.components.recorder.RotorStepRecorder
import enigma.components.recorder.StepRecorder
import shared.Util.Companion.normalize
import shared.Util.Companion.validate

/**
 * The associations between all left-contacts and right-contacts of a rotor are unique for a particular rotor type
 * and are defined by its encryption table.
 *
 * When the encryption table reads 'B is mapped to E' (for the signal travelling from right to left),
 * this translated in our model to
 * - when right-contact ID = 1
 * - then left-contact ID = right-contact ID +3 = 1 + 3 = 4
 * (note that the alphabet ranging from 'A' to 'Z' is translated into 0 to 25 for easier internal calculation)
 */
open class RotorCore (
    // the rotorType also defines the alphabet size
    val rotorType: RotorType,

    // Each rotor core a plate on both side, each with a circle of contacts.
    // Those contacts are numbered (0, 1, 2, ...) in our code representation (not in the physical reality),
    // and contacts on both plates which are opposite each other have the same number
    // When a rotor is positioned as a wheel in an Enigma machine or a drum in a Bombe,
    // the 0-contact (the first contact) is positioned several steps away from a fixed reference point on the machine
    // it is positioned in.
    // The startOffset indicates how many steps ahead this first contact is compared to the fixed machine reference point.
    // (value between 0 and alphabetSize-1)
    var startOffset: Int,
) {
    init {
        check(startOffset < rotorType.encryptionTable.length) { "$startOffset should be between 0 and ${rotorType.encryptionTable.length - 1}, but it is set at $startOffset" }
    }

    var steps = 0
        private set

    // when a rotor for a 26-letter alphabet has stepped 28 times, the net number of steps is 2
    // (the rotor's position is 2 steps ahead of its original position
    fun getNetSteps() : Int {
        return Util.Companion.normalize(currentOffset - startOffset, alphabetsize)
    }

    val alphabetsize = rotorType.alphabetsize

    // the number of steps the 0-contact of this rotor is ahead of the fixed machine reference point
    // (value between 0 and alphabetsize-1)
    var currentOffset = startOffset
        private set

    fun rotateToStartOffset(offset: Int) {
        startOffset = offset
        currentOffset = startOffset
    }

    fun increaseStartOffset() {
        startOffset++
        currentOffset = startOffset
    }

    companion object {
        // the wiring maps are used to calculate how a signals flow from rotor to rotor
        // * when the encryptionTable defines that B (2nd in alphabet) maps to E (3 characters further on in the alphabet),
        //   this means that the 2nd contact on the right-hand side of the rotor is connected to the 5th contact on the
        //   left-hand side of the rotor
        //   this fact will be represented as _wiringMapRtoL[1] = 3
        // * when the encryptionTable defines that Z (26th in alphabet) maps to X (2 characters *earlier* on in the alphabet),
        //   this means that the 26th contact on the right-hand side of the rotor is connected to the 24th contact on the
        //   left-hand side of the rotor
        //   this fact will be represented as _wiringMapLtoR[25] = -2
        val wiringMapsRtoL = RotorType.values().map { it to deriveWiringMapRtoL(it.encryptionTable) }.toMap()

        // same thing, but reversed
        val wiringMapsLtoR = RotorType.values().map { it to deriveWiringMapLtoR(it.encryptionTable) }.toMap()

        // input: B maps to K
        //   B is 2nd letter in alphabet --> index = 1 (0-based index)
        //   K is 11th letter in alphabet
        // result: array[1] = 'K as int' - 'B as int' = 11 - 2 = 9
        //   meaning: B maps to a character 9 positions further on in the alphabet = K
        fun deriveWiringMapRtoL(encryptionTable: String): IntArray {
            var wiringMap = IntArray(encryptionTable.length)

            var pos = 0
            while (pos < encryptionTable.length) {
                wiringMap[pos] = encryptionTable[pos].code - ('A'.code + pos)
                pos++
            }
            return wiringMap
        }

        fun deriveWiringMapLtoR(encryptionTable: String): IntArray {
            // input: B maps to K, meaning K will map to B when the signal travels through the rotor from left to right
            //   K is the 11h letter in alphabet --> index = 10 (0-based index)
            //   B is the 2nd letter in alphabet
            // result: array[10] = 'B as int' - 'K as int' = 2 - 11 = -9
            //   meaning: K maps to a character 9 positions earlier on in the alphabet = B
            var wiringTable = IntArray(encryptionTable.length)

            var pos = 0
            while (pos < encryptionTable.length) {
                wiringTable[encryptionTable[pos].code - 'A'.code] = ('A'.code + pos) - encryptionTable[pos].toInt()
                pos++
            }
            return wiringTable
        }
    }


    /**
     * Translate a signal arriving at a specific inputChannel on the *right*
     * to the corresponding outputChannel on the *left*.
     *
     * @param inputChannel integer between 0 (representing 'A') and 25 (representing 'Z')
     * @return integer between 0 (representing 'A') and 25 (representing 'Z')
     */
    fun encryptRightToLeft(rightActiveContactOffset:Int, recorders:MutableList<StepRecorder>? = null) : Int {
        val normalized = Util.normalize(rightActiveContactOffset, 26)
        require (validate(normalized, alphabetsize)) {"input must be a int between 0 and ${alphabetsize-1}, actual input is $rightActiveContactOffset"}
//        println("currentOffset: $currentOffset")
        val rightContactId = this.offsetToContact(normalized)
        val leftContactId = encryptContactRightToLeft(rightContactId)
        val leftContactOffset = this.contactToOffset(leftContactId)

        if (recorders != null) {
            var recorder = RotorStepRecorder()
            recorders.add(recorder)
            recorder.rotorType = rotorType
            recorder.rotorPosition = 'A'.plus(currentOffset)
            recorder.rotorRingSetting = 0
            recorder.rightToLeft = true
            recorder.inputContactChannel = rightActiveContactOffset
            recorder.inputValue = rightContactId
            recorder.outputValue = leftContactId
            recorder.outputContactChannel = leftContactOffset
        }
        check (validate(leftContactOffset, alphabetsize)) {"$leftContactOffset must be between 0 and ${alphabetsize-1}"}
        return leftContactOffset
    }

    /**
     * Translate a signal arriving at a specific inputChannel on the *left*
     * to the corresponding outputChannel on the *right*.
     *
     * @param inputChannel integer between 0 (representing 'A') and 25 (representing 'Z')
     * @return integer between 0 (representing 'A') and 25 (representing 'Z')
     */
    fun encryptLeftToRight(leftActiveContactOffset:Int, recorders:MutableList<StepRecorder>? = null) : Int {
        require (validate(leftActiveContactOffset, alphabetsize)) {"input must be a int between 0 and ${alphabetsize-1}, actual input is $leftActiveContactOffset"}
        val leftContact = this.offsetToContact(leftActiveContactOffset)
        val rightContact = encryptContactLeftToRight(leftContact)
        val rightContactChannel = this.contactToOffset(rightContact)

        if (recorders != null) {
            var recorder = RotorStepRecorder()
            recorders.add(recorder)
            recorder.rotorType = rotorType
            recorder.rotorPosition = 'A'.plus(currentOffset)
            recorder.rotorRingSetting = 0
            recorder.rightToLeft = false
            recorder.inputContactChannel = leftActiveContactOffset
            recorder.inputValue = leftContact
            recorder.outputValue = rightContact
            recorder.outputContactChannel = rightContactChannel
        }
        check (validate(rightContactChannel, alphabetsize)) {"$rightContactChannel must be between 0 and ${alphabetsize-1}"}
        return rightContactChannel
    }

    open fun offsetToContact(offset:Int): Int {
        return normalize(offset + currentOffset)
    }

    open fun contactToOffset(contact:Int) : Int {
        return normalize(contact - currentOffset)
    }

    private fun encryptContactRightToLeft(right:Int): Int {
//        println("encryptContactRightToLeft($right)")
        return normalize(right + wiringMapsRtoL[rotorType]!![right])
    }

    private fun encryptContactLeftToRight(left:Int): Int {
//        println("encryptContactLeftToRight($left)")
        return normalize(left + wiringMapsLtoR[rotorType]!![left])
    }

    /**
     * Advance this rotor one step
     */
    open fun stepRotor() : Boolean{
        currentOffset = normalize(currentOffset + 1)
        steps++
        return true
    }

    /**
     * Reset this rotor to its starting position
     */
    fun reset () {
        currentOffset = startOffset
        steps = 0
    }

}