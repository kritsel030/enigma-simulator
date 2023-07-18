package bombe.operators

import bombe.BombeRunInstructions
import bombe.components.DrumType
import enigma.components.ReflectorType
import shared.RotorType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpertBombeOperatorTest {

    @Test
    // Implements the example described here:
    // https://www.lysator.liu.se/~koma/turingbombe/
    // https://www.lysator.liu.se/~koma/turingbombe/TuringBombeTutorial.pdf
    //
    // original message:   WETTERVORHERSAGE
    // encyphered message: SNMKGGSTZZUGARLV
    // The PDF describes the menu derived from this example, that menu is used as input for the test
    //
    // The original message can be encrypted into the enciphered message with these enigma settings:
    // (of course other settings are possible as well, as equal differences in both rotor position and rotor ring setting
    //  cancel each other out when you ignore rotor turnover)
    // - reflector: B
    // - rotor 1 (left): II-Y-4 (type, rotor position, ring setting)
    // - rotor 2 (middle): V-W-11
    // - rotor 3 (right): III-Y-24
    // - plugboard: UF-ET-GQ-AD-VN-HM-ZP-LJ-IK-XO
    //
    // This test depends on Scrambler.fixRotorStartPositionMap because we need to accommodate for this mistake:
    // (quoted from page 2 of the PDF)
    // "Probably by mistake, drums I, II, III, VI, VII and VIII on the Bombe
    //   are one letter ahead of the corresponding Enigma rotors.
    //  Drum IV is two steps ahead, and rotor V is three steps ahead."
    fun linkopingBombeSimulatorExample() {
        val instructions = BombeRunInstructions(
            // the menu
            listOf("U-11-E-5-G-6-R-14-A-13-S-7-V-16-E-2-N", "H-10-Z-9-R-12-G-15-L"),
            'G',
            // left, middle, right rotor
            listOf(listOf(DrumType.II, DrumType.V, DrumType.III)),
            ReflectorType.B
        )
        val operator = AutomatedBombeOperator()
        val stops = operator.executeRun(instructions)

        assertTrue(stops.size > 1)
        assertEquals("D", stops[0].possibleSteckerPartnersForCentralLetter.joinToString(""))
        assertEquals(
            "SNY",
            listOf(stops[0].rotor1RingStellung, stops[0].rotor2RingStellung, stops[0].rotor3RingStellung).joinToString("")
        )

        assertEquals("Q", stops[1].possibleSteckerPartnersForCentralLetter.joinToString(""))
        assertEquals(
            "DKX",
            listOf(stops[1].rotor1RingStellung, stops[1].rotor2RingStellung, stops[1].rotor3RingStellung).joinToString("")
        )
    }
}