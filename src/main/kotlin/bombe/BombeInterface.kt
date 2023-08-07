package bombe

import bombe.components.*

/**
 * Represents bombe features, controls, switches etc. which in reality are accessible to a bombe operator.
 * It basically provides access to everything needed to
 * - set-up a bombe for a particular menu
 * - run the bombbe
 * - when the bombe stops, read the output
 */
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
    // id starts with 1
    fun getReflectorBoardBay(id:Int) : ReflectorBoardBay?

    // diagonal board jack panels on the back side of the bombe
    fun getDiagonalBoardJackPanel(id:Int) : DiagonalBoardJackPanel?

    fun getDiagonalBoardJackPanels() : List<DiagonalBoardJackPanel>

    fun getCables() : List<Cable>

    fun getBridges() : List<Bridge>

}