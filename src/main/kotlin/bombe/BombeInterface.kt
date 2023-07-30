package bombe

import bombe.components.*

interface BombeInterface {

    // front side
    fun getIndicatorDrums() : List<IndicatorDrum>

    // panel on the right side of the bombe
    fun getBombeControlpanel() : BombeControlPanel?

    // panel on the right side of the bombe
    fun getChainControlPanel(id:Int) : ChainControlPanel?

    fun getChainControlPanels() : List<ChainControlPanel>

    // chain input jack on the back side of the bombe
    fun getChainJackPanel(id:Int) : ChainJackPanel?

    fun getChainJackPanels(): List<ChainJackPanel>
    fun getChainDisplay(id: Int) : ChainIndicator?

    fun getChainDisplays(): List<ChainIndicator>

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