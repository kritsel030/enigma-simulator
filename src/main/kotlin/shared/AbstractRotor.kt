package shared

import enigma.components.recorder.StepRecorder

/**
 * Example with a 5 letter rotor
 *
 *                              ---> rotor core contacts, the one marked with * is contact 0
 *                              |    (on an Enigma rotor this would usually be a red dot)
 *     outer letter ring <---   |   ---> rotor core contact IDs
 *                          |   |   |    (not actually printer on a rotor core, but used in the code)
 *
 *                          A   o  (3)
 *                         ---
 *                          E   o  (2)
 *  orientation/           ---
 *  reference point <---  | D | o  (1)
 *                         ---
 *                          C   o* (0)
 *                         ---
 *                          B   o  (4)
 *
 *  In this situation:
 *  - rotor.ringSetting     = C -> contact 0 of the inner rotor core is aligned with 'C' on the outer letter ring
 *  - rotorCore.orientation = 1 -> contact 1 is facing the orientation/reference point
 */

open class AbstractRotor(val rotorCore: RotorCore, open val letterRing: PlainLetterRing, val ringSetting: Char) {

    private val _ringSetting : Int = Util.toInt(ringSetting)

    fun getRotorType() : RotorType {
        return rotorCore.rotorType
    }

    fun startRingOrientation() : Char {
        return 'A'.plus(Util.normalize(rotorCore.startOrientation + _ringSetting, rotorCore.alphabetsize))
    }

    fun currentRingOrientation() : Char {
        return 'A'.plus(Util.normalize(rotorCore.currentOrientation + _ringSetting, rotorCore.alphabetsize))
    }

    fun rotateToStartOrientation(newStartRingOrientation: Char) {
        rotorCore.rotateToStartOffset(Util.normalize(Util.toInt(newStartRingOrientation) - _ringSetting,rotorCore.alphabetsize))
    }

    fun advanceStartOrientation() {
        rotorCore.increaseStartOffset()
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