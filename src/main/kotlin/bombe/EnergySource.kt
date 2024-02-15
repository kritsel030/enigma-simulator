package bombe

import bombe.sensingcircuit.Coil

/**
 * Anything that can be connected to the (positive of a) coil belonging to a differential relay
 * must extend this class.
 * See the ChainSensingCircuit class for more information.
 */
open class EnergySource {
    protected var connectedCoils = mutableListOf<Coil>()

    fun addConnectedCoil(coil: Coil) {
        connectedCoils.add(coil)
    }

    fun removeConnectedCoil(coil: Coil) {
        connectedCoils.remove(coil)
    }

    // The bombe's sensing circuit consists of several differential relays, each relay consisting of two coils.
    // The coils of those relays can be connected to bombe elements which are EnergySource instances
    // (either the single MainCircuit or a ChainInputContact (each chain has 26 input contacts)).
    // This means that when this EnergySource gets energized, the connected coils get to be energized as well.
    // See the ChainSensingCircuit class for more information.
    open fun energize() {
        connectedCoils.forEach { it.energize() }
    }

}