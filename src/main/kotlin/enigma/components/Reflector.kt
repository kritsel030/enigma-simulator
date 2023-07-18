package enigma.components

import enigma.components.recorder.StepRecorder
import enigma.components.recorder.ReflectorStepRecorder
import shared.Util.Companion.normalize
import shared.Util.Companion.toInt
import shared.Util.Companion.toChar
import shared.Util.Companion.validate
import java.lang.IllegalArgumentException

class Reflector (
    val reflectorType: ReflectorType
) {
    private val _encryptionTable = optimize(reflectorType.encryptionTable)

    fun encrypt(inputChannel:Char, recorders:MutableList<StepRecorder>? = null) : Char {
        if (!validate(inputChannel)) {
            throw IllegalArgumentException("input must be a character between A and Z (capital!)")
        }
        return toChar(encrypt(toInt(inputChannel), recorders))
    }

    fun encrypt(contactChannel:Int, recorders:MutableList<StepRecorder>? = null) : Int {
        val result = normalize(contactChannel + _encryptionTable[contactChannel])

        if (recorders != null) {
            var recorder = ReflectorStepRecorder()
            recorders.add(recorder)
            recorder.reflectorType = reflectorType
            recorder.inputContactChannel = contactChannel
            recorder.outputContactChannel = result
        }
        return result
    }

    companion object {
        @JvmStatic
        // input: B maps to R
        //   B is 2nd letter in alphabet --> index = 1 (0-based index)
        //   R is 18th letter in alphabet
        // result: array[1] = R.toInt() - B.toInt() = 18 - 2 = 16
        //   meaning: B maps to a character 16 positions further on in the alphabet = R
        fun optimize(encryptionTable: String): IntArray {
            var internalEncryptionTable = IntArray(26)

            var pos = 0
            while (pos < encryptionTable.length) {
                internalEncryptionTable[pos] = encryptionTable[pos].toInt() - ('A'.toInt() + pos)
                pos++
            }
            return internalEncryptionTable
        }
    }
}