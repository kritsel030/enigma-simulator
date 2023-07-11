package bombe.components

import bombe.connectors.Connector
import bombe.Bombe
import bombe.recorder.CurrentPathElement

abstract class CircuitComponent(val label: String, val bombe: Bombe) {

    var connectors = mutableListOf<Connector>()

    fun addConnector(connector: Connector) : List<Connector> {
        connectors.add(connector)
        return connectors
    }

    /**
     * Remove voltage/current throughout the system, so we're prepared for the next step/drum-rotation
     * As our bombe-in-code only represents voltage/current as active contacts in connectors (Jacks and Plugs),
     * we only need to reset the current in these connectors.
     */
    fun resetCurrent() {
        connectors.forEach{it.resetContacts()}
    }

    abstract fun passCurrent(contact: Char, activatedVia: Connector, previousPathElement: CurrentPathElement)

}