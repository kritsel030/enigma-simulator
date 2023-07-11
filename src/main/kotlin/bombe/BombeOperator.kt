package bombe

import bombe.components.*
import bombe.connectors.CablePlug
import bombe.connectors.Jack
import enigma.components.RotorType
import java.lang.IllegalStateException

fun main() {
    val instructions = getInstructions_1()
    val operator = BombeOperator()
//    operator.executeRun(instructions, 10, true, false)
    val stops = operator.executeRun(instructions)
    println("stops:")
    stops.forEach{it.print()}
    println("the end")
}

fun getInstructions_1() : BombeRunInstructions {

    // example taken from
    // https://www.lysator.liu.se/~koma/turingbombe/TuringBombeTutorial.pdf
    //
    // input:  WETTERVORHERSAGE
    // output: SNMKGGSTZZUGARLV
    //
    // encrypted with enigma settings:
    // - reflector: B
    // - rotor 1 (left): II-Y-4 (type, rotor position, ring setting)
    // - rotor 2 (middle): V-W-11
    // - rotor 3 (right): III-Y-24

    return BombeRunInstructions(
        listOf("U-11-E-5-G-6-R-14-A-13-S-7-V-16-E-2-N", "H-10-Z-9-R-12-G-15-L"),
        'G',
        // left, middle, right rotor
        listOf(listOf(RotorType.II, RotorType.V, RotorType.III))
    )
}

class BombeOperator() {

    var _bombe : Bombe? = null
    private fun getBombe() : Bombe {
        require(_bombe != null) {"Bombe has not been initialized"}
        return _bombe!!
    }

    fun executeRun(instructions: BombeRunInstructions, numberOfSteps: Int? = null, printStepResult: Boolean = false, printCurrentPath: Boolean = false) : List<Stop>{
        createBombe(instructions)
        plugUpBackSide(instructions)
        verifyPluggedUpBackSide()
        prepareFrontSide(instructions)
        prepareRightSide(instructions)
        return getBombe().run(numberOfSteps, printStepResult, printCurrentPath)
    }

    // We act as if a bombe operator can construct a brand new bombe machine out of thin-air,
    // specifically suited to support the given bombe run instructions (like a sufficient number
    // of scrambler banks to test all rotor orders specified in the instructions)
    private fun createBombe(instructions: BombeRunInstructions) {
        _bombe =  Bombe(26, instructions.rotorConfigurations.size, 12)
    }

    private fun plugUpBackSide(instructions: BombeRunInstructions) {
        // note: we use 1 bank for every rotor configuration

        for (bankId in 1 .. instructions.rotorConfigurations.size) {
            // 1. place bridges between connected scramblers
            for (chain in instructions.parsedMenu) {
                var previousScrambler : Scrambler? = null
                for ((index,link) in chain.withIndex()) {
                    var scrambler : Scrambler = getBombe().getBank(bankId).claimNextAvailableScrambler(link.positionInMenu)
                    link.setScrambler(scrambler)
                    if (index > 0) {
                        // only for non-first menu links:
                        // connect the scrambler for this link the scrambler for the previous link
                        getBombe().createAndConnectBridge(previousScrambler!!.outputJack, scrambler.inputJack)
                    }
                    previousScrambler = scrambler
                }
            }

            // 2. connect scramblers (free input, free output or bridge jack) to the appropriate
            // DiagonalBoard jack or Commons )
            for (chain in instructions.parsedMenu) {
                for ((index, link) in chain.withIndex()) {
                    if (link.getScrambler().inputJack.pluggedTo == null) {
                        // connect this link's scrambler's inputJack
                        connectJackToALetterJack(bankId, link.getScrambler().inputJack, link.inputChar)
                    } else {
                        // there should be a bridge connected to this link's scrambler input-jack, connect that bridge's jack
                        val bridgeJack = (link.getScrambler().inputJack.pluggedTo!!.attachedTo as Bridge).jack
                        if (bridgeJack.pluggedTo == null) {
                            connectJackToALetterJack(bankId, bridgeJack, link.inputChar)
                        }
                    }
                    if (link.getScrambler().outputJack.pluggedTo == null) {
                        // connect this link's scrambler's outputJack
                        connectJackToALetterJack(bankId, link.getScrambler().outputJack, link.outputChar)
                    }
                    else {
                        // there should be a bridge connected to this link's scrambler output-jack, connect that bridge's jack
                        val bridgeJack = (link.getScrambler().outputJack.pluggedTo!!.attachedTo as Bridge).jack
                        if (bridgeJack.pluggedTo == null) {
                            connectJackToALetterJack(bankId, bridgeJack, link.outputChar)
                        }
                    }
                }
            }
            
            // 3. connect the bank's input jack
            connectJackToALetterJack(bankId, getBombe().getBank(bankId).jack, instructions.centralLetter)
        }
    }

