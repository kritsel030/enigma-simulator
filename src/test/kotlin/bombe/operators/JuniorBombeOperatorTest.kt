package bombe.operators

import bombe.Bombe
import bombe.components.DrumType
import bombe.components.DummyComponent
import bombe.connectors.CablePlug
import bombe.connectors.Jack
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JuniorBombeOperatorTest {

    @Test
    fun createAndConnectCable() {
        val bombe = Bombe(3, 1, 1, 1)
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
        val bombe = Bombe(3, 1, 2, 1)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        val scrambler1 = bombe.getBank(1).getScrambler(1)
        val scrambler2 = bombe.getBank(1).getScrambler(2)

        val bridge = operator.attachBridgeTo(scrambler1!!.outputJack, scrambler2!!.inputJack)
        assertNotNull(bridge)
        assertNotNull(scrambler1.outputJack.pluggedUpBy())
        assertNotNull(scrambler2.inputJack.pluggedUpBy())
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
            operator.placeDrums(1, scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, 1, "ZZS")
        operator.setStartRingOrientations(1, 2, "ZZZ")
        operator.setStartRingOrientations(1, 3, "ZAX")
        operator.setStartRingOrientations(1, 4, "ZAS")
        operator.setStartRingOrientations(1, 5, "ZAY")
        operator.setStartRingOrientations(1, 6, "ZZW")
        operator.setStartRingOrientations(1, 7, "ZAV")

        // back side - place bridges
        val bridge_12 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(1).outputJack,
            bombe.getBank(1).getScrambler(2).inputJack
        )
        val bridge_23 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(2).outputJack,
            bombe.getBank(1).getScrambler(3).inputJack
        )
        val bridge_34 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(3).outputJack,
            bombe.getBank(1).getScrambler(4).inputJack
        )
        val bridge_45 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(4).outputJack,
            bombe.getBank(1).getScrambler(5).inputJack
        )
        val bridge_56 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(5).outputJack,
            bombe.getBank(1).getScrambler(6).inputJack
        )
        val bridge_67 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(6).outputJack,
            bombe.getBank(1).getScrambler(7).inputJack
        )

        // back side - draw cables for 'U'
        val commons_U = bombe.claimAvailableCommonsSet(1, 'U')
        // diagonal board to commons_U
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('U'), commons_U.getAvailableJack())
        // scrambler 1 input to commons_U
        operator.drawCableBetween(bombe.getBank(1).getScrambler(1).inputJack, commons_U.getAvailableJack())
        // bridge_23 to commons_U
        operator.drawCableBetween(bridge_23.jack, commons_U.getAvailableJack())
        // bridge_56 to commons_U
        operator.drawCableBetween(bridge_56.jack, commons_U.getAvailableJack())

        // back side - draw cables for 'N'
        val commons_N = bombe.claimAvailableCommonsSet(1, 'N')
        // diagonal board to commons_N
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('N'), commons_N.getAvailableJack())
        // bridge_12 to commons_N
        operator.drawCableBetween(bridge_12.jack, commons_N.getAvailableJack())
        // bridge_34 to commons_N
        operator.drawCableBetween(bridge_34.jack, commons_N.getAvailableJack())
        // scrambler 7 output to commons_N
        operator.drawCableBetween(bombe.getBank(1).getScrambler(7).outputJack, commons_N.getAvailableJack())

        val commons_E = bombe.claimAvailableCommonsSet(1, 'E')
        // bank input to common_E
        operator.drawCableBetween(bombe.getChain(1).inputJack, commons_E.getAvailableJack())
        // diagonal board to commons_E
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('E'), commons_E.getAvailableJack())
        // bridge_45 to commons_E
        operator.drawCableBetween(bridge_45.jack, commons_E.getAvailableJack())
        // bridge_67 to commons_E
        operator.drawCableBetween(bridge_67.jack, commons_E.getAvailableJack())

        // switch on the bank
        // TODO: add a unittest for an off bank
        bombe.getChain(1).switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChain(1).setContactToActivate('A')

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
            operator.placeDrums(1, scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, 1, "EKR")
        operator.setStartRingOrientations(1, 2, "RTN")
        operator.setStartRingOrientations(1, 3, "SAO")
        operator.setStartRingOrientations(1, 4, "EKP")
        operator.setStartRingOrientations(1, 5, "RTI")
        operator.setStartRingOrientations(1, 6, "RTQ")
        operator.setStartRingOrientations(1, 7, "SAT")
        operator.setStartRingOrientations(1, 8, "SAS")
        operator.setStartRingOrientations(1, 9, "RTR")
        operator.setStartRingOrientations(1, 10, "RTJ")

        // back side - place bridges
        val bridge_12 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(1).outputJack,
            bombe.getBank(1).getScrambler(2).inputJack
        )
        val bridge_23 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(2).outputJack,
            bombe.getBank(1).getScrambler(3).inputJack
        )
        val bridge_34 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(3).outputJack,
            bombe.getBank(1).getScrambler(4).inputJack
        )
        val bridge_45 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(4).outputJack,
            bombe.getBank(1).getScrambler(5).inputJack
        )
        val bridge_56 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(5).outputJack,
            bombe.getBank(1).getScrambler(6).inputJack
        )
        val bridge_67 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(6).outputJack,
            bombe.getBank(1).getScrambler(7).inputJack
        )
        val bridge_78 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(7).outputJack,
            bombe.getBank(1).getScrambler(8).inputJack
        )
        // no bridge between 8 and 9
        val bridge_910 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(9).outputJack,
            bombe.getBank(1).getScrambler(10).inputJack
        )

        // back side - draw cables for 'U'
        val commons_U = bombe.claimAvailableCommonsSet(1, 'U')
        // diagonal board to commons_U
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('U'), commons_U.getAvailableJack())
        // scrambler 1 input to commons_U
        operator.drawCableBetween(bombe.getBank(1).getScrambler(1).inputJack, commons_U.getAvailableJack())
        // scrambler 9 input to commons_U
        operator.drawCableBetween(bombe.getBank(1).getScrambler(9).inputJack, commons_U.getAvailableJack())

        // back side - draw cables for 'E'
        val commons_E = bombe.claimAvailableCommonsSet(1, 'E')
        // diagonal board to commons_E
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('E'), commons_E.getAvailableJack())
        // bridge_12 to commons_E
        operator.drawCableBetween(bridge_12.jack, commons_E.getAvailableJack())
        // bridge_910 to commons_E
        operator.drawCableBetween(bridge_910.jack, commons_E.getAvailableJack())

        // back side - draw cables for 'Y'
        val commons_Y = bombe.claimAvailableCommonsSet(1, 'Y')
        // diagonal board to commons_Y
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('Y'), commons_Y.getAvailableJack())
        // bridge_23 to commons_Y
        operator.drawCableBetween(bridge_23.jack, commons_Y.getAvailableJack())

        // back side - draw cables for 'H'
        val commons_H = bombe.claimAvailableCommonsSet(1, 'H')
        // bank input to common_H
        operator.drawCableBetween(bombe.getChain(1).inputJack, commons_H.getAvailableJack())
        // diagonal board to commons_H
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('H'), commons_H.getAvailableJack())
        // bridge_34 to commons_H
        operator.drawCableBetween(bridge_34.jack, commons_H.getAvailableJack())
        // bridge_67 to commons_H
        operator.drawCableBetween(bridge_67.jack, commons_H.getAvailableJack())
        // scrambler 8 output to commons_H
        operator.drawCableBetween(bombe.getBank(1).getScrambler(8).outputJack, commons_H.getAvailableJack())

        // back side - draw cables for 'F'
        val commons_F = bombe.claimAvailableCommonsSet(1, 'F')
        // diagonal board to commons_F
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('F'), commons_F.getAvailableJack())
        // bridge_45 to commons_F
        operator.drawCableBetween(bridge_45.jack, commons_F.getAvailableJack())

        // back side - draw cables for 'B'
        val commons_B = bombe.claimAvailableCommonsSet(1, 'B')
        // diagonal board to commons_B
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('B'), commons_B.getAvailableJack())
        // bridge_56 to commons_B
        operator.drawCableBetween(bridge_56.jack, commons_B.getAvailableJack())

        // back side - draw cables for 'T'
        val commons_T = bombe.claimAvailableCommonsSet(1, 'T')
        // diagonal board to commons_T
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('T'), commons_T.getAvailableJack())
        // bridge_78 to commons_T
        operator.drawCableBetween(bridge_78.jack, commons_T.getAvailableJack())

        // back side - draw cables for 'X'
        // diagonal board to scrambler 10 out
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('X'), bombe.getBank(1).getScrambler(10).outputJack)

        // switch on the bank
        // TODO: add a unittest for an off bank
        bombe.getChain(1).switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChain(1).setContactToActivate('A')

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
            operator.placeDrums(1, scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, 1, "ZAP")
        operator.setStartRingOrientations(1, 2, "ZZJ")
        operator.setStartRingOrientations(1, 3, "ZAJ")
        operator.setStartRingOrientations(1, 4, "ZZP")
        operator.setStartRingOrientations(1, 5, "ZZN")
        operator.setStartRingOrientations(1, 6, "ZAN")
        operator.setStartRingOrientations(1, 7, "ZZK")
        operator.setStartRingOrientations(1, 8, "ZAQ") // initially read 'ZAG', corrected based on http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_Va.txt
        operator.setStartRingOrientations(1, 9, "ZZO")
        operator.setStartRingOrientations(1, 10, "ZZR")

        // back side - place bridges
        val bridge_12 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(1).outputJack,
            bombe.getBank(1).getScrambler(2).inputJack
        )
        val bridge_23 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(2).outputJack,
            bombe.getBank(1).getScrambler(3).inputJack
        )
        val bridge_34 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(3).outputJack,
            bombe.getBank(1).getScrambler(4).inputJack
        )
        val bridge_45 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(4).outputJack,
            bombe.getBank(1).getScrambler(5).inputJack
        )
        val bridge_56 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(5).outputJack,
            bombe.getBank(1).getScrambler(6).inputJack
        )
        val bridge_67 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(6).outputJack,
            bombe.getBank(1).getScrambler(7).inputJack
        )
        val bridge_78 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(7).outputJack,
            bombe.getBank(1).getScrambler(8).inputJack
        )
        val bridge_89 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(8).outputJack,
            bombe.getBank(1).getScrambler(9).inputJack
        )
        // no bridge between 9 and 10

        // back side - draw cables for 'T'
        val commons_T = bombe.claimAvailableCommonsSet(1, 'T')
        // scrambler 1 input to commons_T
        operator.drawCableBetween(bombe.getBank(1).getScrambler(1).inputJack, commons_T.getAvailableJack())

        // back side - draw cables for 'E'
        val commons_E = bombe.claimAvailableCommonsSet(1, 'E')
        // diagonal board to commons_E
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('E'), commons_E.getAvailableJack())
        // bridge_12 to commons_E
        operator.drawCableBetween(bridge_12.jack, commons_E.getAvailableJack())
        // bridge_89 to commons_E
        operator.drawCableBetween(bridge_89.jack, commons_E.getAvailableJack())
        // scrambler 10 input to commons_E
        operator.drawCableBetween(bombe.getBank(1).getScrambler(10).inputJack, commons_E.getAvailableJack())

        // back side - draw cables for 'S'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_23.jack, bombe.getDiagonalBoard(1).getJack('S'))

        // back side - draw cables for 'Y'
        // bridge_34 to diagonal board
        operator.drawCableBetween(bridge_34.jack, bombe.getDiagonalBoard(1).getJack('Y'))

        // back side - draw cables for 'F'
        val commons_F = bombe.claimAvailableCommonsSet(1, 'F')
        // bank input to common_F
        operator.drawCableBetween(bombe.getChain(1).inputJack, commons_F.getAvailableJack())
        // diagonal board to commons_F
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('F'), commons_F.getAvailableJack())
        // bridge_45 to commons_F
        operator.drawCableBetween(bridge_45.jack, commons_F.getAvailableJack())
        // scrambler 10 output to commons_F
        operator.drawCableBetween(bombe.getBank(1).getScrambler(10).outputJack, commons_F.getAvailableJack())

        // back side - draw cables for 'G'
        // bridge_56 to diagonal board
        operator.drawCableBetween(bridge_56.jack, bombe.getDiagonalBoard(1).getJack('G'))

        // back side - draw cables for 'C'
        val commons_C = bombe.claimAvailableCommonsSet(1, 'C')
        // diagonal board to commons_C
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('C'), commons_C.getAvailableJack())
        // bridge_67 to commons_C
        operator.drawCableBetween(bridge_67.jack, commons_C.getAvailableJack())
        // scrambler 9 output to commons_c
        operator.drawCableBetween(bombe.getBank(1).getScrambler(9).outputJack, commons_C.getAvailableJack())

        // back side - draw cables for 'R'
        // bridge_78 to diagonal board
        operator.drawCableBetween(bridge_78.jack, bombe.getDiagonalBoard(1).getJack('R'))

        // switch on the bank
        // TODO: add a unittest for an off bank
        bombe.getChain(1).switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChain(1).setContactToActivate('A')

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
        val bombe = Bombe(26, 1, 14, 3)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.II, DrumType.IV, DrumType.III)
        for (scramblerId in 1..14) {
            operator.placeDrums(1, scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, 1, "ZAB")
        operator.setStartRingOrientations(1, 2, "ZZB")
        operator.setStartRingOrientations(1, 3, "ZZK")
        operator.setStartRingOrientations(1, 4, "ZZL")
        operator.setStartRingOrientations(1, 5, "ZZE")
        operator.setStartRingOrientations(1, 6, "ZZH")
        operator.setStartRingOrientations(1, 7, "ZZN")
        operator.setStartRingOrientations(1, 8, "ZZF")
        operator.setStartRingOrientations(1, 9, "ZZM")
        operator.setStartRingOrientations(1, 10, "ZZG")
        operator.setStartRingOrientations(1, 11, "ZAA")
        operator.setStartRingOrientations(1, 12, "ZZA")
        operator.setStartRingOrientations(1, 13, "ZZI")
        operator.setStartRingOrientations(1, 14, "ZZJ")

        // back side - place bridges
        val bridge_12 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(1).outputJack,
            bombe.getBank(1).getScrambler(2).inputJack
        )
        val bridge_23 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(2).outputJack,
            bombe.getBank(1).getScrambler(3).inputJack
        )
        val bridge_34 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(3).outputJack,
            bombe.getBank(1).getScrambler(4).inputJack
        )
        val bridge_45 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(4).outputJack,
            bombe.getBank(1).getScrambler(5).inputJack
        )
        val bridge_56 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(5).outputJack,
            bombe.getBank(1).getScrambler(6).inputJack
        )
        val bridge_67 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(6).outputJack,
            bombe.getBank(1).getScrambler(7).inputJack
        )
        val bridge_78 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(7).outputJack,
            bombe.getBank(1).getScrambler(8).inputJack
        )
        val bridge_89 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(8).outputJack,
            bombe.getBank(1).getScrambler(9).inputJack
        )
        val bridge_910 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(9).outputJack,
            bombe.getBank(1).getScrambler(10).inputJack
        )
        val bridge_1011 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(10).outputJack,
            bombe.getBank(1).getScrambler(11).inputJack
        )
        val bridge_1112 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(11).outputJack,
            bombe.getBank(1).getScrambler(12).inputJack
        )
        // no bridge between 12 and 13
        // no bridge between 13 and 14

        // back side - draw cables for 'R'
        // scrambler 1 input to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(1).inputJack, bombe.getDiagonalBoard(1).getJack('R'))

        // back side - draw cables for 'O'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_12.jack, bombe.getDiagonalBoard(1).getJack('O'))

        // back side - draw cables for 'I'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_23.jack, bombe.getDiagonalBoard(1).getJack('I'))

        // back side - draw cables for 'L'
        // bridge_34 to diagonal board
        operator.drawCableBetween(bridge_34.jack, bombe.getDiagonalBoard(1).getJack('L'))

        // back side - draw cables for 'A'
        val commons_A = bombe.claimAvailableCommonsSet(1, 'A')
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('A'), commons_A.getAvailableJack())
        // bridge_45 to commons
        operator.drawCableBetween(bridge_45.jack, commons_A.getAvailableJack())
        // scrambler 13 input to commons
        // this connection is not mentioned in the menu, but it is depicted in the drawing
        operator.drawCableBetween(bombe.getBank(1).getScrambler(13).inputJack, commons_A.getAvailableJack())

        // back side - draw cables for 'K'
        // scrambler 13 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(13).outputJack, bombe.getDiagonalBoard(1).getJack('K'))

        // back side - draw cables for 'Y'
        // bridge_56 to diagonal board
        operator.drawCableBetween(bridge_56.jack, bombe.getDiagonalBoard(1).getJack('Y'))

        // back side - draw cables for 'F'
        val commons_F = bombe.claimAvailableCommonsSet(1, 'F')
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('F'), commons_F.getAvailableJack())
        // bridge_67 to commons
        operator.drawCableBetween(bridge_67.jack, commons_F.getAvailableJack())
        // scrambler 12 output to commons
        operator.drawCableBetween(bombe.getBank(1).getScrambler(12).outputJack, commons_F.getAvailableJack())
        // bank input to commons
        operator.drawCableBetween(bombe.getChain(1).inputJack, commons_F.getAvailableJack())

        // back side - draw cables for 'H'
        // bridge_78 to diagonal board
        operator.drawCableBetween(bridge_78.jack, bombe.getDiagonalBoard(1).getJack('H'))

        // back side - draw cables for 'T'
        // bridge_89 to diagonal board
        operator.drawCableBetween(bridge_89.jack, bombe.getDiagonalBoard(1).getJack('T'))

        // back side - draw cables for 'D'
        // bridge_910 to diagonal board
        operator.drawCableBetween(bridge_910.jack, bombe.getDiagonalBoard(1).getJack('D'))

        // back side - draw cables for 'Z'
        // bridge_1011 to diagonal board
        operator.drawCableBetween(bridge_1011.jack, bombe.getDiagonalBoard(1).getJack('Z'))

        // back side - draw cables for 'E'
        val commons_E = bombe.claimAvailableCommonsSet(1, 'E')
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('E'), commons_E.getAvailableJack())
        // bridge_1112 to commons
        operator.drawCableBetween(bridge_1112.jack, commons_E.getAvailableJack())
        // scrambler 14 input to commons
        operator.drawCableBetween(bombe.getBank(1).getScrambler(14).inputJack, commons_E.getAvailableJack())

        // back side - draw cables for 'U'
        // scrambler 14 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(14).outputJack, bombe.getDiagonalBoard(1).getJack('U'))


        // switch on the bank
        // TODO: add a unittest for an off bank
        bombe.getChain(1).switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChain(1).setContactToActivate('I')

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
    fun usBombeReport1944_testMenu_IVa_doubleinput() {
        // we need 2 banks for this one!
        val bombe = Bombe(26, 2, 14, 3)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.II, DrumType.IV, DrumType.III)
        for (scramblerId in 1..14) {
            operator.placeDrums(1, scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, 1, "ZAB")
        operator.setStartRingOrientations(1, 2, "ZZB")
        operator.setStartRingOrientations(1, 3, "ZZK")
        operator.setStartRingOrientations(1, 4, "ZZL")
        operator.setStartRingOrientations(1, 5, "ZZE")
        operator.setStartRingOrientations(1, 6, "ZZR")  // change compared to IV
        operator.setStartRingOrientations(1, 7, "ZZN")
        operator.setStartRingOrientations(1, 8, "ZZF")
        operator.setStartRingOrientations(1, 9, "ZZM")
        operator.setStartRingOrientations(1, 10, "ZZG")
        operator.setStartRingOrientations(1, 11, "ZAA")
        operator.setStartRingOrientations(1, 12, "ZZA")
        operator.setStartRingOrientations(1, 13, "ZZI")
        operator.setStartRingOrientations(1, 14, "ZZJ")

        // back side - place bridges
        val bridge_12 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(1).outputJack,
            bombe.getBank(1).getScrambler(2).inputJack
        )
        val bridge_23 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(2).outputJack,
            bombe.getBank(1).getScrambler(3).inputJack
        )
        val bridge_34 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(3).outputJack,
            bombe.getBank(1).getScrambler(4).inputJack
        )
        val bridge_45 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(4).outputJack,
            bombe.getBank(1).getScrambler(5).inputJack
        )
        val bridge_56 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(5).outputJack,
            bombe.getBank(1).getScrambler(6).inputJack
        )
        // no bridge between 6 and 7 , change compared to menu IV
        val bridge_78 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(7).outputJack,
            bombe.getBank(1).getScrambler(8).inputJack
        )
        val bridge_89 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(8).outputJack,
            bombe.getBank(1).getScrambler(9).inputJack
        )
        val bridge_910 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(9).outputJack,
            bombe.getBank(1).getScrambler(10).inputJack
        )
        val bridge_1011 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(10).outputJack,
            bombe.getBank(1).getScrambler(11).inputJack
        )
        val bridge_1112 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(11).outputJack,
            bombe.getBank(1).getScrambler(12).inputJack
        )
        // no bridge between 12 and 13
        // no bridge between 13 and 14

        // back side - draw cables for 'R', change compared to menu IV
        val commons_R = bombe.claimAvailableCommonsSet(1, 'R')
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('R'), commons_R.getAvailableJack())
        // scrambler 1 input to commons
        operator.drawCableBetween(bombe.getBank(1).getScrambler(1).inputJack, commons_R.getAvailableJack())
        // scrambler 6 output to commons
        operator.drawCableBetween(bombe.getBank(1).getScrambler(6).outputJack, commons_R.getAvailableJack())

        // back side - draw cables for 'O'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_12.jack, bombe.getDiagonalBoard(1).getJack('O'))

        // back side - draw cables for 'I'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_23.jack, bombe.getDiagonalBoard(1).getJack('I'))

        // back side - draw cables for 'L'
        // bridge_34 to diagonal board
        operator.drawCableBetween(bridge_34.jack, bombe.getDiagonalBoard(1).getJack('L'))

        // back side - draw cables for 'A',  change compared to menu IV
        val commons_A = bombe.claimAvailableCommonsSet(1, 'A')
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('A'), commons_A.getAvailableJack())
        // bridge_45 to commons
        operator.drawCableBetween(bridge_45.jack, commons_A.getAvailableJack())
        // scrambler 13 input to commons
        operator.drawCableBetween(bombe.getBank(1).getScrambler(13).inputJack, commons_A.getAvailableJack())
        // input 2 to commons
        operator.drawCableBetween(bombe.getChain(2).inputJack, commons_A.getAvailableJack())

        // back side - draw cables for 'K'
        // scrambler 13 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(13).outputJack, bombe.getDiagonalBoard(1).getJack('K'))

        // back side - draw cables for 'Y'
        // bridge_56 to diagonal board
        operator.drawCableBetween(bridge_56.jack, bombe.getDiagonalBoard(1).getJack('Y'))

        // back side - draw cables for 'F', change compared to menu IV
        val commons_F = bombe.claimAvailableCommonsSet(1, 'F')
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('F'), commons_F.getAvailableJack())
        // scrambler 12 output to commons
        operator.drawCableBetween(bombe.getBank(1).getScrambler(12).outputJack, commons_F.getAvailableJack())
        // scrambler 7 input to commons
        operator.drawCableBetween(bombe.getBank(1).getScrambler(7).inputJack, commons_F.getAvailableJack())

        // back side - draw cables for 'H'
        // bridge_78 to diagonal board
        operator.drawCableBetween(bridge_78.jack, bombe.getDiagonalBoard(1).getJack('H'))

        // back side - draw cables for 'T'
        // bridge_89 to diagonal board
        operator.drawCableBetween(bridge_89.jack, bombe.getDiagonalBoard(1).getJack('T'))

        // back side - draw cables for 'D'
        // bridge_910 to diagonal board
        operator.drawCableBetween(bridge_910.jack, bombe.getDiagonalBoard(1).getJack('D'))

        // back side - draw cables for 'Z'
        // bridge_1011 to diagonal board
        operator.drawCableBetween(bridge_1011.jack, bombe.getDiagonalBoard(1).getJack('Z'))

        // back side - draw cables for 'E', change compared to menu IV
        val commons_E = bombe.claimAvailableCommonsSet(1, 'E')
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('E'), commons_E.getAvailableJack())
        // bridge_1112 to commons
        operator.drawCableBetween(bridge_1112.jack, commons_E.getAvailableJack())
        // scrambler 14 input to commons
        operator.drawCableBetween(bombe.getBank(1).getScrambler(14).inputJack, commons_E.getAvailableJack())
        // input 1 to commons
        operator.drawCableBetween(bombe.getChain(1).inputJack, commons_E.getAvailableJack())

        // back side - draw cables for 'U'
        // scrambler 14 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(14).outputJack, bombe.getDiagonalBoard(1).getJack('U'))

        // switch on the used chains
        // TODO: add a unittest for an off bank
        bombe.getChain(1).switchOn()
        bombe.getChain(2).switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChain(1).setContactToActivate('R')
        bombe.getChain(2).setContactToActivate('E')

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
        val bombe = Bombe(26, 1, 14, 3)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.I, DrumType.IV, DrumType.V)
        for (scramblerId in 1..14) {
            operator.placeDrums(1, scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, 1, "YXW") // initially read 'VXW', corrected based on http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_Va.txt
        operator.setStartRingOrientations(1, 2, "OKM")
        operator.setStartRingOrientations(1, 3, "AME")
        operator.setStartRingOrientations(1, 4, "FMQ")
        operator.setStartRingOrientations(1, 5, "OKN")
        operator.setStartRingOrientations(1, 6, "OTO")
        operator.setStartRingOrientations(1, 7, "YXV")
        operator.setStartRingOrientations(1, 8, "OTP")
        operator.setStartRingOrientations(1, 9, "AMG")
        operator.setStartRingOrientations(1, 10, "OTN")
        operator.setStartRingOrientations(1, 11, "OKO")
        operator.setStartRingOrientations(1, 12, "FMR")
        operator.setStartRingOrientations(1, 13, "AMF")
        operator.setStartRingOrientations(1, 14, "YXX")

        // back side - place bridges
        val bridge_12 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(1).outputJack,
            bombe.getBank(1).getScrambler(2).inputJack
        )
        val bridge_23 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(2).outputJack,
            bombe.getBank(1).getScrambler(3).inputJack
        )
        val bridge_34 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(3).outputJack,
            bombe.getBank(1).getScrambler(4).inputJack
        )
        val bridge_45 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(4).outputJack,
            bombe.getBank(1).getScrambler(5).inputJack
        )
        val bridge_56 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(5).outputJack,
            bombe.getBank(1).getScrambler(6).inputJack
        )
        val bridge_67 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(6).outputJack,
            bombe.getBank(1).getScrambler(7).inputJack
        )
        // no bridge between scrambler 7 and 8
        val bridge_89 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(8).outputJack,
            bombe.getBank(1).getScrambler(9).inputJack
        )
        // no bridge between scrambler 9 and 10
        // no bridge between scrambler 10 and 11
        val bridge_1112 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(11).outputJack,
            bombe.getBank(1).getScrambler(12).inputJack
        )
        val bridge_1213 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(12).outputJack,
            bombe.getBank(1).getScrambler(13).inputJack
        )
        val bridge_1314 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(13).outputJack,
            bombe.getBank(1).getScrambler(14).inputJack
        )

        // back side - draw cables for 'W'
        // scrambler 1 input to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(1).inputJack, bombe.getDiagonalBoard(1).getJack('W'))

        // back side - draw cables for 'D'
        // bridge_12 to diagonal board
        operator.drawCableBetween(bridge_12.jack, bombe.getDiagonalBoard(1).getJack('D'))

        // back side - draw cables for 'Z'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_23.jack, bombe.getDiagonalBoard(1).getJack('Z'))

        // back side - draw cables for 'O'
        val commons_O = bombe.claimAvailableCommonsSet(1, 'O')
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('O'), commons_O.getAvailableJack())
        // bridge_34 to commons
        operator.drawCableBetween(bridge_34.jack, commons_O.getAvailableJack())
        // scrambler 10 input to commons
        operator.drawCableBetween(bombe.getBank(1).getScrambler(10).inputJack, commons_O.getAvailableJack())

        // back side - draw cables for 'B'
        // bridge_45 to diagonal board
        operator.drawCableBetween(bridge_45.jack, bombe.getDiagonalBoard(1).getJack('B'))

        // back side - draw cables for 'H'
        // bridge_56 to diagonal board
        operator.drawCableBetween(bridge_56.jack, bombe.getDiagonalBoard(1).getJack('H'))

        // back side - draw cables for 'K'
        val commons_K = bombe.claimAvailableCommonsSet(1, 'K')
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('K'), commons_K.getAvailableJack())
        // bridge_67 to commons
        operator.drawCableBetween(bridge_67.jack, commons_K.getAvailableJack())
        // bridge_1213 to commons
        operator.drawCableBetween(bridge_1213.jack, commons_K.getAvailableJack())
        // chain input to commons
        operator.drawCableBetween(bombe.getChain(1).inputJack, commons_K.getAvailableJack())

        // back side - draw cables for 'C'
        // bridge_1314 to diagonal board
        operator.drawCableBetween(bridge_1314.jack, bombe.getDiagonalBoard(1).getJack('C'))

        // back side - draw cables for 'N'
        // bridge_1112 to diagonal board
        operator.drawCableBetween(bridge_1112.jack, bombe.getDiagonalBoard(1).getJack('N'))

        // back side - draw cables for 'L'
        // bridge_89 to diagonal board
        operator.drawCableBetween(bridge_89.jack, bombe.getDiagonalBoard(1).getJack('L'))

        // back side - draw cables for 'G'
        // scrambler 8 input to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(8).inputJack, bombe.getDiagonalBoard(1).getJack('G'))

        // back side - draw cables for 'A'
        // scrambler 11 input to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(11).inputJack, bombe.getDiagonalBoard(1).getJack('A'))

        // back side - draw cables for 'J'
        // scrambler 10 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(10).outputJack, bombe.getDiagonalBoard(1).getJack('J'))

        // back side - draw cables for 'U'
        // scrambler 9 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(9).outputJack, bombe.getDiagonalBoard(1).getJack('U'))

        // back side - draw cables for 'E'
        // scrambler 7 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(7).outputJack, bombe.getDiagonalBoard(1).getJack('E'))

        // back side - draw cables for 'X'
        // scrambler 14 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(14).outputJack, bombe.getDiagonalBoard(1).getJack('X'))

        // switch on the used chains
        // TODO: add a unittest for an off bank
        bombe.getChain(1).switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChain(1).setContactToActivate('Z')

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
        val bombe = Bombe(26, 1, 14, 3)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.I, DrumType.IV, DrumType.V)
        for (scramblerId in 1..14) {
            operator.placeDrums(1, scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, 1, "YXW") // initially read 'VXW', corrected based on http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_Va.txt
        operator.setStartRingOrientations(1, 2, "OKM")
        operator.setStartRingOrientations(1, 3, "AME")
        operator.setStartRingOrientations(1, 4, "FMQ")
        operator.setStartRingOrientations(1, 5, "OKN")
        operator.setStartRingOrientations(1, 6, "OTO")
        operator.setStartRingOrientations(1, 7, "YXV")
        operator.setStartRingOrientations(1, 8, "OTP")
        operator.setStartRingOrientations(1, 9, "AMG")
        operator.setStartRingOrientations(1, 10, "OLN") // OTN in other variant
        operator.setStartRingOrientations(1, 11, "OKO")
        operator.setStartRingOrientations(1, 12, "FMR")
        operator.setStartRingOrientations(1, 13, "AMF")
        operator.setStartRingOrientations(1, 14, "YXV") // YXX in other variant

        // back side - place bridges
        val bridge_12 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(1).outputJack,
            bombe.getBank(1).getScrambler(2).inputJack
        )
        val bridge_23 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(2).outputJack,
            bombe.getBank(1).getScrambler(3).inputJack
        )
        val bridge_34 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(3).outputJack,
            bombe.getBank(1).getScrambler(4).inputJack
        )
        val bridge_45 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(4).outputJack,
            bombe.getBank(1).getScrambler(5).inputJack
        )
        val bridge_56 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(5).outputJack,
            bombe.getBank(1).getScrambler(6).inputJack
        )
        val bridge_67 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(6).outputJack,
            bombe.getBank(1).getScrambler(7).inputJack
        )
        // no bridge between scrambler 7 and 8
        val bridge_89 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(8).outputJack,
            bombe.getBank(1).getScrambler(9).inputJack
        )
        // no bridge between scrambler 9 and 10
        // no bridge between scrambler 10 and 11
        val bridge_1112 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(11).outputJack,
            bombe.getBank(1).getScrambler(12).inputJack
        )
        val bridge_1213 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(12).outputJack,
            bombe.getBank(1).getScrambler(13).inputJack
        )
        val bridge_1314 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(13).outputJack,
            bombe.getBank(1).getScrambler(14).inputJack
        )

        // back side - draw cables for 'W'
        // scrambler 1 input to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(1).inputJack, bombe.getDiagonalBoard(1).getJack('W'))

        // back side - draw cables for 'D'
        // bridge_12 to diagonal board
        operator.drawCableBetween(bridge_12.jack, bombe.getDiagonalBoard(1).getJack('D'))

        // back side - draw cables for 'Z'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_23.jack, bombe.getDiagonalBoard(1).getJack('Z'))

        // back side - draw cables for 'O'
        val commons_O = bombe.claimAvailableCommonsSet(1, 'O')
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('O'), commons_O.getAvailableJack())
        // bridge_34 to commons
        operator.drawCableBetween(bridge_34.jack, commons_O.getAvailableJack())
        // scrambler 10 input to commons
        operator.drawCableBetween(bombe.getBank(1).getScrambler(10).inputJack, commons_O.getAvailableJack())

        // back side - draw cables for 'B'
        // bridge_45 to diagonal board
        operator.drawCableBetween(bridge_45.jack, bombe.getDiagonalBoard(1).getJack('B'))

        // back side - draw cables for 'H'
        // bridge_56 to diagonal board
        operator.drawCableBetween(bridge_56.jack, bombe.getDiagonalBoard(1).getJack('H'))

        // back side - draw cables for 'K'
        val commons_K = bombe.claimAvailableCommonsSet(1, 'K')
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('K'), commons_K.getAvailableJack())
        // bridge_67 to commons
        operator.drawCableBetween(bridge_67.jack, commons_K.getAvailableJack())
        // bridge_1213 to commons
        operator.drawCableBetween(bridge_1213.jack, commons_K.getAvailableJack())
        // chain input to commons
        operator.drawCableBetween(bombe.getChain(1).inputJack, commons_K.getAvailableJack())

        // back side - draw cables for 'C'
        // bridge_1314 to diagonal board
        operator.drawCableBetween(bridge_1314.jack, bombe.getDiagonalBoard(1).getJack('C'))

        // back side - draw cables for 'N'
        // bridge_1212 to diagonal board
        operator.drawCableBetween(bridge_1112.jack, bombe.getDiagonalBoard(1).getJack('N'))

        // back side - draw cables for 'L'
        // bridge_89 to diagonal board
        operator.drawCableBetween(bridge_89.jack, bombe.getDiagonalBoard(1).getJack('L'))

        // back side - draw cables for 'G'
        // scrambler 8 input to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(8).inputJack, bombe.getDiagonalBoard(1).getJack('G'))

        // back side - draw cables for 'A'
        // scrambler 11 input to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(11).inputJack, bombe.getDiagonalBoard(1).getJack('A'))

        // back side - draw cables for 'J'
        // scrambler 10 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(10).outputJack, bombe.getDiagonalBoard(1).getJack('J'))

        // back side - draw cables for 'U'
        // scrambler 9 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(9).outputJack, bombe.getDiagonalBoard(1).getJack('U'))

        // back side - draw cables for 'E'
        // scrambler 7 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(7).outputJack, bombe.getDiagonalBoard(1).getJack('E'))

        // back side - draw cables for 'X'
        // scrambler 14 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(14).outputJack, bombe.getDiagonalBoard(1).getJack('X'))

        // switch on the used chains
        // TODO: add a unittest for an off bank
        bombe.getChain(1).switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChain(1).setContactToActivate('Z')

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
        val bombe = Bombe(26, 2, 14, 3)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.I, DrumType.II, DrumType.III)
        for (scramblerId in 1..14) {
            operator.placeDrums(1, scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, 1, "ZZA")
        operator.setStartRingOrientations(1, 2, "ZZD")
        operator.setStartRingOrientations(1, 3, "ZAD")
        operator.setStartRingOrientations(1, 4, "ZAI")
        operator.setStartRingOrientations(1, 5, "ZZB")
        operator.setStartRingOrientations(1, 6, "ZZO")
        operator.setStartRingOrientations(1, 7, "ZZK")
        operator.setStartRingOrientations(1, 8, "ZAF")
        operator.setStartRingOrientations(1, 9, "ZAD")
        operator.setStartRingOrientations(1, 10, "ZZR")
        operator.setStartRingOrientations(1, 11, "ZAJ")
        operator.setStartRingOrientations(1, 12, "ZAE")
        operator.setStartRingOrientations(1, 13, "ZAY")
        operator.setStartRingOrientations(1, 14, "ZAK")

        // back side - place bridges
        val bridge_12 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(1).outputJack,
            bombe.getBank(1).getScrambler(2).inputJack
        )
        val bridge_23 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(2).outputJack,
            bombe.getBank(1).getScrambler(3).inputJack
        )
        val bridge_34 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(3).outputJack,
            bombe.getBank(1).getScrambler(4).inputJack
        )
        val bridge_45 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(4).outputJack,
            bombe.getBank(1).getScrambler(5).inputJack
        )
        val bridge_56 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(5).outputJack,
            bombe.getBank(1).getScrambler(6).inputJack
        )
        val bridge_67 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(6).outputJack,
            bombe.getBank(1).getScrambler(7).inputJack
        )
        val bridge_78 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(7).outputJack,
            bombe.getBank(1).getScrambler(8).inputJack
        )
        // no bridge between scrambler 8 and 9
        val bridge_910 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(9).outputJack,
            bombe.getBank(1).getScrambler(10).inputJack
        )
        val bridge_1011 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(10).outputJack,
            bombe.getBank(1).getScrambler(11).inputJack
        )
        val bridge_1112 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(11).outputJack,
            bombe.getBank(1).getScrambler(12).inputJack
        )
        val bridge_1213 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(12).outputJack,
            bombe.getBank(1).getScrambler(13).inputJack
        )
        val bridge_1314 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(13).outputJack,
            bombe.getBank(1).getScrambler(14).inputJack
        )

        // back side - draw cables for 'A'
        // scrambler 1 input to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(1).inputJack, bombe.getDiagonalBoard(1).getJack('A'))

        // back side - draw cables for 'I'
        // bridge_12 to diagonal board
        operator.drawCableBetween(bridge_12.jack, bombe.getDiagonalBoard(1).getJack('I'))

        // back side - draw cables for 'H'
        // bridge_23 to diagonal board
        operator.drawCableBetween(bridge_23.jack, bombe.getDiagonalBoard(1).getJack('H'))

        // back side - draw cables for 'F'
        // bridge_34 to diagonal board
        operator.drawCableBetween(bridge_34.jack, bombe.getDiagonalBoard(1).getJack('F'))

        // back side - draw cables for 'Y'
        val commons_Y = bombe.claimAvailableCommonsSet(1, 'Y')
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('Y'), commons_Y.getAvailableJack())
        // bridge_45 to commons
        operator.drawCableBetween(bridge_45.jack, commons_Y.getAvailableJack())
        // chain 1 input to commons
        operator.drawCableBetween(bombe.getChain(1).inputJack, commons_Y.getAvailableJack())

        // back side - draw cables for 'K'
        // bridge_56 to diagonal board
        operator.drawCableBetween(bridge_56.jack, bombe.getDiagonalBoard(1).getJack('K'))

        // back side - draw cables for 'D'
        // bridge_67 to diagonal board
        operator.drawCableBetween(bridge_67.jack, bombe.getDiagonalBoard(1).getJack('D'))

        // back side - draw cables for 'L'
        // bridge_78 to diagonal board
        operator.drawCableBetween(bridge_78.jack, bombe.getDiagonalBoard(1).getJack('L'))

        // back side - draw cables for 'Q'  // initially read this as a 'G
        // scrambler 8 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(8).outputJack, bombe.getDiagonalBoard(1).getJack('Q'))

        // back side - draw cables for 'T'
        // scrambler 9 input to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(9).inputJack, bombe.getDiagonalBoard(1).getJack('T'))

        // back side - draw cables for 'W'
        // bridge_910 to diagonal board
        operator.drawCableBetween(bridge_910.jack, bombe.getDiagonalBoard(1).getJack('W'))

        // back side - draw cables for 'U'
        // bridge_1011 to diagonal board
        operator.drawCableBetween(bridge_1011.jack, bombe.getDiagonalBoard(1).getJack('U'))

        // back side - draw cables for 'O'
        val commons_O = bombe.claimAvailableCommonsSet(1, 'O')
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('O'), commons_O.getAvailableJack())
        // bridge_1112 to commons
        operator.drawCableBetween(bridge_1112.jack, commons_O.getAvailableJack())
        // chain 1I input to commons
        operator.drawCableBetween(bombe.getChain(2).inputJack, commons_O.getAvailableJack())

        // back side - draw cables for 'S'
        // bridge_1213 to diagonal board
        operator.drawCableBetween(bridge_1213.jack, bombe.getDiagonalBoard(1).getJack('S'))

        // back side - draw cables for 'P'
        // bridge_1314 to diagonal board
        operator.drawCableBetween(bridge_1314.jack, bombe.getDiagonalBoard(1).getJack('P'))

        // back side - draw cables for 'C'
        // scrambler 14 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(14).outputJack, bombe.getDiagonalBoard(1).getJack('C'))

        // switch on the used chains
        // TODO: add a unittest for an off bank
        bombe.getChain(1).switchOn()
        bombe.getChain(2).switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChain(1).setContactToActivate('O')
        bombe.getChain(2).setContactToActivate('Y')

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
        val bombe = Bombe(26, 1, 27, 3)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        // front side - place drums
        val drumTypes = listOf(DrumType.V, DrumType.IV, DrumType.III)
        for (scramblerId in 1..27) {
            operator.placeDrums(1, scramblerId, drumTypes)
        }

        // front side - set start orientation of drums
        operator.setStartRingOrientations(1, 1, "AGP")
        operator.setStartRingOrientations(1, 2, "ENF")
        operator.setStartRingOrientations(1, 3, "EMI")
        operator.setStartRingOrientations(1, 4, "ENH")
        operator.setStartRingOrientations(1, 5, "EMK")
        operator.setStartRingOrientations(1, 6, "ENL")
        operator.setStartRingOrientations(1, 7, "EMO")
        operator.setStartRingOrientations(1, 8, "ENK")
        operator.setStartRingOrientations(1, 9, "EMN")
        operator.setStartRingOrientations(1, 10, "ENM")
        operator.setStartRingOrientations(1, 11, "EMP")
        operator.setStartRingOrientations(1, 12, "CIQ")
        operator.setStartRingOrientations(1, 13, "EMT")
        operator.setStartRingOrientations(1, 14, "ENQ")
        operator.setStartRingOrientations(1, 15, "ENN")
        operator.setStartRingOrientations(1, 16, "EMQ")
        operator.setStartRingOrientations(1, 17, "ENG")
        operator.setStartRingOrientations(1, 18, "EMJ")
        operator.setStartRingOrientations(1, 19, "ENR")
        operator.setStartRingOrientations(1, 20, "EMU")
        operator.setStartRingOrientations(1, 21, "CIR")
        operator.setStartRingOrientations(1, 22, "EML")
        operator.setStartRingOrientations(1, 23, "ENI")
        operator.setStartRingOrientations(1, 24, "EMR")
        operator.setStartRingOrientations(1, 25, "ENO")
        operator.setStartRingOrientations(1, 26, "CIP")
        operator.setStartRingOrientations(1, 27, "AGN")

        // back side - place bridges
        val bridge_12 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(1).outputJack,
            bombe.getBank(1).getScrambler(2).inputJack
        )
        // bridge 2 to 3 represents a ?
        operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(2).outputJack,
            bombe.getBank(1).getScrambler(3).inputJack
        )
        val bridge_34 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(3).outputJack,
            bombe.getBank(1).getScrambler(4).inputJack
        )
        // bridge 4 to 5 represents a ?
        operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(4).outputJack,
            bombe.getBank(1).getScrambler(5).inputJack
        )
        val bridge_56 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(5).outputJack,
            bombe.getBank(1).getScrambler(6).inputJack
        )
        // bridge 6 to 7 represents a ?
        operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(6).outputJack,
            bombe.getBank(1).getScrambler(7).inputJack
        )
        val bridge_78 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(7).outputJack,
            bombe.getBank(1).getScrambler(8).inputJack
        )
        // bridge 8 to 9 represents a ?
        operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(8).outputJack,
            bombe.getBank(1).getScrambler(9).inputJack
        )
        // no bridge between scrambler 9 and 10
        // bridge 10 to 11 represents a ?
        operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(10).outputJack,
            bombe.getBank(1).getScrambler(11).inputJack
        )
        val bridge_1112 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(11).outputJack,
            bombe.getBank(1).getScrambler(12).inputJack
        )
        // no bridge between 12 and 13
        // bridge 13 to 14 represents a ?
        operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(13).outputJack,
            bombe.getBank(1).getScrambler(14).inputJack
        )
        val bridge_1415 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(14).outputJack,
            bombe.getBank(1).getScrambler(15).inputJack
        )
        // bridge 15 to 16 represents a ?
        operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(15).outputJack,
            bombe.getBank(1).getScrambler(16).inputJack
        )
        val bridge_1617 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(16).outputJack,
            bombe.getBank(1).getScrambler(17).inputJack
        )
        // bridge 17 to 18 represents a ?
        operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(17).outputJack,
            bombe.getBank(1).getScrambler(18).inputJack
        )
        // no bridge between 18 and 19
        // bridge 19 to 20 represents a ?
        operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(19).outputJack,
            bombe.getBank(1).getScrambler(20).inputJack
        )
        val bridge_2021 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(20).outputJack,
            bombe.getBank(1).getScrambler(21).inputJack
        )
        val bridge_2122 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(21).outputJack,
            bombe.getBank(1).getScrambler(22).inputJack
        )
        // bridge 22 to 23 represents a ?
        operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(22).outputJack,
            bombe.getBank(1).getScrambler(23).inputJack
        )
        val bridge_2324 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(23).outputJack,
            bombe.getBank(1).getScrambler(24).inputJack
        )
        // bridge 24 to 25 represents a ?
        operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(24).outputJack,
            bombe.getBank(1).getScrambler(25).inputJack
        )
        // no bridge between 25 en 26
        val bridge_2627 = operator.attachBridgeTo(
            bombe.getBank(1).getScrambler(26).outputJack,
            bombe.getBank(1).getScrambler(27).inputJack
        )

        // back side - draw cables for 'Q'
        // scrambler 1 input to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(1).inputJack, bombe.getDiagonalBoard(1).getJack('Q'))

        // back side - draw cables for 'N'
        // bridge_12 to diagonal board
        operator.drawCableBetween(bridge_12.jack, bombe.getDiagonalBoard(1).getJack('N'))

        // back side - draw cables for 'O'
        // bridge_34 to diagonal board
        operator.drawCableBetween(bridge_34.jack, bombe.getDiagonalBoard(1).getJack('O'))

        // back side - draw cables for 'K'
        // bridge_56 to diagonal board
        operator.drawCableBetween(bridge_56.jack, bombe.getDiagonalBoard(1).getJack('K'))

        // back side - draw cables for 'V'
        val commons_V = bombe.claimAvailableCommonsSet(1, 'V')
        // diagonal board to commons
        operator.drawCableBetween(bombe.getDiagonalBoard(1).getJack('V'), commons_V.getAvailableJack())
        // bridge_78 to commons
        operator.drawCableBetween(bridge_78.jack, commons_V.getAvailableJack())
        // scrambler 10 input to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(10).inputJack, commons_V.getAvailableJack())
        // scrambler 13 input to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(13).inputJack, commons_V.getAvailableJack())
        // chain 1 input to commons
        operator.drawCableBetween(bombe.getChain(1).inputJack, commons_V.getAvailableJack())

        // back side - draw cables for 'A'
        // scrambler 9 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(9).outputJack, bombe.getDiagonalBoard(1).getJack('A'))

        // back side - draw cables for 'J'
        // bridge_1112 to diagonal board
        operator.drawCableBetween(bridge_1112.jack, bombe.getDiagonalBoard(1).getJack('J'))

        // back side - draw cables for 'I'
        // scrambler 12 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(12).outputJack, bombe.getDiagonalBoard(1).getJack('I'))

        // back side - draw cables for 'Z'
        // bridge_1415 to diagonal board
        operator.drawCableBetween(bridge_1415.jack, bombe.getDiagonalBoard(1).getJack('Z'))

        // back side - draw cables for 'Y'
        // bridge_1617 to diagonal board
        operator.drawCableBetween(bridge_1617.jack, bombe.getDiagonalBoard(1).getJack('Y'))

        // back side - draw cables for 'M'
        // scrambler 18 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(18).outputJack, bombe.getDiagonalBoard(1).getJack('M'))

        // back side - draw cables for 'W'
        // scrambler 19 input to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(19).inputJack, bombe.getDiagonalBoard(1).getJack('W'))

        // back side - draw cables for 'P'
        val commons_P = bombe.claimAvailableCommonsSet(1, 'P')
        // bridge_2021 to diagonal board
        operator.drawCableBetween(bridge_2021.jack, commons_P.getAvailableJack())
        // bridge_2324 to diagonal board
        operator.drawCableBetween(bridge_2324.jack, commons_P.getAvailableJack())

        // back side - draw cables for 'S'
        // bridge_2122 to diagonal board
        operator.drawCableBetween(bridge_2122.jack, bombe.getDiagonalBoard(1).getJack('S'))

        // back side - draw cables for 'E'
        // scrambler 25 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(25).outputJack, bombe.getDiagonalBoard(1).getJack('E'))

        // back side - draw cables for 'D'
        // scrambler 26 input to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(26).inputJack, bombe.getDiagonalBoard(1).getJack('D'))

        // back side - draw cables for 'C'
        // bridge_2627 to diagonal board
        operator.drawCableBetween(bridge_2627.jack, bombe.getDiagonalBoard(1).getJack('C'))

        // back side - draw cables for 'B'
        // scrambler 27 output to diagonal board
        operator.drawCableBetween(bombe.getBank(1).getScrambler(27).outputJack, bombe.getDiagonalBoard(1).getJack('B'))

        // switch on the used chains
        // TODO: add a unittest for an off bank
        bombe.getChain(1).switchOn()

        // put the current on the correct wire
        // TODO: add a unittest for a bank without an active contact
        bombe.getChain(1).setContactToActivate('A')

        // count scramblers which don't have both input and output plugged up
        val incompleteScramblers = bombe.getBank(1).getScramblers().filter { it.inputJack.pluggedUpBy() == null || it.outputJack.pluggedUpBy() == null }.count()
        println("$incompleteScramblers scramblers without both jacks plugged up")

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