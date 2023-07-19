package bombe.components

import bombe.ChainControlPanel
import bombe.ChainDisplay
import bombe.Bombe
import bombe.connectors.Connector
import bombe.connectors.Jack
import bombe.recorder.CurrentPathElement

class Chain (val id: Int, bombe: Bombe)
    : CircuitComponent("Chain-$id", bombe), ChainControlPanel, ChainDisplay {

    // create a Jack for the chain which will be present at the back of the bombe
    // (on a real bombe these Jacks are named CH1, CH2 and CH3, one for each Bank)
    val inputJack = Jack("CH$id", "input-CH$id", this)

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

    // by default the bank's test register is connected to the bank's input Jack
    var testRegisterConnectedTo : Connector = inputJack

    // The test register can be connected to another connector to demonstrate
    //  single line scanning as was the case in the first bombe prototype (Victory))
    fun connectTestRegisterTo(connector: Connector) {
        testRegisterConnectedTo = connector
    }

    // Return the currently active contacts in the test register
    override fun readTestRegister() : Map<Char, Boolean> {
        return inputJack.readContacts()
    }

    // ***********************************************************************************************************
    // Features to support the running of a bombe
    fun run(previousPathElement: CurrentPathElement) {
        if (on) {
            inputJack.passCurrentOutbound(this.contactToActivate!!, previousPathElement)
        }
    }
    override fun passCurrent(contact: Char, activatedVia: Connector, previousPathElement: CurrentPathElement?) {
        // nothing to do
    }

    fun checkStepResult() : Pair<Boolean, List<Char>?> {
        var potentialSteckerPartners : List<Char>? = null
        val activeContacts = readTestRegister().filter{entry -> entry.value }.map{it.key}.toList()
//        val stop = activeContacts.size == 1 || activeContacts.size == bombe.alphabetSize - 1
        val stop = activeContacts.size < bombe.alphabetSize
        if (stop) {
            if (activeContacts.size == 1) {
                // pick the single active contact
                potentialSteckerPartners = activeContacts
            } else {
                // pick all inactive entries in the test register
                potentialSteckerPartners =
                    readTestRegister().filter { entry -> !entry.value }.map { it.key }.toList()
            }
        }
        return Pair(stop, potentialSteckerPartners)
    }

}