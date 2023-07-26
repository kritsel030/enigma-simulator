package bombe.components

import bombe.Bombe
import bombe.connectors.*
import bombe.recorder.CurrentPathElement
import enigma.components.*
import shared.Util
import shared.BasicScrambler

class Scrambler (val id: Int, val noOfRotorsPerScrambler: Int, reflector: Reflector?, bombe: Bombe) :
    CircuitComponent("Scrambler-$id", bombe), ScramblerJackPanel {

    val _inputJack = Jack("IN$id", "IN$id", this)
    val _outputJack = Jack("OUT$id", "OUT$id", this)

    override fun getExternalLabel() : String {
        return id.toString()
    }

    override fun getInputJack() : Jack {
        return _inputJack
    }
    override fun getOutputJack() : Jack {
        return _outputJack
    }

    val letchworthEnigma: BasicScrambler = BasicScrambler(id.toString(), noOfRotorsPerScrambler, reflector)

    // ******************************************************************************************************
    // Features needed to support setting up the front-side of a bombe

    fun placeDrums(drumTypes: List<DrumType>) {
        check (drumTypes.size == noOfRotorsPerScrambler)
        {"expected $noOfRotorsPerScrambler rotor types to be placed on this scrambler, but received only ${drumTypes.size}"}

        val drumsOrRotors = drumTypes.map{Drum(it, 'Z')}.toList()

        letchworthEnigma.placeRotors(drumsOrRotors)
    }

    fun setRelativeStartOrientation(steps:Int) {
        letchworthEnigma.checkRotorsAndReflector()
        for (s in 1..steps) {
            // step the drum representing the right-rotor in the enigma
            letchworthEnigma.rightRotor!!.advanceStartOrientation()
        }
    }

    /**
     * startOrientations: String where each character specifies the start orientation of a rotor in this scrambler
     */
    fun setDrumStartOrientations(startOrientations:String) {
        letchworthEnigma.checkRotorsAndReflector()
        check(letchworthEnigma.rotors.size == startOrientations.length) {"this scrambler has ${letchworthEnigma.rotors.size} scramblers, got ${startOrientations.length} rotor start positions"}
        for ( (index, startPos) in startOrientations.withIndex() ) {
            letchworthEnigma.getRotor(index+1)!!.rotateToStartOrientation(startPos)
        }
    }

    // ******************************************************************************************************
    // Features needed to support changing/removing/placing of reflector boards on the left side of the bombe
    fun setReflector(reflector: Reflector?) {
        letchworthEnigma.reflector = reflector
    }


    // ******************************************************************************************************
    // Features needed to execute a bombe run

    override fun passCurrent(contact: Char, activatedVia : Connector, previousPathElement: CurrentPathElement?) {
        // check to see if the reflector and rotors are set-up on the internal scrambler
        letchworthEnigma.checkRotorsAndReflector()

        // println("pass current from ${activatedVia.connectedTo?.component?.label}-${activatedVia.connectedTo?.label} to ${this.label} at contact $contact")
        val resultContact = scramble(contact)
        var newPathElement : CurrentPathElement? = null
        if (previousPathElement != null) {
            newPathElement = CurrentPathElement(label, javaClass.simpleName, contact, resultContact, previousPathElement, previousPathElement.root)
            previousPathElement.addNext(newPathElement)
        }
        if (activatedVia == _inputJack) {
            _outputJack.passCurrentOutbound(resultContact, newPathElement)
        } else {
            _inputJack.passCurrentOutbound(resultContact, newPathElement)
        }
    }

    fun scramble(input: Char) : Char {
//        println("scrambler input: $input (${input.code})")
        val output = Util.toChar(letchworthEnigma!!.encrypt(Util.toInt(input)))
//        println("scrambler output: $output")
        return output
    }

}