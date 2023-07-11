package bombe.connectors

import bombe.components.Cable

class CablePlug (label: String, cable: Cable): Plug(label, cable) {

    // get the plug at the other end of this cable
    fun getOtherPlug() : CablePlug {
        return this.attachedTo.connectors.filter{it != this}.first() as CablePlug
    }
}