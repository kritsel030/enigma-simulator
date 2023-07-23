package bombe

import bombe.Util.PluggingUpUtil
import bombe.components.Chain

class Stop (
    val rotor1RingStellung: Char,
    val rotor2RingStellung: Char,
    val rotor3RingStellung: Char,
    val chainInputLetter: Char,
    val possibleSteckerPartnersForChainInputLetter:List<Char>) {

    fun print() {
        println(toString())
    }
    override fun toString(): String {
//        return "[$activatedContact] $rotor1RingStellung $rotor2RingStellung $rotor3RingStellung : ${possibleSteckerPartnersForCentralLetter.joinToString()}"
        return "$rotor1RingStellung$rotor2RingStellung$rotor3RingStellung | $chainInputLetter:${possibleSteckerPartnersForChainInputLetter.joinToString("")}"
    }

}