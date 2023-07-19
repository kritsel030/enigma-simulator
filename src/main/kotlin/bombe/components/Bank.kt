package bombe.components

import bombe.Bombe
import java.lang.IllegalStateException

class Bank (val id: Int, noOfScramblersPerBank: Int, noOfRotorsPerScrambler: Int, val bombe: Bombe)
    /*: CircuitComponent("Bank-$id", bombe), BankControlPanel, BankDisplay */{

    // ***********************************************************************************************************
    // Contract a Bank of scramblers, and ever

    // construct a bank of scramblers
    // key starts at 1
    private val scramblers = mutableMapOf<Int, Scrambler>()

    fun getScramblers() : List<Scrambler> {
        return scramblers.values.toList()
    }
    // index is a 1-based index
    fun getScrambler(index: Int) : Scrambler {
        if (scramblers.containsKey(index)) {
            return scramblers[index]!!
        } else {
            throw IllegalStateException("you're asking for scrambler $index but there are only ${scramblers.size} scramblers in this bank")
        }
    }

    init {
        for (s in 1..noOfScramblersPerBank) {
//            scramblers.put(s, Scrambler(s, noOfRotorsPerScrambler, this))
            scramblers.put(s, Scrambler(s, noOfRotorsPerScrambler, this))
        }
    }

//    // create a Jack for the bank which will be present at the back of the bombe
//    // (on a real bombe these Jacks are named CH1, CH2 and CH3, one for each Bank)
//    val inputJack = Jack("CH$id", "input-CH$id", this)

    // ***********************************************************************************************************
    // control panel support

//    private var on: Boolean = false
//    override fun switchOn() {
//        on = true
//    }
//    override fun switchOff() {
//        on = false
//    }
//    override fun isOn() : Boolean {
//        return on
//    }
//
//    private var contactToActivate: Char? = null
//    override fun setContactToActivate(contact: Char) {
//        contactToActivate = contact
//    }
//
//    override fun getContactToActivate(): Char? {
//        return contactToActivate
//    }

    // ***********************************************************************************************************
    // BankDisplay support

//    // by default the bank's test register is connected to the bank's input Jack
//    var testRegisterConnectedTo : Connector = inputJack
//
//    // The test register can be connected to another connector to demonstrate
//    //  single line scanning as was the case in the first bombe prototype (Victory))
//    fun connectTestRegisterTo(connector: Connector) {
//        testRegisterConnectedTo = connector
//    }
//
//    // Return the currently active contacts in the test register
//    override fun readTestRegister() : Map<Char, Boolean> {
//        return inputJack.readContacts()
//    }

    // ***********************************************************************************************************
    // Features to support the plugging up of a bombe based on a menu

    // Fit each scrambler in this bank with the given set of drum types
    fun placeDrums(drumTypes: List<DrumType>) {
        for (scrambler in scramblers.values) {
            scrambler.placeDrums(drumTypes)
        }
    }

    // ***********************************************************************************************************
    // Features to support the running of a bombe
//    fun run(previousPathElement: CurrentPathElement) {
//        if (on) {
//            inputJack.passCurrentOutbound(this.contactToActivate!!, previousPathElement)
//        }
//    }

//    override fun passCurrent(contact: Char, activatedVia: Connector, previousPathElement: CurrentPathElement?) {
//        // nothing to do
//    }

//    fun checkStepResult() : Pair<Boolean, List<Char>?> {
//        var potentialSteckerPartners : List<Char>? = null
//        val activeContacts = readTestRegister().filter{entry -> entry.value }.map{it.key}.toList()
////        val stop = activeContacts.size == 1 || activeContacts.size == bombe.alphabetSize - 1
//        val stop = activeContacts.size < bombe.alphabetSize
//        if (stop) {
//            if (activeContacts.size == 1) {
//                // pick the single active contact
//                potentialSteckerPartners = activeContacts
//            } else {
//                // pick all inactive entries in the test register
//                potentialSteckerPartners =
//                    readTestRegister().filter { entry -> !entry.value }.map { it.key }.toList()
//            }
//        }
//        return Pair(stop, potentialSteckerPartners)
//    }
}