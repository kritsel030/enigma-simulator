package bombe

import bombe.components.*
import bombe.recorder.CurrentPathElement
import enigma.components.ReflectorType
import java.lang.IllegalStateException
import kotlin.math.pow

interface BombeInterface {

    // panel on the right side of the bombe
    fun getBombeControlpanel() : BombeControlPanel?

    // panel on the right side of the bombe
    fun getChainControlPanel(id:Int) : ChainControlPanel?

    // chain input jack on the back side of the bombe
    fun getChainJackPanel(id:Int) : ChainJackPanel?

    fun getChainJackPanels() : List<ChainJackPanel>

    fun getChainDisplay(id: Int) : ChainDisplay?

    fun getScramblerJackPanel(id: Int): ScramblerJackPanel?

    fun getScramblerJackPanels(): List<ScramblerJackPanel>

    fun getScramblerJackPanel(bankId: Int, scramblerIndexId: Int) : ScramblerJackPanel?

    // reflector board bays on the left side of the bombe
    fun getReflectorBoardBay(id:Int) : ReflectorBoardBay?

    // diagonal board jack panels on the back side of the bombe
    fun getDiagonalBoardJackPanel(id:Int) : DiagonalBoardJackPanel?

    fun getDiagonalBoardJackPanels() : List<DiagonalBoardJackPanel>

    fun getCables() : List<Cable>

    fun getBridges() : List<Bridge>

}