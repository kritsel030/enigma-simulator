package bombe.components

import bombe.ChainControlPanel
import bombe.ChainDisplay
import bombe.Bombe
import bombe.connectors.Connector
import bombe.connectors.Jack
import bombe.recorder.CurrentPathElement

class Chain (val id: Int, bombe: Bombe)
    : CircuitComponent("Chain-$id", bombe), ChainJackPanel, ChainControlPanel, ChainDisplay {

    // create a Jack for the chain which will be present at the back of the bombe
    // (on a real bombe these Jacks are named CH1, CH2 and CH3, one for each Bank)
    val _inputJack = Jack("CH$id", "input-CH$id", this)

    override fun getInputJack() : Jack {
        return _inputJack
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
    // BankDisplay support

    private var testRegister : Map<Char, Boolean> = mapOf<Char, Boolean>()
    init {
        resetTestRegister()
    }
    fun fillTestRegister(contactsOfInputJack:Map<Char, Boolean>) {
        testRegister = contactsOfInputJack
    }

    fun resetTestRegister() {
        val freshRegister = mutableMapOf<Char, Boolean>()
        var char = 'A'
        for (i in 0..bombe.alphabetSize-1) {
            freshRegister[char.plus(i)] = false;
        }
        testRegister = freshRegister
    }

    override fun readTestRegister() : Map<Char, Boolean> {
        return testRegister
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

    fun checkStepResult() : Pair<Boolean, List<Char>?> {
        var potentialSteckerPartners : List<Char>? = null
        val activeContacts = _inputJack.readContacts().filter{entry -> entry.value }.map{it.key}.toList()
        val stop = activeContacts.size < bombe.alphabetSize
        if (stop) {
            if (activeContacts.size == 1) {
                // pick the single active contact
                potentialSteckerPartners = activeContacts
            } else {
                // pick all inactive entries in the test register
                potentialSteckerPartners =
                    _inputJack.readContacts().filter { entry -> !entry.value }.map { it.key }.toList()
            }
        }
        return Pair(stop, potentialSteckerPartners)
    }

}