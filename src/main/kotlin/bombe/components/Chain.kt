package bombe.components

import bombe.Bombe
import bombe.EnergySource
import bombe.Switch
import bombe.connectors.Connector
import bombe.connectors.Jack
import bombe.recorder.CurrentPathElement
import bombe.sensingcircuit.ChainInputContact
import bombe.sensingcircuit.ChainSensingCircuit

class Chain (private val id: Int, bombe: Bombe)
    : CircuitComponent("Chain-$id", bombe), ChainJackPanel, ChainControlPanel, ChainIndicator {

    val chainInputContacts = mutableMapOf<Char, ChainInputContact>()
    init {
        for (i in 0 .. bombe.alphabetSize-1) {
            val letter = 'A'.plus(i)
            chainInputContacts.put(letter, ChainInputContact(this, letter))
        }
    }

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

    var chainSwitch = Switch("$label-switch")
    init {
        switchOff()
    }

    override fun switchOff() {
        chainSwitch.switchOff()
    }

    override fun switchOn() {
        chainSwitch.switchOn()
    }

    override fun isOn() : Boolean {
        return chainSwitch.isOn()
    }

    private val searchLetterSwitches = mutableMapOf<Char, Boolean>()

    /*
    * Spider Number 3 - Goldon Welchman - Autumn 1940
    * https://www.joelgreenberg.co.uk/_files/ugd/bbe412_691c3b0212d64e4dbcc1ebf180bc50bd.pdf
    * from section "THE SENSING RELAYS":
    *
    * About a SensingRelay connected to a specific input contact of Chain:
    * "Normally the positive end of the primary coil is connected to the positive side of the main circuit [...]
    *  and the positive end of the secondary coil is connected to the corresponding line of the input."
"   */
    override fun swichOffSearchLetter(letter:Char) {
        // register that this letter's switch is off
        searchLetterSwitches[letter] = false
        // change the connections of the differential sensing relays accordingly
        // (see ChainSensingCircuit for an explanation)
        val sensingRelay = bombe.sensingCircuit.chainSensingCircuits[id.toString()]!!.sensingRelays[letter]!!
        sensingRelay.primaryCoil.connectTo(bombe.mainCircuit)
        sensingRelay.secondaryCoil.connectTo(chainInputContacts[letter]!!)
        // disconnect the chain's input contact associated with this letter from the main circuit
        bombe.mainCircuit.connectedChainInputContacts.remove(chainInputContacts[letter]!!)
    }

    /*
    * Spider Number 3 - Goldon Welchman - Autumn 1940
    * https://www.joelgreenberg.co.uk/_files/ugd/bbe412_691c3b0212d64e4dbcc1ebf180bc50bd.pdf
    * from section "THE SENSING RELAYS":
    *
    * About a SensingRelay connected to a specific input contact of Chain:
    * "But a current entry switch breaks these two connections [see 'switchOff']
    *  and connects the line of the input to the positive side of the main circuit."
"   */
    override fun swichOnSearchLetter(letter:Char) {
        // register that this letter's switch is on
        searchLetterSwitches[letter] = true
        // change the connections of the differential sensing relays accordingly
        // (see ChainSensingCircuit for an explanation)
        val sensingRelay = bombe.sensingCircuit.chainSensingCircuits[id.toString()]!!.sensingRelays[letter]!!
        sensingRelay.primaryCoil.disconnect()
        sensingRelay.secondaryCoil.disconnect()
        // connect the chain's input contact associated with this letter to the main circuit
        bombe.mainCircuit.connectedChainInputContacts.add(chainInputContacts[letter]!!)
    }

    private var contactToActivate: Char? = null
    override fun setContactToActivate(contact: Char) {
        contactToActivate = contact
        swichOnSearchLetter(contact)
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
        if (chainSwitch.isOn()) {
            _inputJack.passCurrentOutbound(this.contactToActivate!!, previousPathElement)
        }
    }
    override fun passCurrent(contact: Char, activatedVia: Connector, previousPathElement: CurrentPathElement?) {
        // needed to reach the sensing circuit
        chainInputContacts[contact]!!.energize()
    }

}