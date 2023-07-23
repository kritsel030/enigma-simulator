package bombe

import bombe.components.DrumType
import enigma.components.ReflectorType
import org.junit.jupiter.api.Test
import kotlin.test.*

class BombeTest {

    @Test
    fun constructor() {
        val alphabetSize = 12
        val noOfChains = 3
        val noOfBanks = 5
        val noOfScramblersPerBank = 7
        val noOfRotorsPerScrambler = 3
        val noOfCommonsSetsPerBank = 5
        val reflectorType = ReflectorType.B
        val bombe = Bombe(alphabetSize, noOfChains, noOfBanks, noOfScramblersPerBank, noOfRotorsPerScrambler, noOfCommonsSetsPerBank, reflectorType)

        assertResetResult(bombe)
        assertNoCurrent(bombe)
    }

    @Test
    fun createCable() {
        val bombe = Bombe(3, 1, 1, 1, 3, 1, ReflectorType.B)
        val cable = bombe.createCable()
        assertNotNull(cable)
        assertEquals(1, bombe.getCables().size)
    }

    @Test
    fun createBridge() {
        val bombe = Bombe(3, 1, 1,  1, 3, 1, ReflectorType.B)
        val bridge = bombe.createBridge()
        assertNotNull(bridge)
        assertEquals(1, bombe.getBridges().size)
    }

    @Test
    fun rotateDrums() {
        // prepare a bombe with scramblers
        val bombe = Bombe(26, 1, 1, 1, 3, 1, ReflectorType.B)
        bombe.getScramblers().forEach {
            run {
                it.placeDrums(listOf(DrumType.V, DrumType.I, DrumType.III))
            }
        }

        // we need to calculate a number of steps which produces a different
        // net position advancement for each of the three drums/rotors
        val fastDrumSteps = 7
        val middleDrumSteps = 5
        val slowDrumSteps  = 3
        val steps = slowDrumSteps * bombe.alphabetSize * bombe.alphabetSize +
                middleDrumSteps * bombe.alphabetSize +
                fastDrumSteps
        for (i in 1 .. steps) {
            bombe.stepDrums()
        }
        // check
        bombe.getScramblers().forEach { it ->
            run {
                // in a bombe, the drum which represents the left (and slow moving) rotor in the enigma, is the drum which is rotating the fastest
                assertEquals(fastDrumSteps, it.enigma!!.leftRotor!!.getNetSteps(), "fast drum should have a net advance of $fastDrumSteps")
                assertEquals(middleDrumSteps, it.enigma!!.middleRotor!!.getNetSteps(), "middle drum should have a net advance of $middleDrumSteps")
                assertEquals(slowDrumSteps, it.enigma!!.rightRotor!!.getNetSteps(), "slow drum should have a a net advance of $slowDrumSteps")
            }
        }
    }

    private fun assertResetResult(bombe: Bombe) {
        // scramblers
        assertEquals(bombe.noOfBanks * bombe.noOfScramblersPerBank, bombe.getScramblers().size, "number of scramblers in the bombe")

        bombe.getScramblers().forEach { scrambler ->
            run {
                assertNotNull(scrambler.enigma,"expect an internal scrambler - without rotors - to be present")
                assertEquals(bombe.noOfRotorsPerScrambler, scrambler.enigma.rotorPositions)
            }
        }

        // commons sets
        assertEquals(bombe.noOfBanks * bombe.noOfCommonsSetsPerBank, bombe.getCommonsSets().size, "number of commonsSets in the bombe")

        // diagonal boards
        val actualNoOfDiagonalBoards = bombe.getDiagonalBoardJackPanels().size
        assertEquals(bombe.noOfBanks, actualNoOfDiagonalBoards,  "expected $bombe.noOfBanks diagonal boards, got $actualNoOfDiagonalBoards")

        // commons columns
        bombe.getCommonsSets().forEach { commonsSet ->
            run{
                assertTrue(commonsSet.jacks().size > 2, "expected several jacks for each CommonsSet")
            }
        }

        // bridges
        assertEquals(0, bombe.getBridges().size, "expected an empty list of bridges")

        // cables
        assertEquals(0, bombe.getCables().size, "expected an empty list of cables")

        // indicator drums
        bombe.indicatorDrums.forEach { drum ->
            assertEquals('A'.plus(bombe.alphabetSize-1), drum.position, "expect each drum to be in the 'last letter of alphabet' position")
        }

        // drumRotations
        assertEquals(0, bombe.drumRotations, "expect drumrotations to be 0")
    }

    private fun assertNoCurrent(bombe: Bombe) {
        // chains
         bombe.getChainJackPanels().forEach { chain ->
            run{
                assertEquals(0,chain.getInputJack().readActiveContacts().size, "expect each chain's inputJack to have no active contacts")
            }
        }

        // scramblers
        bombe.getScramblers().forEach { scrambler ->
            run {
                assertEquals(0,scrambler._inputJack.readActiveContacts().size, "expect each scrambler's inputJack to have no active contacts")
                assertEquals(0,scrambler._outputJack.readActiveContacts().size, "expect each bank's inputJack to have no active contacts")
            }
        }

        // diagonal boards
        bombe.getDiagonalBoardJackPanels().forEach { db ->
            run{
                db.getJacks().forEach { connector ->
                    run {
                        assertEquals(0,connector.readActiveContacts().size, "expect all diagonal board's jacks to have no active contacts")
                    }
                }
            }
        }

        // commons columns
        bombe.getCommonsSets().forEach { commonsSet ->
            run{
                commonsSet.connectors.forEach { connector ->
                    run {
                        assertEquals(0,connector.readActiveContacts().size, "expect all commonsSets' jacks to have no active contacts")
                    }
                }
            }

        }
    }
}