package bombe.components

import bombe.Bombe
import bombe.connectors.Connector
import bombe.connectors.DiagonalBoardJack
import bombe.connectors.Jack
import bombe.recorder.CurrentPathElement

class DiagonalBoard(id: Int, bombe: Bombe) : CircuitComponent ("DB-$id", bombe){

    val jacks = mutableMapOf<Char, DiagonalBoardJack>()
    init {
        for (i in 0 .. bombe.alphabetSize) {
            var letter = 'A'.plus(i)
            jacks.put(letter, DiagonalBoardJack(letter, this))
        }
    }
    override fun passCurrent(contact: Char, activatedVia: Connector, previousPathElement: CurrentPathElement?) {
        var newPathElement : CurrentPathElement? = null
        if (previousPathElement != null) {
            newPathElement = CurrentPathElement(label, javaClass.simpleName, contact, activatedVia.label.first(), previousPathElement, previousPathElement.root)
            previousPathElement.addNext(newPathElement)
        }
        val outputJack = getJack(contact)
        outputJack.passCurrentOutbound(activatedVia.label.first(), newPathElement)
    }

    fun getJack(letter: Char) : Jack {
        return jacks.get(letter)!!
    }
}