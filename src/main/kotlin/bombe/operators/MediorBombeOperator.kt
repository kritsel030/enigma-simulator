package bombe.operators

import bombe.Bombe
import bombe.Stop
import bombe.components.*
import bombe.connectors.CablePlug
import bombe.connectors.Jack
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
open class MediorBombeOperator(bombe: Bombe) : JuniorBombeOperator(bombe) {


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
                errors.addAll(verifyCableTo(panel.getInputJack(), listOf(CommonsSet::class.java.simpleName, DiagonalBoard::class.java.simpleName)))
            }
        }

        // each diagonal board jack which is plugged up should be connected - via a cable - to a bridge, commonsSet, scrambler or bank
        // (connected to a bank is only the case when the 'central letter' does not appear in the menu)
        getBombeInterface().getDiagonalBoardJackPanels().forEach {
            it.getJacks().forEach { jack ->
                run {
                    if (jack.pluggedUpBy() != null) {
                        errors.addAll(
                            verifyCableTo(jack,
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
                // test menu 7 in US bombe report 1944:
                // there can be unknown letters in the menu, resulting in a bridge's jack not being plugged up
                errors.addAll(
                    verifyCableTo(bridge.jack,
                        listOf(
                            CommonsSet::class.java.simpleName,
                            DiagonalBoard::class.java.simpleName
                        ),false)
                )
            }
        }

        // all scramblers should have none or both jacks plugged up
        getBombeInterface().getScramblerJackPanels().forEach {
            run {
                if (it.getInputJack().pluggedUpBy() != null && it.getOutputJack().pluggedUpBy() == null) {
                    errors.add("Scrambler ${it.getExternalLabel()} has its input jack plugged up, while its output jack is unconnected")
                }
                if (it.getInputJack().pluggedUpBy() == null && it.getOutputJack().pluggedUpBy() != null) {
                    errors.add("Scrambler ${it.getExternalLabel()} has its output jack plugged in, while its input jack is unconnected")
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

    // verifies if this jack
    // - is plugged up with a plug which is attached to a cable
    // - if the other plug of that cable is plugged into a jack of a component whose type is mentioned in the given
    //   list of component types
    // returns a list of verification error messages (empty list when all is OK)
    fun verifyCableTo(jack: Jack, componentTypes: List<String>, jackMustBePluggedUp: Boolean = true) : List<String> {
        val errors = mutableListOf<String>()
        val plugInsertedToJack = jack.pluggedUpBy()
        if (plugInsertedToJack == null) {
            if (jackMustBePluggedUp) {
                errors.add("${jack.attachedTo.label}.${jack.externalLabel} : jack is not plugged up")
            }
        } else {
            if (plugInsertedToJack!!.attachedTo !is Cable) {
                errors.add("${jack.attachedTo.label}.${jack.externalLabel} : jack is plugged up with a plug connected to a ${jack.attachedTo.javaClass.simpleName}, expected Cable")
            }
            val jackOnOtherSideOfCable = (plugInsertedToJack as CablePlug).getOppositePlug().pluggedInto()
            if (jackOnOtherSideOfCable == null) {
                errors.add("${jack.attachedTo.label}.${jack.externalLabel} : other side of the plugged in cable is not plugged in")
            } else if (!componentTypes.contains(jackOnOtherSideOfCable!!.attachedTo.javaClass.simpleName)) {
                errors.add(
                    "${jack.attachedTo.label}.${jack.externalLabel} : jack is connected to a ${jackOnOtherSideOfCable!!.attachedTo.javaClass.simpleName}, expected ${
                        componentTypes.joinToString(" or ")
                    }"
                )
            }
        }
        return errors
    }
}