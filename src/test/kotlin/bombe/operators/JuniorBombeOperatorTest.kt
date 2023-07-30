package bombe.operators

import bombe.Bombe
import bombe.components.DrumType
import bombe.components.DummyComponent
import bombe.connectors.CablePlug
import bombe.connectors.Jack
import enigma.components.ReflectorType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JuniorBombeOperatorTest {

    @Test
    fun createAndConnectCable() {
        val bombe = Bombe(3, 1, 1, 1, 1, 1,ReflectorType.B)
        val operator = JuniorBombeOperator(bombe)

        val component1 = DummyComponent(bombe)
        val jack1 = Jack("label", "label", component1)
        val component2 = DummyComponent(bombe)
        val jack2 = Jack("label", "label", component2)

        val cable = operator.drawCableBetween(jack1, jack2)
        assertNotNull(cable)
        assertEquals(jack2, (jack1.pluggedUpBy() as CablePlug).getOppositePlug().pluggedInto())
    }

    @Test
    fun createAndConnectBridge() {
        val bombe = Bombe(3, 1, 1, 2, 1, 1, ReflectorType.B)
        val operator = JuniorBombeOperator(bombe)


        val scrambler1 = bombe.getScramblerJackPanel(1)
        val scrambler2 = bombe.getScramblerJackPanel(2)

        val bridge = operator.attachBridgeTo(scrambler1!!.getOutputJack(), scrambler2!!.getInputJack())
        assertNotNull(bridge)
        assertNotNull(scrambler1.getOutputJack().pluggedUpBy())
        assertNotNull(scrambler2.getInputJack().pluggedUpBy())
    }

    @Test
    fun findFreeCommonsSets_1() {
        val bombe = Bombe()
        val operator = JuniorBombeOperator(bombe)

        val freeCommonsSet = operator.findFreeCommonsSet()
        assertNotNull(freeCommonsSet)
    }

    @Test
    fun findFreeCommonsSets_2() {
        val bombe = Bombe()
        val operator = JuniorBombeOperator(bombe)

        val freeCommonsSetA = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1, 'A'), freeCommonsSetA)

        val freeCommonsSetB = operator.findFreeCommonsSet()
        assertNotEquals(freeCommonsSetA, freeCommonsSetB)
    }

    @Test
    fun registerCommonsSet() {
        val bombe = Bombe()
        val operator = JuniorBombeOperator(bombe)

        val freeCommonsSetA = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1, 'A'), freeCommonsSetA)
        val foundCommonsSet = operator.commonsSetRegister.get(Pair(1, 'A'))

        assertNotNull(foundCommonsSet)
    }

    /**
     *     ZZA     ZZB     ZZC     ZZD
     *      1       2       3       4
     *  A ----- B ----- C ----- A ----- B
     *
     *  input on A, wire X
     */
    @Test
    fun simpleMenu() {
        val bombe = Bombe()
        val operator = JuniorBombeOperator(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.I, DrumType.II, DrumType.III)
        for (scramblerId in 1..4) {
            operator.placeDrums(scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, "ZZA")
        operator.setStartRingOrientations(2, "ZZB")
        operator.setStartRingOrientations(3, "ZZC")
        operator.setStartRingOrientations(4, "ZZD")

        // back side - place bridges
        val bridge_12 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(1)!!.getOutputJack(),
            bombe.getScramblerJackPanel(2)!!.getInputJack()
        )
        val bridge_23 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(2)!!.getOutputJack(),
            bombe.getScramblerJackPanel(3)!!.getInputJack()
        )
        val bridge_34 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(3)!!.getOutputJack(),
            bombe.getScramblerJackPanel(4)!!.getInputJack()
        )

        // back side - draw cables for 'A'
        val commons_A = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'A'), commons_A)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('A'), commons_A.getAvailableJack())
        // scrambler 1 input to commons
        operator.drawCableBetween(bombe.getScrambler(1)!!.getInputJack(), commons_A.getAvailableJack())
        // bridge_34 to commons
        operator.drawCableBetween(bridge_34.jack, commons_A.getAvailableJack())
        // input chain 1 to commons
        operator.drawCableBetween(bombe.getChainJackPanel(1)!!.getInputJack(), commons_A.getAvailableJack())

        // back side - draw cables for 'B'
        // bridge_12 to diagonal board
        operator.drawCableBetween(bridge_12.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('B'))

        // back side - draw cables for 'C'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_23.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('C'))

        // switch on the bank
        // TODO: add a unittest for an off bank
        bombe.getChainControlPanel(1)!!.switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChainControlPanel(1)!!.setContactToActivate('X')

        // run the bombe only once
        val stops = operator.runJob(1)
        assertTrue(stops.size > 0, "expected stops")
        assertTrue(stops[0].possibleSearchLetters.size > 0, "expected multiple possible stecker partners")
    }

    // US 6812 Bombe Report 1944 - chapter 3
    // test menu I
    // https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
    // http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_I.txt
    //
    // See ExpertBombeOperatorTest for the other test menus
    @Test
    fun usBombeReport1944_testMenu_I() {
        val bombe = Bombe()
        val operator = JuniorBombeOperator(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.III, DrumType.II, DrumType.IV)
        for (scramblerId in 1..7) {
            operator.placeDrums(scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, "ZZS")
        operator.setStartRingOrientations(2, "ZZZ")
        operator.setStartRingOrientations(3, "ZAX")
        operator.setStartRingOrientations(4, "ZAS")
        operator.setStartRingOrientations(5, "ZAY")
        operator.setStartRingOrientations(6, "ZZW")
        operator.setStartRingOrientations(7, "ZAV")

        // back side - place bridges
        val bridge_12 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(1)!!.getOutputJack(),
            bombe.getScramblerJackPanel(2)!!.getInputJack()
        )
        val bridge_23 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(2)!!.getOutputJack(),
            bombe.getScramblerJackPanel(3)!!.getInputJack()
        )
        val bridge_34 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(3)!!.getOutputJack(),
            bombe.getScramblerJackPanel(4)!!.getInputJack()
        )
        val bridge_45 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(4)!!.getOutputJack(),
            bombe.getScramblerJackPanel(5)!!.getInputJack()
        )
        val bridge_56 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(5)!!.getOutputJack(),
            bombe.getScramblerJackPanel(6)!!.getInputJack()
        )
        val bridge_67 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(6)!!.getOutputJack(),
            bombe.getScramblerJackPanel(7)!!.getInputJack()
        )

        // back side - draw cables for 'U'
        val commons_U = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'U'), commons_U)
        // diagonal board to commons_U
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('U'), commons_U.getAvailableJack())
        // scrambler 1 input to commons_U
        operator.drawCableBetween(bombe.getScramblerJackPanel(1)!!.getInputJack(), commons_U.getAvailableJack())
        // bridge_23 to commons_U
        operator.drawCableBetween(bridge_23.jack, commons_U.getAvailableJack())
        // bridge_56 to commons_U
        operator.drawCableBetween(bridge_56.jack, commons_U.getAvailableJack())

        // back side - draw cables for 'N'
        val commons_N = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'N'), commons_N)
        // diagonal board to commons_N
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('N'), commons_N.getAvailableJack())
        // bridge_12 to commons_N
        operator.drawCableBetween(bridge_12.jack, commons_N.getAvailableJack())
        // bridge_34 to commons_N
        operator.drawCableBetween(bridge_34.jack, commons_N.getAvailableJack())
        // scrambler 7 output to commons_N
        operator.drawCableBetween(bombe.getScramblerJackPanel(7)!!.getOutputJack(), commons_N.getAvailableJack())

        val commons_E = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'E'), commons_E)
        // chain input to common_E
        operator.drawCableBetween(bombe.getChainJackPanel(1)!!.getInputJack(), commons_E.getAvailableJack())
        // diagonal board to commons_E
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('E'), commons_E.getAvailableJack())
        // bridge_45 to commons_E
        operator.drawCableBetween(bridge_45.jack, commons_E.getAvailableJack())
        // bridge_67 to commons_E
        operator.drawCableBetween(bridge_67.jack, commons_E.getAvailableJack())

        // switch on the bank
        bombe.getChainControlPanel(1)!!.switchOn()

        // put the current on the correct wire
        bombe.getChainControlPanel(1)!!.setContactToActivate('A')

        // run the job
        val stops = operator.runJob()
//        println("test menu I stops (${stops.size}):")
//        stops.forEach { it.print() }

        assertEquals(1, stops.size, "expected 1 stop")
        assertEquals('B', stops.get(0).rotor1RingStellung)
        assertEquals('U', stops.get(0).rotor2RingStellung)
        assertEquals('O', stops.get(0).rotor3RingStellung)
        assertEquals("L", stops.get(0).getPossibleSearchLettersString())
    }
}