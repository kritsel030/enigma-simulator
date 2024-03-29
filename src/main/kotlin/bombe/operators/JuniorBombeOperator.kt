package bombe.operators

import bombe.Bombe
import bombe.BombeInterface
import bombe.StopSlip
import bombe.Util.PluggingUpUtil
import bombe.components.*
import bombe.connectors.Jack

/**
 * https://en.wikipedia.org/wiki/Women_in_Bletchley_Park
 *
 * Please meet Amanda, our junior bombe operator. She recently arrived at Bletchly Park and has only had some basic training.
 *
 *  She can help you to plug up a bombe, but you will have to give her detailed instructions like
 * 'draw a cable from this jack to that jack'.
 *
 * If you need a more experience bombe operator, you check out her colleagues:
 * - MediumBombeOperator
 * - ExpertBombeOperator
 */
open class JuniorBombeOperator(private val bombe: Bombe) {

    fun getBombeInterface(): BombeInterface {
        return getBombe()
    }

    protected fun getBombe(): Bombe {
        return bombe
    }

    // *************************************************************************************************
    // Bombe back side set-up features

    fun attachBridgeTo(
        firstScramblerOutputJack: Jack,
        nextScramblerInputJack: Jack
    ): Bridge {
        val bridge = getBombe().createBridge()
        bridge.outPlug.plugInto(firstScramblerOutputJack)
        bridge.inPlug.plugInto(nextScramblerInputJack)
        return bridge
    }

    fun drawCableBetween(
        leftJack: Jack,
        rightJack: Jack
    ): Cable {
        val cable = getBombe().createCable()
        cable.leftPlug.plugInto(leftJack)
        cable.rightPlug.plugInto(rightJack)
        return cable
    }

    // Pair<Int, Char> : Pair of a group ID of commonsSets and a menu letter the commons set represents
    // When setting up a bombe to run the same menu for various rotor configurations, each configuration
    // will use its own group of commonsSets. In such a group, each commonsSet is used for a particular
    // letter in the menu.
    val commonsSetRegister = mutableMapOf<Pair<Int, Char>, CommonsSet>()

    fun findFreeCommonsSet() : CommonsSet{
        val freeCommonsSet = getBombe().getCommonsSets().filter {
            // free commonsSet:
            // - no jack is plugged up
            // - it hasn't already been claimed/registered
            it.jacks().filter { jack -> jack.pluggedUpBy() != null}.isEmpty() && !commonsSetRegister.values.contains(it)
        }.firstOrNull()
        check(freeCommonsSet != null) {"no more free commonsSet available"}
        return freeCommonsSet
    }

    // *************************************************************************************************
    // Bombe front side set-up features
    fun placeDrums(scramblerId: Int, drumTypes: List<DrumType>) {
        getBombe().getScrambler(scramblerId)!!.placeDrums(drumTypes)
    }

    /**
     * Scrambler <scramblerId> of bank <bankId> has a number of drums.
     * Those drums should be set to the specified startOrientations.
     *
     * Example: startOrientations DXF means that the top drum will be rotated
     *    to start orientation 'D', the middle drum to 'X' and the lower drum to 'F'
     */
    fun setStartRingOrientations(scramblerId: Int, startOrientations: String) {
        getBombe().getScrambler(scramblerId)!!.setDrumStartOrientations(startOrientations)
    }

    // *************************************************************************************************
    // Features needed to run a bombe job
    fun runJob(
        numberOfSteps: Int? = null,
        printStepResult: Boolean = false,
        printCurrentPath: Boolean = false
    ): MutableList<StopSlip> {
        val stopSlips = mutableListOf<StopSlip>()
        var doContinue = true
        while (doContinue) {
            // when you start the bombe and it doesn't return any stops this is because
            // - it has stepped 'numberOfSteps' times
            // - it has tried all drum positions
            doContinue = getBombe().start(numberOfSteps, printStepResult, printCurrentPath)
            stopSlips.addAll(lookAtChainIndicatorPanelAndCreateStopSlips())
        }
        return stopSlips
    }

    private fun lookAtChainIndicatorPanelAndCreateStopSlips():List<StopSlip> {
        return bombe.getChainDisplays()
            .filter{ it.readSearchLetters().isNotEmpty() }
            .map { createStop(bombe.getChain(it.getId())!!) }
            .toList()
    }

    private fun createStop(chain: Chain) : StopSlip {
        return StopSlip(bombe.getIndicatorDrums()[0].position, bombe.getIndicatorDrums()[1].position, bombe.getIndicatorDrums()[2].position,
            determineChainInputLetter(chain), chain.readSearchLetters())
    }

