package bombe

class Stop (
    val rotor1RingStellung: Char,
    val rotor2RingStellung: Char,
    val rotor3RingStellung: Char,
    val chainInputLetter: Char,

    // possibleSteckerPartnersForChainInputLetter
    val senseRelaySet:Map<Char, Boolean>) {

    fun getPotentialSteckerPartnersString() : String {
        if (senseRelaySet.filter { it.value }.count() == 1) {
            // return the key of the single relay which is true, the key represents the stecker partner
            // for the input letter
            return senseRelaySet.filter { it.value }.map { it.key }.toList().joinToString("")
        } else {
            // returns the keys of the relays which are false, each key represents a potential stecker partner
            // for the input letter
            return senseRelaySet.filter { !it.value }.map { it.key }.toList().joinToString("")
        }
    }

    fun print() {
        println(toString())
    }
    override fun toString(): String {
        return "$rotor1RingStellung$rotor2RingStellung$rotor3RingStellung | $chainInputLetter:${getPotentialSteckerPartnersString()}"
    }

}