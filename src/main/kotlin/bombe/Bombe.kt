package bombe

import bombe.Util.PluggingUpUtil
import bombe.components.*
import bombe.recorder.CurrentPathElement
import enigma.components.ReflectorType
import kotlin.math.pow

// TODO: make this dynamic?
const val COMMONSSETS_PER_COLUMN = 8

val DEFAULT_REFLECTOR_TYPE = ReflectorType.B

class Bombe (
    // physical construction parameters

    // the german enigma supported an alphabet consisting of 26 letters,
    // for demonstration purposes our bombe emulator can also be configured to support a smaller alphabet size
    // (e.g. 8 letters, from A to H)
    val alphabetSize : Int,

    val noOfChains: Int,

    // number of banks of scrambler in the bombe (in the Atlanta bombe this was 3)
    val noOfBanks : Int,

    // number of scramblers (a scrambler consists of 3 drums + reflector) per bank
    // (in the Atlanta this was 12)
    val noOfScramblersPerBank : Int,

    // we can support both 3 and 4 rotor scramblers
    val noOfRotorsPerScrambler: Int,

    val noOfCommonsSetsPerBank: Int,

    val initialReflectorType: ReflectorType
) : BombeControlPanel, BombeInterface {
    constructor(params: BombeConstructionParameters, reflectorType: ReflectorType = DEFAULT_REFLECTOR_TYPE) : this(params.alphabetSize, params.noOfChains, params.noOfBanks, params.noOfScramblersPerBank, params.noOfRotorsPerScrambler, params.noOfCommonsSetsPerBank, reflectorType)
    constructor(reflectorType: ReflectorType = DEFAULT_REFLECTOR_TYPE) : this(BombeConstructionParameters.getBombeConstructionParameters(BombeTemplate.ATLANTA), reflectorType)

    // ******************************************************************************************************
    // Features needed to represent and access the components of a bombe
    // Including initialization/reset

    private var chains = mutableMapOf<Int, Chain>()
    private var scramblers = mutableMapOf<Int, Scrambler>()
    private var reflectorBoardBays = mutableMapOf<Int, ReflectorBoardBay>()
    private var diagonalBoards = mutableMapOf<Int, DiagonalBoard>()
    private var _commonsSets = mutableMapOf<Int, CommonsSet>()
    private var _cables = mutableListOf<Cable>()
    private var _bridges = mutableListOf<Bridge>()
    var indicatorDrums = mutableListOf<IndicatorDrum>()
    init {
        reset()
    }
    fun reset() {
        chains = mutableMapOf<Int, Chain>()
        for (c in 1..noOfChains) {
            chains.put(c, Chain(c, this))
        }

        scramblers = mutableMapOf()
        for (b in 1..noOfBanks) {
            for (s in 1..noOfScramblersPerBank) {
                val scramblerId = ((b-1) * noOfScramblersPerBank) + s
                scramblers.put(scramblerId, Scrambler(scramblerId, noOfRotorsPerScrambler, null, this))
            }
        }

        reflectorBoardBays = mutableMapOf()
        for (b in 1..noOfBanks) {
            val reflectorBoardBay = ReflectorBoardBay(b, this)
            reflectorBoardBay.placeReflectorBoard(ReflectorBoard(initialReflectorType))
            reflectorBoardBays.put(b, reflectorBoardBay)
        }

        diagonalBoards = mutableMapOf<Int,DiagonalBoard>()
        for (b in 1..noOfBanks) {
            diagonalBoards.put(b, DiagonalBoard(b, this))
        }

        _commonsSets = mutableMapOf()
        for (b in 1..noOfBanks) {
            for (cs in 1..noOfCommonsSetsPerBank) {
                val id = ((b-1) * noOfCommonsSetsPerBank) + cs
                _commonsSets.put(id, CommonsSet(id, this))
            }
        }

        // We imagine to have an unlimited supply of cables and bridges.
        // These are created on-the-fly whenever they are needed (as opposed to e.g. Scramblers and CommonsSets
        // which are pre-created and subsequently claimed when needed).
        // Because of this, resetting is as simple as 'throwing away' any previous set of cables/bridges
        // which were created in the previous run by re-initializing with an empty list.
        _cables = mutableListOf<Cable>()
        _bridges = mutableListOf<Bridge>()

        indicatorDrums = mutableListOf<IndicatorDrum>()
        for (i in 1..3) {
            indicatorDrums.add(IndicatorDrum(this))
        }

        drumRotations = 0
    }

    override fun getBombeControlpanel(): BombeControlPanel? {
        return this
    }

    fun getChain(id:Int) : Chain? {
        return chains.get(id)
    }
    override fun getChainControlPanel(id:Int) : ChainControlPanel? {
        return getChain(id)
    }

    override fun getChainJackPanel(id:Int) : ChainJackPanel? {
        return getChain(id)
    }

    override fun getChainJackPanels() : List<ChainJackPanel> {
        return chains.values.toList()
    }

    override fun getChainDisplay(id:Int) : ChainDisplay? {
        return getChain(id)
    }

    fun getScrambler(id: Int): Scrambler? {
        return scramblers.get(id)
    }

    fun getScramblers() : List<Scrambler> {
        return scramblers.values.toList()
    }

    // both bankId and scramblerIndexId are 1-based
    // scramblerIndexId is the sequence number of a scrambler *within a scrambler*
    fun getScrambler(bankId: Int, scramblerIndexId: Int): Scrambler? {
        return scramblers.get( ((bankId-1) * noOfScramblersPerBank) + scramblerIndexId)
    }

    override fun getScramblerJackPanel(id: Int): ScramblerJackPanel? {
        return scramblers.get(id)
    }

    override fun getScramblerJackPanel(bankId: Int, scramblerIndexId: Int): ScramblerJackPanel? {
        return getScrambler(bankId, scramblerIndexId)
    }

    override fun getScramblerJackPanels(): List<ScramblerJackPanel> {
        return scramblers.values.toList()
    }

    override fun getReflectorBoardBay(id:Int) : ReflectorBoardBay? {
        return reflectorBoardBays.get(id)
    }

    override fun getDiagonalBoardJackPanel(id:Int) : DiagonalBoardJackPanel? {
        return diagonalBoards.get(id)
    }

    override fun getDiagonalBoardJackPanels() : List<DiagonalBoardJackPanel> {
        return diagonalBoards.values.toList()
    }

    override fun getCables(): List<Cable> {
        return _cables
    }

    override fun getBridges(): List<Bridge> {
        return _bridges
    }


    fun getCommonsSet(id: Int): CommonsSet? {
        return _commonsSets.get(id)
    }

    fun getCommonsSets() : List<CommonsSet> {
        return _commonsSets.values.toList()
    }

    // both columnId and commonsSetIndexInColumn are 1-based
    // commonsSetIndexInColumn is the sequence number of a scrambler *within a scrambler*
    fun getCommonsSet(columnId: Int, commonsSetIndexInColumn: Int): CommonsSet? {
        return _commonsSets.get( ((columnId-1) * noOfCommonsSetsPerBank) + commonsSetIndexInColumn)
    }

    // ***********************************************************************************************************
    // Bombe control panel support

    private var doubleInputOn: Boolean = false

    override fun switchDoubleInputOn() {
        doubleInputOn = true
    }
    override fun switchDoubleInputOff() {
        doubleInputOn = false
    }
    override fun isDoubleInputOn() : Boolean {
        return doubleInputOn
    }

    // ******************************************************************************************************
    // Features needed to support setting up a menu on the back-side of a bombe

    fun createCable() : Cable {
        val cable = Cable("cable-${_cables.size+1}", this)
        _cables.add(cable)
        return cable
    }

    fun createBridge() : Bridge {
        val bridge = Bridge("bridge-${_bridges.size+1}", this)
        _bridges.add(bridge)
        return bridge
    }

    // ******************************************************************************************************
    // Features needed to support executing a run on a bombe

    var stops : MutableList<Stop> = mutableListOf()
        private set
    fun run (numberOfSteps: Int? = null, printStepResult: Boolean = false, printCurrentPath: Boolean = false) : List<Stop> {
        for (step in 1.. if (numberOfSteps != null) numberOfSteps!! else pow(alphabetSize,3) ) {
            val doubleInputStops = mutableListOf<Stop>()
            for ((index, chain) in chains.values.withIndex()) {
                if (chain.isOn()) {
                    var root = CurrentPathElement.createRoot(chain.getContactToActivate()!!)
                    chain.run(root)
                    if (printCurrentPath) {
                        println("chain ${chain.id} current path")
                        root.print()
                    }
                    val stop = checkResult(chain)
                    if (stop != null) {
                        if (!isDoubleInputOn()) {
                            stops.add(stop)
                            // fill test register with a copy of the input jack contacts
//                            chain.fillTestRegister(chain.getInputJack().readContacts().toMap())
                        } else {
                            doubleInputStops.add(stop)
                        }
                    }
                }
            }
            if (isDoubleInputOn()) {
                // when 'double input' is switch on, all active chains must have produced a stop
                if (doubleInputStops.size == chains.values.filter { it.isOn() }.count()) {
                    stops.addAll(doubleInputStops)
                    // fill test registers with a copy of the chain input jack contacts
//                    getChain(1)!!.fillTestRegister(getChain(1)!!.getInputJack().readContacts().toMap())
//                    getChain(2)!!.fillTestRegister(getChain(2)!!.getInputJack().readContacts().toMap())
                }
            }
            resetCurrent()
            stepDrums()
        }
        return stops
    }

    var drumRotations = 0
        private set
    // https://www.codesandciphers.org.uk/virtualbp/tbombe/thebmb.htm
    // "the top, fast, drum on the Bombe corresponds to the slow left hand drum on the Enigma machine"
    // meaning: the (top) fast-moving drum on a bombe corresponds with the (left) slow-moving rotor
    fun stepDrums() {
        drumRotations++
        // every drum rotation, all drums representing the left rotor (position 1) in an enigma machine take a step
        // and the corresponding indicator drum takes a step
        scramblers.values.forEach { it.enigma?.getRotor(1)?.stepRotor() }
        indicatorDrums[0].rotate()

        // every 26th rotation, all drums representing the middle rotor in an enigma machine (position 2) take a step as well
        // and the corresponding indicator drum takes a step
        if (drumRotations % alphabetSize == 0) {
            scramblers.values.forEach { it.enigma?.getRotor(2)?.stepRotor() }
            indicatorDrums[1].rotate()
        }

        // every 26*26th rotation, all drums representing the right rotor (position 3) in an enigma machine take a step as well
        // and the corresponding indicator drum takes a step
        if (drumRotations % (alphabetSize * alphabetSize) == 0) {
            scramblers.values.forEach { it.enigma?.getRotor(3)?.stepRotor() }
            indicatorDrums[2].rotate()
        }
    }

    private fun checkResult(chain: Chain) : Stop?{
        val stepResult = chain.checkStepResult()
        // stepResult.first indicates whether the result of this step is a valid stop
        if (stepResult.first) {
            return Stop(indicatorDrums[0].position, indicatorDrums[1].position, indicatorDrums[2].position,
                determineChainInputLetter(chain), stepResult.second!!)
        }
        return null
    }

    private fun determineChainInputLetter(chain: Chain) : Char {
        return PluggingUpUtil.findConnectedDiagonalBoardJack(chain.getInputJack())!!.letter
    }

    // ***************************************************************************************************************
    // Reset methods

    /**
     * Remove voltage/current throughout the system, so we're prepared for the next step/drum-rotation
     * As our bombe-in-code only represents voltage/current as active contacts in connectors (Jacks and Plugs),
     * we only need to reset the current in these connectors.
     */
    fun resetCurrent() {
        // reset all connectors of all components
        scramblers.values.forEach{it.resetCurrent() }
        chains.values.forEach{it.resetCurrent()}
        diagonalBoards.values.forEach { it.resetCurrent() }
        _commonsSets.values.forEach{it.resetCurrent()}
        _bridges.forEach {it.resetCurrent()}
        _cables.forEach { it.resetCurrent() }
    }

    // ******************************************************************************************************
    // Helper methods
    /**
     * Calculate 'base' to the power of 'power', assuming that the result will always fit in an Int
     */
    private fun pow(base: Int, power: Int) : Int {
        return (base.toDouble()).pow(power).toInt()
    }

}