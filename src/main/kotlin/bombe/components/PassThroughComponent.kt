package bombe.components

import bombe.Bombe
import bombe.connectors.Connector
import bombe.recorder.CurrentPathElement

// When a contact of one of this component's connectors gets activated,
// the same contact in the other connectors attached to this component get activated as well

abstract class PassThroughComponent(label: String, bombe: Bombe) : CircuitComponent(label, bombe) {

    override fun passCurrent(wire: Char, activatedVia: Connector, previousPathElement: CurrentPathElement) {
        val newPathElement = CurrentPathElement(label, javaClass.simpleName, wire, wire, previousPathElement, previousPathElement.root)
        previousPathElement.addNext(newPathElement)
        for (connector in connectors) {
            connector.activateContactOutbound(wire, newPathElement)
        }
    }
}