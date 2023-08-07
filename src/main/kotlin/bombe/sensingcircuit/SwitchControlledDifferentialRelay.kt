package bombe.sensingcircuit

import bombe.MainCircuit
import bombe.Switch

/*
* Differential relay whose operation is controlled by a switch
 */
open class SwitchControlledDifferentialRelay(mainCircuit: MainCircuit, switch: Switch) : DifferentialRelay(SwitchControlledCoil(mainCircuit, switch), SwitchControlledCoil(mainCircuit, switch)){

}