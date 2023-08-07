package bombe

import bombe.connectors.Connector
import bombe.sensingcircuit.ChainInputContact
import bombe.sensingcircuit.Coil

/**
 * This main circuit class has references to all bombe elements which somehow register their energized state
 * in member variables.
 * This design facilitates the implementation of 'powerDown', as we only need to remove the current
 * from those elements, so they can reset their internal state.
 */
class MainCircuit(val bombe:Bombe) : EnergySource(){
    // represents all connectors (jacks and plugs) attached to any component of the bombe
    // this includes 'mobile components' like cables and bridges
    var allConnectors = mutableListOf<Connector>()

    // represents all coils of all relays
    var allRelayCoils = mutableListOf<Coil>()

    val connectedChainInputContacts = mutableListOf<ChainInputContact>()

    fun powerUp() {
        super.energize()
        connectedChainInputContacts.forEach { if (it.chain.isOn()) it.energize() }
    }

    fun powerDown() {
        allConnectors.forEach { it.removeCurrent() }
        allRelayCoils.forEach { it.removeCurrent() }
    }
}