package bombe

import bombe.components.ReflectorBoard

interface ReflectorBoardControlPanel {

    fun changeReflectorBoard (reflectorBoard: ReflectorBoard)

    fun placeReflectorBoard(reflectorBoard: ReflectorBoard)

    fun removeReflectorBoard()
}