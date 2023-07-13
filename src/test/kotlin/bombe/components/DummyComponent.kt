package bombe.components

import bombe.Bombe
import bombe.connectors.Connector
import bombe.recorder.CurrentPathElement

/**
 * Dummy compont subclass, needed to be able to create Connectors in test cases
 */
class DummyComponent(bombe: Bombe)  : CircuitComponent("dummy", bombe) {

    constructor() : this(Bombe()) {
    }

    override fun passCurrent(contact: Char, activatedVia: Connector, previousPathElement: CurrentPathElement?) {
        // nothing to do
    }
}