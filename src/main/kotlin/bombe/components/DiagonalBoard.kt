package bombe.components

import bombe.Bombe
import bombe.connectors.Connector
import bombe.connectors.Jack
import bombe.recorder.CurrentPathElement

class DiagonalBoard(id: Int, bombe: Bombe) : CircuitComponent ("DB-$id", bombe){

    val jacks = mutableMapOf<Char, Jack>()
    init {
        for (i in 0 .. bombe.alphabetSize) {
            var letter = 'A'.plus(i)
            jacks.put(letter, Jack(letter.toString(), letter.toString(), this))
        }
    }
    override fun passCurrent(contact: Char, activatedVia: Connector, previousPathElement: CurrentPathElement) {
        val newPathElement = CurrentPathElement(label, javaClass.simpleName, contact, activatedVia.label.first(), previousPathElement, previousPathElement.root)
        previousPathElement.addNext(newPathElement)
        val outputJack = getJack(contact)
        outputJack.activateContactOutbound(activatedVia.label.first(), newPathElement)
    }

    fun getJack(letter: Char) : Jack {
        return jacks.get(letter)!!
    }


}