    private fun determineChainInputLetter(chain: Chain) : Char {
        return PluggingUpUtil.findConnectedDiagonalBoardJack(chain.getInputJack())!!.letter
    }


//    fun setUpMenu_example1 (){
//        setUpMenuOld(RotorType.III, RotorType.V, RotorType.II,
//        "11-UE, 5-EG, 6-GR, 13-AS, 7-SV, 16-VE, 2-EN, 10-HZ, 9-ZR, 12-RG, 15-GL", 'G')
//    }

    
//    fun setUpMenuOld(rotorTypeRotor1: RotorType, rotorTypeRotor2: RotorType, rotorTypeRotor3: RotorType, menu:String, centralLetter: Char) {
//        bombe.getBank(1).placeDrums(rotorTypeRotor1, rotorTypeRotor2, rotorTypeRotor3)
//
//        // parse the menu string
//        val scramblersSetup = mutableListOf<Triple<Int, Char, Char>>()
//        val scramblerSettings = menu.split(", ")
//       for (ss in scramblerSettings) {
//        //for ((index, ss) in scramblerSettings.withIndex()) {
//            val settingElements = ss.split("-")
//            scramblersSetup.add(Triple(settingElements[0].toInt(), settingElements[1][0], settingElements[1][1]))
//        }
//
//        // now process the parsed menu
//        // list of Triples
//        // - first: offset
//        // - letter
//        // - scrambler to letter
//
//        // 1. install the bridges
//        val bridgeMap = mutableMapOf<Char, Bridge>()
//        for ((index, ss) in scramblersSetup.withIndex()) {
//            if (index < scramblersSetup.size - 1) {
//                val thisScrambler = bombe.getBank(1).getScrambler(index + 1)
//                val nextScrambler = bombe.getBank(1).getScrambler(index + 2)
//                val bridge = bombe.connectNewBridge(thisScrambler.outputJack, nextScrambler.inputJack)
//                bridgeMap[ss.third] = bridge
//            }
//        }
//
//        // 2. initialize commons
//        // we need to use a commons for every letter in the menu which appears more than twice in the menu
//        val commonsInUse = mutableMapOf<Char, Commons>()
//        val allLetters = scramblersSetup.map { listOf(it.second, it.third) }.toList().flatten()
//        val uniqueLetters = allLetters.toSet()
//        val commonsLetters = mutableListOf<Char>()
//        for (ul in uniqueLetters) {
//            if (allLetters.filter{it.equals(ul)}.count() > 2) {
//                val newCommons = bombe.newCommons()
//                commonsInUse[ul] = newCommons
//                // connect a jack of this commons to the appropriate DiagonalBoard jack with a cable
//                bombe.connectNewCable(newCommons.getFreeJack(), bombe.diagonalBoard.getJack(ul))
//            }
//        }
//
//        // 3. connect the scramblers; either
//        // - connect a non-bridged input or output
//        //   - to a commons
//        //   - or to the diagonal board
//        // - connect each bridge
//        //   - to a commons
//        //   - or to the diagonal board
//        // first find the letters for which we need to use a commons (letters which appear more than twice)
//        for ((index, ss) in scramblersSetup.withIndex()) {
//            if (index < scramblersSetup.size - 1) {
//                val scrambler = bombe.getBank(1).getScrambler(index + 1)
//                if (scrambler.inputJack.connectedTo == null) {
//                    // scrambler with non-bridged input
//                    connectJack(scrambler.inputJack, ss.second, commonsInUse)
//
//                } else if (scrambler.outputJack.connectedTo == null) {
//                    // scrambler with non-bridged output
//                    connectJack(scrambler.outputJack, ss.third, commonsInUse)
//                } else {
//                    // scrambler with bridged input and output
//                    // we're going to connect the bridge connected to this scrambler's input
//                    // to the appropriate common or diagonal board
//                    connectJack((scrambler.inputJack.connectedTo!!.component as Bridge).jack, ss.second, commonsInUse)
//                }
//            }
//        }
//
//        // 4. connect the bank's input
//        connectJack(bombe.getBank(1).jack, centralLetter, commonsInUse)
//    }

//    private fun connectJack(jack: Jack, letter: Char, commonsInUse: Map<Char, Commons>) {
//        var otherJack : Jack = if (commonsInUse.contains(letter)) commonsInUse.get(letter)!!.getFreeJack() else bombe.diagonalBoard.getJack(letter)
//        bombe.connectNewCable(jack, otherJack)
//    }
}