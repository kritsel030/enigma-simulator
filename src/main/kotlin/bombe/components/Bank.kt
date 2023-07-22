package bombe.components

import bombe.Bombe
import java.lang.IllegalStateException

class Bank (val id: Int, noOfScramblersPerBank: Int, noOfRotorsPerScrambler: Int, val bombe: Bombe) {

    // ***********************************************************************************************************
    // Contract a Bank of scramblers, and ever

    // construct a bank of scramblers
    // key starts at 1
    private val scramblers = mutableMapOf<Int, Scrambler>()

    fun getScramblers() : List<Scrambler> {
        return scramblers.values.toList()
    }

    fun getScrambler(index: Int) : Scrambler? {
        return scramblers[index]!!
    }
    // index is a 1-based index
    fun getScramblerJackPanel(index: Int) : ScramblerJackPanel {
        if (scramblers.containsKey(index)) {
            return scramblers[index]!!
        } else {
            throw IllegalStateException("you're asking for scrambler $index but there are only ${scramblers.size} scramblers in this bank")
        }
    }

//    init {
//        for (s in 1..noOfScramblersPerBank) {
//            // when the reflectorBoardBay associated with this bank has already been fitted with a a reflectorBoard,
//            // get a reflector from that board; otherwise the reflector remains null
//            val reflector = bombe.getReflectorBoardBay(id)?.reflectorBoard?.getReflector()
//            scramblers.put(s, Scrambler(s, noOfRotorsPerScrambler, reflector, this))
//        }
//    }

    // ***********************************************************************************************************
    // Features to support the plugging up of a bombe based on a menu

    // Fit each scrambler in this bank with the given set of drum types
    fun placeDrums(drumTypes: List<DrumType>) {
        for (scrambler in scramblers.values) {
            scrambler.placeDrums(drumTypes)
        }
    }

}