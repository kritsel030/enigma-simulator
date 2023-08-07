package bombe

import bombe.sensingcircuit.Coil

open class EnergySource {
    protected var connectedCoils = mutableListOf<Coil>()

    fun addConnectedCoil(coil: Coil) {
        connectedCoils.add(coil)
    }

    fun removeConnectedCoil(coil: Coil) {
        connectedCoils.remove(coil)
    }

    // The bombe's sensing circuit consists of several differential relays, each rely consisting of two coils.
    // The coils of those relays can be connected to bombe components which are EnergySource instances.
    // This means that when this EnergySource gets energized, the connected coils need to be energized as well.
    open fun energize() {
        connectedCoils.forEach { it.energize() }
    }

}