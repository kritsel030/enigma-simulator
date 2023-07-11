package bombe

import bombe.components.Scrambler

class MenuLink (
    val positionInMenu : Int,
    val positionInMenuChain: Int,
    val inputChar: Char,
    val rotorOffset: Int,
    val outputChar: Char) {

    private var _scrambler: Scrambler? = null
    fun setScrambler(scrambler: Scrambler) {
        _scrambler = scrambler
    }
    fun getScrambler() : Scrambler {
        require(_scrambler != null) {"MenuLink has not yet been associated with a Scrambler"}
        return _scrambler!!
    }
}