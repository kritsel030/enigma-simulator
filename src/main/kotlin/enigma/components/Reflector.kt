package enigma.components

import enigma.components.recorder.StepRecorder
import enigma.components.recorder.ReflectorStepRecorder
import enigma.util.Util.Companion.normalize
import enigma.util.Util.Companion.toInt
import enigma.util.Util.Companion.toChar
import enigma.util.Util.Companion.validate
import java.lang.IllegalArgumentException

class Reflector (
    val reflectorType: ReflectorType
) {
    private val optimizedEncryptionTable = optimize(reflectorType.encryptionTable)

    fun encryptContact(inputChannel:Char, recorders:MutableList<StepRecorder>?) : Char {
        if (!validate(inputChannel)) {
            throw IllegalArgumentException("input must be a character between A and Z (capital!)")
        }
        return toChar(encryptContact(toInt(inputChannel), recorders))
    }

    fun encryptContact(contactChannel:Int, recorders:MutableList<StepRecorder>?) : Int {
        val result = normalize(contactChannel + optimizedEncryptionTable[contactChannel])

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
            var optimizedEncryptionTable = IntArray(26)

            var pos = 0
            while (pos < encryptionTable.length) {
                optimizedEncryptionTable[pos] = encryptionTable[pos].toInt() - ('A'.toInt() + pos)
                pos++
            }
            return optimizedEncryptionTable
        }
    }
}