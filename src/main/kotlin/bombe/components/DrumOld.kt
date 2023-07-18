package bombe.components

import enigma.components.Rotor

class DrumOld(drumType: DrumType, startRingPosition: Char) : Rotor(drumType.rotorType, startRingPosition, startRingPosition.plus(drumType.coreOffset)) {

    constructor(drumType: DrumType) : this(drumType, 'Z')
}