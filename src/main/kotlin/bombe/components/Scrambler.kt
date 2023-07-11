package bombe.components

import bombe.Bank
import bombe.connectors.Connector
import bombe.connectors.Jack
import bombe.recorder.CurrentPathElement
import enigma.Enigma
import enigma.components.*

class Scrambler (id: Int, val bank: Bank) : CircuitComponent("Scrambler-${bank.id}.$id", bank.bombe){

    val inputJack = Jack("IN", "IN", this)
    val outputJack = Jack("OUT", "OUT", this)
    var enigma: Enigma? = null


    // ******************************************************************************************************
    // Features needed to support setting up the back-side of a bombe

    // When setting up a bombe based on a menu, we need a way of requesting an available
    // (= not yet claimed) Scrambler from the total set of Scrambler instances available
    // on a bombe. These fields and methods support this feature.
    // As a Scrambler is always used to represent a certain position in the menu,
    // we also register that position for debugging/informational purposes.
    var claimedForMenuPosition : Int? = null
        private set

    fun isAvailable() : Boolean {
        return claimedForMenuPosition == null
    }
    fun claimForMenuPosition(menuPosition: Int) {
        claimedForMenuPosition = menuPosition
    }

    // ******************************************************************************************************
    // Features needed to support setting up the front-side of a bombe

    fun placeEnigma(rotorTypeRotor1:RotorType, rotorTypeRotor2: RotorType, rotorTypeRotor3: RotorType) {
        val reflector = Reflector(ReflectorType.B)
        var rotor1 = Rotor(rotorTypeRotor1, 'Z', 1)
        var rotor2 = Rotor(rotorTypeRotor2, 'Z', 1)
        var rotor3 = Rotor(rotorTypeRotor3, 'Z', 1)
        val noPlugboard = Plugboard("UF-ET-GQ-AD-VN-HM-ZP-LJ-IK-XO")
        enigma = Enigma(reflector, rotor1, rotor2, rotor3, noPlugboard)
    }

    fun setRelativePosition(pos:Int) {
        for (p in 1..pos) {
            enigma?.rotor1?.stepRotor()
        }
    }

    // ******************************************************************************************************
    // Features needed to execute a bombe run

    override fun passCurrent(contact: Char, activatedVia : Connector, previousPathElement: CurrentPathElement) {
        // println("pass current from ${activatedVia.connectedTo?.component?.label}-${activatedVia.connectedTo?.label} to ${this.label} at contact $contact")
        val resultContact = scramble(contact)
        val newPathElement = CurrentPathElement(label, javaClass.simpleName, contact, resultContact, previousPathElement, previousPathElement.root)
        previousPathElement.addNext(newPathElement)
        if (activatedVia == inputJack) {
            outputJack.activateContactOutbound(resultContact, newPathElement)
        } else {
            inputJack.activateContactOutbound(resultContact, newPathElement)
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