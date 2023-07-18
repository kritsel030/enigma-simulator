package enigma.components.recorder

import enigma.components.ReflectorType
import shared.Util

class ReflectorStepRecorder (
    var reflectorType: ReflectorType? = null,

    var inputContactChannel: Int? = null,
    var outputContactChannel: Int? = null
): StepRecorder(){
    override fun toStringSimple(useCharacters:Boolean): String {
        if (useCharacters) {
            return "reflector $reflectorType | ${Util.toChar(inputContactChannel!!)} -> ${Util.toChar(outputContactChannel!!)}"
        } else {
            return "reflector $reflectorType | $inputContactChannel -> $outputContactChannel"
        }
    }

    override fun toStringVerbose(useCharacters:Boolean): String {
        var buf = StringBuffer()
        buf.append("reflector $reflectorType \n")
        if (useCharacters) {
            buf.append("input channel ${Util.toChar(inputContactChannel!!)} -> output channel ${Util.toChar(outputContactChannel!!)} \n")
        } else {
            buf.append("input channel $inputContactChannel -> output channel $outputContactChannel \n")
        }
        return buf.toString()
    }
}