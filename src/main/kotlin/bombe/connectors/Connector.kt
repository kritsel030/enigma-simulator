package bombe.connectors

import bombe.components.CircuitComponent
import bombe.recorder.CurrentPathElement

abstract class Connector (val label:String, val attachedTo: CircuitComponent) {
    // each contact is indicated by an uppercase letter (e.g. 'B')
    // the boolean value indicates whether the contact is live or not
    private val contacts : MutableMap<Char, Boolean> = mutableMapOf<Char, Boolean>()

    init {
        resetContacts()
        attachedTo.addConnector(this)
    }

    fun resetContacts() {
        var char = 'A'
        for (i in 0..attachedTo.bombe.alphabetSize-1) {
            contacts[char.plus(i)] = false;
        }
    }

    fun readContacts() : Map<Char, Boolean> {
        return contacts
    }

    var pluggedTo : Connector? = null

    // Whenever two connectors (a plug and a jack) get connected, a two-way relationship is established between the connectors
    // This will allow a live contact in one connector to activate the proper contact in the other connector
    // and vice versa.
    protected fun connectTo(otherConnector : Connector) {
        // both connectors need to be free
        if (this.pluggedTo != null) {
            throw IllegalStateException("Cannot plug ${this.label} to ${otherConnector.label} because ${this.label} is already plugged into ${this.pluggedTo!!.label}")
        } else if (otherConnector.pluggedTo != null) {
            throw IllegalStateException("Cannot plug ${this.label} to ${otherConnector.label} because ${otherConnector.pluggedTo!!.label} is already plugged into ${otherConnector.label}")
        }
        this.pluggedTo = otherConnector
        otherConnector.pluggedTo = this
    }

    fun unconnect() {
        pluggedTo?.pluggedTo = null
        this.pluggedTo = null
    }

    // A connector can be connected to another connector (e.g. a Plug of one component can be plugged into a Jack
    // of another component, these are each others connector mates).
    // By 'inbound' we mean that the connector is getting a current from another component via its connector-mate;
    // it is 'inbound' current from the perspective of the component this connector is attached to.
    fun activateContactInbound(contact: Char, previousPathElement: CurrentPathElement) {
        if (attachedTo is CircuitComponent) {
            // only pass the current when this contact was not already active
            if (!isContactActive(contact)) {
                contacts[contact] = true
                attachedTo.passCurrent(contact, this, previousPathElement)
            }
        }
    }

    // A connector is always physically attached to component (e.g. a Plug is attached to a Cable).
    // By 'outbound' we mean that the connector is getting a current the component it is attached to,
    // and if this connector is connected to a connector of another component, it will pass the current to that connector;
    // it is 'outbound' current from the perspective of the component this connector is attached to.
    fun activateContactOutbound(contact: Char, previousPathElement: CurrentPathElement) {
        // only pass the current when this contact was not already active
        if (!isContactActive(contact)) {
            contacts[contact] = true
            pluggedTo?.activateContactInbound(contact, previousPathElement)
        }
    }

    fun isContactActive(contact:Char) : Boolean {
        return contacts[contact]!!;
    }

    fun printStatus() {
        println("[${attachedTo.label}] $label")
        for (c in contacts.entries) {
            if (c.value) {
                print(c.key)
            } else {
                print('.')
            }
        }
        println()
    }


}