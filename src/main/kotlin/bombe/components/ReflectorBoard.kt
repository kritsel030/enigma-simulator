package bombe.components

import enigma.components.Reflector
import enigma.components.ReflectorType

/**
 * On the left side of the bombe an operator can change reflector boards.
 * Each reflector board belongs to a bank of scrambles. It contains a set of reflectors of the same type,
 * and these reflectors are wired to the scramblers of this bank.
 */
class ReflectorBoard(val reflectorType: ReflectorType) {

    fun getReflector() : Reflector {
        return Reflector(reflectorType)
    }
}