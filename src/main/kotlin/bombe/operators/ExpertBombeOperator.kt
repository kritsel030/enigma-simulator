package bombe.operators

import bombe.*
import bombe.Util.PluggingUpUtil
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

class ExpertBombeOperator(bombe: Bombe) : MediorBombeOperator(bombe) {

    fun runExpertJob(
        instructions: BombeJobInstructions,
        numberOfSteps: Int? = null,
        printStepResult: Boolean = false,
        printCurrentPath: Boolean = false
    ): List<Stop> {
        plugUpBackSide(instructions)
        verifyPluggedUpBackSide()
        verifyPluggedUpBackSide(instructions)
        prepareFrontSide(instructions)
        throwSwitchesRightSide(instructions)

        return runJob(numberOfSteps, printStepResult, printCurrentPath)
    }


    fun plugUpBackSide(instructions: BombeJobInstructions) {
        when(instructions.bombeStrategy) {
            BombeRunStrategy.SINGLE_LINE_SCANNING -> plugUpBackSide_singleLineScanning(instructions)
            BombeRunStrategy.SIMULTANEOUS_SCANNING -> plugUpBackSide_simultaneousScanning(instructions)
            BombeRunStrategy.DIAGONAL_BOARD -> plugUpBackSide_diagonalBoard(instructions)
        }
    }
    fun plugUpBackSide_diagonalBoard(instructions: BombeJobInstructions) {
        // note: we use 1 bank of scramblers and 1 group of commonsSets for every rotor configuration
        for (configId in 1..instructions.drumConfigurations.size) {
            val bankId = configId
            val commonsSetGroupId = configId
            // 1. claim a scrambler for each link in a menu chain
            // and place bridges between each pair of consecutive scramblers/menulinks in a menu chain
            for (menuSegment in instructions.parsedMenu) {
                var previousScrambler: ScramblerJackPanel? = null
                for ((index, link) in menuSegment.withIndex()) {
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
                    if (link.inputLetter != '?') {
                        if (scramblerJackPanel!!.getInputJack().pluggedUpBy() == null) {
                            // connect this link's scrambler's inputJack
                            connectJackToDBLetterJack(
                                scramblerJackPanel.getInputJack(),
                                commonsSetGroupId,
                                link.inputLetter
                            )
                        } else {
                            // there should be a bridge connected to this link's scrambler input-jack,
                            // connect that bridge's jack
                            val bridgeJack =
                                (scramblerJackPanel.getInputJack().pluggedUpBy()!!.attachedTo as Bridge).jack
                            if (bridgeJack.pluggedUpBy() == null) {
                                connectJackToDBLetterJack(bridgeJack, commonsSetGroupId, link.inputLetter)
                            }
                        }
                    }
                    if (link.outputLetter != '?') {
                        if (scramblerJackPanel!!.getOutputJack().pluggedUpBy() == null) {
                            // connect this link's scrambler's outputJack
                            connectJackToDBLetterJack(
                                scramblerJackPanel.getOutputJack(),
                                commonsSetGroupId,
                                link.outputLetter
                            )
                        } else {
                            // there should be a bridge connected to this link's scrambler output-jack, connect that bridge's jack
                            val bridgeJack =
                                (scramblerJackPanel.getOutputJack().pluggedUpBy()!!.attachedTo as Bridge).jack
                            if (bridgeJack.pluggedUpBy() == null) {
                                connectJackToDBLetterJack(bridgeJack, commonsSetGroupId, link.outputLetter)
                            }
                        }
                    }
                }
            }
            // 3. connect the chain input jack when SINGLE INPUT is used
            // (See BombeJobInstructions for an explanation about single input and double input)
            if (instructions.singleInput) {
                // single input
                connectJackToDBLetterJack(
                    getBombeInterface().getChainJackPanel(configId)!!.getInputJack(),
                    commonsSetGroupId,
                    instructions.chain1InputLetter!!
                )
            }
        }

        // 3. connect the chain input jacks when DOUBLE INPUT is used
        // (See BombeJobInstructions for an explanation about single input and double input)
        if (!instructions.singleInput) {
            // double input
            connectJackToDBLetterJack(
                getBombeInterface().getChainJackPanel(1)!!.getInputJack(),
                1,
                instructions.chain1InputLetter!!
            )
            connectJackToDBLetterJack(
                getBombeInterface().getChainJackPanel(2)!!.getInputJack(),
                1,
                instructions.chain2InputLetter!!
            )
        }
    }

    // Doesn't use the diagonal board
    // Can only be used with menus with a singe loop
    fun plugUpBackSide_singleLineScanning(instructions: BombeJobInstructions) {

    }

    // Doesn't use the diagonal board
    // Can only be used with menus with a singe loop
    // Output of the loop is fed back into the loop
    fun plugUpBackSide_simultaneousScanning(instructions: BombeJobInstructions) {

    }

