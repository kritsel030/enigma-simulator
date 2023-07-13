package bombe

import bombe.components.*
import bombe.connectors.CablePlug
import bombe.connectors.Jack
import enigma.components.ReflectorType
import enigma.components.RotorType

fun main() {
    val instructions = getInstructions_1()
    val operator = AutomatedBombeOperator()
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
    // - plugboard: UF-ET-GQ-AD-VN-HM-ZP-LJ-IK-XO

    // stop results according to PDF :
    // SNY:D
    // DKX:Q

    // stop results in our bombe:
    // G -> D @ TQZ (1st 1 off, 2nd 3 off, 3rd, 1 off, compared to the PDF results)
    // G -> Q @ ENY (1st 1 off, 2nd 3 off, 3rd, 1 off, compared to the PDF results)
    // this is caused by (https://www.lysator.liu.se/~koma/turingbombe/TuringBombeTutorial.pdf, page 2)
    // "Probably by mistake, drums I, II, III,
    //  VI, VII and VIII on the Bombe are one letter ahead of the corresponding Enigma rotors.
    //  Drum IV is two steps ahead, and rotor V is three steps ahead."

    // when using these rotor start settings
    //         var rotor1 = Rotor(rotorTypeRotor1, 'Y', 26)
    //        var rotor2 = Rotor(rotorTypeRotor2, 'W', 26)
    //        var rotor3 = Rotor(rotorTypeRotor3, 'Y', 26)
    // or these
//    var rotor1 = Rotor(rotorTypeRotor1, 'Z', 1) // 1 instead of 26
//    var rotor2 = Rotor(rotorTypeRotor2, 'Z', 3) // 3 instead of 26
//    var rotor3 = Rotor(rotorTypeRotor3, 'Z', 1) // 1 instead of 26
    // it produces these results which are the same as the ones mentioned in the PDF
//    G -> D @ SNY
//    G -> Q @ DKX

    return BombeRunInstructions(
        listOf("U-11-E-5-G-6-R-14-A-13-S-7-V-16-E-2-N", "H-10-Z-9-R-12-G-15-L"),
        'G', // should be 'G'
        // left, middle, right rotor
        listOf(listOf(RotorType.II, RotorType.V, RotorType.III)),
        ReflectorType.B,
        'A'
    )
}

class AutomatedBombeOperator() : MediorBombeOperator() {

    fun executeRun(
        instructions: BombeRunInstructions,
        numberOfSteps: Int? = null,
        printStepResult: Boolean = false,
        printCurrentPath: Boolean = false
    ): List<Stop> {
        createBombe(instructions)
        plugUpBackSide(instructions)
        verifyPluggedUpBackSide()
        verifyPluggedUpBackSide(instructions)
        prepareFrontSide(instructions)
        prepareRightSide(instructions)
        return getBombe().run(numberOfSteps, printStepResult, printCurrentPath, instructions.centralLetter)
    }

    // We act as if a bombe operator can construct a brand new bombe machine out of thin-air,
    // specifically suited to support the given bombe run instructions (like a sufficient number
    // of scrambler banks to test all rotor orders specified in the instructions)
    private fun createBombe(instructions: BombeRunInstructions) {
        // we can create a bombe with dimensions (link number of banks) which are specifically targeted
        // to suit the instructions
        var constructionParams = BombeConstructionParameters(26, instructions.rotorConfigurations.size, 12, 3)
        // but this can be overruled in the instructions by choosing a specific bombe template
        // (e.g. 'ATLANTA' which will produce a bombe with the ATLANTA dimensions
        if (instructions.bombeTemplate != BombeTemplate.MAGIC) {
            constructionParams = BombeConstructionParameters.getBombeConstructionParameters(instructions.bombeTemplate)
        }
        _bombe = Bombe(constructionParams)
    }

