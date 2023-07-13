package bombe

import enigma.components.RotorType

class Stop (
    val rotor1RotorType: RotorType,
    val rotor2RotorType: RotorType,
    val rotor3RotorType: RotorType,
    val rotor1RingStellung: Char,
    val rotor2RingStellung: Char,
    val rotor3RingStellung: Char,
    val centralLetter: Char,
    val possibleSteckerPartnersForCentralLetter:List<Char>) {

    fun print() {
        println("[for rotor order $rotor1RotorType-$rotor2RotorType-$rotor3RotorType] $centralLetter -> ${possibleSteckerPartnersForCentralLetter.joinToString()} @ $rotor1RingStellung$rotor2RingStellung$rotor3RingStellung")
    }
}