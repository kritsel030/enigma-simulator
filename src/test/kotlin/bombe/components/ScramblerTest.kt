package bombe.components

import bombe.Bank
import bombe.Bombe
import shared.RotorType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ScramblerTest {

    @Test
    fun passCurrent_viaInputJack() {
        val scrambler = Scrambler(1, 3, Bank(1, 12, 3, Bombe(26, 1, 12, 3)))
        scrambler.placeRotors(listOf(RotorType.II, RotorType.V, RotorType.III))

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
        scrambler.placeRotors(listOf(RotorType.II, RotorType.V, RotorType.III))

        val input = 'A'
        val enigmaOutput = scrambler.scramble(input)
        scrambler.outputJack.passCurrentInbound(input)
        val activeOutputContacts = scrambler.inputJack.readActiveContacts()


        assertEquals(1, activeOutputContacts.size, "expected 1 active contact on the input jack")
        assertEquals(enigmaOutput, activeOutputContacts[0], "expected the enigma output ($enigmaOutput) to be identical to the scrambler output (${activeOutputContacts[0]})")
    }
}