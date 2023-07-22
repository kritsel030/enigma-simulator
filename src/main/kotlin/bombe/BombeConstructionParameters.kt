package bombe

import enigma.components.ReflectorType
import java.lang.IllegalStateException

class BombeConstructionParameters(
    // physical construction parameters

    // the german enigma had a fixed alphabet consisting of 26 letters,
    // for demonstration purposes our bombe emulator can also be configured to support a smaller alphabet size
    // (e.g. 8 letters, from A to H)
    val alphabetSize : Int,

    // number of banks of scrambler in the bombe
    val noOfBanks : Int,

    // number of scramblers (a scrambler consists of 3 or 4 drums + reflector) per bank
    val noOfScramblersPerBank : Int,

    // usually 3, but can also be 4
    val noOfRotorsPerScrambler : Int,

    val noOfCommonsSetsPerBank: Int
) {
    companion object {
        fun getBombeConstructionParameters(bombeTemplate: BombeTemplate) : BombeConstructionParameters {
            when(bombeTemplate) {
                BombeTemplate.ATLANTA -> return BombeConstructionParameters(26, 3, 12, 3, 4)
                BombeTemplate.MAGIC -> throw IllegalStateException("BombeTemplate.MAGIC means that the bombe construction parameters are based on the instructions and cannot be hardcoded")
            }
        }
    }
}