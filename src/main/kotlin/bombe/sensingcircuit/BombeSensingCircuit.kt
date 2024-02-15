package bombe.sensingcircuit

import bombe.Bombe
import bombe.components.Chain

class BombeSensingCircuit(val bombe: Bombe) {

    var chainSensingCircuits = mutableMapOf<String, ChainSensingCircuit>()

    fun addOrReplaceCircuitForChainId(chain: Chain) {
        chainSensingCircuits.put(chain.getId().toString(), ChainSensingCircuit(chain))
        // needed to initialize the sensing circuit (ChainSensingCircuit must have been set before executing this part)
        chain.chainInputContacts.keys.forEach { chain.swichOffSearchLetter(it) }
    }

    fun removeCircuitForChainId(id: String) {
        chainSensingCircuits.remove(id)
    }

    fun shouldBombeStop(printCurrentPath: Boolean) : Boolean {
        var stop = false
        val activeAndOpenChainSensingCircuits = chainSensingCircuits.values.filter { it.chain.isOn() && it.isOpen()}

        // when 'double input' is switched on, both chains must indicate a stop for the bombe to actually stop
        // 'double input' as described in the US bombe report 1944, chapter I, paragraph TYPES OF MENUS, page 25:
        // https://www.codesandciphers.org.uk/documents/bmbrpt/bmbpg029.htm
        // "In the newer type machines the "double input" switch operated. [...]
        // This changes the association of wiring and the contacts of the sensing relays so that the bombe
        // will not stop unless there is an open circuit on both parts of the menu."
        if ( (bombe.isDoubleInputOn() && activeAndOpenChainSensingCircuits.size == 2) || (!bombe.isDoubleInputOn() && activeAndOpenChainSensingCircuits.size > 0)) {
            activeAndOpenChainSensingCircuits.forEach { run {
                transferSenseRelaysStateToIndicatorRelays(it.chain)
            } }
            stop = true
        }
        return stop
    }

    private fun transferSenseRelaysStateToIndicatorRelays(chain: Chain) {
        if (chain.getInputJack().readContacts().filter { it.value }.count() == 1) {
            chain.searchLetterIndicatorRelays = chain.getInputJack().readContacts().toMap()
        } else {
            chain.searchLetterIndicatorRelays = chain.getInputJack().readContacts().map { it.key to !it.value }.toMap()
        }
    }
}