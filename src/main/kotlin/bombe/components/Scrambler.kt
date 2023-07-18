package bombe.components

import bombe.MenuLink
import bombe.connectors.*
import bombe.recorder.CurrentPathElement
import enigma.components.*
import shared.Util
import shared.BasicScrambler

class Scrambler (val id: Int, val noOfRotorsPerScrambler: Int, val bank: Bank) : CircuitComponent("Scrambler-${bank.id}.$id", bank.bombe){

    val inputJack = Jack("IN$id", "IN$id", this)
    val outputJack = Jack("OUT$id", "OUT$id", this)
    val internalScrambler: BasicScrambler = BasicScrambler(noOfRotorsPerScrambler, Reflector(ReflectorType.B))

    // ******************************************************************************************************
    // Features needed to support setting up the front-side of a bombe

    fun placeDrums(drumTypes: List<DrumType>) {
        check (drumTypes.size == noOfRotorsPerScrambler)
        {"expected $noOfRotorsPerScrambler rotor types to be placed on this scrambler, but received only ${drumTypes.size}"}

        val drums = drumTypes.map{Drum(it)}.toList()

        internalScrambler.placeDrums(drums)
    }

    fun setRelativePosition(steps:Int) {
        internalScrambler.checkRotors()
        for (p in 1..steps) {
            // step the drum representing the right-rotor in the enigma
            internalScrambler.rightRotor!!.advanceRingOrientation()
        }
    }

    /**
     * startOrientations: String where each character specifies the start orientation of a rotor in this scrambler
     */
    fun setDrumStartOrientations(startOrientations:String) {
        internalScrambler.checkRotors()
        check(internalScrambler.rotors.size == startOrientations.length) {"this scrambler has ${internalScrambler.rotors.size} scramblers, got ${startOrientations.length} rotor start positions"}
        for ( (index, startPos) in startOrientations.withIndex() ) {
            internalScrambler.getRotor(index+1).rotateToRingOrientation(startPos)
        }
    }

    // ******************************************************************************************************
    // Features needed to verify correct plugging up of a bombe

    // each scrambler in use has an inputJack and an outputJack,
    // both of these jacks represent a letter in the menu
    // each jack should ultimately be connected to a DiagonalBoard jack which represents this same letter
    // possible connection paths:
    // - scrambler.in/outputJack --cable--> diagonalBoard
    // - scrambler.in/outputJack --cable--> commonsSet --cable--> diagonalBoard
    // - scrambler.in/outputJack --bridge--> diagonalBoard
    // - scrambler.in/outputJack --bridge--> commonsSet --cable--> diagonalBoard
    fun checkConnections(menuLink: MenuLink) : MutableList<String> {
        val errors = mutableListOf<String>()
        errors.addAll(checkJackConnections(inputJack, menuLink.inputLetter))
        errors.addAll(checkJackConnections(outputJack, menuLink.outputLetter))
        return errors
    }
    fun checkJackConnections(scramblerJack: Jack, representsLetter: Char) : MutableList<String>{
        val errors = mutableListOf<String>()
        val diagonalBoardJackForScramblerJack = findConnectedDiagonalBoardJack(scramblerJack)
        if (diagonalBoardJackForScramblerJack != null) {
            if (representsLetter != diagonalBoardJackForScramblerJack.letter) {
                errors.add(
                    "$label.${scramblerJack.externalLabel} is not correctly plugged up, " +
                            "expected ${representsLetter} -> ${representsLetter}, " +
                            "got ${representsLetter} -> ${diagonalBoardJackForScramblerJack.letter}"
                )
            }
        } else {
            errors.add("$label.${scramblerJack.externalLabel} is not correctly plugged up, " +
                    "it is not ultimately connected to the diagonal board")
        }
        return errors
    }

    private fun findConnectedDiagonalBoardJack(jack: Jack) : DiagonalBoardJack? {
        if (jack.pluggedUpBy() is CablePlug) {
            // this jack has a cable plugged into it
            // find the component on the other end of the cable
            val otherComponentJack = ((jack.pluggedUpBy() as CablePlug).getOppositePlug().pluggedInto()) as Jack
            val otherComponent = otherComponentJack!!.attachedTo
            if (otherComponent is DiagonalBoard) {
                return otherComponentJack as DiagonalBoardJack
            } else if (otherComponent is CommonsSet) {
                // find the jack of this CommonsSet which is plugged into the DiagonalBoard
                val commonsJackConnectedToDiagonalBoard = (otherComponent as CommonsSet).jacks().filter{it.pluggedUpBy() != null && (it.pluggedUpBy() as CablePlug).getOppositePlug().pluggedInto()!!.attachedTo is DiagonalBoard}.firstOrNull()
                if (commonsJackConnectedToDiagonalBoard == null)
                    return null
                else return (commonsJackConnectedToDiagonalBoard.pluggedUpBy() as CablePlug).getOppositePlug().pluggedInto() as DiagonalBoardJack
            }
        } else {
            // this jack has a bridge plugged into it,
            // find the diagonal board jack ultimately connected to this bridge's jack
            return findConnectedDiagonalBoardJack((jack.pluggedUpBy()!!.attachedTo as Bridge).jack)
        }
        // we should actually never get here...
        return null
    }

    fun checkRotors() {
        internalScrambler.checkRotors()
    }


    // ******************************************************************************************************
    // Features needed to execute a bombe run

    override fun passCurrent(contact: Char, activatedVia : Connector, previousPathElement: CurrentPathElement?) {
        // println("pass current from ${activatedVia.connectedTo?.component?.label}-${activatedVia.connectedTo?.label} to ${this.label} at contact $contact")
        val resultContact = scramble(contact)
        var newPathElement : CurrentPathElement? = null
        if (previousPathElement != null) {
            newPathElement = CurrentPathElement(label, javaClass.simpleName, contact, resultContact, previousPathElement, previousPathElement.root)
            previousPathElement.addNext(newPathElement)
        }
        if (activatedVia == inputJack) {
            outputJack.passCurrentOutbound(resultContact, newPathElement)
        } else {
            inputJack.passCurrentOutbound(resultContact, newPathElement)
        }
    }

    fun scramble(input: Char) : Char {
//        println("scrambler input: $input (${input.code})")
        val output = Util.toChar(internalScrambler!!.encrypt(Util.toInt(input)))
//        println("scrambler output: $output")
        return output
    }

}