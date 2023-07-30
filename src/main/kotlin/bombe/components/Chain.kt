package bombe.components

import bombe.Bombe
import bombe.connectors.Connector
import bombe.connectors.Jack
import bombe.recorder.CurrentPathElement

class Chain (private val id: Int, bombe: Bombe)
    : CircuitComponent("Chain-$id", bombe), ChainJackPanel, ChainControlPanel, ChainIndicator {

    // create a Jack for the chain which will be present at the back of the bombe
    // (on a real bombe these Jacks are named CH1, CH2 and CH3, one for each Bank)
    val _inputJack = Jack("CH$id", "input-CH$id", this)

    override fun getInputJack() : Jack {
        return _inputJack
    }

    override fun getId() :Int {
        return id
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

    var searchLetterIndicatorRelays : Map<Char, Boolean> = mapOf<Char, Boolean>()
    init {
        resetSearchLetterIndicatorRelays()
    }

    fun resetSearchLetterIndicatorRelays() {
        val freshRegister = mutableMapOf<Char, Boolean>()
        var char = 'A'
        for (i in 0..bombe.alphabetSize-1) {
            freshRegister[char.plus(i)] = false;
        }
        searchLetterIndicatorRelays = freshRegister
    }

    override fun readSearchLetterIndicators() : Map<Char, Boolean> {
        return searchLetterIndicatorRelays
    }

    override fun readSearchLetters() : List<Char> {
        return searchLetterIndicatorRelays.filter { it.value }.map{it.key}.toList()
    }

    // ***********************************************************************************************************
    // Features to support the running of a bombe
    fun injectCurrent(previousPathElement: CurrentPathElement) {
        if (on) {
            _inputJack.passCurrentOutbound(this.contactToActivate!!, previousPathElement)
        }
    }
    override fun passCurrent(contact: Char, activatedVia: Connector, previousPathElement: CurrentPathElement?) {
        // nothing to do
    }

}