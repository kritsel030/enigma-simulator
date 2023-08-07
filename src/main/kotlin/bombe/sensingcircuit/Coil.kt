package bombe.sensingcircuit

import bombe.EnergySource
import bombe.MainCircuit

/*
 A Coil represents one of the two windings/coils of a DifferentialRelay.
 */
open class Coil(mainCircuit: MainCircuit) {

    init {
        mainCircuit.allRelayCoils.add(this)
    }

    private var energized = false

    open fun energize() {
        energized = true
    }

    open fun removeCurrent() {
        energized = false
    }

    fun isEnergized() : Boolean {
        return energized
    }

    var connectedTo: EnergySource? = null
        private set

    fun connectTo(energySource: EnergySource) {
        connectedTo = energySource
        energySource.addConnectedCoil(this)
    }

    fun disconnect() {
        connectedTo?.removeConnectedCoil(this)
        connectedTo = null
    }
}