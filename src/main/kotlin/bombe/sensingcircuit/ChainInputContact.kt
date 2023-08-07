package bombe.sensingcircuit

import bombe.EnergySource
import bombe.components.Chain

class ChainInputContact (val chain: Chain, val contactId:Char) : EnergySource() {

    override fun energize() {
        if (chain.isOn()) {
            super.energize()
            chain.getInputJack().passCurrentOutbound(contactId)
        }
    }
}