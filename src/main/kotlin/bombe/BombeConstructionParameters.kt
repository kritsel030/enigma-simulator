package bombe

import java.lang.IllegalStateException

class BombeConstructionParameters(
    // physical construction parameters

    // the german enigma had a fixed alphabet consisting of 26 letters,
    // for demonstration purposes our bombe emulator can also be configured to support a smaller alphabet size
    // (e.g. 8 letters, from A to H)
    val alphabetSize : Int = 1,

    // number of banks of scrambler in the bombe
    val noOfBanks : Int = 1,

    // number of scramblers (a scrambler consists of 3 or 4 drums + reflector) per bank
    val noOfScramblersPerBank : Int = 1,

    // not in use yet
    val noOfRotorsPerScrambler : Int = 1
) {
    companion object {
        fun getBombeConstructionParameters(bombeTemplate: BombeTemplate) : BombeConstructionParameters {
            when(bombeTemplate) {
                BombeTemplate.ATLANTA -> return BombeConstructionParameters(26, 3, 12, 3)
                BombeTemplate.MAGIC -> throw IllegalStateException("cannot do it")
            }
        }
    }
}