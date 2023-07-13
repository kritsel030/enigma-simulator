package bombe.connectors

import bombe.components.CircuitComponent

// Convention used in this codebase: each *male* connector is named a *Plug*
// This includes the connectors on all cables, plus the two connectors of a bridge
// which go into the output jack of one scrambler and the input jack of a neighbouring scrambler
// on the backside of a bombe

// Whenever a plug get plugged into a jack, both a relationship from plug-to-jack and a relationship
// from jack-to-plug is established. This allows a live contact in a plug to activate the proper
// contact in the jack it is plugged into, and vice versa.

// https://en.wikipedia.org/wiki/Gender_of_connectors_and_fasteners
open class Plug (label:String, component: CircuitComponent) : Connector(label, component) {

    // Whenever a Plug is plugged into a Jack, a two-way relationship is established between Plug and Jack
    // (this will allow a live contact in one connector to activate the proper contact in the other connector
    // and vice versa).
    fun plugInto(jack : Jack) {
        this.connectTo(jack)
    }

    fun pluggedInto() : Jack? {
        return connectedTo as? Jack
    }

    fun unplug() {
        disconnect()
    }
}