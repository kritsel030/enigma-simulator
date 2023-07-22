package bombe.components

import bombe.connectors.Jack

interface ScramblerJackPanel {

    fun getExternalLabel() : String
    fun getInputJack() : Jack
    fun getOutputJack() : Jack
}