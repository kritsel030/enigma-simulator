package enigma.components

import enigma.components.recorder.PlugboardStepRecorder
import enigma.components.recorder.StepRecorder
import shared.Util

class Plugboard(
    // e.g. AC-DK-GI-JX-OE-XZ
    val connectedLetters:String
) {
    private val _encryptionTable = optimize(connectedLetters)

    fun encrypt(inputChannel:Char, recorders:MutableList<StepRecorder>? = null) : Char {
        if (!Util.validate(inputChannel)) {
            throw IllegalArgumentException("input must be a character between A and Z (capital!)")
        }
        return Util.toChar(encrypt(Util.toInt(inputChannel), recorders))
    }

    fun encrypt(contactChannel:Int, recorders:MutableList<StepRecorder>? = null) : Int {
        val result = Util.normalize(contactChannel + _encryptionTable[contactChannel])

        if (recorders != null) {
            var recorder = PlugboardStepRecorder()
            recorders.add(recorder)
            recorder.connectedLetters = connectedLetters
            recorder.inputContactChannel = contactChannel
            recorder.outputContactChannel = result
        }
        return result
    }

    companion object {
        @JvmStatic
        // input: AC-DK-GI-JX-OE-XZ ==> A maps to C, and C maps to A (and so on)
        //   A is 1st letter in alphabet --> index = 0 (0-based index)
        //   C is 3rd letter in alphabet --> index = 2
        // result: array[0] = C.toInt() - A.toInt() = 3 - 1 = 2
        //   meaning: A maps to a character 2 positions further on in the alphabet = C
        // result: array[2] = A.toInt() - C.toInt() = 1 - 3 = -2
        //   meaning: C maps to a character 2 positions earlier on in the alphabet = A
        fun optimize(connectedLetters: String): IntArray {
            var internalEncryptionTable = IntArray(26)

            // initialize internalEncryptionTable as if each letter is mapped onto itself
            for (index in 0..25) {
                internalEncryptionTable[index] = 0
            }

            // interpret the given letter mapping
            val pairs = connectedLetters.split('-')
            for (pair in pairs) {
                if (pair.length == 2) {
                    val letterA = pair.get(0)
                    val letterB = pair.get(1)
                    internalEncryptionTable[letterA.toInt() - 'A'.toInt()] = letterB.toInt() - letterA.toInt()
                    internalEncryptionTable[letterB.toInt() - 'A'.toInt()] = letterA.toInt() - letterB.toInt()
                }
            }
            return internalEncryptionTable
        }
    }

}