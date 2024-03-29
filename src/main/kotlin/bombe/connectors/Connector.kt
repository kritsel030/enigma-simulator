package bombe.connectors

import bombe.EnergySource
import bombe.components.CircuitComponent
import bombe.recorder.CurrentPathElement

abstract class Connector (val label:String, val attachedTo: CircuitComponent) : EnergySource() {
    // each contact is indicated by an uppercase letter (e.g. 'B')
    // the boolean value indicates whether the contact is live or not
    private val contacts : MutableMap<Char, Boolean> = mutableMapOf<Char, Boolean>()

    init {
        var char = 'A'
        for (i in 0..attachedTo.bombe.alphabetSize-1) {
            contacts[char.plus(i)] = false;
        }
        attachedTo.addConnector(this)
        attachedTo.bombe.mainCircuit.allConnectors.add(this)
    }

    fun removeCurrent() {
        contacts.replaceAll{_, value -> false}
    }

    fun readContacts() : Map<Char, Boolean> {
        // return a copy
        return contacts.toMap()
    }

    fun readActiveContacts() : List<Char>{
        return contacts.filterValues { it }.keys.toList()
    }

    fun countActiveContacts() : Int {
        return readActiveContacts().count()
    }

    protected var connectedTo : Connector? = null

    // Whenever two connectors (e.g. a plug and a jack) get connected, a two-way relationship is established between the connectors
    // This will allow a live contact in one connector to activate the proper contact in the other connector
    // and vice versa.
    protected fun connectTo(otherConnector : Connector) {
        // both connectors need to be free
        if (this.connectedTo != null) {
            throw IllegalStateException("Cannot plug ${this.attachedTo.label}.${this.label} to ${otherConnector.attachedTo.label}.${otherConnector.label} because ${this.attachedTo.label}.${this.label} is already plugged into ${this.connectedTo!!.attachedTo.label}.${this.connectedTo!!.label}")
        } else if (otherConnector.connectedTo != null) {
            throw IllegalStateException("Cannot plug ${this.attachedTo.label}.${this.label} to ${otherConnector.attachedTo.label}.${otherConnector.label} because ${otherConnector.connectedTo!!.attachedTo.label}.${otherConnector.connectedTo!!.label} is already plugged into ${otherConnector.attachedTo.label}.${otherConnector.label}")
        }
        this.connectedTo = otherConnector
        otherConnector.connectedTo = this
    }

    fun disconnect() {
        check(this.connectedTo != null) {"cannot disconnect as this connector ($label) isn't connected at the moment"}
        connectedTo?.connectedTo = null
        this.connectedTo = null
    }

    // A connector can be connected to another connector (e.g. a Plug of one component can be plugged into a Jack
    // of another component').
    // By 'inbound' we mean that the connector is getting a current from another component's connector this connector is connected to
    // (it is 'inbound' current from the perspective of the component this connector is attached to)
    fun passCurrentInbound (contact: Char, previousPathElement: CurrentPathElement? = null) : Boolean {
        // only pass the current when this contact was not already active
        if (!isContactActive(contact)) {
            contacts[contact] = true
            attachedTo.passCurrent(contact, this, previousPathElement)
            return true
        }
        return false
    }

    // A connector is always physically attached to component (e.g. a Plug is attached to a Cable).
    // By 'outbound' we mean that the connector is getting a current from the component it is attached to,
    // and if this connector is connected to a connector of another component, it will pass the current to that connector;
    // (it is 'outbound' current from the perspective of the component this connector is attached to).
    fun passCurrentOutbound(contact: Char, previousPathElement: CurrentPathElement? = null) : Boolean {
        // only pass the current when this contact was not already active
        if (!isContactActive(contact)) {
            contacts[contact] = true
            connectedTo?.passCurrentInbound(contact, previousPathElement)
            return true
        }
        return false
    }

    fun isContactActive(contact:Char) : Boolean {
        // todo remove
        check(contacts.containsKey(contact)) {"contact $contact does not exist"}
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