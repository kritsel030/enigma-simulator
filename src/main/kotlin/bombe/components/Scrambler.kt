package bombe.components

import bombe.Bank
import bombe.MenuLink
import bombe.connectors.*
import bombe.recorder.CurrentPathElement
import enigma.Enigma
import enigma.components.*

class Scrambler (val id: Int, val noOfRotorsPerScrambler: Int, val bank: Bank) : CircuitComponent("Scrambler-${bank.id}.$id", bank.bombe){

    val inputJack = Jack("IN$id", "IN$id", this)
    val outputJack = Jack("OUT$id", "OUT$id", this)
    var enigma: Enigma? = null

    companion object {
        // https://www.lysator.liu.se/~koma/turingbombe/TuringBombeTutorial.pdf, page 2:
        // "Probably by mistake, drums I, II, III, VI, VII and VIII on the Bombe
        //   are one letter ahead of the corresponding Enigma rotors.
        //  Drum IV is two steps ahead, and rotor V is three steps ahead."
        val fixRotorStartPositionMap = mapOf(
            Pair(RotorType.I, 'Y'),    // Z - 1
            Pair(RotorType.II, 'Y'),   // Z - 1
            Pair(RotorType.III, 'Y'),  // Z - 1
            Pair(RotorType.IV, 'X'),   // Z - 2
            Pair(RotorType.V, 'W')     // Z - 3
        )
    }

    // ******************************************************************************************************
    // Features needed to support setting up the front-side of a bombe

    fun placeEnigma(rotorTypes: List<RotorType>) {
        check (rotorTypes.size == noOfRotorsPerScrambler)
        {"expected $noOfRotorsPerScrambler rotor types to be placed on this scrambler, but received only ${rotorTypes.size}"}

        val reflector = Reflector(ReflectorType.B)

        val rotors = rotorTypes.map{Rotor(it, fixRotorStartPositionMap.getOrDefault(it, 'Z'), 26)}.toList()

        val noPlugboard = Plugboard("")

        enigma = Enigma(reflector, rotors, noPlugboard)
    }

    fun setRelativePosition(pos:Int) {
        for (p in 1..pos) {
            // step the drum representing the right-rotor in the enigma
            enigma?.getRotor(3)?.stepRotor()
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

    private fun scramble(input: Char) : Char {
//        println("scrambler input: $input (${input.code})")
        // dummy implementation: proceed 10 characters down the alphabet
        //val output = Char(((input.code - 'A'.code + 10) % bombe.alphabetSize) + 'A'.code)
        val output = enigma!!.encrypt(input, false)
//        println("scrambler output: $output")
        return output
    }

}