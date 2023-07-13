package bombe.connectors

import bombe.components.*

// Convention used in this codebase: each *female* connector is named a *Jack*
// This includes all fixed connectors on the backside of the bombe,
// plus the jack on a bridge to allow another cable to be plugged in to a bridge

// Whenever a plug get plugged into a jack (see the Plug class), both a relationship from plug-to-jack and a relationship
// from jack-to-plug is established. This allows a live contact in a plug to activate the proper
// contact in the jack it is plugged into, and vice versa.

// https://en.wikipedia.org/wiki/Gender_of_connectors_and_fasteners
/**
 * - externalLabel: label as it would appear on the backside of an actual bombe machine
 *   (these are not unique)
 * - label: label used for simulator information purposes to uniquely identify connectors
 *   attached to the same component
 * - component: the component this Jack is physically attached to
 */
open class Jack (val externalLabel: String,
            label: String,
            component: CircuitComponent) : Connector(label, component) {

    fun insertedPlug() : Plug? {
        return connectedTo as? Plug
    }

    // verifies if this jack
    // - is plugged up with a plug attached to a cable
    // - if the other plug of that cable is plugged into a jack of a component whose type is mentioned in the given
    //   list of component types
    // returns a list of verification error messages (empty list when all is OK)
    fun verifyCableTo(componentTypes: List<String>) : List<String> {
        val errors = mutableListOf<String>()
        val plugInsertedToJack = this.insertedPlug()
        if (plugInsertedToJack == null) {
            errors.add("${attachedTo.label}.${externalLabel} : jack is not plugged up")
        } else {
            if (plugInsertedToJack!!.attachedTo !is Cable) {
                errors.add("${attachedTo.label}.${externalLabel} : jack is plugged up with a plug connected to a ${attachedTo.javaClass.simpleName}, expected Cable")
            }
            val jackOnOtherSideOfCable = (plugInsertedToJack as CablePlug).getOppositePlug().pluggedInto()
            if (jackOnOtherSideOfCable == null) {
                errors.add("${attachedTo.label}.${externalLabel} : other side of the plugged in cable is not plugged in")
            } else if (!componentTypes.contains(jackOnOtherSideOfCable!!.attachedTo.javaClass.simpleName)) {
                errors.add(
                    "${attachedTo.label}.${externalLabel} : jack is connected to a ${jackOnOtherSideOfCable!!.attachedTo.javaClass.simpleName}, expected ${
                        componentTypes.joinToString(" or ")
                    }"
                )
            }
        }
        return errors
    }
}