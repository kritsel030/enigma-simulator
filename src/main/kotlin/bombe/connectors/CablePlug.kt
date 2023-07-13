package bombe.connectors

import bombe.components.Cable

class CablePlug (label: String, cable: Cable): Plug(label, cable) {

    // this plug is attached to a cable, get the plug at the other end of that cable
    fun getOppositePlug() : CablePlug {
        return this.attachedTo.connectors.filter{it != this}.first() as CablePlug
    }
}