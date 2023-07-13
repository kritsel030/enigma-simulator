package bombe

import enigma.components.RotorType
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import java.lang.Math.abs
import kotlin.test.*

class BombeTest {

    @Test
    fun constructor() {
        val alphabetSize = 12
        val noOfBanks = 5
        val noOfScramblersPerBank = 7
        val noOfRotorsPerScrambler = 3
        val bombe = Bombe(alphabetSize, noOfBanks, noOfScramblersPerBank, noOfRotorsPerScrambler)

        assertResetResult(bombe)
        assertNoCurrent(bombe)
    }

    @Test
    fun claimAvailableCommonsSet_ok() {
        val bombe = Bombe(3, 1, 1, 3)
        val commonsSet = bombe.claimAvailableCommonsSet(1, 'X')
        assertNotNull(commonsSet)
    }

    @Test
    fun claimAvailableCommonsSet_identical() {
        val bombe = Bombe(3, 1, 1, 3)
        bombe.claimAvailableCommonsSet(1, 'X')
        // not allowed to have 2 or more CommonsSets in the same column for the same letter
        // (this is not a feature of an actual bombe machine, it is a limitation of this specific bombe implementation)
        assertFailsWith<IllegalStateException> { bombe.claimAvailableCommonsSet(1, 'X') }
    }

    @Test
    fun claimAvailableCommonsSet_tooMany() {
        val bombe = Bombe(3, 1,1, 3 )
        val noOfCommonsSetsInColumn = bombe.commonsSetsColumns[1]!!.size
        val letter = 'A'
        for (i in 1 .. noOfCommonsSetsInColumn) {
            // this should be safe
            bombe.claimAvailableCommonsSet(1, letter.plus(i))
        }
        // but the next one should fail
        assertFailsWith<IllegalStateException> { bombe.claimAvailableCommonsSet(1, 'Z') }
    }

    @Test
    fun createCable() {
        val bombe = Bombe(3, 1, 1, 3)
        val cable = bombe.createCable()
        assertNotNull(cable)
        assertEquals(1, bombe.cables.size)
    }

    @Test
    fun createBridge() {
        val bombe = Bombe(3, 1, 1, 3)
        val bridge = bombe.createBridge()
        assertNotNull(bridge)
        assertEquals(1, bombe.bridges.size)
    }

    @Test
    fun rotateDrums() {
        // prepare a bombe with scramblers
        val bombe = Bombe(26, 1, 1, 3)
        bombe.banks.values.forEach { bank ->
            run {
                bank.placeDrums(listOf(RotorType.V, RotorType.I, RotorType.III))
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
            bombe.rotateDrums()
        }
        // check
        bombe.banks.values.forEach { bank ->
            run {
                bank.getScramblers().forEach { scrambler ->
                    run {
                        // in a bombe, the drum which represents the left (and slow moving) rotor in the enigma, is the drum which is rotating the fastest
                        assertEquals(fastDrumSteps, (bombe.alphabetSize + scrambler.enigma!!.getRotor(1).currentPosition.code - scrambler.enigma!!.getRotor(1).startPosition.code ) % bombe.alphabetSize, "fast drum should have a net advance of $fastDrumSteps")
                        assertEquals(middleDrumSteps, (bombe.alphabetSize + scrambler.enigma!!.getRotor(2).currentPosition.code - scrambler.enigma!!.getRotor(2).startPosition.code ) % bombe.alphabetSize, "middle drum should have a net advance of $middleDrumSteps")
                        assertEquals(slowDrumSteps, (bombe.alphabetSize + scrambler.enigma!!.getRotor(3).currentPosition.code - scrambler.enigma!!.getRotor(3).startPosition.code) % bombe.alphabetSize, "slow drum should have a a net advance of $slowDrumSteps")
                    }
                }
            }
        }
    }

    private fun assertResetResult(bombe: Bombe) {
        // banks
        val actualNoOfBanks = bombe.banks.size
        assertEquals(bombe.noOfBanks, actualNoOfBanks, "expected $bombe.noOfBanks banks, got $actualNoOfBanks")

        // scramblers
        val actualNoOfScramblersPerBank = bombe.banks.values.first().getScramblers().size
        assertEquals(bombe.noOfScramblersPerBank, actualNoOfScramblersPerBank, "expected $bombe.noOfScramblersPerBank scramblers per bank, got $actualNoOfScramblersPerBank")
        bombe.banks.values.forEach { bank ->
            run{
                bank.getScramblers().forEach { scrambler ->
                    run {
                        assertNull(scrambler.enigma, "expect no enigma drums to be set on the scramblers")
                    }
                }
            }
        }

        // diagonal boards
        val actualNoOfDiagonalBoards = bombe.diagonalBoards.size
        assertEquals(bombe.noOfBanks, actualNoOfDiagonalBoards,  "expected $bombe.noOfBanks diagonal boards, got $actualNoOfDiagonalBoards")

        // commons columns
        val actualNoOfCommonsColumns = bombe.commonsSetsColumns.size
        assertEquals(bombe.noOfBanks, actualNoOfCommonsColumns,  "expected $bombe.noOfBanks commons columns, got $actualNoOfCommonsColumns")
        bombe.commonsSetsColumns.values.forEach{ commonsColumn ->
            run {
                assertTrue(commonsColumn.size > 1, "expected several CommonsSets in every commons column")
                commonsColumn.forEach { commonsSet ->
                    run{
                        assertTrue(commonsSet.jacks().size > 2, "expected several jacks for each CommonsSet")
                    }
                }
            }
        }

        // bridges
        assertEquals(0, bombe.bridges.size, "expected an empty list of bridges")

        // cables
        assertEquals(0, bombe.cables.size, "expected an empty list of cables")

        // indicator drums
        bombe.indicatorDrums.forEach { drum ->
            assertEquals('A'.plus(bombe.alphabetSize-1), drum.position, "expect each drum to be in the 'last letter of alphabet' position")
        }

        // drumRotations
        assertEquals(0, bombe.drumRotations, "expect drumrotations to be 0")
    }

    private fun assertNoCurrent(bombe: Bombe) {
        // banks
         bombe.banks.values.forEach { bank ->
            run{
                assertEquals(0,bank.inputJack.readActiveContacts().size, "expect each bank's inputJack to have no active contacts")
            }
        }

        // scramblers
        bombe.banks.values.forEach { bank ->
            run{
                bank.getScramblers().forEach { scrambler ->
                    run {
                        assertEquals(0,scrambler.inputJack.readActiveContacts().size, "expect each scrambler's inputJack to have no active contacts")
                        assertEquals(0,scrambler.outputJack.readActiveContacts().size, "expect each bank's inputJack to have no active contacts")
                    }
                }
            }
        }

        // diagonal boards
        bombe.diagonalBoards.values.forEach { db ->
            run{
                db.connectors.forEach { connector ->
                    run {
                        assertEquals(0,connector.readActiveContacts().size, "expect all diagonal board's jacks to have no active contacts")
                    }
                }
            }
        }
        // commons columns
        bombe.commonsSetsColumns.values.forEach{ commonsColumn ->
            run {
                commonsColumn.forEach { commonsSet ->
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
    }
}