package bombe.components

import bombe.connectors.Jack

interface DiagonalBoardJackPanel {

    fun getJack(letter: Char) : Jack

    fun getJacks() : List<Jack>
}