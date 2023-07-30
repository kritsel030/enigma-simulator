package bombe

class Stop (
    val rotor1RingStellung: Char,
    val rotor2RingStellung: Char,
    val rotor3RingStellung: Char,
    val chainInputLetter: Char,

    val possibleSearchLetters:List<Char>) {

    fun getPossibleSearchLettersString() : String {
        return possibleSearchLetters.joinToString("")
    }

    fun print() {
        println(toString())
    }
    override fun toString(): String {
        return "$rotor1RingStellung$rotor2RingStellung$rotor3RingStellung | $chainInputLetter:${getPossibleSearchLettersString()}"
    }

}