package bombe.components

import bombe.Bombe
import bombe.ReflectorBoardControlPanel

/**
 * The left side of the bombe has several reflector board bays, each bay being wired to a particular bank
 * of scramblers.
 * Each bay can hold a reflect board. That board holds the reflectors used by the scramblers/enigmas in the bank
 * the bay is wired to.
 */
class ReflectorBoardBay(val id:Int, val bombe:Bombe) : ReflectorBoardControlPanel {
    var reflectorBoard : ReflectorBoard? = null

    override fun changeReflectorBoard(reflectorBoard: ReflectorBoard) {
        this.reflectorBoard = reflectorBoard
        removeReflectorsOfBankScramblers()
        setReflectorsOfBankScramblers()
    }

    override fun placeReflectorBoard(reflectorBoard: ReflectorBoard) {
        this.reflectorBoard = reflectorBoard
        setReflectorsOfBankScramblers()
    }

    override fun removeReflectorBoard() {
        removeReflectorsOfBankScramblers()
    }

    private fun removeReflectorsOfBankScramblers() {
        // each reflector board is associated with the scramblers in a bank with the same id
        bombe.getScramblers()?.forEach { it.setReflector(null) }
    }

    private fun setReflectorsOfBankScramblers() {
        // each reflector board is associated with the scramblers in a bank with the same id
        bombe.getScramblers()?.forEach { it.setReflector(reflectorBoard!!.getReflector()) }
    }
}