    fun plugUpBackSide(instructions: BombeRunInstructions) {
        when(instructions.bombeStrategy) {
            BombeStrategy.SINGLE_LINE_SCANNING -> plugUpBackSide_singleLineScanning(instructions)
            BombeStrategy.SIMULTANEOUS_SCANNING -> plugUpBackSide_simultaneousScanning(instructions)
            BombeStrategy.DIAGONAL_BOARD -> plugUpBackSide_diagonalBoard(instructions)
        }
    }
    fun plugUpBackSide_diagonalBoard(instructions: BombeRunInstructions) {
        // note: we use 1 bank for every rotor configuration
        for (bankId in 1..instructions.rotorConfigurations.size) {
            val bank = getBombe().getBank(bankId)
            // 1. claim a scrambler for each link in a menu chain
            // and place bridges between each pair of consecutive scramblers/menulinks in a menu chain
            for (chain in instructions.parsedMenu) {
                var previousScrambler: Scrambler? = null
                for ((index, link) in chain.withIndex()) {
                    val scrambler = bank.getScrambler(link.positionInMenu)
                    if (index > 0) {
                        // only for non-first menu links/scramblers:
                        // connect the scrambler for this link to the scrambler associated with the previous link
                        // each bridge actually represents a letter in the menu, that letter is also captured
                        // (only used for informational purposes)
                        createAndConnectBridge(previousScrambler!!.outputJack, scrambler.inputJack)
                    }
                    previousScrambler = scrambler
                }
            }

            // 2. use cables to connect scramblers jacks (or the jack of the bridge plugged into a scrambler jack)
            // to the appropriate DiagonalBoard jack or CommonsSet Jack
            for (chain in instructions.parsedMenu) {
                for (link in chain) {
                    val scrambler = bank.getScrambler(link.positionInMenu)
                    if (scrambler.inputJack.insertedPlug() == null) {
                        // connect this link's scrambler's inputJack
                        connectJackToALetterJack(bankId, scrambler.inputJack, link.inputLetter)
                    } else {
                        // there should be a bridge connected to this link's scrambler input-jack,
                        // connect that bridge's jack
                        val bridgeJack = (scrambler.inputJack.insertedPlug()!!.attachedTo as Bridge).jack
                        if (bridgeJack.insertedPlug() == null) {
                            connectJackToALetterJack(bankId, bridgeJack, link.inputLetter)
                        }
                    }
                    if (scrambler.outputJack.insertedPlug() == null) {
                        // connect this link's scrambler's outputJack
                        connectJackToALetterJack(bankId, scrambler.outputJack, link.outputLetter)
                    } else {
                        // there should be a bridge connected to this link's scrambler output-jack, connect that bridge's jack
                        val bridgeJack = (scrambler.outputJack.insertedPlug()!!.attachedTo as Bridge).jack
                        if (bridgeJack.insertedPlug() == null) {
                            connectJackToALetterJack(bankId, bridgeJack, link.outputLetter)
                        }
                    }
                }
            }
            // 3. connect the bank's input jack
            connectJackToALetterJack(bankId, getBombe().getBank(bankId).inputJack, instructions.centralLetter)
        }
    }


    // Doesn't use the diagonal board
    // Can only be used with menus with a singe loop
    fun plugUpBackSide_singleLineScanning(instructions: BombeRunInstructions) {

    }

    // Doesn't use the diagonal board
    // Can only be used with menus with a singe loop
    // Output of the loop is fed back into the loop
    fun plugUpBackSide_simultaneousScanning(instructions: BombeRunInstructions) {

    }

    fun verifyPluggedUpBackSide(instructions: BombeRunInstructions) : List<String> {
        val errors = mutableListOf<String>()
        for ( (index, rc) in instructions.rotorConfigurations.withIndex()) {
            val bank = getBombe().getBank(index + 1)
            errors.addAll(instructions.parsedMenu.map { chain ->
                chain.map { menuLink ->
                     bank.getScrambler(menuLink.positionInMenu).checkConnections(menuLink)
                }.toList().flatten()
            }.toList().flatten())
        }
        return errors
    }

    fun prepareFrontSide(instructions: BombeRunInstructions) {
        // install drums (in 'Z' position)
        // each rotorConfiguration in the instructions will get its own bank
        // every scrambler in that bank will be set-up with the same set of drum/rotor types
        for ((index, rotorConfig) in instructions.rotorConfigurations.withIndex()) {
            getBombe().getBank(index + 1).placeDrums(rotorConfig[0], rotorConfig[1], rotorConfig[2])
        }

        // set the offset for every first drum of each scrambler
        for ((index, rotorConfig) in instructions.rotorConfigurations.withIndex()) {
            val bank = getBombe().getBank(index + 1)
            for (chain in instructions.parsedMenu) {
                for (link in chain) {
                    val scrambler = bank.getScrambler(link.positionInMenu)
                    scrambler.setRelativePosition(link.rotorOffset)
                }
            }
        }
    }

    fun prepareRightSide(instructions: BombeRunInstructions) {
        for (i in 1 .. instructions.rotorConfigurations.size) {
            getBombe().getBank(i).switchOn()
            getBombe().getBank(i).setContactToActivate(instructions.activateContact)
        }
    }

    fun connectJackToALetterJack(bankId: Int, jack:Jack, letter: Char) {
        // connect to the correct DiagonalBridge jack when that Jack is still free
        val dbJack = getBombe().getDiagonalBoard(bankId).getJack(letter)
        if (dbJack.insertedPlug() == null) {
            // connect a cable between this dbJack and the given jack
            createAndConnectCable(jack, dbJack)
        } else {
            // is there already a commonsSet for this letter?
            val commonsSet : CommonsSet? = getBombe().claimedCommonsSets.get(Pair(bankId, letter))
            if (commonsSet != null) {
                // connect a cable between a free jack of this commons and the given jack
                val commonsJack = commonsSet.getAvailableJack()
                createAndConnectCable(jack, commonsJack)
            } else {
                // claim an available commonsSet for this letter
                val newCommons = getBombe().claimAvailableCommonsSet(bankId, letter)
                // the cable going into the dbJack is currently going into a bridge or scrambler
                // that cable needs to be unplugged from the bridge/scrambler and plugged into this commonsSet
                val otherPlugOfDbCable = (dbJack.insertedPlug() as CablePlug).getOppositePlug()
                // determine the jack before unplugging!
                val replugJack = otherPlugOfDbCable.pluggedInto() as Jack
                otherPlugOfDbCable.unplug()
                otherPlugOfDbCable.plugInto(newCommons.getAvailableJack())
                // and an additional cable between that bridge and this commons needs to be connected
                createAndConnectCable(replugJack, newCommons.getAvailableJack())
            }
        }
    }
}