    fun prepareFrontSide(instructions: BombeJobInstructions) {
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
                    if (link.rotorOffset != null) {
                        scrambler!!.setRelativeStartOrientation(link.rotorOffset)
                    } else if (link.drumStartOrientations != null) {
                        scrambler!!.setDrumStartOrientations(link.drumStartOrientations)
                    }
                }
            }
        }
    }

    fun throwSwitchesRightSide(instructions: BombeJobInstructions) {
        // See BombeJobInstructions for an explanation about single input and double input
        if (instructions.singleInput) {
            for (i in 1..instructions.drumConfigurations.size) {
                getBombeInterface().getChainControlPanel(i)!!.switchOn()
                getBombeInterface().getChainControlPanel(i)!!.setContactToActivate(instructions.chain1SearchLetter!!)
            }
        } else {
            // double input
            getBombeInterface().getBombeControlpanel()!!.switchDoubleInputOn()
            getBombeInterface().getChainControlPanel(1)!!.switchOn()
            getBombeInterface().getChainControlPanel(1)!!.setContactToActivate(instructions.chain1SearchLetter!!)
            getBombeInterface().getChainControlPanel(2)!!.switchOn()
            getBombeInterface().getChainControlPanel(2)!!.setContactToActivate(instructions.chain2SearchLetterContact!!)
        }
    }

    fun connectJackToDBLetterJack(jack:Jack, commonsSetGroupId: Int, letter: Char) {
        // connect to the correct DiagonalBridge jack when that Jack is still free
        val dbJack = getBombeInterface().getDiagonalBoardJackPanel(commonsSetGroupId)!!.getJack(letter)
        if (dbJack.pluggedUpBy() == null) {
            // connect a cable between this dbJack and the given jack
            drawCableBetween(jack, dbJack)
        } else {
            // find a commonsSet for this letter
            // (use a previously registered one for this letter, or claims a free commonsSet)
            val registeredCommonsSet : CommonsSet? = commonsSetRegister.get(Pair(commonsSetGroupId, letter))
            if (registeredCommonsSet != null) {
                // connect a cable between a free jack of this commons and the given jack
                val commonsJack = registeredCommonsSet.getAvailableJack()
                drawCableBetween(jack, commonsJack)
            } else {
                // claim an available commonsSet for this letter
                val newCommons = findFreeCommonsSet()
                commonsSetRegister.put(Pair(commonsSetGroupId, letter), newCommons)
                // the cable going into the dbJack is currently going into a bridge or scrambler
                // that cable needs to be unplugged from the bridge/scrambler and plugged into this commonsSet
                val otherPlugOfDbCable = (dbJack.pluggedUpBy() as CablePlug).getOppositePlug()
                // determine the jack before unplugging!
                val replugJack = otherPlugOfDbCable.pluggedInto() as Jack
                otherPlugOfDbCable.unplug()
                otherPlugOfDbCable.plugInto(newCommons.getAvailableJack())
                // and an additional cable between that bridge/scrambler and this commons
                drawCableBetween(replugJack, newCommons.getAvailableJack())
                // connect a cable between a free jack of this commons and the given jack
                drawCableBetween(jack, newCommons.getAvailableJack())
            }
        }
    }

    fun verifyPluggedUpBackSide(instructions: BombeJobInstructions) : List<String> {
        val errors = mutableListOf<String>()
        for ( bankId in 1 .. instructions.drumConfigurations.size) {
            errors.addAll(instructions.parsedMenu.map { chain ->
                chain.map { menuLink ->
                    checkScramblerConnections( getBombe().getScrambler(bankId, menuLink.positionInMenu)!!, menuLink)
                }.toList().flatten()
            }.toList().flatten())
        }
        return errors
    }

    // ****************************************************************************************************************
    // Features required to check the correct set-up of the bombe

    // each scrambler in use has an inputJack and an outputJack,
    // both of these jacks represent a letter in the menu
    // each jack should ultimately be connected to a DiagonalBoard jack which represents this same letter
    // possible connection paths:
    // - scrambler.in/outputJack --cable--> diagonalBoard
    // - scrambler.in/outputJack --cable--> commonsSet --cable--> diagonalBoard
    // - scrambler.in/outputJack --bridge--> diagonalBoard
    // - scrambler.in/outputJack --bridge--> commonsSet --cable--> diagonalBoard
    fun checkScramblerConnections(scrambler: ScramblerJackPanel, menuLink: MenuLink) : MutableList<String> {
        val errors = mutableListOf<String>()
        errors.addAll(checkScramblerJackConnections(scrambler, scrambler.getInputJack(), menuLink.inputLetter))
        errors.addAll(checkScramblerJackConnections(scrambler, scrambler.getOutputJack(), menuLink.outputLetter))
        return errors
    }
    fun checkScramblerJackConnections(scrambler: ScramblerJackPanel, scramblerJack: Jack, representsLetter: Char) : MutableList<String>{
        val errors = mutableListOf<String>()
        val diagonalBoardJackForScramblerJack = PluggingUpUtil.findConnectedDiagonalBoardJack(scramblerJack)
        if (diagonalBoardJackForScramblerJack != null) {
            if (representsLetter != diagonalBoardJackForScramblerJack.letter) {
                errors.add(
                    "${scrambler.getExternalLabel()}.${scramblerJack.externalLabel} is not correctly plugged up, " +
                            "expected ${representsLetter} -> ${representsLetter}, " +
                            "got ${representsLetter} -> ${diagonalBoardJackForScramblerJack.letter}"
                )
            }
        } else {
            errors.add("${scrambler.getExternalLabel()}.${scramblerJack.externalLabel} is not correctly plugged up, " +
                    "it is not ultimately connected to the diagonal board")
        }
        return errors
    }

}