package bombe.components

import bombe.Bombe
import bombe.connectors.Jack
import bombe.components.PassThroughComponent
import bombe.connectors.Plug

class Bridge (label: String, bombe : Bombe) : PassThroughComponent(label, bombe){

    // plug that goes into the out-jack of a scrambler
    val outPlug = Plug("out" ,this)

    // plug that goes into the in-jack of a subsequent scrambler
    val inPlug = Plug("in", this)

    // jack to plug a cable into, whose other plug either connects to a common
    // or to the diagonal board
    val jack = Jack("jack", "jack", this)
}