package bombe.components

import bombe.connectors.Jack
import bombe.components.PassThroughComponent
import bombe.connectors.Plug
import bombe.Bombe
import bombe.connectors.CablePlug

class Cable (label: String, bombe : Bombe) : PassThroughComponent(label, bombe){

    constructor(label: String, plugLeftPlugInto: Jack, plugRightPlugInto: Jack, bombe: Bombe) : this(label, bombe) {
        leftPlug.plugInto(plugLeftPlugInto)
        rightPlug.plugInto(plugRightPlugInto)
    }

    val leftPlug = CablePlug("left", this)
    val rightPlug = CablePlug("right", this)

}