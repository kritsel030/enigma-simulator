package shared

import enigma.components.recorder.StepRecorder
import enigma.util.Util

open class AbstractRotor(val rotorCore: RotorCore, open val letterRing: PlainLetterRing, val startRingOrientation: Char, val ringSetting: Char) {

    private val _ringSetting : Int = Util.toInt(ringSetting)

    fun getRotorType() : RotorType {
        return rotorCore.rotorType
    }

    // orientation?
    fun currentRingOrientation() : Char {
        return 'A'.plus(Util.normalize(rotorCore.currentOffset + _ringSetting))
    }

    fun rotateToRingOrientation(newStartRingOrientation: Char) {
        rotorCore.rotateToStartOffset(Util.normalize(Util.toInt(newStartRingOrientation) - _ringSetting))
    }

    /**
     * Advance this rotor one step
     */
    open fun stepRotor() : Boolean {
        rotorCore.stepRotor()
        return false
    }

    fun encryptRightToLeft(rightActiveContactOffset:Int, recorders:MutableList<StepRecorder>? = null) : Int {
        return rotorCore.encryptRightToLeft(rightActiveContactOffset, recorders)
    }

    fun encryptLeftToRight(leftActiveContactOffset:Int, recorders:MutableList<StepRecorder>? = null) : Int {
        return rotorCore.encryptLeftToRight(leftActiveContactOffset, recorders)
    }

    fun reset() {
        rotorCore.reset()
    }

    fun getNetSteps() : Int {
        return rotorCore.getNetSteps()
    }

}