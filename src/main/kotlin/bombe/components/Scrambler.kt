package bombe.components

import bombe.Bank
import bombe.MenuLink
import bombe.connectors.*
import bombe.recorder.CurrentPathElement
import enigma.Enigma
import enigma.components.*

class Scrambler (val id: Int, val bank: Bank) : CircuitComponent("Scrambler-${bank.id}.$id", bank.bombe){

    val inputJack = Jack("IN$id", "IN$id", this)
    val outputJack = Jack("OUT$id", "OUT$id", this)
    var enigma: Enigma? = null

    // ******************************************************************************************************
    // Features needed to support setting up the front-side of a bombe

    fun placeEnigma(rotorTypeRotor1:RotorType, rotorTypeRotor2: RotorType, rotorTypeRotor3: RotorType) {
        val reflector = Reflector(ReflectorType.B)
        var rotor1 = Rotor(rotorTypeRotor1, 'Y', 26)
        var rotor2 = Rotor(rotorTypeRotor2, 'W', 26)
        var rotor3 = Rotor(rotorTypeRotor3, 'Y', 26)

//        var rotor1 = Rotor(rotorTypeRotor1, 'Z', 26)
//        var rotor2 = Rotor(rotorTypeRotor2, 'Z', 26)
//        var rotor3 = Rotor(rotorTypeRotor3, 'Z', 26)


        val noPlugboard = Plugboard("")
        enigma = Enigma(reflector, rotor1, rotor2, rotor3, noPlugboard)
    }

    fun setRelativePosition(pos:Int) {
        for (p in 1..pos) {
            // step the drum representing the right-rotor in the enigma
            enigma?.rotor3?.stepRotor()
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
        if (jack.insertedPlug() is CablePlug) {
            // this jack has a cable plugged into it
            // find the component on the other end of the cable
            val otherComponentJack = ((jack.insertedPlug() as CablePlug).getOppositePlug().pluggedInto()) as Jack
            val otherComponent = otherComponentJack!!.attachedTo
            if (otherComponent is DiagonalBoard) {
                return otherComponentJack as DiagonalBoardJack
            } else if (otherComponent is CommonsSet) {
                // find the jack of this CommonsSet which is plugged into the DiagonalBoard
                val commonsJackConnectedToDiagonalBoard = (otherComponent as CommonsSet).jacks().filter{it.insertedPlug() != null && (it.insertedPlug() as CablePlug).getOppositePlug().pluggedInto()!!.attachedTo is DiagonalBoard}.firstOrNull()
                if (commonsJackConnectedToDiagonalBoard == null)
                    return null
                else return (commonsJackConnectedToDiagonalBoard.insertedPlug() as CablePlug).getOppositePlug().pluggedInto() as DiagonalBoardJack
            }
        } else {
            // this jack has a bridge plugged into it,
            // find the diagonal board jack ultimately connected to this bridge's jack
            return findConnectedDiagonalBoardJack((jack.insertedPlug()!!.attachedTo as Bridge).jack)
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