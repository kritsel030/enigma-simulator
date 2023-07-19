package bombe

import bombe.components.*
import bombe.recorder.CurrentPathElement
import java.lang.IllegalStateException
import kotlin.math.pow

// TODO: make this dynamic?
const val COMMONSSETS_PER_COLUMN = 8

class Bombe(
    // physical construction parameters

    // the german enigma supported an alphabet consisting of 26 letters,
    // for demonstration purposes our bombe emulator can also be configured to support a smaller alphabet size
    // (e.g. 8 letters, from A to H)
    val alphabetSize : Int,

    // number of banks of scrambler in the bombe (in the Atlanta bombe this was 3)
    val noOfBanks : Int,

    // number of scramblers (a scrambler consists of 3 drums + reflector) per bank
    // (in the Atlanta this was 12)
    val noOfScramblersPerBank : Int,

    // we can support both 3 and 4 rotor scramblers
    val noOfRotorsPerScrambler: Int
) : BombeControlPanel {
    constructor(params: BombeConstructionParameters) : this(params.alphabetSize, params.noOfBanks, params.noOfScramblersPerBank, params.noOfRotorsPerScrambler)
    constructor() : this(BombeConstructionParameters.getBombeConstructionParameters(BombeTemplate.ATLANTA))

    // ******************************************************************************************************
    // Features needed to represent and access the components of a bombe
    // Including initialization/reset

    var chains = mutableMapOf<Int, Chain>()
    var banks = mutableMapOf<Int, Bank>()
    var diagonalBoards = mutableMapOf<Int, DiagonalBoard>()
    var commonsSetsColumns = mutableMapOf<Int, MutableList<CommonsSet>>()
    var cables = mutableListOf<Cable>()
    var bridges = mutableListOf<Bridge>()
    var indicatorDrums = mutableListOf<IndicatorDrum>()
    init {
        reset()
    }
    fun reset() {
        chains = mutableMapOf<Int, Chain>()
        for (b in 1..noOfBanks) {
            chains.put(b, Chain(b, this))
        }

        banks = mutableMapOf<Int, Bank>()
        for (b in 1..noOfBanks) {
            banks.put(b, Bank(b, noOfScramblersPerBank, noOfRotorsPerScrambler, this))
        }

        diagonalBoards = mutableMapOf<Int,DiagonalBoard>()
        for (b in 1..noOfBanks) {
            diagonalBoards.put(b, DiagonalBoard(b, this))
        }

        // each bombe has multiple columns of CommonsSets, one per bank
        // (note that a columns of CommonsSets is not physically connected to a bank,
        // it is just a way of organizing the Jacks of the CommonsSets on the back panel
        // of an actual bombe machine)
        commonsSetsColumns = mutableMapOf<Int,MutableList<CommonsSet>>()
        val claimedCommonsSets = mutableMapOf<Pair<Int, Char>, CommonsSet>()
        for (b in 1..noOfBanks) {
            val commonsSetList = mutableListOf<CommonsSet>()
            commonsSetsColumns.put(b, commonsSetList)
            for (c in 1 .. COMMONSSETS_PER_COLUMN) {
                // assign each CommonsSet a unique id, starting with 1
                commonsSetList.add(CommonsSet(commonsSetsColumns.map { it.value.size }.sum() + 1, this))
            }
        }

        // We imagine to have an unlimited supply of cables and bridges.
        // These are created on-the-fly whenever they are needed (as opposed to e.g. Scramblers and CommonsSets
        // which are pre-created and subsequently claimed when needed).
        // Because of this, resetting is as simple as 'throwing away' any previous set of cables/bridges
        // which were created in the previous run by re-initializing with an empty list.
        cables = mutableListOf<Cable>()
        bridges = mutableListOf<Bridge>()

        indicatorDrums = mutableListOf<IndicatorDrum>()
        for (i in 1..3) {
            indicatorDrums.add(IndicatorDrum(this))
        }

        drumRotations = 0
    }

    fun getChain(id:Int) : Chain {
        return chains.get(id)!!
    }

    fun getBank(id:Int) : Bank {
        return banks.get(id)!!
    }

    fun getDiagonalBoard(id:Int) : DiagonalBoard {
        return diagonalBoards.get(id)!!
    }

    // ***********************************************************************************************************
    // Bombe ontrol panel support

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

    val claimedCommonsSets = mutableMapOf<Pair<Int, Char>, CommonsSet>()
    /**
     * - column: there are multiple columns of CommonsSets available, you must indicate in which column
     *   you want to claim the first available one
     *   (this has no impact on the operation of the bombe, you can set-up a menu on bank 1 and use the
     *    CommonsSets in columns 2; it is more a matter of convention to use the commons which are close
     *     to the scramblers you're setting up for the menu)
     * - letter: each CommonsSet is used to represent a certain letter, for debugging/informational
     *   purposes we register this letter
     */
    fun claimAvailableCommonsSet(column: Int, letter:Char) : CommonsSet {
        if (claimedCommonsSets.containsKey(Pair(column, letter))) {
            throw IllegalStateException("commons column $column already has a CommonsSet for '$letter', it is not possible to claim another one for this same combination")
        }
        val availableCommonsSet = commonsSetsColumns.get(column)!!.filter{ it -> it.isAvailable()}.firstOrNull()
        if (availableCommonsSet == null) {
            throw IllegalStateException("[column $column] no free CommonsSet available anymore in this column; "+
                    "${commonsSetsColumns.get(column)!!.filter{ it -> !it.isAvailable()}.count()} have been claimed already")
        }
        availableCommonsSet.claimFor(letter)
        claimedCommonsSets.put(Pair(column, letter), availableCommonsSet)
        return availableCommonsSet
    }

    fun createCable() : Cable {
        val cable = Cable("cable-${cables.size+1}", this)
        cables.add(cable)
        return cable
    }

    fun createBridge() : Bridge {
        val bridge = Bridge("bridge-${bridges.size+1}", this)
        bridges.add(bridge)
        return bridge
    }

    // ******************************************************************************************************
    // Features needed to support executing a run on a bombe

    fun run (numberOfSteps: Int? = null, printStepResult: Boolean = false, printCurrentPath: Boolean = false) : List<Stop> {
        for (step in 1.. if (numberOfSteps != null) numberOfSteps!! else pow(alphabetSize,3) ) {
            val doubleInputStops = mutableListOf<Stop>()
            for (chain in chains.values) {
                if (chain.isOn()) {
                    var root = CurrentPathElement.createRoot(chain.getContactToActivate()!!)
                    chain.run(root)
                    if (printStepResult) {
                        chain.testRegisterConnectedTo?.printStatus()
                    }
                    if (printCurrentPath) {
                        println("chain ${chain.id} current path")
                        root.print()
                    }
                    val stop = checkResult(chain)
                    if (stop != null) {
                        if (!isDoubleInputOn()) {
                            stops.add(stop)
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
                }
            }
            resetCurrent()
            stepDrums()
        }
        return stops
    }

    /**
     * Remove voltage/current throughout the system, so we're prepared for the next step/drum-rotation
     * As our bombe-in-code only represents voltage/current as active contacts in connectors (Jacks and Plugs),
     * we only need to reset the current in these connectors.
     */
    fun resetCurrent() {
        // reset all connectors of all components
        banks.values.forEach{it.getScramblers().forEach { s -> s.resetCurrent()} }
        chains.values.forEach{it.resetCurrent()}
        diagonalBoards.values.forEach { it.resetCurrent() }
        commonsSetsColumns.values.forEach{it.forEach { c -> c.resetCurrent()}}
        cables.forEach { it.resetCurrent() }
        bridges.forEach {it.resetCurrent()}
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
        banks.values.forEach{it.getScramblers().forEach { it.internalScrambler?.getRotor(1)?.stepRotor() }}
        indicatorDrums[0].rotate()

        // every 26th rotation, all drums representing the middle rotor in an enigma machine (position 2) take a step as well
        // and the corresponding indicator drum takes a step
        if (drumRotations % alphabetSize == 0) {
            banks.values.forEach{it.getScramblers().forEach { it.internalScrambler?.getRotor(2)?.stepRotor() }}
            indicatorDrums[1].rotate()
        }

        // every 26*26th rotation, all drums representing the right rotor (position 3) in an enigma machine take a step as well
        // and the corresponding indicator drum takes a step
        if (drumRotations % (alphabetSize * alphabetSize) == 0) {
            banks.values.forEach{it.getScramblers().forEach { it.internalScrambler?.getRotor(3)?.stepRotor() }}
            indicatorDrums[2].rotate()
        }
    }

    /**
     * - steps: number of drum rotations done so far
     */
    private fun checkResult(chain: Chain) : Stop?{
//        for (chain in chains.values) {
            if (chain.isOn()) {
                val stepResult = chain.checkStepResult()
                // stepResult.first indicates whether the result of this step is a valid stop
                if (stepResult.first) {
//                    captureStop(chain, stepResult.second!!)
                    return Stop(indicatorDrums[0].position, indicatorDrums[1].position, indicatorDrums[2].position,
                        chain.getContactToActivate()!!, stepResult.second!!)
                }
            }
//        }
        return null
    }

    var stops : MutableList<Stop> = mutableListOf()
        private set
//    private fun captureStop(chain: Chain, possibleSteckerPartnersForCentralLetter : List<Char>) {
//        stops.add(Stop(indicatorDrums[0].position, indicatorDrums[1].position, indicatorDrums[2].position,
//            chain.getContactToActivate()!!, possibleSteckerPartnersForCentralLetter))
//    }

    // ******************************************************************************************************
    // Helper methods
    /**
     * Calculate 'base' to the power of 'power', assuming that the result will always fit in an Int
     */
    private fun pow(base: Int, power: Int) : Int {
        return (base.toDouble()).pow(power).toInt()
    }

}