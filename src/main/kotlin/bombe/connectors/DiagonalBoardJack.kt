package bombe.connectors

import bombe.components.DiagonalBoard

class DiagonalBoardJack (val letter:Char, diagonalBoard: DiagonalBoard):
    Jack(letter.toString(), letter.toString(), diagonalBoard) {

}