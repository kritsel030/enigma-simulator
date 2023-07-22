package bombe.components

import bombe.Bombe
import bombe.connectors.Connector
import bombe.connectors.DiagonalBoardJack
import bombe.connectors.Jack
import bombe.recorder.CurrentPathElement

class DiagonalBoard(id: Int, bombe: Bombe) : CircuitComponent ("DB-$id", bombe), DiagonalBoardJackPanel{

    val _jacks = mutableMapOf<Char, DiagonalBoardJack>()
    override fun getJacks() : List<Jack> {
        return _jacks.values.toList()
    }
    init {
        for (i in 0 .. bombe.alphabetSize) {
            var letter = 'A'.plus(i)
            _jacks.put(letter, DiagonalBoardJack(letter, this))
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

    override fun getJack(letter: Char) : Jack {
        return _jacks.get(letter)!!
    }
}