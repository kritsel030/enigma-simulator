package bombe.connectors

import bombe.components.*

// Convention used in this codebase: each *female* connector is named a *Jack* (a.k.a. socket)
// This includes all fixed connectors on the backside of the bombe,
// plus the jack on a bridge to allow another cable to be plugged in to a bridge

// Whenever a plug get plugged into a jack (see the Plug class), both a relationship from plug-to-jack and a relationship
// from jack-to-plug is established. This allows a live contact in a plug to activate the proper
// contact in the jack it is plugged into, and vice versa.

// https://en.wikipedia.org/wiki/Gender_of_connectors_and_fasteners
/**
 * - externalLabel: label as it would appear on the backside of an actual bombe machine
 *   (these are not unique)
 * - label: label used for simulator information purposes to uniquely identify connectors
 *   attached to the same component
 * - component: the component this Jack is physically attached to
 */
open class Jack (val externalLabel: String,
            label: String,
            component: CircuitComponent) : Connector(label, component) {

    fun pluggedUpBy() : Plug? {
        return connectedTo as? Plug
    }

}