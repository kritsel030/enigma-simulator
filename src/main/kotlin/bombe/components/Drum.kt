package bombe.components

import shared.Util
import shared.AbstractRotor
import shared.PlainLetterRing
import shared.RotorCore

/**
 * Based on "The Turing Bombe" by Frank Carter, Bletchley Park Trust. ISBN: 978-1-906723-03-3.
 *
 * Each bombe drum is meant to represent an enigma rotor of a certain type with its ring setting set to 'Z'
 * (meaning: contact 00 of the inner core should be aligned with 'Z' on the outer ring)
 * Unfortunately, when the drums were designed, the actual orientation of the wiring cores within the drum ended up
 * being one position behind the intended orientation: contact 25 got to be aligned with 'Z' and contact 00 with 'A'.
 * Additionally, orientations of the wiring cores for rotors IV and V as given by the Polish Cypher Bureau were misunderstood
 * by the British, resulting in an additional orientation difference for these rotor types.
 *
 * Legend to the diagram below:
 *   X, Y, Z, A, B, C : letters on the outer ring
 *   o    : a contact on the inner core
 *   o*   : the contact with id 00
 *   (10) : contact ID (as used in this code, not physically present on the drum)
 *
 *   intended drum   |    actual drum    |     actual drum   |    actual drum
 *      design       |      design       |       design      |      design
 *    all types      |  type I, II, III  |       type IV     |      type V
 *  =================|===================|===================|===================
 *    etc.           |    etc.           |    etc.           |    etc.
 *    ---            |    ---            |    ---            |    ---
 *     C   o  (03)   |     C   o  (02)   |     C   o  (01)   |     C   o* (00)
 *    ---            |    ---            |    ---            |    ---
 *     B   o  (02)   |     B   o  (01)   |     B   o* (00)   |     B   o  (25)
 *    ---            |    ---            |    ---            |    ---
 *     A   o  (01)   |     A   o  (00)   |     A   o  (25)   |     A   o  (24)
 *    ---            |    ---            |    ---            |    ---
 *     Z   o* (00)   |     Z   o* (25)   |     Z   o  (24)   |     Z   o  (23)
 *    ---            |    ---            |    ---            |    ---  
 *     Y   o  (25)   |     Y   o  (24)   |     Y   o  (23)   |     Y   o  (22)
 *    ---            |    ---            |    ---            |    ---
 *     X   0  (24)   |     X   o  (23)   |     X   o  (22)   |     X   o  (21)
 *    ---            |    ---            |    ---            |    ---
 *    etc.           |    etc.           |    etc.           |    etc.
 */
class Drum internal constructor (drumType: DrumType, startOrientation: Char, correctedRingSetting: Char) :

    AbstractRotor(
        RotorCore(drumType.rotorType, Util.normalize(Util.toInt(startOrientation) - Util.toInt(correctedRingSetting), drumType.rotorType.alphabetsize)),
        PlainLetterRing(),
        correctedRingSetting) {

        constructor(drumType: DrumType, startOrientation: Char) : this(drumType, startOrientation, 'Z'.plus(drumType.ringSettingDelta))
}