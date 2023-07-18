package enigma.components.recorder

import shared.RotorType
import enigma.util.Util

class RotorStepRecorder (
    var rotorType: RotorType? = null,
    var rotorPosition: Char? = null,
    var rotorRingSetting: Int? = null,
    var rightToLeft: Boolean? = null,

    var inputContactChannel: Int? = null,
    var inputValue: Int? = null,
    var outputValue: Int? = null,
    var outputContactChannel: Int? = null
): StepRecorder(){
    override fun toStringSimple(useCharacters:Boolean): String {
        if (useCharacters) {
            return "rotor $rotorType     | ${Util.toChar(inputContactChannel!!)} -> ${Util.toChar(outputContactChannel!!)}"
        } else {
            return "rotor $rotorType     | $inputContactChannel -> $outputContactChannel"
        }
    }

    override fun toStringVerbose(useCharacters:Boolean): String {
        var buf = StringBuffer()
        buf.append("rotor $rotorType | position $rotorPosition | ring setting $rotorRingSetting \n")
        if (rightToLeft!!) {
            buf.append("direction right-to-left \n")
            if (useCharacters) {
                buf.append("R channel ${Util.toChar(inputContactChannel!!)} -> R contact ${Util.toChar(inputValue!!)} -> L contact ${Util.toChar(outputValue!!)} -> L channel ${Util.toChar(outputContactChannel!!)} \n")
            } else {
                buf.append("R channel $inputContactChannel -> R contact $inputValue -> L contact $outputValue -> L channel $outputContactChannel \n")

            }
        }else {
            buf.append("direction left-to-right \n")
            if (useCharacters) {
                buf.append("L channel ${Util.toChar(inputContactChannel!!)} -> L contact ${Util.toChar(inputValue!!)} -> R contact ${Util.toChar(outputValue!!)} -> R channel ${Util.toChar(outputContactChannel!!)} \n")
            } else {
                buf.append("L channel $inputContactChannel -> L contact $inputValue -> R contact $outputValue -> R channel $outputContactChannel \n")
            }
        }
        return buf.toString()
    }

}