package bombe.operators

import bombe.*
import bombe.components.*
import bombe.connectors.CablePlug
import bombe.connectors.Jack

/**
 * https://en.wikipedia.org/wiki/Women_in_Bletchley_Park
 *
 * Please meet Charlotte, our expert bombe operator. She's been with us from the very beginning.
 *
 * You can simply give her a menu, together with the various rotor type orders you want to have tested,
 * (BombeRunInstructions) and she'll take it from there.
 * Based on this information she will autonomously plug up the  bombe and start it,
 * without requiring any additional instructions from you.
 */

class ExpertBombeOperator() : MediorBombeOperator() {

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
        return getBombe().run(numberOfSteps, printStepResult, printCurrentPath)
    }

    // Our expert bombe operator can actually conjure a new bombe machine out of thin air,
    // specifically suited to support the given bombe run instructions (like a sufficient number
    // of scrambler banks to test all rotor orders (which may be more than 3) specified in the instructions)
    private fun createBombe(instructions: BombeRunInstructions) {
        // we can create a bombe with dimensions (link number of banks) which are specifically targeted
        // to suit the instructions
        var constructionParams = BombeConstructionParameters(26, instructions.drumConfigurations.size, 12, 3, 5)
        // but this can be overruled in the instructions by choosing a specific bombe template
        // (e.g. 'ATLANTA' which will produce a bombe with the ATLANTA dimensions
        if (instructions.bombeTemplate != BombeTemplate.MAGIC) {
            constructionParams = BombeConstructionParameters.getBombeConstructionParameters(instructions.bombeTemplate)
        }
        _bombe = Bombe(constructionParams, instructions.reflectorType)
    }

    fun plugUpBackSide(instructions: BombeRunInstructions) {
        when(instructions.bombeStrategy) {
            BombeRunStrategy.SINGLE_LINE_SCANNING -> plugUpBackSide_singleLineScanning(instructions)
            BombeRunStrategy.SIMULTANEOUS_SCANNING -> plugUpBackSide_simultaneousScanning(instructions)
            BombeRunStrategy.DIAGONAL_BOARD -> plugUpBackSide_diagonalBoard(instructions)
        }
    }
    fun plugUpBackSide_diagonalBoard(instructions: BombeRunInstructions) {
        // note: we use 1 bank for every rotor configuration
        for (configId in 1..instructions.drumConfigurations.size) {
            val bankId = configId
            val commonsSetGroupId = configId
            // 1. claim a scrambler for each link in a menu chain
            // and place bridges between each pair of consecutive scramblers/menulinks in a menu chain
            for (chain in instructions.parsedMenu) {
                var previousScrambler: ScramblerJackPanel? = null
                for ((index, link) in chain.withIndex()) {
                    val scramblerJackPanel = getBombeInterface().getScramblerJackPanel(bankId, link.positionInMenu)
                    if (index > 0) {
                        // only for non-first menu links/scramblers:
                        // connect the scrambler for this link to the scrambler associated with the previous link
                        // each bridge actually represents a letter in the menu, that letter is also captured
                        // (only used for informational purposes)
                        attachBridgeTo(previousScrambler!!.getOutputJack(), scramblerJackPanel!!.getInputJack())
                    }
                    previousScrambler = scramblerJackPanel
                }
            }

            // 2. use cables to connect scramblers jacks (or the jack of the bridge plugged into a scrambler jack)
            // to the appropriate DiagonalBoard jack or CommonsSet Jack
            for (chain in instructions.parsedMenu) {
                for (link in chain) {
                    val scramblerJackPanel = getBombeInterface().getScramblerJackPanel(bankId, link.positionInMenu)
                    if (scramblerJackPanel!!.getInputJack().pluggedUpBy() == null) {
                        // connect this link's scrambler's inputJack
                        connectJackToALetterJack(commonsSetGroupId, scramblerJackPanel.getInputJack(), link.inputLetter)
                    } else {
                        // there should be a bridge connected to this link's scrambler input-jack,
                        // connect that bridge's jack
                        val bridgeJack = (scramblerJackPanel.getInputJack().pluggedUpBy()!!.attachedTo as Bridge).jack
                        if (bridgeJack.pluggedUpBy() == null) {
                            connectJackToALetterJack(commonsSetGroupId, bridgeJack, link.inputLetter)
                        }
                    }
                    if (scramblerJackPanel.getOutputJack().pluggedUpBy() == null) {
                        // connect this link's scrambler's outputJack
                        connectJackToALetterJack(commonsSetGroupId, scramblerJackPanel.getOutputJack(), link.outputLetter)
                    } else {
                        // there should be a bridge connected to this link's scrambler output-jack, connect that bridge's jack
                        val bridgeJack = (scramblerJackPanel.getOutputJack().pluggedUpBy()!!.attachedTo as Bridge).jack
                        if (bridgeJack.pluggedUpBy() == null) {
                            connectJackToALetterJack(commonsSetGroupId, bridgeJack, link.outputLetter)
                        }
                    }
                }
            }
            // 3. connect the bank's input jack
            connectJackToALetterJack(commonsSetGroupId, getBombeInterface().getChainJackPanel(configId)!!.getInputJack(), instructions.centralLetter)
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
        for ( bankId in 1 .. instructions.drumConfigurations.size) {
            errors.addAll(instructions.parsedMenu.map { chain ->
                chain.map { menuLink ->
                     getBombe().getScrambler(bankId, menuLink.positionInMenu)!!.checkConnections(menuLink)
                }.toList().flatten()
            }.toList().flatten())
        }
        return errors
    }

    fun prepareFrontSide(instructions: BombeRunInstructions) {
        // install drums (in 'Z' position)
        // each rotorConfiguration in the instructions will get its own bank of scramblers
        // every scrambler in that bank will be set-up with the same set of drum/rotor types
        for ((index, drumConfig) in instructions.drumConfigurations.withIndex()) {
            val bankId = index + 1
            for (i in 1..getBombe().noOfScramblersPerBank) {
                getBombe().getScrambler(index + 1, i)!!.placeDrums(drumConfig)
            }
        }

        // set the offset for every first drum of each scrambler
        for ((index, drumConfig) in instructions.drumConfigurations.withIndex()) {
            val bankId = index + 1
            for (chain in instructions.parsedMenu) {
                for (link in chain) {
                    val scrambler = getBombe().getScrambler(bankId, link.positionInMenu)
                    scrambler!!.setRelativePosition(link.rotorOffset)
                }
            }
        }
    }

    fun prepareRightSide(instructions: BombeRunInstructions) {
        for (i in 1 .. instructions.drumConfigurations.size) {
            getBombeInterface().getChainControlPanel(i)!!.switchOn()
            getBombeInterface().getChainControlPanel(i)!!.setContactToActivate(instructions.activateContact)
        }
    }

    fun connectJackToALetterJack(bankId: Int, jack:Jack, letter: Char) {
        // connect to the correct DiagonalBridge jack when that Jack is still free
        val dbJack = getBombeInterface().getDiagonalBoardJackPanel(bankId)!!.getJack(letter)
        if (dbJack.pluggedUpBy() == null) {
            // connect a cable between this dbJack and the given jack
            drawCableBetween(jack, dbJack)
        } else {
            // find a commonsSet for this letter
            // (use a previously registered one for this letter, or claims a free commonsSet)
            val registeredCommonsSet : CommonsSet? = commonsSetRegister.get(Pair(bankId, letter))
            if (registeredCommonsSet != null) {
                // connect a cable between a free jack of this commons and the given jack
                val commonsJack = registeredCommonsSet.getAvailableJack()
                drawCableBetween(jack, commonsJack)
            } else {
                // claim an available commonsSet for this letter
                val newCommons = findFreeCommonsSet()
                commonsSetRegister.put(Pair(bankId, letter), newCommons)
                // the cable going into the dbJack is currently going into a bridge or scrambler
                // that cable needs to be unplugged from the bridge/scrambler and plugged into this commonsSet
                val otherPlugOfDbCable = (dbJack.pluggedUpBy() as CablePlug).getOppositePlug()
                // determine the jack before unplugging!
                val replugJack = otherPlugOfDbCable.pluggedInto() as Jack
                otherPlugOfDbCable.unplug()
                otherPlugOfDbCable.plugInto(newCommons.getAvailableJack())
                // and an additional cable between that bridge and this commons needs to be connected
                drawCableBetween(replugJack, newCommons.getAvailableJack())
            }
        }
    }
}