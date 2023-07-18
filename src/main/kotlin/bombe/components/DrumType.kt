package bombe.components

import shared.RotorType

enum class DrumType(
    val rotorType: RotorType,
    val coreOffset: Int
) {
    I (RotorType.I, 1),
    II (RotorType.II, 1),
    III (RotorType.III, 1),
    IV (RotorType.IV, 2),
    V (RotorType.V, 3);
}