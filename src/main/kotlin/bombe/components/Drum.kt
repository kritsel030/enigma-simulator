package bombe.components

import enigma.components.Rotor
import enigma.util.Util
import shared.AbstractRotor
import shared.PlainLetterRing
import shared.RotorCore

class Drum(drumType: DrumType, startRingPosition: Char) :
    AbstractRotor(
        RotorCore(drumType.rotorType, Util.normalize(-drumType.coreOffset)),
        PlainLetterRing(),
        startRingPosition,
        startRingPosition.plus(drumType.coreOffset)) {

    constructor(drumType: DrumType) : this(drumType, 'Z')
}