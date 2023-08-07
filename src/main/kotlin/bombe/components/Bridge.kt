package bombe.components

import bombe.Bombe
import bombe.connectors.Jack
import bombe.components.PassThroughComponent
import bombe.connectors.Plug

/**
 * A bridge is used to connect the output and input of two consecutive scramblers on the back side of a bombe.
 * The output jack of one scrambler is connected to the input jack of the scrambler right below it.
 *
 * The bridge itself also has a jack. Via this jack the bridge can be connected to the diagonal board,
 * a commons or a chain input.
 */
class Bridge (label: String, bombe : Bombe) : PassThroughComponent(label, bombe){

    // plug that goes into the out-jack of a scrambler
    val outPlug = Plug("out" ,this)

    // plug that goes into the in-jack of a subsequent scrambler
    val inPlug = Plug("in", this)

    // jack to plug a cable into, whose other plug either connects to a commons
    // or to the diagonal board
    val jack = Jack("jack", "jack", this)
}