package bombe.components

import bombe.Bombe
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ScramblerTest {

    @Test
    fun placeDrums_ok() {
        val bombe = Bombe(26, 1, 1, 3)
        val scrambler = bombe.getBank(1).getScrambler(1)
        scrambler.placeDrums(listOf(DrumType.II, DrumType.III, DrumType.I))
        scrambler.setDrumStartOrientations("DKX")
        assertEquals(scrambler.internalScrambler.leftRotor!!.getRotorType(), DrumType.II.rotorType)
        assertEquals(scrambler.internalScrambler.middleRotor!!.getRotorType(), DrumType.III.rotorType)
        assertEquals(scrambler.internalScrambler.rightRotor!!.getRotorType(), DrumType.I.rotorType)
    }

    @Test
    fun setDrumStartOrientations() {
        val bombe = Bombe(26, 1, 1, 3)
        val scrambler = bombe.getBank(1).getScrambler(1)
        scrambler.placeDrums(listOf(DrumType.II, DrumType.III, DrumType.I))
        scrambler.setDrumStartOrientations("DKX")
        assertEquals('D', scrambler.internalScrambler.leftRotor!!.startRingOrientation())
        assertEquals('K', scrambler.internalScrambler.middleRotor!!.startRingOrientation())
        assertEquals('X', scrambler.internalScrambler.rightRotor!!.startRingOrientation())
    }

    @Test
    fun passCurrent_viaInputJack() {
        val scrambler = Scrambler(1, 3, Bank(1, 12, 3, Bombe(26, 1, 12, 3)))
        scrambler.placeDrums(listOf(DrumType.II, DrumType.V, DrumType.III))

        val input = 'A'
        val enigmaOutput = scrambler.scramble(input)
        scrambler.inputJack.passCurrentInbound(input)
        val activeOutputContacts = scrambler.outputJack.readActiveContacts()


        assertEquals(1, activeOutputContacts.size, "expected 1 active contact on the output jack")
        assertEquals(enigmaOutput, activeOutputContacts[0], "expected the enigma output ($enigmaOutput) to be identical to the scrambler output (${activeOutputContacts[0]})")
    }

    @Test
    fun passCurrent_viaOutputJack() {
        val scrambler = Scrambler(1, 3, Bank(1, 12, 3, Bombe(26, 1, 12, 3)))
        scrambler.placeDrums(listOf(DrumType.II, DrumType.V, DrumType.III))

        val input = 'A'
        val enigmaOutput = scrambler.scramble(input)
        scrambler.outputJack.passCurrentInbound(input)
        val activeOutputContacts = scrambler.inputJack.readActiveContacts()


        assertEquals(1, activeOutputContacts.size, "expected 1 active contact on the input jack")
        assertEquals(enigmaOutput, activeOutputContacts[0], "expected the enigma output ($enigmaOutput) to be identical to the scrambler output (${activeOutputContacts[0]})")
    }
}