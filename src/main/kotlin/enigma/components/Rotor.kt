package enigma.components

import enigma.components.recorder.StepRecorder
import enigma.components.recorder.RotorStepRecorder
import enigma.util.Util.Companion.normalize
import enigma.util.Util.Companion.toChar
import enigma.util.Util.Companion.toInt
import enigma.util.Util.Companion.validate
import java.lang.IllegalArgumentException

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
class Rotor (
    val rotorType: RotorType,
    // character between 'A' and 'Z' (including)
    var startPosition: Char,
    // value between 1 and 26 (including)
    val ringSetting: Int
) {
    // encryption table optimized for the internal algorithm, for encrypting an incoming signal on the right
    // to an outcoming signal on the left
    // * when the encryptionTable defines that B (2nd in alphabet) maps to E (3 characters further on in the alphabet),
    //   the 2nd element in the optimized table will have the value 3
    // * when the encryptionTable defines that Z (26th in alphabet) maps to X (2 characters earlier on in the alphabet),
    //   the 26th element in the optimized table will have the value -2
    private val _encryptionTableRtoL = optimizeRtoL(rotorType.encryptionTable)

    // encryption table optimized for the internal algorithm, for encrypting an incoming signal on the left
    // to an outcoming signal on the right
    private val _encryptionTableLtoR = optimizeLtoR(rotorType.encryptionTable)

    // current position of the rotor, identified by a character between 'A' and 'Z'
    var currentPosition = startPosition

    // current position value optimized for the internal algorithm: 'A' is represented by 0, and so on
    private var _position = 0

    // ringsetting value optimized for the internal algorithm: 1 is represented by 0, and so on
    private val _ringSetting = ringSetting - 1

    init {
        setOptimizedPosition()
    }

    /**
     * Translate a signal arriving at a specific inputChannel on the *right*
     * to the corresponding outputChannel on the *left*.
     *
     * @param inputChannel character between 'A' and 'Z'
     * @return character between 'A' and 'Z'
     */
    fun encryptRightToLeft(inputChannel:Char, recorders:MutableList<StepRecorder>? = null) : Char {
        if (!validate(inputChannel)) {
            throw IllegalArgumentException("input must be a character between A and Z (capital!)")
        }
        return toChar(encryptRightToLeft(toInt(inputChannel), recorders))
    }

    /**
     * Translate a signal arriving at a specific inputChannel on the *left*
     * to the corresponding outputChannel on the *right*.
     *
     * @param inputChannel character between 'A' and 'Z'
     * @return character between 'A' and 'Z'
     */
    fun encryptLeftToRight(inputChannel:Char, recorders:MutableList<StepRecorder>? = null) : Char {
        if (!validate(inputChannel)) {
            throw IllegalArgumentException("input must be a character between A and Z (capital!)")
        }
        return toChar(encryptLeftToRight(toInt(inputChannel), recorders))
    }

    /**
     * Translate a signal arriving at a specific inputChannel on the *right*
     * to the corresponding outputChannel on the *left*.
     *
     * @param inputChannel integer between 0 (representing 'A') and 25 (representing 'Z')
     * @return integer between 0 (representing 'A') and 25 (representing 'Z')
     */
    private fun encryptRightToLeft(rightContactChannel:Int, recorders:MutableList<StepRecorder>?) : Int {
        val rightContact = rightContactFromChannel(rightContactChannel)
        val leftContact = encryptContactRightToLeft(rightContact)
        val leftContactChannel = leftContactChannelFromContact(leftContact)

        if (recorders != null) {
            var recorder = RotorStepRecorder()
            recorders.add(recorder)
            recorder.rotorType = rotorType
            recorder.rotorPosition = currentPosition
            recorder.rotorRingSetting = ringSetting
            recorder.rightToLeft = true
            recorder.inputContactChannel = rightContactChannel
            recorder.inputValue = rightContact
            recorder.outputValue = leftContact
            recorder.outputContactChannel = leftContactChannel
        }
        return leftContactChannel
    }

    /**
     * Translate a signal arriving at a specific inputChannel on the *left*
     * to the corresponding outputChannel on the *right*.
     *
     * @param inputChannel integer between 0 (representing 'A') and 25 (representing 'Z')
     * @return integer between 0 (representing 'A') and 25 (representing 'Z')
     */
    private fun encryptLeftToRight(leftContactChannel:Int, recorders:MutableList<StepRecorder>?) : Int {
        val leftContact = leftContactFromChannel(leftContactChannel)
        val rightContact = encryptContactLeftToRight(leftContact)
        val rightContactChannel = rightContactChannelFromContact(rightContact)

        if (recorders != null) {
            var recorder = RotorStepRecorder()
            recorders.add(recorder)
            recorder.rotorType = rotorType
            recorder.rotorPosition = currentPosition
            recorder.rotorRingSetting = ringSetting
            recorder.rightToLeft = false
            recorder.inputContactChannel = leftContactChannel
            recorder.inputValue = leftContact
            recorder.outputValue = rightContact
            recorder.outputContactChannel = rightContactChannel
        }
        return rightContactChannel
    }

    /**
     * A 'contact channel' does not actually exist in the Enigma.
     * In between two rotors, envision 26 contact channels or connections. Each channel connects to a contact to
     * the rotor on its right, and to a contact to the rotor on its left.
     * With each step taken by a rotor, its contacts move, while the channels stays stationary, causing all contacts
     * to get associated with different channels.
     *
     * Given a contact channel on the right side of the rotor, this method determines which contact currently
     * connects to that channel.
     *
     * @param rightContactChannel identification of stationary contact channel on the right of the rotor
     *   integer between 0 (representing 'A') and 25 (representing 'Z')
     * @result identification of contact on the right of the rotor currently connected to the rightContactChannel
     *   integer between 0 (representing 'A') and 25 (representing 'Z')
     */
    private fun rightContactFromChannel(rightContactChannel:Int): Int {
        return normalize(rightContactChannel + _position - _ringSetting)
    }

    private fun encryptContactRightToLeft(right:Int): Int {
        return normalize(right + _encryptionTableRtoL[right])
    }

    private fun leftContactChannelFromContact(leftContact:Int) : Int {
        return normalize(leftContact - _position + _ringSetting)
    }

    private fun leftContactFromChannel(leftContactChannel:Int): Int {
        return normalize(leftContactChannel + _position - _ringSetting)
    }

    private fun encryptContactLeftToRight(left:Int): Int {
        return normalize(left + _encryptionTableLtoR[left])
    }

    private fun rightContactChannelFromContact(rightContact:Int) : Int {
        return normalize(rightContact - _position + _ringSetting)
    }

    companion object {
        @JvmStatic
        // input: B maps to K
        //   B is 2nd letter in alphabet --> index = 1 (0-based index)
        //   K is 11th letter in alphabet
        // result: array[1] = 'K as int' - 'B as int' = 11 - 2 = 9
        //   meaning: B maps to a character 9 positions further on in the alphabet = K
        fun optimizeRtoL(encryptionTable: String): IntArray {
            var optimizedEncryptionTable = IntArray(26)

            var pos = 0
            while (pos < encryptionTable.length) {
                optimizedEncryptionTable[pos] = encryptionTable[pos].toInt() - ('A'.toInt() + pos)
                pos++
            }
            return optimizedEncryptionTable
        }

        @JvmStatic
        fun optimizeLtoR(encryptionTable: String): IntArray {
            // input: B maps to K, meaning K will map to B when the signal travels through the rotor from left to right
            //   K is the 11h letter in alphabet --> index = 10 (0-based index)
            //   B is the 2nd letter in alphabet
            // result: array[10] = 'B as int' - 'K as int' = 2 - 11 = -9
            //   meaning: K maps to a character 9 positions earlier on in the alphabet = B
            var optimizedEncryptionTable = IntArray(26)

            var pos = 0
            while (pos < encryptionTable.length) {
                optimizedEncryptionTable[encryptionTable[pos].toInt() - 'A'.toInt()] = ('A'.toInt() + pos) - encryptionTable[pos].toInt()
                pos++
            }
            return optimizedEncryptionTable
        }
    }

    /**
     * Advance this rotor one step
     *
     * When the rotor reaches its turnover position, the rotor to its left should step as well
     * https://en.wikipedia.org/wiki/Enigma_machine#Turnover
     *
     * result: true when the rotor to the left should step as well
     */
    fun stepRotor() : Boolean {
        currentPosition = toChar(normalize(toInt(++currentPosition) ))
        setOptimizedPosition()

        return currentPosition.equals(rotorType.turnoverPosition)
    }

    /**
     * Rest this rotor to its starting position
     */
    fun reset () {
        currentPosition = startPosition
        setOptimizedPosition()
    }

    private fun setOptimizedPosition() {
        _position = currentPosition.toInt() -'A'.toInt()
    }
}