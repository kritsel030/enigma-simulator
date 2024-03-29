package bombe

import bombe.components.*
import bombe.sensingcircuit.BombeSensingCircuit
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
    constructor(params: BombeConstructionParameters, reflectorType: ReflectorType = DEFAULT_REFLECTOR_TYPE) : this(
        params.alphabetSize,
        params.noOfChains,
        params.noOfBanks,
        params.noOfScramblersPerBank,
        params.noOfRotorsPerScrambler,
        params.noOfCommonsSetsPerBank,
        reflectorType
    )

    constructor(reflectorType: ReflectorType = DEFAULT_REFLECTOR_TYPE) : this(
        BombeConstructionParameters.getBombeConstructionParameters(
            BombeTemplate.ATLANTA
        ), reflectorType
    )

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
    private var indicatorDrums = mutableListOf<IndicatorDrum>()
    var sensingCircuit = BombeSensingCircuit(this)
    var mainCircuit = MainCircuit(this)

//    // represents all connectors (jacks and plugs) attached to any component of the bombe
//    // this includes 'mobile components' like cables and bridges
//    private var allConnectors = mutableListOf<Connector>()

    init {
        reset()
    }

    fun reset() {
        for (c in chains.keys) {
            chains.remove(c)
            sensingCircuit.removeCircuitForChainId(c.toString())
        }
        for (c in 1..noOfChains) {
            val chain = Chain(c, this)
            chains.put(c, chain)
            sensingCircuit.addOrReplaceCircuitForChainId(chain)
        }

        scramblers = mutableMapOf()
        for (b in 1..noOfBanks) {
            for (s in 1..noOfScramblersPerBank) {
                val scramblerId = ((b - 1) * noOfScramblersPerBank) + s
                scramblers.put(scramblerId, Scrambler(scramblerId, noOfRotorsPerScrambler, null, this))
            }
        }

        reflectorBoardBays = mutableMapOf()
        for (b in 1..noOfBanks) {
            val reflectorBoardBay = ReflectorBoardBay(b, this)
            reflectorBoardBay.placeReflectorBoard(ReflectorBoard(initialReflectorType))
            reflectorBoardBays.put(b, reflectorBoardBay)
        }

        diagonalBoards = mutableMapOf<Int, DiagonalBoard>()
        for (b in 1..noOfBanks) {
            diagonalBoards.put(b, DiagonalBoard(b, this))
        }

        _commonsSets = mutableMapOf()
        for (b in 1..noOfBanks) {
            for (cs in 1..noOfCommonsSetsPerBank) {
                val id = ((b - 1) * noOfCommonsSetsPerBank) + cs
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

//        sensingCircuit = BombeSensingCircuit(this, chains)

//        // needed to initialize the sensing circuit (bombe.sensingCircuit must have been set before executing this part)
//        chains.values.forEach { chain -> chain.chainInputContacts.keys.forEach { chain.swichOffSearchLetter(it) }}

    }

    override fun getIndicatorDrums() : List<IndicatorDrum> {
        return indicatorDrums
    }

    override fun getBombeControlpanel(): BombeControlPanel? {
        return this
    }

    fun getChain(id: Int): Chain? {
        return chains.get(id)
    }

    override fun getChainControlPanel(id: Int): ChainControlPanel? {
        return getChain(id)
    }

    override fun getChainControlPanels() : List<ChainControlPanel> {
        return chains.values.toList()
    }

    override fun getChainJackPanel(id: Int): ChainJackPanel? {
        return getChain(id)
    }

    override fun getChainJackPanels(): List<ChainJackPanel> {
        return chains.values.toList()
    }

    override fun getChainDisplay(id: Int): ChainIndicator? {
        return getChain(id)
    }

    override fun getChainDisplays(): List<ChainIndicator> {
        return chains.values.toList()
    }

    fun getScrambler(id: Int): Scrambler? {
        return scramblers.get(id)
    }

    fun getScramblers(): List<Scrambler> {
        return scramblers.values.toList()
    }

    // both bankId and scramblerIndexId are 1-based
    // scramblerIndexId is the sequence number of a scrambler *within a scrambler*
    fun getScrambler(bankId: Int, scramblerIndexId: Int): Scrambler? {
        return scramblers.get(((bankId - 1) * noOfScramblersPerBank) + scramblerIndexId)
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

    override fun getReflectorBoardBay(id: Int): ReflectorBoardBay? {
        return reflectorBoardBays.get(id)
    }

    override fun getDiagonalBoardJackPanel(id: Int): DiagonalBoardJackPanel? {
        return diagonalBoards.get(id)
    }

    override fun getDiagonalBoardJackPanels(): List<DiagonalBoardJackPanel> {
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

    fun getCommonsSets(): List<CommonsSet> {
        return _commonsSets.values.toList()
    }

    // both columnId and commonsSetIndexInColumn are 1-based
    // commonsSetIndexInColumn is the sequence number of a scrambler *within a scrambler*
    fun getCommonsSet(columnId: Int, commonsSetIndexInColumn: Int): CommonsSet? {
        return _commonsSets.get(((columnId - 1) * noOfCommonsSetsPerBank) + commonsSetIndexInColumn)
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

    override fun isDoubleInputOn(): Boolean {
        return doubleInputOn
    }

    // ******************************************************************************************************
    // Features needed to support setting up a menu on the back-side of a bombe

    fun createCable(): Cable {
        val cable = Cable("cable-${_cables.size + 1}", this)
        _cables.add(cable)
        return cable
    }

    fun createBridge(): Bridge {
        val bridge = Bridge("bridge-${_bridges.size + 1}", this)
        _bridges.add(bridge)
        return bridge
    }

    // ******************************************************************************************************
    // Features needed to support executing a run on a bombe
    fun start(
        numberOfSteps: Int? = null,
        printStepResult: Boolean = false,
        printCurrentPath: Boolean = false
    ): Boolean {
        // reset the indicator relays for all chains
        chains.values.filter { it.isOn() }.forEach { it.resetSearchLetterIndicatorRelays() }
        while (true) {
            // A.reset the current in the entire system
            mainCircuit.powerDown()

            // B. step the drum(s)
            stepDrums()

            // C. power up the main circuit
            mainCircuit.powerUp()

            // D. has the sensing circuit detected a stop?
            var stop = sensingCircuit.shouldBombeStop(printCurrentPath)
            if (stop) {
                return true
            }

            // E. extra stop conditions
            // these are not features of an actual bombe, they are only present in this bombe simulator to
            // a) prevent a bombe from forever running when no stop is determined
            // b) instruct a bombe to only set a single step, for test-purposes only
            if (allRotationsDone() || (numberOfSteps != null && drumRotations < numberOfSteps)) {
                return false
            }
        }
        return true
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
        scramblers.values.forEach { it.letchworthEnigma?.getRotor(1)?.stepRotor() }
        indicatorDrums[0].rotate()

        // every 26th rotation, all drums representing the middle rotor in an enigma machine (position 2) take a step as well
        // and the corresponding indicator drum takes a step
        if (drumRotations % alphabetSize == 0) {
            scramblers.values.forEach { it.letchworthEnigma?.getRotor(2)?.stepRotor() }
            indicatorDrums[1].rotate()
        }

        // every 26*26th rotation, all drums representing the right rotor (position 3) in an enigma machine take a step as well
        // and the corresponding indicator drum takes a step
        if (drumRotations % (alphabetSize * alphabetSize) == 0) {
            scramblers.values.forEach { it.letchworthEnigma?.getRotor(3)?.stepRotor() }
            indicatorDrums[2].rotate()
        }
    }

    fun allRotationsDone(): Boolean {
        return drumRotations >= pow(alphabetSize, noOfRotorsPerScrambler)
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