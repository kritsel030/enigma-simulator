package bombe

class Stop (
    val rotor1RingStellung: Char,
    val rotor2RingStellung: Char,
    val rotor3RingStellung: Char,
    val activatedContact: Char,
    val possibleSteckerPartnersForCentralLetter:List<Char>) {

    fun print() {
        println(toString())
    }
    override fun toString(): String {
        return "[$activatedContact] $rotor1RingStellung $rotor2RingStellung $rotor3RingStellung : ${possibleSteckerPartnersForCentralLetter.joinToString()}"
    }

    override fun hashCode(): Int {
        var result = rotor1RingStellung.hashCode()
        result = 31 * result + rotor2RingStellung.hashCode()
        result = 31 * result + rotor3RingStellung.hashCode()
        result = 31 * result + possibleSteckerPartnersForCentralLetter.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Stop

        if (rotor1RingStellung != other.rotor1RingStellung) return false
        if (rotor2RingStellung != other.rotor2RingStellung) return false
        if (rotor3RingStellung != other.rotor3RingStellung) return false
        if (possibleSteckerPartnersForCentralLetter != other.possibleSteckerPartnersForCentralLetter) return false

        return true
    }


}