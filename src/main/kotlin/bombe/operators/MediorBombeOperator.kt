package bombe.operators

import bombe.Stop
import bombe.components.*
import java.lang.IllegalStateException

/**
 * https://en.wikipedia.org/wiki/Women_in_Bletchley_Park
 *
 * Please meet Beatrice, our medior bombe operator. She's been with us for several weeks now.
 *
 * She can help you to plug up a bombe, but you will have to give her detailed instructions like
 * 'draw a cable from this jack to that jack'.
 * Based on her experience, she can also check whether the bombe is correctly set-up, once you're done
 * with your instructions, before starting the bombe.
 *
 * If you need a more experience bombe operator, you check out her colleague:
 * - ExpertBombeOperator
 */
open class MediorBombeOperator() : JuniorBombeOperator() {


    // ************************************************************************************************
    // Bombe run features

    fun executeRun(
        numberOfSteps: Int? = null,
        printStepResult: Boolean = false,
        printCurrentPath: Boolean = false,
    ): List<Stop> {
        verifyPluggedUpBackSide()
        return getBombe().run(numberOfSteps, printStepResult, printCurrentPath)
    }

    /**
     * Verify whether the back-side of the bombe is plugged up in a logical way
     * (general rules are verified, the specific menu is not taken into account)
     * When errors are found, an exception is thrown
     */
    fun verifyPluggedUpBackSide() {
        val errors = mutableListOf<String>()

        // each Bank has an input-jack, when plugged up it should be connected - via a cable - to a CommonsSet or DiagonalBoard
        getBombeInterface().getChainJackPanels().forEach { panel ->
            if (panel.getInputJack().pluggedUpBy() != null) {
                errors.addAll(panel.getInputJack().verifyCableTo(listOf(CommonsSet::class.java.simpleName, DiagonalBoard::class.java.simpleName)))
            }
        }

        // each diagonal board jack which is plugged up should be connected - via a cable - to a bridge, commonsSet, scrambler or bank
        // (connected to a bank is only the case when the 'central letter' does not appear in the menu)
        getBombeInterface().getDiagonalBoardJackPanels().forEach {
            it.getJacks().forEach { jack ->
                run {
                    if (jack.pluggedUpBy() != null) {
                        errors.addAll(
                            jack.verifyCableTo(
                                listOf(
                                    CommonsSet::class.java.simpleName,
                                    Bridge::class.java.simpleName,
                                    Scrambler::class.java.simpleName,
                                    Chain::class.java.simpleName
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
        getBombeInterface().getBridges().forEach { bridge ->
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
        getBombeInterface().getScramblerJackPanels().forEach {
            run {
                if (!((it.getInputJack().pluggedUpBy() == null && it.getOutputJack().pluggedUpBy() == null) ||
                            (it.getInputJack().pluggedUpBy() != null && it.getOutputJack().pluggedUpBy() != null))
                ) {
                    errors.add("${it.getExternalLabel()} has only 1 jack plugged in, expected none or both")
                }
            }
        }

        // for each CommonsSet the following should be true
        // - either 0 or at least jacks are plugged up
        // - of the set of plugged op jacks, exactly 1 is connected to a DiagonalBoardJack
        errors.addAll(getBombe().getCommonsSets().map {
            it.verifyConnections() }.toList().flatten())

        if (errors.size > 0) {
            throw IllegalStateException("errors found in plugging up of bombe back-side: ${errors.joinToString(", ")}")
        }
    }


}