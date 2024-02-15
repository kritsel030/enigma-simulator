package bombe.sensingcircuit

import bombe.MainCircuit

/*
* Sensing relays in the Turing-Welchman bombe are so-called 'differential relays'.
*
* Turing mentions the term 'differential relay' when explaining the Bombe's
* "mechanism for "distinguishes correct from incorrect positions".
* Turing's Treatise on the Enigma | "Prof's book" | Dr Alan M. Turing | Chapter 6
* Crown copyright is reproduced with the permission of the Controller of Her Majesty's Stationery Office.
* (edited by Ralph Erskine, Philip Marks and Frode Weierud in 1999)
* https://cryptocellar.org/turing/turchap6.pdf
* On page 110:
* "Normally current will flow through all the differential relays and they will not move.
* When one reaches a position that might be correct the current fails to reach one of those relays,
* and the current permanently flowing in the other coil of the relay causes it to close,
* and bring the stopping mechanism into play. "
*
* Gordon Welchan doesn't use the term 'differential relay', but he does describe the workings of a 'sensing relay'
* which fits the general description of a differential relay
* Spider Number 3 - Goldon Welchman - Autumn 1940
* https://www.joelgreenberg.co.uk/_files/ugd/bbe412_691c3b0212d64e4dbcc1ebf180bc50bd.pdf
* from section "THE SENSING RELAYS":
* "Each sensing relay has two windings [a.k.a. 'coils'], in opposite directions, and the relay is “up”, i.e.
* the relay switches operate, when current passes through one coil but not through the other.
* When current passes through both coils or through neither coil the switches do not operate,
* and the relay is in its normal or “down” position."
 */
open class DifferentialRelay protected constructor(val primaryCoil: Coil, val secondaryCoil: Coil)
{
    constructor(mainCircuit: MainCircuit) : this(Coil(mainCircuit), Coil(mainCircuit))

    // true  = current passes through one coil but not through the other
    //         relay will pass current
    //         relay "operates" | relay is "up" | relay "moves"
    // false = current passes through both coils or through neither
    //         relay will not pass the current
    //         relay does not operate | relay is "down" | relay does not move | relay is in its "normal" position
    fun operates() : Boolean {
        return primaryCoil.isEnergized().xor(secondaryCoil.isEnergized())    }
}