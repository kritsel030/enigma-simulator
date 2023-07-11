package bombe.scramblerelements

class Drum {

    // these maps indicate which contacts are live
    // outer ring - input
    val firstRingContacts = mutableMapOf<Char, Boolean>()
    // output of incoming scramble step
    val secondRingContacts = mutableMapOf<Char, Boolean>()
    // input for outgoing scramble step
    val thirdRingContacts = mutableMapOf<Char, Boolean>()
    // inner ring - output
    val fourthRingContacts = mutableMapOf<Char, Boolean>()

    fun passCurrent(contact:Char) {

    }


}