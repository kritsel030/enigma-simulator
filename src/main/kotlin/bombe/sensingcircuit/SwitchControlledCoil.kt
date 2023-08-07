package bombe.sensingcircuit

import bombe.MainCircuit
import bombe.Switch

/*
 A Coil (part of a DifferentialRelay) whose operation (energization) is controlled by a switch
 */
class SwitchControlledCoil(mainCircuit: MainCircuit, private val switch: Switch) : Coil(mainCircuit){

    override fun energize() {
        if (switch.isOn()) {
            super.energize()
        }
    }
}