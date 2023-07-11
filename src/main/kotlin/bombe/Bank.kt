package bombe

import bombe.connectors.Connector
import bombe.connectors.Jack
import bombe.components.CircuitComponent
import bombe.components.Scrambler
import bombe.recorder.CurrentPathElement
import enigma.components.RotorType
import java.lang.IllegalStateException

class Bank (val id: Int, noOfScramblersPerBank: Int, bombe: Bombe)
    : CircuitComponent("Bank-$id", bombe), BankControlPanel, BankDisplay {
    // Contract a Bank ****************************************************************************

    // construct a bank of scramblers
    val scramblers = mutableMapOf<Int, Scrambler>()
    init {
        for (s in 1..noOfScramblersPerBank) {
            scramblers.put(s, Scrambler(s, this))
        }
    }
    fun getScrambler(id:Int) : Scrambler {
        return scramblers.get(id)!!
    }

    fun claimNextAvailableScrambler(menuPosition: Int) : Scrambler {
        val availableScrambler = scramblers.values.filter { it -> it.isAvailable()}.firstOrNull();
        if (availableScrambler == null) throw IllegalStateException("[bank $id] No more free scrambler available in this bank")
        availableScrambler.claimForMenuPosition(menuPosition)
        return availableScrambler!!
    }

    // create a Jack for the bank which will be present at the back of the bombe
    // (on a real bombe these Jacks are named CH1, CH2 and CH3, one for each Bank)
    val jack = Jack("CH$id", "CH$id", this)

    // create a test register and connect it to the jack
    // (the test register can be connected to another connector to demonstrate
    //  single line scanning as was the case in the first bombe prototype (Victory))
    var testRegisterConnectedTo : Connector = jack
    fun connectTestRegisterTo(connector: Connector) {
        testRegisterConnectedTo = connector
    }


    // control panel support *************************************************************************
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

    // BankDisplay support ******************************************************************************
    override fun readTestRegister() : Map<Char, Boolean> {
        return jack.readContacts()
    }

    // Place drums on each scrambler of this bank *******************************************************
    // (each scrambler in the bank will be fitted with the same drum types)
    fun placeDrums(rotorTypeRotor1: RotorType, rotorTypeRotor2: RotorType, rotorTypeRotor3: RotorType) {
        for (scrambler in scramblers.values) {
            scrambler.placeEnigma(rotorTypeRotor1, rotorTypeRotor2, rotorTypeRotor3)
        }
    }

    // Run the bombe  ***********************************************************************************
    fun run(previousPathElement: CurrentPathElement) {
        if (on) {
            jack.activateContactOutbound(this.contactToActivate!!, previousPathElement)
        }
    }

    override fun passCurrent(contact: Char, activatedVia: Connector, previousPathElement: CurrentPathElement) {
        // nothing to do
    }

    fun checkStepResult() : Pair<Boolean, Map<Char, Boolean>?> {
        val activeContacts = readTestRegister().filter{entry -> entry.value }.count()
        val stop = activeContacts == 1 || activeContacts == bombe.alphabetSize - 1
//        val stop = activeContacts < bombe.alphabetSize
        if (stop) {
            // return true + the single inactive contact
            return Pair(true, readTestRegister())
        } else {
            return Pair(false, null)
        }
    }
}