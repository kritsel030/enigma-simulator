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
        val bombe = Bombe(3, 1, 1, 1, 1,ReflectorType.B)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

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
        val bombe = Bombe(3, 1, 2, 1, 1, ReflectorType.B)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

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
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        val freeCommonsSet = operator.findFreeCommonsSet()
        assertNotNull(freeCommonsSet)
    }

    @Test
    fun findFreeCommonsSets_2() {
        val bombe = Bombe()
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        val freeCommonsSetA = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1, 'A'), freeCommonsSetA)

        val freeCommonsSetB = operator.findFreeCommonsSet()
        assertNotEquals(freeCommonsSetA, freeCommonsSetB)
    }

    @Test
    fun registerCommonsSet() {
        val bombe = Bombe()
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

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
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

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

        // run the job
        val stops = bombe.run(1)
        assertTrue(stops.size > 0, "expected stops")
        if (stops.size > 0) {
            stops[0].print()
        }
        assertTrue(stops[0].possibleSteckerPartnersForCentralLetter.size > 0, "expected multiple possible stecker partners")
    }

    // US 6812 Bombe Report 1944 - chapter 3
    // test menu I
    // https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
    // http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_I.txt
    @Test
    fun usBombeReport1944_testMenu_I() {
        val bombe = Bombe()
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

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
        // bank input to common_E
        operator.drawCableBetween(bombe.getChainJackPanel(1)!!.getInputJack(), commons_E.getAvailableJack())
        // diagonal board to commons_E
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('E'), commons_E.getAvailableJack())
        // bridge_45 to commons_E
        operator.drawCableBetween(bridge_45.jack, commons_E.getAvailableJack())
        // bridge_67 to commons_E
        operator.drawCableBetween(bridge_67.jack, commons_E.getAvailableJack())

        // switch on the bank
        // TODO: add a unittest for an off bank
        bombe.getChainControlPanel(1)!!.switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChainControlPanel(1)!!.setContactToActivate('A')

        // run the job
        val stops = bombe.run()
        println("test menu I stops (${stops.size}):")
        stops.forEach { it.print() }

        assertEquals(1, stops.size, "expected 1 stop")
        assertEquals('B', stops.get(0).rotor1RingStellung)
        assertEquals('U', stops.get(0).rotor2RingStellung)
        assertEquals('O', stops.get(0).rotor3RingStellung)
        assertEquals(listOf('L'), stops.get(0).possibleSteckerPartnersForCentralLetter)
    }

    // US 6812 Bombe Report 1944 - chapter 3
    // test menu II
    // https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
    // http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_II.txt
    @Test
    fun usBombeReport1944_testMenu_II() {
        val bombe = Bombe()
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.IV, DrumType.III, DrumType.II)
        for (scramblerId in 1..10) {
            operator.placeDrums(scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, "EKR")
        operator.setStartRingOrientations(2, "RTN")
        operator.setStartRingOrientations(3, "SAO")
        operator.setStartRingOrientations(4, "EKP")
        operator.setStartRingOrientations(5, "RTI")
        operator.setStartRingOrientations(6, "RTQ")
        operator.setStartRingOrientations(7, "SAT")
        operator.setStartRingOrientations(8, "SAS")
        operator.setStartRingOrientations(9, "RTR")
        operator.setStartRingOrientations(10, "RTJ")

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
        val bridge_78 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(7)!!.getOutputJack(),
            bombe.getScramblerJackPanel(8)!!.getInputJack()
        )
        // no bridge between 8 and 9
        val bridge_910 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(9)!!.getOutputJack(),
            bombe.getScramblerJackPanel(10)!!.getInputJack()
        )

        // back side - draw cables for 'U'
        val commons_U = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'U'), commons_U)
        // diagonal board to commons_U
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('U'), commons_U.getAvailableJack())
        // scrambler 1 input to commons_U
        operator.drawCableBetween(bombe.getScramblerJackPanel(1)!!.getInputJack(), commons_U.getAvailableJack())
        // scrambler 9 input to commons_U
        operator.drawCableBetween(bombe.getScramblerJackPanel(9)!!.getInputJack(), commons_U.getAvailableJack())

        // back side - draw cables for 'E'
        val commons_E = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'E'), commons_E)
        // diagonal board to commons_E
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('E'), commons_E.getAvailableJack())
        // bridge_12 to commons_E
        operator.drawCableBetween(bridge_12.jack, commons_E.getAvailableJack())
        // bridge_910 to commons_E
        operator.drawCableBetween(bridge_910.jack, commons_E.getAvailableJack())

        // back side - draw cables for 'Y'
        val commons_Y = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'Y'), commons_Y)
        // diagonal board to commons_Y
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('Y'), commons_Y.getAvailableJack())
        // bridge_23 to commons_Y
        operator.drawCableBetween(bridge_23.jack, commons_Y.getAvailableJack())

        // back side - draw cables for 'H'
        val commons_H = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'H'), commons_H)
        // bank input to common_H
        operator.drawCableBetween(bombe.getChainJackPanel(1)!!.getInputJack(), commons_H.getAvailableJack())
        // diagonal board to commons_H
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('H'), commons_H.getAvailableJack())
        // bridge_34 to commons_H
        operator.drawCableBetween(bridge_34.jack, commons_H.getAvailableJack())
        // bridge_67 to commons_H
        operator.drawCableBetween(bridge_67.jack, commons_H.getAvailableJack())
        // scrambler 8 output to commons_H
        operator.drawCableBetween(bombe.getScramblerJackPanel(8)!!.getOutputJack(), commons_H.getAvailableJack())

        // back side - draw cables for 'F'
        val commons_F = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'F'), commons_F)
        // diagonal board to commons_F
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('F'), commons_F.getAvailableJack())
        // bridge_45 to commons_F
        operator.drawCableBetween(bridge_45.jack, commons_F.getAvailableJack())

        // back side - draw cables for 'B'
        val commons_B = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'B'), commons_B)
        // diagonal board to commons_B
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('B'), commons_B.getAvailableJack())
        // bridge_56 to commons_B
        operator.drawCableBetween(bridge_56.jack, commons_B.getAvailableJack())

        // back side - draw cables for 'T'
        val commons_T = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'T'), commons_T)
        // diagonal board to commons_T
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('T'), commons_T.getAvailableJack())
        // bridge_78 to commons_T
        operator.drawCableBetween(bridge_78.jack, commons_T.getAvailableJack())

        // back side - draw cables for 'X'
        // diagonal board to scrambler 10 out
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('X'), bombe.getScramblerJackPanel(10)!!.getOutputJack())

        // switch on the bank
        // TODO: add a unittest for an off bank
        bombe.getChainControlPanel(1)!!.switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChainControlPanel(1)!!.setContactToActivate('A')

        // run the job
        val stops = bombe.run()
        println("test menu II stops (${stops.size}):")
        stops.forEach { it.print() }

        assertEquals(4, stops.size, "expected 4 stops")
        assertEquals('F', stops.get(0).rotor1RingStellung)
        assertEquals('J', stops.get(0).rotor2RingStellung)
        assertEquals('W', stops.get(0).rotor3RingStellung)
        assertEquals(listOf('F'), stops.get(0).possibleSteckerPartnersForCentralLetter)
    }

    // US 6812 Bombe Report 1944 - chapter 3
    // test menu III
    // https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
    // http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_III.txt
    @Test
    fun usBombeReport1944_testMenu_III() {
        val bombe = Bombe()
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.I, DrumType.V, DrumType.III)
        for (scramblerId in 1..10) {
            operator.placeDrums(scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, "ZAP")
        operator.setStartRingOrientations(2, "ZZJ")
        operator.setStartRingOrientations(3, "ZAJ")
        operator.setStartRingOrientations(4, "ZZP")
        operator.setStartRingOrientations(5, "ZZN")
        operator.setStartRingOrientations(6, "ZAN")
        operator.setStartRingOrientations(7, "ZZK")
        operator.setStartRingOrientations(8, "ZAQ") // initially read 'ZAG', corrected based on http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_Va.txt
        operator.setStartRingOrientations(9, "ZZO")
        operator.setStartRingOrientations(10, "ZZR")

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
        val bridge_78 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(7)!!.getOutputJack(),
            bombe.getScramblerJackPanel(8)!!.getInputJack()
        )
        val bridge_89 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(8)!!.getOutputJack(),
            bombe.getScramblerJackPanel(9)!!.getInputJack()
        )
        // no bridge between 9 and 10

        // back side - draw cables for 'T'
        val commons_T = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'T'), commons_T)
        // scrambler 1 input to commons_T
        operator.drawCableBetween(bombe.getScramblerJackPanel(1)!!.getInputJack(), commons_T.getAvailableJack())

        // back side - draw cables for 'E'
        val commons_E = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'E'), commons_E)
        // diagonal board to commons_E
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('E'), commons_E.getAvailableJack())
        // bridge_12 to commons_E
        operator.drawCableBetween(bridge_12.jack, commons_E.getAvailableJack())
        // bridge_89 to commons_E
        operator.drawCableBetween(bridge_89.jack, commons_E.getAvailableJack())
        // scrambler 10 input to commons_E
        operator.drawCableBetween(bombe.getScramblerJackPanel(10)!!.getInputJack(), commons_E.getAvailableJack())

        // back side - draw cables for 'S'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_23.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('S'))

        // back side - draw cables for 'Y'
        // bridge_34 to diagonal board
        operator.drawCableBetween(bridge_34.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('Y'))

        // back side - draw cables for 'F'
        val commons_F = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'F'), commons_F)
        // bank input to common_F
        operator.drawCableBetween(bombe.getChainJackPanel(1)!!.getInputJack(), commons_F.getAvailableJack())
        // diagonal board to commons_F
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('F'), commons_F.getAvailableJack())
        // bridge_45 to commons_F
        operator.drawCableBetween(bridge_45.jack, commons_F.getAvailableJack())
        // scrambler 10 output to commons_F
        operator.drawCableBetween(bombe.getScramblerJackPanel(10)!!.getOutputJack(), commons_F.getAvailableJack())

        // back side - draw cables for 'G'
        // bridge_56 to diagonal board
        operator.drawCableBetween(bridge_56.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('G'))

        // back side - draw cables for 'C'
        val commons_C = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'C'), commons_C)
        // diagonal board to commons_C
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('C'), commons_C.getAvailableJack())
        // bridge_67 to commons_C
        operator.drawCableBetween(bridge_67.jack, commons_C.getAvailableJack())
        // scrambler 9 output to commons_c
        operator.drawCableBetween(bombe.getScramblerJackPanel(9)!!.getOutputJack(), commons_C.getAvailableJack())

        // back side - draw cables for 'R'
        // bridge_78 to diagonal board
        operator.drawCableBetween(bridge_78.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('R'))

        // switch on the bank
        // TODO: add a unittest for an off bank
        bombe.getChainControlPanel(1)!!.switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChainControlPanel(1)!!.setContactToActivate('A')

        // run the job
        val stops = bombe.run()
        println("test menu III stops (${stops.size}):")
        stops.forEach { it.print() }

        assertEquals(6, stops.size, "expected 6 stops")
        assertEquals('A', stops.get(0).rotor1RingStellung)
        assertEquals('F', stops.get(0).rotor2RingStellung)
        assertEquals('Y', stops.get(0).rotor3RingStellung)
        assertEquals(listOf('K'), stops.get(0).possibleSteckerPartnersForCentralLetter)

    }

    // US 6812 Bombe Report 1944 - chapter 3
    // test menu IV
    // https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
    // http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_IV.txt
    @Test
    fun usBombeReport1944_testMenu_IV() {
        val bombe = Bombe(26, 1, 14, 3, 5, ReflectorType.B)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.II, DrumType.IV, DrumType.III)
        for (scramblerId in 1..14) {
            operator.placeDrums( scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, "ZAB")
        operator.setStartRingOrientations(2, "ZZB")
        operator.setStartRingOrientations(3, "ZZK")
        operator.setStartRingOrientations(4, "ZZL")
        operator.setStartRingOrientations(5, "ZZE")
        operator.setStartRingOrientations(6, "ZZH")
        operator.setStartRingOrientations(7, "ZZN")
        operator.setStartRingOrientations(8, "ZZF")
        operator.setStartRingOrientations(9, "ZZM")
        operator.setStartRingOrientations(10, "ZZG")
        operator.setStartRingOrientations(11, "ZAA")
        operator.setStartRingOrientations(12, "ZZA")
        operator.setStartRingOrientations(13, "ZZI")
        operator.setStartRingOrientations(14, "ZZJ")

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
        val bridge_78 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(7)!!.getOutputJack(),
            bombe.getScramblerJackPanel(8)!!.getInputJack()
        )
        val bridge_89 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(8)!!.getOutputJack(),
            bombe.getScramblerJackPanel(9)!!.getInputJack()
        )
        val bridge_910 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(9)!!.getOutputJack(),
            bombe.getScramblerJackPanel(10)!!.getInputJack()
        )
        val bridge_1011 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(10)!!.getOutputJack(),
            bombe.getScramblerJackPanel(11)!!.getInputJack()
        )
        val bridge_1112 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(11)!!.getOutputJack(),
            bombe.getScramblerJackPanel(12)!!.getInputJack()
        )
        // no bridge between 12 and 13
        // no bridge between 13 and 14

        // back side - draw cables for 'R'
        // scrambler 1 input to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(1)!!.getInputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('R'))

        // back side - draw cables for 'O'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_12.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('O'))

        // back side - draw cables for 'I'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_23.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('I'))

        // back side - draw cables for 'L'
        // bridge_34 to diagonal board
        operator.drawCableBetween(bridge_34.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('L'))

        // back side - draw cables for 'A'
        val commons_A = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'A'), commons_A)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('A'), commons_A.getAvailableJack())
        // bridge_45 to commons
        operator.drawCableBetween(bridge_45.jack, commons_A.getAvailableJack())
        // scrambler 13 input to commons
        // this connection is not mentioned in the menu, but it is depicted in the drawing
        operator.drawCableBetween(bombe.getScramblerJackPanel(13)!!.getInputJack(), commons_A.getAvailableJack())

        // back side - draw cables for 'K'
        // scrambler 13 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(13)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('K'))

        // back side - draw cables for 'Y'
        // bridge_56 to diagonal board
        operator.drawCableBetween(bridge_56.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('Y'))

        // back side - draw cables for 'F'
        val commons_F = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'F'), commons_F)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('F'), commons_F.getAvailableJack())
        // bridge_67 to commons
        operator.drawCableBetween(bridge_67.jack, commons_F.getAvailableJack())
        // scrambler 12 output to commons
        operator.drawCableBetween(bombe.getScramblerJackPanel(12)!!.getOutputJack(), commons_F.getAvailableJack())
        // bank input to commons
        operator.drawCableBetween(bombe.getChainJackPanel(1)!!.getInputJack(), commons_F.getAvailableJack())

        // back side - draw cables for 'H'
        // bridge_78 to diagonal board
        operator.drawCableBetween(bridge_78.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('H'))

        // back side - draw cables for 'T'
        // bridge_89 to diagonal board
        operator.drawCableBetween(bridge_89.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('T'))

        // back side - draw cables for 'D'
        // bridge_910 to diagonal board
        operator.drawCableBetween(bridge_910.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('D'))

        // back side - draw cables for 'Z'
        // bridge_1011 to diagonal board
        operator.drawCableBetween(bridge_1011.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('Z'))

        // back side - draw cables for 'E'
        val commons_E = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'E'), commons_E)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('E'), commons_E.getAvailableJack())
        // bridge_1112 to commons
        operator.drawCableBetween(bridge_1112.jack, commons_E.getAvailableJack())
        // scrambler 14 input to commons
        operator.drawCableBetween(bombe.getScramblerJackPanel(14)!!.getInputJack(), commons_E.getAvailableJack())

        // back side - draw cables for 'U'
        // scrambler 14 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(14)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('U'))

        // switch on the bank
        // TODO: add a unittest for an off bank
        bombe.getChainControlPanel(1)!!.switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChainControlPanel(1)!!.setContactToActivate('I')

        // run the job
        val stops = bombe.run()
        println("test menu IV stops (${stops.size}):")
        stops.forEach { it.print() }

        assertEquals(2, stops.size, "expected 2 stops")
        assertEquals('D', stops.get(0).rotor1RingStellung)
        assertEquals('G', stops.get(0).rotor2RingStellung)
        assertEquals('T', stops.get(0).rotor3RingStellung)
        assertEquals(listOf('I'), stops.get(0).possibleSteckerPartnersForCentralLetter)
    }

    // US 6812 Bombe Report 1944 - chapter 3
    // test menu IVa
    // https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
    // http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_IVa.txt
    @Test
    fun usBombeReport1944_testMenu_IVa() {
        // we need 2 banks for this one!
        val bombe = Bombe(26, 2, 14, 3, 5, ReflectorType.B)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.II, DrumType.IV, DrumType.III)
        for (scramblerId in 1..14) {
            operator.placeDrums(scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, "ZAB")
        operator.setStartRingOrientations(2, "ZZB")
        operator.setStartRingOrientations(3, "ZZK")
        operator.setStartRingOrientations(4, "ZZL")
        operator.setStartRingOrientations(5, "ZZE")
        operator.setStartRingOrientations(6, "ZZR")  // change compared to IV
        operator.setStartRingOrientations(7, "ZZN")
        operator.setStartRingOrientations(8, "ZZF")
        operator.setStartRingOrientations(9, "ZZM")
        operator.setStartRingOrientations(10, "ZZG")
        operator.setStartRingOrientations(11, "ZAA")
        operator.setStartRingOrientations(12, "ZZA")
        operator.setStartRingOrientations(13, "ZZI")
        operator.setStartRingOrientations(14, "ZZJ")

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
        // no bridge between 6 and 7 , change compared to menu IV
        val bridge_78 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(7)!!.getOutputJack(),
            bombe.getScramblerJackPanel(8)!!.getInputJack()
        )
        val bridge_89 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(8)!!.getOutputJack(),
            bombe.getScramblerJackPanel(9)!!.getInputJack()
        )
        val bridge_910 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(9)!!.getOutputJack(),
            bombe.getScramblerJackPanel(10)!!.getInputJack()
        )
        val bridge_1011 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(10)!!.getOutputJack(),
            bombe.getScramblerJackPanel(11)!!.getInputJack()
        )
        val bridge_1112 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(11)!!.getOutputJack(),
            bombe.getScramblerJackPanel(12)!!.getInputJack()
        )
        // no bridge between 12 and 13
        // no bridge between 13 and 14

        // back side - draw cables for 'R', change compared to menu IV
        val commons_R = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'R'), commons_R)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('R'), commons_R.getAvailableJack())
        // scrambler 1 input to commons
        operator.drawCableBetween(bombe.getScramblerJackPanel(1)!!.getInputJack(), commons_R.getAvailableJack())
        // scrambler 6 output to commons
        operator.drawCableBetween(bombe.getScramblerJackPanel(6)!!.getOutputJack(), commons_R.getAvailableJack())

        // back side - draw cables for 'O'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_12.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('O'))

        // back side - draw cables for 'I'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_23.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('I'))

        // back side - draw cables for 'L'
        // bridge_34 to diagonal board
        operator.drawCableBetween(bridge_34.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('L'))

        // back side - draw cables for 'A',  change compared to menu IV
        val commons_A = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'A'), commons_A)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('A'), commons_A.getAvailableJack())
        // bridge_45 to commons
        operator.drawCableBetween(bridge_45.jack, commons_A.getAvailableJack())
        // scrambler 13 input to commons
        operator.drawCableBetween(bombe.getScramblerJackPanel(13)!!.getInputJack(), commons_A.getAvailableJack())
        // input 2 to commons
        operator.drawCableBetween(bombe.getChainJackPanel(2)!!.getInputJack(), commons_A.getAvailableJack())

        // back side - draw cables for 'K'
        // scrambler 13 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(13)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('K'))

        // back side - draw cables for 'Y'
        // bridge_56 to diagonal board
        operator.drawCableBetween(bridge_56.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('Y'))

        // back side - draw cables for 'F', change compared to menu IV
        val commons_F = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'F'), commons_F)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('F'), commons_F.getAvailableJack())
        // scrambler 12 output to commons
        operator.drawCableBetween(bombe.getScramblerJackPanel(12)!!.getOutputJack(), commons_F.getAvailableJack())
        // scrambler 7 input to commons
        operator.drawCableBetween(bombe.getScramblerJackPanel(7)!!.getInputJack(), commons_F.getAvailableJack())

        // back side - draw cables for 'H'
        // bridge_78 to diagonal board
        operator.drawCableBetween(bridge_78.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('H'))

        // back side - draw cables for 'T'
        // bridge_89 to diagonal board
        operator.drawCableBetween(bridge_89.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('T'))

        // back side - draw cables for 'D'
        // bridge_910 to diagonal board
        operator.drawCableBetween(bridge_910.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('D'))

        // back side - draw cables for 'Z'
        // bridge_1011 to diagonal board
        operator.drawCableBetween(bridge_1011.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('Z'))

        // back side - draw cables for 'E', change compared to menu IV
        val commons_E = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'E'), commons_E)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('E'), commons_E.getAvailableJack())
        // bridge_1112 to commons
        operator.drawCableBetween(bridge_1112.jack, commons_E.getAvailableJack())
        // scrambler 14 input to commons
        operator.drawCableBetween(bombe.getScramblerJackPanel(14)!!.getInputJack(), commons_E.getAvailableJack())
        // input 1 to commons
        operator.drawCableBetween(bombe.getChainJackPanel(1)!!.getInputJack(), commons_E.getAvailableJack())

        // back side - draw cables for 'U'
        // scrambler 14 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(14)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('U'))

        // switch on the used chains
        // TODO: add a unittest for an off bank
        bombe.getChainControlPanel(1)!!.switchOn()
        bombe.getChainControlPanel(2)!!.switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChainControlPanel(1)!!.setContactToActivate('R')
        bombe.getChainControlPanel(2)!!.setContactToActivate('E')

        // switch on 'double input'
        bombe.switchDoubleInputOn()

        // run the job
        val stops = bombe.run()
        println("test menu IVa (double input) stops (${stops.size}):")
        stops.forEach { it.print() }

        assertEquals(6, stops.size, "expected 6 stops")
        assertEquals('B', stops.get(0).rotor1RingStellung)
        assertEquals('S', stops.get(0).rotor2RingStellung)
        assertEquals('T', stops.get(0).rotor3RingStellung)
        assertEquals(listOf('W'), stops.get(0).possibleSteckerPartnersForCentralLetter)
    }


    // US 6812 Bombe Report 1944 - chapter 3
    // test menu V - first variant
    // https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
    // http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_V.txt
    @Test
    fun usBombeReport1944_testMenu_V_1() {
        val bombe = Bombe(26, 1, 14, 3, 5, ReflectorType.B)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.I, DrumType.IV, DrumType.V)
        for (scramblerId in 1..14) {
            operator.placeDrums(scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, "YXW") // initially read 'VXW', corrected based on http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_Va.txt
        operator.setStartRingOrientations(2, "OKM")
        operator.setStartRingOrientations(3, "AME")
        operator.setStartRingOrientations(4, "FMQ")
        operator.setStartRingOrientations(5, "OKN")
        operator.setStartRingOrientations(6, "OTO")
        operator.setStartRingOrientations(7, "YXV")
        operator.setStartRingOrientations(8, "OTP")
        operator.setStartRingOrientations(9, "AMG")
        operator.setStartRingOrientations(10, "OTN")
        operator.setStartRingOrientations(11, "OKO")
        operator.setStartRingOrientations(12, "FMR")
        operator.setStartRingOrientations(13, "AMF")
        operator.setStartRingOrientations(14, "YXX")

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
        // no bridge between scrambler 7 and 8
        val bridge_89 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(8)!!.getOutputJack(),
            bombe.getScramblerJackPanel(9)!!.getInputJack()
        )
        // no bridge between scrambler 9 and 10
        // no bridge between scrambler 10 and 11
        val bridge_1112 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(11)!!.getOutputJack(),
            bombe.getScramblerJackPanel(12)!!.getInputJack()
        )
        val bridge_1213 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(12)!!.getOutputJack(),
            bombe.getScramblerJackPanel(13)!!.getInputJack()
        )
        val bridge_1314 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(13)!!.getOutputJack(),
            bombe.getScramblerJackPanel(14)!!.getInputJack()
        )

        // back side - draw cables for 'W'
        // scrambler 1 input to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(1)!!.getInputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('W'))

        // back side - draw cables for 'D'
        // bridge_12 to diagonal board
        operator.drawCableBetween(bridge_12.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('D'))

        // back side - draw cables for 'Z'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_23.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('Z'))

        // back side - draw cables for 'O'
        val commons_O = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'O'), commons_O)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('O'), commons_O.getAvailableJack())
        // bridge_34 to commons
        operator.drawCableBetween(bridge_34.jack, commons_O.getAvailableJack())
        // scrambler 10 input to commons
        operator.drawCableBetween(bombe.getScramblerJackPanel(10)!!.getInputJack(), commons_O.getAvailableJack())

        // back side - draw cables for 'B'
        // bridge_45 to diagonal board
        operator.drawCableBetween(bridge_45.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('B'))

        // back side - draw cables for 'H'
        // bridge_56 to diagonal board
        operator.drawCableBetween(bridge_56.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('H'))

        // back side - draw cables for 'K'
        val commons_K = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'K'), commons_K)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('K'), commons_K.getAvailableJack())
        // bridge_67 to commons
        operator.drawCableBetween(bridge_67.jack, commons_K.getAvailableJack())
        // bridge_1213 to commons
        operator.drawCableBetween(bridge_1213.jack, commons_K.getAvailableJack())
        // chain input to commons
        operator.drawCableBetween(bombe.getChainJackPanel(1)!!.getInputJack(), commons_K.getAvailableJack())

        // back side - draw cables for 'C'
        // bridge_1314 to diagonal board
        operator.drawCableBetween(bridge_1314.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('C'))

        // back side - draw cables for 'N'
        // bridge_1112 to diagonal board
        operator.drawCableBetween(bridge_1112.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('N'))

        // back side - draw cables for 'L'
        // bridge_89 to diagonal board
        operator.drawCableBetween(bridge_89.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('L'))

        // back side - draw cables for 'G'
        // scrambler 8 input to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(8)!!.getInputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('G'))

        // back side - draw cables for 'A'
        // scrambler 11 input to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(11)!!.getInputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('A'))

        // back side - draw cables for 'J'
        // scrambler 10 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(10)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('J'))

        // back side - draw cables for 'U'
        // scrambler 9 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(9)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('U'))

        // back side - draw cables for 'E'
        // scrambler 7 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(7)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('E'))

        // back side - draw cables for 'X'
        // scrambler 14 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(14)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('X'))

        // switch on the used chains
        // TODO: add a unittest for an off bank
        bombe.getChainControlPanel(1)!!.switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChainControlPanel(1)!!.setContactToActivate('Z')

        // run the job
        val stops = bombe.run()
        println("test menu V_1 stops (${stops.size}):")
        stops.forEach { it.print() }

        assertEquals(20, stops.size, "expected 20 stops")
        assertEquals('D', stops.get(0).rotor1RingStellung)
        assertEquals('E', stops.get(0).rotor2RingStellung)
        assertEquals('Z', stops.get(0).rotor3RingStellung)
        assertEquals(listOf('M'), stops.get(0).possibleSteckerPartnersForCentralLetter)
    }

    // US 6812 Bombe Report 1944 - chapter 3
    // test menu V - second variant
    // https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
    // http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_Va.txt
    @Test
    fun usBombeReport1944_testMenu_V_2() {
        val bombe = Bombe(26, 1, 14, 3, 5, ReflectorType.B)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.I, DrumType.IV, DrumType.V)
        for (scramblerId in 1..14) {
            operator.placeDrums(scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, "YXW") // initially read 'VXW', corrected based on http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_Va.txt
        operator.setStartRingOrientations(2, "OKM")
        operator.setStartRingOrientations(3, "AME")
        operator.setStartRingOrientations(4, "FMQ")
        operator.setStartRingOrientations(5, "OKN")
        operator.setStartRingOrientations(6, "OTO")
        operator.setStartRingOrientations(7, "YXV")
        operator.setStartRingOrientations(8, "OTP")
        operator.setStartRingOrientations(9, "AMG")
        operator.setStartRingOrientations(10, "OLN") // OTN in other variant
        operator.setStartRingOrientations(11, "OKO")
        operator.setStartRingOrientations(12, "FMR")
        operator.setStartRingOrientations(13, "AMF")
        operator.setStartRingOrientations(14, "YXV") // YXX in other variant

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
        // no bridge between scrambler 7 and 8
        val bridge_89 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(8)!!.getOutputJack(),
            bombe.getScramblerJackPanel(9)!!.getInputJack()
        )
        // no bridge between scrambler 9 and 10
        // no bridge between scrambler 10 and 11
        val bridge_1112 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(11)!!.getOutputJack(),
            bombe.getScramblerJackPanel(12)!!.getInputJack()
        )
        val bridge_1213 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(12)!!.getOutputJack(),
            bombe.getScramblerJackPanel(13)!!.getInputJack()
        )
        val bridge_1314 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(13)!!.getOutputJack(),
            bombe.getScramblerJackPanel(14)!!.getInputJack()
        )

        // back side - draw cables for 'W'
        // scrambler 1 input to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(1)!!.getInputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('W'))

        // back side - draw cables for 'D'
        // bridge_12 to diagonal board
        operator.drawCableBetween(bridge_12.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('D'))

        // back side - draw cables for 'Z'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_23.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('Z'))

        // back side - draw cables for 'O'
        val commons_O = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'O'), commons_O)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('O'), commons_O.getAvailableJack())
        // bridge_34 to commons
        operator.drawCableBetween(bridge_34.jack, commons_O.getAvailableJack())
        // scrambler 10 input to commons
        operator.drawCableBetween(bombe.getScramblerJackPanel(10)!!.getInputJack(), commons_O.getAvailableJack())

        // back side - draw cables for 'B'
        // bridge_45 to diagonal board
        operator.drawCableBetween(bridge_45.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('B'))

        // back side - draw cables for 'H'
        // bridge_56 to diagonal board
        operator.drawCableBetween(bridge_56.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('H'))

        // back side - draw cables for 'K'
        val commons_K = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'K'), commons_K)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('K'), commons_K.getAvailableJack())
        // bridge_67 to commons
        operator.drawCableBetween(bridge_67.jack, commons_K.getAvailableJack())
        // bridge_1213 to commons
        operator.drawCableBetween(bridge_1213.jack, commons_K.getAvailableJack())
        // chain input to commons
        operator.drawCableBetween(bombe.getChainJackPanel(1)!!.getInputJack(), commons_K.getAvailableJack())

        // back side - draw cables for 'C'
        // bridge_1314 to diagonal board
        operator.drawCableBetween(bridge_1314.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('C'))

        // back side - draw cables for 'N'
        // bridge_1212 to diagonal board
        operator.drawCableBetween(bridge_1112.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('N'))

        // back side - draw cables for 'L'
        // bridge_89 to diagonal board
        operator.drawCableBetween(bridge_89.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('L'))

        // back side - draw cables for 'G'
        // scrambler 8 input to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(8)!!.getInputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('G'))

        // back side - draw cables for 'A'
        // scrambler 11 input to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(11)!!.getInputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('A'))

        // back side - draw cables for 'J'
        // scrambler 10 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(10)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('J'))

        // back side - draw cables for 'U'
        // scrambler 9 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(9)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('U'))

        // back side - draw cables for 'E'
        // scrambler 7 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(7)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('E'))

        // back side - draw cables for 'X'
        // scrambler 14 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(14)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('X'))

        // switch on the used chains
        // TODO: add a unittest for an off bank
        bombe.getChainControlPanel(1)!!.switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChainControlPanel(1)!!.setContactToActivate('Z')

        // run the job
        val stops = bombe.run()
        println("test menu V-2 stops (${stops.size}):")
        stops.forEach { it.print() }

        assertEquals(18, stops.size, "expected 18 stops")
        assertEquals('V', stops.get(0).rotor1RingStellung)
        assertEquals('C', stops.get(0).rotor2RingStellung)
        assertEquals('U', stops.get(0).rotor3RingStellung)
        assertEquals(listOf('Y'), stops.get(0).possibleSteckerPartnersForCentralLetter)
    }

    // US 6812 Bombe Report 1944 - chapter 3
    // test menu VI
    // https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
    // http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_VI.txt
    @Test
    fun usBombeReport1944_testMenu_VI() {
        // double input, so we need to banks in order to get 2 chains
        val bombe = Bombe(26, 2, 14, 3, 5, ReflectorType.B)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.I, DrumType.II, DrumType.III)
        for (scramblerId in 1..14) {
            operator.placeDrums(scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, "ZZA")
        operator.setStartRingOrientations(2, "ZZD")
        operator.setStartRingOrientations(3, "ZAD")
        operator.setStartRingOrientations(4, "ZAI")
        operator.setStartRingOrientations(5, "ZZB")
        operator.setStartRingOrientations(6, "ZZO")
        operator.setStartRingOrientations(7, "ZZK")
        operator.setStartRingOrientations(8, "ZAF")
        operator.setStartRingOrientations(9, "ZAD")
        operator.setStartRingOrientations(10, "ZZR")
        operator.setStartRingOrientations(11, "ZAJ")
        operator.setStartRingOrientations(12, "ZAE")
        operator.setStartRingOrientations(13, "ZAY")
        operator.setStartRingOrientations(14, "ZAK")

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
        val bridge_78 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(7)!!.getOutputJack(),
            bombe.getScramblerJackPanel(8)!!.getInputJack()
        )
        // no bridge between scrambler 8 and 9
        val bridge_910 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(9)!!.getOutputJack(),
            bombe.getScramblerJackPanel(10)!!.getInputJack()
        )
        val bridge_1011 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(10)!!.getOutputJack(),
            bombe.getScramblerJackPanel(11)!!.getInputJack()
        )
        val bridge_1112 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(11)!!.getOutputJack(),
            bombe.getScramblerJackPanel(12)!!.getInputJack()
        )
        val bridge_1213 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(12)!!.getOutputJack(),
            bombe.getScramblerJackPanel(13)!!.getInputJack()
        )
        val bridge_1314 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(13)!!.getOutputJack(),
            bombe.getScramblerJackPanel(14)!!.getInputJack()
        )

        // back side - draw cables for 'A'
        // scrambler 1 input to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(1)!!.getInputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('A'))

        // back side - draw cables for 'I'
        // bridge_12 to diagonal board
        operator.drawCableBetween(bridge_12.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('I'))

        // back side - draw cables for 'H'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_23.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('H'))

        // back side - draw cables for 'F'
        // bridge_34 to diagonal board
        operator.drawCableBetween(bridge_34.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('F'))

        // back side - draw cables for 'Y'
        val commons_Y = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'Y'), commons_Y)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('Y'), commons_Y.getAvailableJack())
        // bridge_45 to commons
        operator.drawCableBetween(bridge_45.jack, commons_Y.getAvailableJack())
        // chain 1 input to commons
        operator.drawCableBetween(bombe.getChainJackPanel(1)!!.getInputJack(), commons_Y.getAvailableJack())

        // back side - draw cables for 'K'
        // bridge_56 to diagonal board
        operator.drawCableBetween(bridge_56.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('K'))

        // back side - draw cables for 'D'
        // bridge_67 to diagonal board
        operator.drawCableBetween(bridge_67.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('D'))

        // back side - draw cables for 'L'
        // bridge_78 to diagonal board
        operator.drawCableBetween(bridge_78.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('L'))

        // back side - draw cables for 'Q'  // initially read this as a 'G
        // scrambler 8 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(8)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('Q'))

        // back side - draw cables for 'T'
        // scrambler 9 input to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(9)!!.getInputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('T'))

        // back side - draw cables for 'W'
        // bridge_910 to diagonal board
        operator.drawCableBetween(bridge_910.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('W'))

        // back side - draw cables for 'U'
        // bridge_1011 to diagonal board
        operator.drawCableBetween(bridge_1011.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('U'))

        // back side - draw cables for 'O'
        val commons_O = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'O'), commons_O)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('O'), commons_O.getAvailableJack())
        // bridge_1112 to commons
        operator.drawCableBetween(bridge_1112.jack, commons_O.getAvailableJack())
        // chain 1I input to commons
        operator.drawCableBetween(bombe.getChainJackPanel(2)!!.getInputJack(), commons_O.getAvailableJack())

        // back side - draw cables for 'S'
        // bridge_1213 to diagonal board
        operator.drawCableBetween(bridge_1213.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('S'))

        // back side - draw cables for 'P'
        // bridge_1314 to diagonal board
        operator.drawCableBetween(bridge_1314.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('P'))

        // back side - draw cables for 'C'
        // scrambler 14 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(14)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('C'))

        // switch on the used chains
        // TODO: add a unittest for an off bank
        bombe.getChainControlPanel(1)!!.switchOn()
        bombe.getChainControlPanel(2)!!.switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChainControlPanel(1)!!.setContactToActivate('O')
        bombe.getChainControlPanel(2)!!.setContactToActivate('Y')

        // switch on 'double input'
        bombe.switchDoubleInputOn()

        // run the job
        val stops = bombe.run()
        println("test menu VI stops (${stops.size}):")
        stops.forEach { it.print() }

        assertEquals(44, stops.size, "expected 44 stops")
        assertEquals('E', stops.get(0).rotor1RingStellung)
        assertEquals('V', stops.get(0).rotor2RingStellung)
        assertEquals('Z', stops.get(0).rotor3RingStellung)
        assertEquals(listOf('E'), stops.get(0).possibleSteckerPartnersForCentralLetter)
    }

    // US 6812 Bombe Report 1944 - chapter 3
    // test menu VII
    // https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
    // http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_VII.txt
    @Test
    fun usBombeReport1944_testMenu_VII() {
        val bombe = Bombe(26, 1, 27, 3, 5, ReflectorType.B)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.V, DrumType.IV, DrumType.III)
        for (scramblerId in 1..27) {
            operator.placeDrums(scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, "AGP")
        operator.setStartRingOrientations(2, "ENF")
        operator.setStartRingOrientations( 3,"EMI")
        operator.setStartRingOrientations( 4,"ENH")
        operator.setStartRingOrientations( 5,"EMK")
        operator.setStartRingOrientations( 6,"ENL")
        operator.setStartRingOrientations( 7,"EMO")
        operator.setStartRingOrientations(8, "ENK")
        operator.setStartRingOrientations(9, "EMN")
        operator.setStartRingOrientations(10, "ENM")
        operator.setStartRingOrientations(11, "EMP")
        operator.setStartRingOrientations(12, "CIQ")
        operator.setStartRingOrientations(13, "EMT")
        operator.setStartRingOrientations(14, "ENQ")
        operator.setStartRingOrientations(15, "ENN")
        operator.setStartRingOrientations(16, "EMQ")
        operator.setStartRingOrientations(17, "ENG")
        operator.setStartRingOrientations(18, "EMJ")
        operator.setStartRingOrientations(19, "ENR")
        operator.setStartRingOrientations(20, "EMU")
        operator.setStartRingOrientations(21, "CIR")
        operator.setStartRingOrientations(22, "EML")
        operator.setStartRingOrientations(23, "ENI")
        operator.setStartRingOrientations(24, "EMR")
        operator.setStartRingOrientations(25, "ENO")
        operator.setStartRingOrientations(26, "CIP")
        operator.setStartRingOrientations(27, "AGN")

        // back side - place bridges between scramblers
        val bridge_12 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(1)!!.getOutputJack(),
            bombe.getScramblerJackPanel(2)!!.getInputJack()
        )
        // bridge 2 to 3 represents a ?
        operator.attachBridgeTo(
            bombe.getScramblerJackPanel(2)!!.getOutputJack(),
            bombe.getScramblerJackPanel(3)!!.getInputJack()
        )
        val bridge_34 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(3)!!.getOutputJack(),
            bombe.getScramblerJackPanel(4)!!.getInputJack()
        )
        // bridge 4 to 5 represents a ?
        operator.attachBridgeTo(
            bombe.getScramblerJackPanel(4)!!.getOutputJack(),
            bombe.getScramblerJackPanel(5)!!.getInputJack()
        )
        val bridge_56 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(5)!!.getOutputJack(),
            bombe.getScramblerJackPanel(6)!!.getInputJack()
        )
        // bridge 6 to 7 represents a ?
        operator.attachBridgeTo(
            bombe.getScramblerJackPanel(6)!!.getOutputJack(),
            bombe.getScramblerJackPanel(7)!!.getInputJack()
        )
        val bridge_78 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(7)!!.getOutputJack(),
            bombe.getScramblerJackPanel(8)!!.getInputJack()
        )
        // bridge 8 to 9 represents a ?
        operator.attachBridgeTo(
            bombe.getScramblerJackPanel(8)!!.getOutputJack(),
            bombe.getScramblerJackPanel(9)!!.getInputJack()
        )
        // no bridge between scrambler 9 and 10
        // bridge 10 to 11 represents a ?
        operator.attachBridgeTo(
            bombe.getScramblerJackPanel(10)!!.getOutputJack(),
            bombe.getScramblerJackPanel(11)!!.getInputJack()
        )
        val bridge_1112 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(11)!!.getOutputJack(),
            bombe.getScramblerJackPanel(12)!!.getInputJack()
        )
        // no bridge between 12 and 13
        // bridge 13 to 14 represents a ?
        operator.attachBridgeTo(
            bombe.getScramblerJackPanel(13)!!.getOutputJack(),
            bombe.getScramblerJackPanel(14)!!.getInputJack()
        )
        val bridge_1415 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(14)!!.getOutputJack(),
            bombe.getScramblerJackPanel(15)!!.getInputJack()
        )
        // bridge 15 to 16 represents a ?
        operator.attachBridgeTo(
            bombe.getScramblerJackPanel(15)!!.getOutputJack(),
            bombe.getScramblerJackPanel(16)!!.getInputJack()
        )
        val bridge_1617 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(16)!!.getOutputJack(),
            bombe.getScramblerJackPanel(17)!!.getInputJack()
        )
        // bridge 17 to 18 represents a ?
        operator.attachBridgeTo(
            bombe.getScramblerJackPanel(17)!!.getOutputJack(),
            bombe.getScramblerJackPanel(18)!!.getInputJack()
        )
        // no bridge between 18 and 19
        // bridge 19 to 20 represents a ?
        operator.attachBridgeTo(
            bombe.getScramblerJackPanel(19)!!.getOutputJack(),
            bombe.getScramblerJackPanel(20)!!.getInputJack()
        )
        val bridge_2021 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(20)!!.getOutputJack(),
            bombe.getScramblerJackPanel(21)!!.getInputJack()
        )
        val bridge_2122 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(21)!!.getOutputJack(),
            bombe.getScramblerJackPanel(22)!!.getInputJack()
        )
        // bridge 22 to 23 represents a ?
        operator.attachBridgeTo(
            bombe.getScramblerJackPanel(22)!!.getOutputJack(),
            bombe.getScramblerJackPanel(23)!!.getInputJack()
        )
        val bridge_2324 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(23)!!.getOutputJack(),
            bombe.getScramblerJackPanel(24)!!.getInputJack()
        )
        // bridge 24 to 25 represents a ?
        operator.attachBridgeTo(
            bombe.getScramblerJackPanel(24)!!.getOutputJack(),
            bombe.getScramblerJackPanel(25)!!.getInputJack()
        )
        // no bridge between 25 en 26
        val bridge_2627 = operator.attachBridgeTo(
            bombe.getScramblerJackPanel(26)!!.getOutputJack(),
            bombe.getScramblerJackPanel(27)!!.getInputJack()
        )

        // back side - draw cables for 'Q'
        // scrambler 1 input to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(1)!!.getInputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('Q'))

        // back side - draw cables for 'N'
        // bridge_12 to diagonal board
        operator.drawCableBetween(bridge_12.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('N'))

        // back side - draw cables for 'O'
        // bridge_34 to diagonal board
        operator.drawCableBetween(bridge_34.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('O'))

        // back side - draw cables for 'K'
        // bridge_56 to diagonal board
        operator.drawCableBetween(bridge_56.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('K'))

        // back side - draw cables for 'V'
        val commons_V = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'V'), commons_V)
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('V'), commons_V.getAvailableJack())
        // bridge_78 to commons
        operator.drawCableBetween(bridge_78.jack, commons_V.getAvailableJack())
        // scrambler 10 input to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(10)!!.getInputJack(), commons_V.getAvailableJack())
        // scrambler 13 input to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(13)!!.getInputJack(), commons_V.getAvailableJack())
        // chain 1 input to commons
        operator.drawCableBetween(bombe.getChainJackPanel(1)!!.getInputJack(), commons_V.getAvailableJack())

        // back side - draw cables for 'A'
        // scrambler 9 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(9)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('A'))

        // back side - draw cables for 'J'
        // bridge_1112 to diagonal board
        operator.drawCableBetween(bridge_1112.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('J'))

        // back side - draw cables for 'I'
        // scrambler 12 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(12)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('I'))

        // back side - draw cables for 'Z'
        // bridge_1415 to diagonal board
        operator.drawCableBetween(bridge_1415.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('Z'))

        // back side - draw cables for 'Y'
        // bridge_1617 to diagonal board
        operator.drawCableBetween(bridge_1617.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('Y'))

        // back side - draw cables for 'M'
        // scrambler 18 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(18)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('M'))

        // back side - draw cables for 'W'
        // scrambler 19 input to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(19)!!.getInputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('W'))

        // back side - draw cables for 'P'
        val commons_P = operator.findFreeCommonsSet()
        operator.commonsSetRegister.put(Pair(1,'P'), commons_P)
        // bridge_2021 to diagonal board
        operator.drawCableBetween(bridge_2021.jack, commons_P.getAvailableJack())
        // bridge_2324 to diagonal board
        operator.drawCableBetween(bridge_2324.jack, commons_P.getAvailableJack())

        // back side - draw cables for 'S'
        // bridge_2122 to diagonal board
        operator.drawCableBetween(bridge_2122.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('S'))

        // back side - draw cables for 'E'
        // scrambler 25 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(25)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('E'))

        // back side - draw cables for 'D'
        // scrambler 26 input to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(26)!!.getInputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('D'))

        // back side - draw cables for 'C'
        // bridge_2627 to diagonal board
        operator.drawCableBetween(bridge_2627.jack, bombe.getDiagonalBoardJackPanel(1)!!.getJack('C'))

        // back side - draw cables for 'B'
        // scrambler 27 output to diagonal board
        operator.drawCableBetween(bombe.getScramblerJackPanel(27)!!.getOutputJack(), bombe.getDiagonalBoardJackPanel(1)!!.getJack('B'))

        // switch on the used chains
        // TODO: add a unittest for an off bank
        bombe.getChainControlPanel(1)!!.switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChainControlPanel(1)!!.setContactToActivate('A')

        // count scramblers which don't have both input and output plugged up
        val incompleteScramblers = bombe.getScramblers().filter { it!!.getInputJack().pluggedUpBy() == null || it!!.getOutputJack().pluggedUpBy() == null }.count()
        println("$incompleteScramblers scramblers without both jacks plugged up")

        // count bridges who's jack is not plugged up (this should be equal to the number of '?' in the menu)
        val unpluggedBridges = bombe.getBridges().filter { it.jack.pluggedUpBy() == null }.count()
        println("$unpluggedBridges bridges which are not plugged up (represent '?' in the menu)")

        // run the job
        val stops = bombe.run()
        println("test menu VII stops (${stops.size}):")
        stops.forEach { it.print() }

        assertEquals(14, stops.size, "expected 14 stops")
        assertEquals('C', stops.get(0).rotor1RingStellung)
        assertEquals('K', stops.get(0).rotor2RingStellung)
        assertEquals('Y', stops.get(0).rotor3RingStellung)
        assertEquals(listOf('H'), stops.get(0).possibleSteckerPartnersForCentralLetter)
    }
}