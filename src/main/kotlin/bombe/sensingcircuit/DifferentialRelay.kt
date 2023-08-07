package bombe.sensingcircuit

import bombe.MainCircuit

/*
* Sensing relays in the Turing-Welchman bombe are so-called 'differential relays'.
* Turing mentions the term 'differential relay' on 'Turing's Treatise on the Enigma', Chapter 6, page 106.
* https://www.ugr.es/~aquiran/cripto/museo/turing/turchap6.pdf
*
* Spider Number 3 - Goldon Welchman - Autumn 1940
* https://www.joelgreenberg.co.uk/_files/ugd/bbe412_691c3b0212d64e4dbcc1ebf180bc50bd.pdf
* from section "THE SENSING RELAYS":
*
* "Each sensing relay has two windings [a.k.a. 'coils], in opposite directions, and the relay is “up”, i.e.
* the relay switches operate, when current passes through one coil but not through the other.
* When current passes through both coils or through neither coil the switches do not operate,
* and the relay is in its normal or “down” position."
 */
open class DifferentialRelay protected constructor(val primaryCoil: Coil, val secondaryCoil: Coil)
{
    constructor(mainCircuit: MainCircuit) : this(Coil(mainCircuit), Coil(mainCircuit))

    // true = current passes through one coil but not through the other
    //          relay will pass current
    //          relay operates | relay is "up" | relay moves
    // false   = current passes through both coils or through neither
    //          relay will not pass the current
    //          relay does not operate | relay is "down" | relay does not move |
    fun operates() : Boolean {
        return primaryCoil.isEnergized().xor(secondaryCoil.isEnergized())    }
}