package enigma.components.recorder

import enigma.util.Util

class PlugboardStepRecorder(
    var connectedLetters:String? = null,
    var inputContactChannel: Int? = null,
    var outputContactChannel: Int? = null
): StepRecorder(){
    override fun toStringSimple(useCharacters:Boolean): String {
        if (useCharacters) {
            return "plubboard | ${Util.toChar(inputContactChannel!!)} -> ${Util.toChar(outputContactChannel!!)}"
        } else {
            return "plugboard | $inputContactChannel -> $outputContactChannel"
        }
    }

    override fun toStringVerbose(useCharacters:Boolean): String {
        var buf = StringBuffer()
        buf.append("plugboard $connectedLetters \n")
        if (useCharacters) {
            buf.append("input channel ${Util.toChar(inputContactChannel!!)} -> output channel ${Util.toChar(outputContactChannel!!)} \n")
        } else {
            buf.append("input channel $inputContactChannel -> output channel $outputContactChannel \n")
        }
        return buf.toString()
    }
}