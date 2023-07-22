package bombe.components

import shared.RotorType

/**
 * Explanation for 'enigmaOffsetDifference':
 * Based on "The Turing Bombe" by Frank Carter, Bletchley Park Trust. ISBN: 978-1-906723-03-3.
 * When the Bombe drums for the initial rotor types (I, II, III) were designed, the orientations of the wiring cores
 * were one position behind the orientation of the Enigma rotors.
 * Additionally, orientations of the wiring cores for rotors IV and V as given by the Polish Cypher Bureau were misunderstood
 * by the British, resulting in an additional offset difference for these rotor types.
 *
 * We implement this difference in our bombe simulator by basing Enigma rotors and Bombe drums on the same wiring mapping,
 * while at the same time specifying a specific 'enigmaOffsetDifference' for each 'drum type'.
 *
 * As an example, take an Enigma rotor and a Bombe drum of type I:
 * - Assume ringsetting 'A' on the Enigma rotor, meaning that the rotor core contact we label as contact 0,
 *   is aligned with 'A' on the rotor's outer ring.
 * - The associated Bombe drum will have that same contact 0 aligned with letter 'Z' (1 position behind 'A').
 *   We specify that as enigmaOffsetDifference=1.
 */
enum class DrumType(
    val rotorType: RotorType,
    // see above for explanation
    val enigmaOffsetDifference: Int
) {
    I (RotorType.I, 1),
    II (RotorType.II, 1),
    III (RotorType.III, 1),
    IV (RotorType.IV, 2),
    V (RotorType.V, 3);
}