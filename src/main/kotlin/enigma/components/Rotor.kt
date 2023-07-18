package enigma.components

import enigma.util.Util
import shared.AbstractRotor
import shared.RotorCore
import shared.RotorType

/**
 * An Enigma rotor is a composition of
 * - an outer letter ring with one (or more) notches
 * - an inner rotor core
 * - the inner rotor core is fixed into a certain position within the outer ring;
 *   this position is specified by the letter on the outer ring which is aligned with the first contact of the rotor core
 *   (contact ID 0); this letter is identified by 'ringSetting'
 */
open class Rotor private constructor (rotorType: RotorType, override val letterRing: LetterRingWithNotches, startRingOrientation: Char, ringSetting: Char) :
    AbstractRotor(
        RotorCore(rotorType, Util.normalize(Util.toInt(startRingOrientation) - Util.toInt(ringSetting))),
        letterRing,
        startRingOrientation,
        ringSetting) {

    constructor(rotorType: RotorType, startRingOrientation: Char, ringSetting: Char) : this(rotorType, LetterRingWithNotches(rotorType), startRingOrientation, ringSetting)

    /**
     * Advance this rotor one step
     *
     * When the rotor reaches its turnover position, the rotor to its left should step as well
     * https://en.wikipedia.org/wiki/Enigma_machine#Turnover
     *
     * result: true when the rotor to the left should step as well
     */
    override fun stepRotor() : Boolean {
        rotorCore.stepRotor()
        return letterRing.getTurnoverPoints().contains(currentRingOrientation())
    }

}