    /**
     * Verify whether the back-side of the bombe is plugged up in a logical way
     * (general rules are verified, the specific menu is not taken into account)
     * When errors are found, an exception is thrown
     */
    private fun verifyPluggedUpBackSide() {
        val errors = mutableListOf<String>()

        // each Bank has an input-jack, when plugged up it should be connected - via a cable - to a CommonsSet
        getBombe().banks.values.forEach{bank->
            if (bank.jack.pluggedTo != null) {
                errors.addAll(bank.jack.checkCableTo(listOf(CommonsSet::class.java.simpleName)))
            }
        }

        // each diagonal board jack which is plugged up should be connected - via a cable - to a bridge, commonsSet or scrambler
        getBombe().diagonalBoards.values.forEach {it.jacks.values.forEach{jack ->
            run {
                if (jack.pluggedTo != null) {
                    errors.addAll(jack.checkCableTo(listOf(CommonsSet::class.java.simpleName, Bridge::class.java.simpleName, Scrambler::class.java.simpleName)))
                }
            }
        }}

        // all bridges
        // - should be (via their jack) connected - via a cable - to a diagonal board or a commonsSet
        // - should have both plugs connected
        getBombe().bridges.forEach { bridge ->
            run {
                errors.addAll(bridge.jack.checkCableTo(listOf(CommonsSet::class.java.simpleName, DiagonalBoard::class.java.simpleName)))
                if (bridge.inPlug.pluggedTo == null) {
                    errors.add("${bridge.label}.${bridge.inPlug.label} is not plugged in")
                }
                if (bridge.outPlug.pluggedTo == null) {
                    errors.add("${bridge.label}.${bridge.outPlug.label} is not plugged in")
                }
            }
        }

        // all scramblers should have none or both jacks plugged up
        getBombe().banks.values.forEach{it.scramblers.values.forEach { scrambler ->
            run {
                if (! ((scrambler.inputJack.pluggedTo == null && scrambler.outputJack.pluggedTo == null) ||
                    (scrambler.inputJack.pluggedTo != null && scrambler.outputJack.pluggedTo != null))  ){
                    errors.add("${scrambler.label} has only 1 jack plugged in, expected none or both")
                }
            }
        }}
        if (errors.size > 0) {
            throw IllegalStateException("errors found in plugging up of bombe back-side: ${errors.joinToString(", ")}")
        }
    }

    private fun prepareFrontSide(instructions: BombeRunInstructions) {
        // install drums (in 'Z' position)
        // each rotorConfiguration in the instructions will get its own bank
        // every scrambler in that bank will be set-up with the same set of drum/rotor types
        for ((index, rotorConfig) in instructions.rotorConfigurations.withIndex()) {
            getBombe().getBank(index + 1).placeDrums(rotorConfig[0], rotorConfig[1], rotorConfig[2])
        }

        // set the offset for every first drum of each scrambler
        for (chain in instructions.parsedMenu) {
            for (link in chain) {
                link.getScrambler().setRelativePosition(link.rotorOffset)
            }
        }
    }

    private fun prepareRightSide(instructions: BombeRunInstructions) {
        for (i in 1 .. instructions.rotorConfigurations.size) {
            getBombe().getBank(i).switchOn()
            getBombe().getBank(i).setContactToActivate(instructions.activateContact)
        }
    }

    private fun connectJackToALetterJack(bankId: Int, jack:Jack, letter: Char) {
        // connect to the correct DiagonalBridge jack when that Jack is still free
        val dbJack = getBombe().getDiagonalBoard(bankId).getJack(letter)
        if (dbJack.pluggedTo == null) {
            // connect a cable between this dbJack and the given jack
            getBombe().createAndConnectCable(jack, dbJack)
        } else {
            // is there already a commons for this letter?
            val commonsSet : CommonsSet? = getBombe().claimedCommonsSets.get(Pair(bankId, letter))
            if (commonsSet != null) {
                // connect a cable between a free jack of this commons and the given jack
                val commonsJack = commonsSet.getAvailableJack()
                getBombe().createAndConnectCable(jack, commonsJack)
            } else {
                // pick a new commons for this letter
                val newCommons = getBombe().claimAvailableCommonsSet(bankId, letter)
                // the cable going into the dbJack is currently going into a bridge or scrambler
                // that cable needs to be unplugged from the bridge/scrambler and plugged into this commonsSet
                val otherPlugOfDbCable = (dbJack.pluggedTo as CablePlug).getOtherPlug()
                // determine the jack before unplugging!
                val replugJack = otherPlugOfDbCable.pluggedTo as Jack
                otherPlugOfDbCable.unplug()
                otherPlugOfDbCable.plugInto(newCommons.getAvailableJack())
                // and an additional cable between that bridge and this commons needs to be connected
                getBombe().createAndConnectCable(replugJack, newCommons.getAvailableJack())
            }
        }
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