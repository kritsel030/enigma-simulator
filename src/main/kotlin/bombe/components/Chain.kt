package bombe.components

import bombe.Bombe
import bombe.connectors.Connector
import bombe.connectors.Jack
import bombe.recorder.CurrentPathElement

class Chain (val id: Int, bombe: Bombe)
    : CircuitComponent("Chain-$id", bombe), ChainJackPanel, ChainControlPanel, ChainIndicator {

    // create a Jack for the chain which will be present at the back of the bombe
    // (on a real bombe these Jacks are named CH1, CH2 and CH3, one for each Bank)
    val _inputJack = Jack("CH$id", "input-CH$id", this)

    override fun getInputJack() : Jack {
        return _inputJack
    }

    // ***********************************************************************************************************
    // sense relays are connected to the chain input
    fun readSenseRelays () : Map<Char, Boolean> {
        return _inputJack.readContacts()
    }

    // ***********************************************************************************************************
    // control panel support

    private var on: Boolean = false
    override fun switchOn() {
        on = true
    }
    override fun switchOff() {
        on = false
    }
    override fun isOn() : Boolean {
        return on
    }

    private var contactToActivate: Char? = null
    override fun setContactToActivate(contact: Char) {
        contactToActivate = contact
    }

    override fun getContactToActivate(): Char? {
        return contactToActivate
    }

    // ***********************************************************************************************************
    // Chain Indicator panel support

    private var indicatorRelays : Map<Char, Boolean> = mapOf<Char, Boolean>()
    init {
        resetIndicatorRelays()
    }
    fun transferSenseRelaysStateToIndicatorRelays() {
        indicatorRelays = readSenseRelays()
    }

    fun resetIndicatorRelays() {
        val freshRegister = mutableMapOf<Char, Boolean>()
        var char = 'A'
        for (i in 0..bombe.alphabetSize-1) {
            freshRegister[char.plus(i)] = false;
        }
        indicatorRelays = freshRegister
    }

    override fun readIndicatorRelays() : Map<Char, Boolean> {
        return indicatorRelays
    }

    // ***********************************************************************************************************
    // Features to support the running of a bombe
    fun run(previousPathElement: CurrentPathElement) {
        if (on) {
            _inputJack.passCurrentOutbound(this.contactToActivate!!, previousPathElement)
        }
    }
    override fun passCurrent(contact: Char, activatedVia: Connector, previousPathElement: CurrentPathElement?) {
        // nothing to do
    }

    /**
     * Indicates whether the current status of the active contacts of the chain's input jack
     * represents a valid stop
     * A valid stop is whenever less than 26 (for a 26 alphabetsize bombe) contacts are live
     */
    fun checkStepResult() : Boolean {
        val activeContactCount = _inputJack.readContacts().filter{entry -> entry.value }.size
        return activeContactCount < bombe.alphabetSize
    }

}