package bombe

import bombe.components.Scrambler

class MenuLink (
    val positionInMenu : Int,
    val positionInMenuChain: Int,
    val inputLetter: Char,
    val rotorOffset: Int,
    val outputLetter: Char) {
}