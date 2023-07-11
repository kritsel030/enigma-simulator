package bombe

class Stop (
    val rotor1RingStellung: Char,
    val rotor2RingStellung: Char,
    val rotor3RingStellung: Char,
    val input: Char,
    val results:Map<Char, Boolean>) {

    fun print() {
        println("$input -> ??? @ $rotor3RingStellung$rotor2RingStellung$rotor1RingStellung")
    }
}