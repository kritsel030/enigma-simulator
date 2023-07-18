package enigma.components

import shared.RotorType
import shared.PlainLetterRing

class LetterRingWithNotches(val rotorType: RotorType) : PlainLetterRing() {

    fun getTurnoverPoints() : List<Char>{
        return listOf<Char>(rotorType.turnoverPosition)
    }
}