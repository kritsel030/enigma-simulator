package bombe

import bombe.components.Bridge
import bombe.components.CommonsSet
import bombe.components.DiagonalBoard
import bombe.components.Scrambler
import java.lang.IllegalStateException

open class MediorBombeOperator() : JuniorBombeOperator() {

    fun executeRun(
        centralLetter: Char,
        numberOfSteps: Int? = null,
        printStepResult: Boolean = false,
        printCurrentPath: Boolean = false,
    ): List<Stop> {
        verifyPluggedUpBackSide()
        return getBombe().run(numberOfSteps, printStepResult, printCurrentPath, centralLetter)
    }

    /**
     * Verify whether the back-side of the bombe is plugged up in a logical way
     * (general rules are verified, the specific menu is not taken into account)
     * When errors are found, an exception is thrown
     */
    fun verifyPluggedUpBackSide() {
        val errors = mutableListOf<String>()

        // each Bank has an input-jack, when plugged up it should be connected - via a cable - to a CommonsSet or DiagonalBoard
        getBombe().banks.values.forEach { bank ->
            if (bank.inputJack.insertedPlug() != null) {
                errors.addAll(bank.inputJack.verifyCableTo(listOf(CommonsSet::class.java.simpleName, DiagonalBoard::class.java.simpleName)))
            }
        }

        // each diagonal board jack which is plugged up should be connected - via a cable - to a bridge, commonsSet, scrambler or bank
        // (connected to a bank is only the case when the 'central letter' does not appear in the menu)
        getBombe().diagonalBoards.values.forEach {
            it.jacks.values.forEach { jack ->
                run {
                    if (jack.insertedPlug() != null) {
                        errors.addAll(
                            jack.verifyCableTo(
                                listOf(
                                    CommonsSet::class.java.simpleName,
                                    Bridge::class.java.simpleName,
                                    Scrambler::class.java.simpleName,
                                    Bank::class.java.simpleName
                                )
                            )
                        )
                    }
                }
            }
        }

        // all bridges
        // - should have both plugs connected
        // - should be (via their jack) connected - via a cable - to a diagonal board or a commonsSet
        getBombe().bridges.forEach { bridge ->
            run {
                if (bridge.inPlug.pluggedInto() == null) {
                    errors.add("${bridge.label}.${bridge.inPlug.label} is not plugged in")
                }
                if (bridge.outPlug.pluggedInto() == null) {
                    errors.add("${bridge.label}.${bridge.outPlug.label} is not plugged in")
                }
                errors.addAll(
                    bridge.jack.verifyCableTo(
                        listOf(
                            CommonsSet::class.java.simpleName,
                            DiagonalBoard::class.java.simpleName
                        )
                    )
                )

            }
        }

        // all scramblers should have none or both jacks plugged up
        getBombe().banks.values.forEach {
            it.getScramblers().forEach { scrambler ->
                run {
                    if (!((scrambler.inputJack.insertedPlug() == null && scrambler.outputJack.insertedPlug() == null) ||
                                (scrambler.inputJack.insertedPlug() != null && scrambler.outputJack.insertedPlug() != null))
                    ) {
                        errors.add("${scrambler.label} has only 1 jack plugged in, expected none or both")
                    }
                }
            }
        }

        // for each CommonsSet the following should be true
        // - either 0 or at least jacks are plugged up
        // - of the set of plugged op jacks, exactly 1 is connected to a DiagonalBoardJack
        errors.addAll(getBombe().commonsSetsColumns.values.map { list ->
            list.map { cs -> cs.verifyConnections() }.toList().flatten()
        }.toList().flatten())

        if (errors.size > 0) {
            throw IllegalStateException("errors found in plugging up of bombe back-side: ${errors.joinToString(", ")}")
        }
    }


}