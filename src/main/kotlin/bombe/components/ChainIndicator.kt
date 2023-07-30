package bombe.components

interface ChainIndicator {

    fun getId() : Int
    fun readSearchLetterIndicators() : Map<Char, Boolean>

    fun readSearchLetters() : List<Char>
}