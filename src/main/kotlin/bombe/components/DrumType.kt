package bombe.components

import shared.RotorType

/**
 * Each DrumType represents an original Enigma rotor (or wheel) type.
 *
 * See the documentation in the Drum class for an explanation about 'ringSettingDelta'
 */
enum class DrumType(
    val rotorType: RotorType,
    val ringSettingDelta: Int
) {
    I (RotorType.I, 1),
    II (RotorType.II, 1),
    III (RotorType.III, 1),
    IV (RotorType.IV, 2),
    V (RotorType.V, 3);
}