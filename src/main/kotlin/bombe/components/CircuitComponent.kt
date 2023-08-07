package bombe.components

import bombe.connectors.Connector
import bombe.Bombe
import bombe.EnergySource
import bombe.recorder.CurrentPathElement

abstract class CircuitComponent(val label: String, val bombe: Bombe) {

    var connectors = mutableListOf<Connector>()

    fun addConnector(connector: Connector) : List<Connector> {
        connectors.add(connector)
        return connectors
    }

    abstract fun passCurrent(contact: Char, activatedVia: Connector, previousPathElement: CurrentPathElement? = null)

}