package showcase

import enigma.Enigma
import enigma.components.Plugboard
import enigma.components.Reflector
import enigma.components.ReflectorType
import enigma.components.Rotor
import shared.RotorType

fun main(args: Array<String>) {
    val showcaseRotorTypes = RotorType.values().filter { it.alphabetsize == 6 }
    val alphabet = "ABCDEF"
    var variant = 0
    showcaseRotorTypes.forEach { leftRotorType ->
        alphabet.forEach { leftStart ->
            showcaseRotorTypes
                .filter { middleRotorType -> middleRotorType != leftRotorType }
                .forEach { middleRotorType ->
                    alphabet.forEach { middleStart ->
                        showcaseRotorTypes
                            .filter { rightRotorType -> rightRotorType != leftRotorType && rightRotorType != middleRotorType }
                            .forEach { rightRotorType ->
                                alphabet.forEach { rightStart ->
                                    alphabet.forEach { stecker1 ->
                                        alphabet
                                            .filter { stecker2 -> stecker2 > stecker1 }
                                            .forEach { stecker2 ->
                                                alphabet
                                                    .filter {stecker3 -> stecker3 > stecker2}
                                                    .forEach { stecker3 ->
                                                        alphabet
                                                            .filter {stecker4 -> stecker4 > stecker3}
                                                            .forEach { stecker4 ->
                                                                val enigma = Enigma(
                                                                    Reflector(ReflectorType.SHOWCASE_REF),
                                                                    Rotor(leftRotorType, leftStart, 'A'),
                                                                    Rotor(middleRotorType, middleStart, 'A'),
                                                                    Rotor(rightRotorType, rightStart, 'A'),
                                                                    Plugboard("$stecker1$stecker2-$stecker3$stecker4", 6)
                                                                )
                                                                val encypheredMsg = enigma.encryptMessage("CAFE")
                                                                //if (encypheredMsg.count { it == 'F' } == 2 && encypheredMsg.first() == 'F' && encypheredMsg.last() == 'F' && encypheredMsg[1] != 'F' && (encypheredMsg[2] == 'B' || encypheredMsg[2] == 'D') && encypheredMsg[1] != encypheredMsg[2]) {
                                                                if (encypheredMsg == "ECEA") { // variant 8 [SHOWCASE_II-C|SHOWCASE_III-B|SHOWCASE_I-A|AB-CE]: ECEA
//                                                                if (encypheredMsg == "FCEC") {
                                                                    variant++
                                                                    println("variant $variant [${enigma.leftRotor!!.getRotorType()}-${enigma.leftRotor!!.startRingOrientation()}|${enigma.middleRotor!!.getRotorType()}-${enigma.middleRotor!!.startRingOrientation()}|${enigma.rightRotor!!.getRotorType()}-${enigma.rightRotor!!.startRingOrientation()}|${enigma.plugboard.connectedLetters}]: $encypheredMsg")
                                                                }
                                                            }
                                                    }
                                        }
                                    }
                                }
                            }
                    }
                }
        }
    }
}

class ShowcaseGenerator {


}