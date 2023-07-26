package bombe.operators

import bombe.Bombe
import bombe.BombeJobInstructions
import bombe.components.DrumType
import enigma.components.ReflectorType
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
    // This test depends on DrumType.coreOffset because we need to accommodate for this mistake:
    // (quoted from page 2 of the PDF)
    // "Probably by mistake, drums I, II, III, VI, VII and VIII on the Bombe
    //   are one letter ahead of the corresponding Enigma rotors.
    //  Drum IV is two steps ahead, and rotor V is three steps ahead."
    fun linkopingBombeSimulatorExample() {
        val instructions = BombeJobInstructions(
            // the menu
            listOf("U-11-E-5-G-6-R-14-A-13-S-7-V-16-E-2-N", "H-10-Z-9-R-12-G-15-L"),
            'G',
            'A',
            // left, middle, right rotor
            listOf(listOf(DrumType.II, DrumType.V, DrumType.III)),
            ReflectorType.B
        )
        val bombe = Bombe(instructions.deriveBombeConstructionParameters())
        val operator = ExpertBombeOperator(bombe)
        val stops = operator.executeJob(instructions)

        assertTrue(stops.size > 1)
        assertEquals("D", stops[0].getPotentialSteckerPartnersString())
        assertEquals(
            "SNY",
            listOf(
                stops[0].rotor1RingStellung,
                stops[0].rotor2RingStellung,
                stops[0].rotor3RingStellung
            ).joinToString("")
        )

        assertEquals("Q", stops[1].getPotentialSteckerPartnersString())
        assertEquals(
            "DKX",
            listOf(
                stops[1].rotor1RingStellung,
                stops[1].rotor2RingStellung,
                stops[1].rotor3RingStellung
            ).joinToString("")
        )
    }

    /**
     US 6812 Bombe Report 1944 - chapter 3
     test menu I
     https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
     http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_I.txt

           5,6
       E=========U        1 ZZS
       \\      ///        2 ZZZ
        \\    ///         3 ZAX
     4,7 \\  /// 1,2,3    4 ZAS
          \\///           5 ZAY
            N             6 ZZW
                          7 ZAV
    */
    @Test
    fun usBombeReport1944_testMenu_I() {
        val instructions = BombeJobInstructions(
            // the menu
            //         1     2     3     4     5     6     7
            listOf("W-ZZS-N-ZZZ-W-ZAX-N-ZAS-E-ZAY-W-ZZW-E-ZAV-N"),
            'E',
            'A',
            // left, middle, right rotor
            listOf(listOf(DrumType.III, DrumType.II, DrumType.IV)),
            ReflectorType.B,
        )

        val bombe = Bombe(instructions.deriveBombeConstructionParameters())
        val operator = ExpertBombeOperator(bombe)
        val stops = operator.executeJob(instructions)

//        println("test menu I stops (${stops.size}):")
//        stops.forEach { it.print() }

        assertEquals(1, stops.size, "expected 1 stop")
        assertEquals('B', stops.get(0).rotor1RingStellung)
        assertEquals('U', stops.get(0).rotor2RingStellung)
        assertEquals('O', stops.get(0).rotor3RingStellung)
        assertEquals("L", stops.get(0).getPotentialSteckerPartnersString())
    }

    /** US 6812 Bombe Report 1944 - chapter 3
        test menu II
        https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
        http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_II.txt

          1,9    2   3   7,8       1  EKR
        U======E---Y---H====T      2  RTN
            10 |     4/ \ 6        3  SAO
               X     F---B         4  EKP
                       5           5  RTI
                                   6  RTQ
                                   7  SAT
                                   8  SAS
                                   9  RTR
                                  10  RTJ
    */
    @Test
    fun usBombeReport1944_testMenu_II() {
        val instructions = BombeJobInstructions(
            // the menu
            //         1     2     3     4     5     6     7     8          9     10
            listOf("U-EKR-E-RTN-Y-SAO-H-EKP-F-RTI-B-RTQ-H-SAT-T-SAS-H", "U-RTR-E-RTJ-X"),
            'H',
            'E',
            // left, middle, right rotor
            listOf(listOf(DrumType.IV, DrumType.III, DrumType.II)),
            ReflectorType.B,
        )

        val bombe = Bombe(instructions.deriveBombeConstructionParameters())
        val operator = ExpertBombeOperator(bombe)
        val stops = operator.executeJob(instructions)

//        println("test menu II stops (${stops.size}):")
//        stops.forEach { it.print() }

        assertEquals(4, stops.size, "expected 4 stops")
        assertEquals('F', stops.get(0).rotor1RingStellung)
        assertEquals('J', stops.get(0).rotor2RingStellung)
        assertEquals('W', stops.get(0).rotor3RingStellung)
        assertEquals("F", stops.get(0).getPotentialSteckerPartnersString())
    }

    /**
     US 6812 Bombe Report 1944 - chapter 3
     test menu III
     https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
     http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_III.txt

            3
         Y-----S       1 ZAP
         |     |  T    2 ZZJ
        4|    2| /1    3 ZAJ
         | 10  |/      4 ZZP
         F-----E\8     5 ZZN
         |     | \     6 ZAN
        5|    9| /R    7 ZZK
         |  6  |/7     8 ZAQ
         G-----C       9 ZZO
                      10 ZZR
    */
    @Test
    fun usBombeReport1944_testMenu_III() {
        val instructions = BombeJobInstructions(
            // the menu
            //         1     2     3     4     5     6     7     8     9          10
            listOf("T-ZAP-E-ZZJ-S-ZAJ-Y-ZZP-F-ZZN-G-ZAN-C-ZZK-R-ZAQ-E-ZZO-C", "E-ZZR-F"),
            'F',
            'A',
            // left, middle, right rotor
            listOf(listOf(DrumType.I, DrumType.V, DrumType.III)),
            ReflectorType.B,
        )

        val bombe = Bombe(instructions.deriveBombeConstructionParameters())
        val operator = ExpertBombeOperator(bombe)
        val stops = operator.executeJob(instructions)
//        println("test menu III stops (${stops.size}):")
//        stops.forEach { it.print() }

        assertEquals(6, stops.size, "expected 6 stops")
        assertEquals('A', stops.get(0).rotor1RingStellung)
        assertEquals('F', stops.get(0).rotor2RingStellung)
        assertEquals('Y', stops.get(0).rotor3RingStellung)
        assertEquals("K", stops.get(0).getPotentialSteckerPartnersString())
    }

    /**
     US 6812 Bombe Report 1944 - chapter 3
     test menu IV
     https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
     http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_IV.txt

               7   8
     Input-->F---H---T         1 ZAB
             |\      |         2 ZZB
            6| \12   |9        3 ZZK
             |  \    |         4 ZZL
             Y   E   D         5 ZZE
             |   |\11|         6 ZZH
            5| 14| \ |10       7 ZZN
          13 |   |  \|         8 ZZF
         K---A   U   Z         9 ZZM
             |                10 ZZG
            4|                11 ZAA
             |                12 ZZA
             L                13 ZZI
             |                14 ZZJ
            3|
             | 2   1
             I---O---R
    */
    @Test
    fun usBombeReport1944_testMenu_IV() {
        val instructions = BombeJobInstructions(
            // the menu
            //         1     2     3     4     5     6     7     8     9     10    11    12
            listOf("R-ZAB-O-ZZB-I-ZZK-L-ZZL-A-ZZE-Y-ZZH-F-ZZN-H-ZZF-T-ZZM-D-ZZG-Z-ZAA-E-ZZA-F",
            //      13         14
                "A-ZZI-K", "E-ZZJ-U"),
            'F',
            'I',
            // left, middle, right rotor
            listOf(listOf(DrumType.II, DrumType.IV, DrumType.III)),
            ReflectorType.B,
        )

        val bombe = Bombe(instructions.deriveBombeConstructionParameters())
        val operator = ExpertBombeOperator(bombe)
        val stops = operator.executeJob(instructions)
//        println("test menu IV stops (${stops.size}):")
//        stops.forEach { it.print() }

        assertEquals(2, stops.size, "expected 2 stops")
        assertEquals('D', stops.get(0).rotor1RingStellung)
        assertEquals('G', stops.get(0).rotor2RingStellung)
        assertEquals('T', stops.get(0).rotor3RingStellung)
        assertEquals("I", stops.get(0).getPotentialSteckerPartnersString())
    }

    /**
    US 6812 Bombe Report 1944 - chapter 3
    test menu IV
    https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
    http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_IV.txt

               7   8
             F---H---T         1 ZAB
             |       |         2 ZZB
           12|       |9        3 ZZK
             | 11  10|         4 ZZL
    input I->E---Z---D         5 ZZE
             |                 6 ZZR
           14|                 7 ZZN
             |                 8 ZZF
             U                 9 ZZM

               6   1
             Y---R---O        10 ZZG
            5|       |        11 ZAA
             | 4   3 |2       12 ZZA
    inputII->A---L---I        13 ZZI
             |                14 ZZJ
           13|
             |
             K
     */
    @Test
    fun usBombeReport1944_testMenu_IVa() {
        val instructions = BombeJobInstructions(
            // the menu
            //         1     2     3     4     5     6
            listOf("R-ZAB-O-ZZB-I-ZZK-L-ZZL-A-ZZE-Y-ZZR-R",
                //  7     8     9     10    11    12
                "F-ZZN-H-ZZF-T-ZZM-D-ZZG-Z-ZAA-E-ZZA-F",
                //  13         14
                "A-ZZI-K", "E-ZZJ-U"),
            listOf('E', 'A'),
            // left, middle, right rotor
            listOf(DrumType.II, DrumType.IV, DrumType.III),
            ReflectorType.B,
        )

        val bombe = Bombe(instructions.deriveBombeConstructionParameters())
        val operator = ExpertBombeOperator(bombe)
        val stops = operator.executeJob(instructions)
//        println("test menu IVa stops (${stops.size}):")
//        stops.forEach { it.print() }

        assertEquals(6, stops.size, "expected 6 stops")
        assertEquals('B', stops.get(0).rotor1RingStellung)
        assertEquals('S', stops.get(0).rotor2RingStellung)
        assertEquals('T', stops.get(0).rotor3RingStellung)
        assertEquals("W", stops.get(0).getPotentialSteckerPartnersString())
    }

    /**
     US 6812 Bombe Report 1944 - chapter 3
     test menu V - first variant
     https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
     http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_V.txt

         7   6   5   4   3   2   1
       E---K---H---B---O---Z---D---W
          / \          |
       12/   \13     10|
        /     \        |
       N       C       J
       |       |                 8   9
     11|       |14             G---L---U
       |       |
       A       X

     1 YXW       8 OTP
     2 OKM       9 AMG
     3 AME      10 OTN
     4 FMQ      11 OKO
     5 OKN      12 FMR
     6 OTO      13 AMF
     7 YXV      14 YXX
     */
     @Test
    fun usBombeReport1944_testMenu_V_1() {
        val instructions = BombeJobInstructions(
            // the menu
            //         1     2     3     4     5     6     7
            listOf("W-YXW-D-OKM-Z-AME-O-FMQ-B-OKN-H-OTO-K-YXV-E",
                //  8     9         10          11    12    13    14
                "G-OTP-L-AMG-U", "O-OTN-J", "A-OKO-N-FMR-K-AMF-C-YXX-X"),
            'K',
            'A',
            // left, middle, right rotor
            listOf(listOf(DrumType.I, DrumType.IV, DrumType.V)),
            ReflectorType.B,
        )

        val bombe = Bombe(instructions.deriveBombeConstructionParameters())
        val operator = ExpertBombeOperator(bombe)
        val stops = operator.executeJob(instructions)

//        println("test menu V_1 stops (${stops.size}):")
//        stops.forEach { it.print() }

        assertEquals(20, stops.size, "expected 20 stops")
        assertEquals('D', stops.get(0).rotor1RingStellung)
        assertEquals('E', stops.get(0).rotor2RingStellung)
        assertEquals('Z', stops.get(0).rotor3RingStellung)
        assertEquals("M", stops.get(0).getPotentialSteckerPartnersString())
    }

    /**
    US 6812 Bombe Report 1944 - chapter 3
    test menu V - first variant
    https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
    http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_V.txt

        7   6   5   4   3   2   1
      E---K---H---B---O---Z---D---W
         / \          |
      12/   \13     10|
       /     \        |
      N       C       J
      |       |                 8   9
    11|       |14             G---L---U
      |       |
      A       X

    1 YXW       8 OTP
    2 OKM       9 AMG
    3 AME      10 OLN (OTN in V_1)
    4 FMQ      11 OKO
    5 OKN      12 FMR
    6 OTO      13 AMF
    7 YXV      14 YXV (YXX in V_1)
     */
    @Test
    fun usBombeReport1944_testMenu_V_2() {
        val instructions = BombeJobInstructions(
            // the menu
            //         1     2     3     4     5     6     7
            listOf("W-YXW-D-OKM-Z-AME-O-FMQ-B-OKN-H-OTO-K-YXV-E",
                //  8     9         10          11    12    13    14
                "G-OTP-L-AMG-U", "O-OLN-J", "A-OKO-N-FMR-K-AMF-C-YXV-X"),
            'K',
            'A',
            // left, middle, right rotor
            listOf(listOf(DrumType.I, DrumType.IV, DrumType.V)),
            ReflectorType.B,
        )

        val bombe = Bombe(instructions.deriveBombeConstructionParameters())
        val operator = ExpertBombeOperator(bombe)
        val stops = operator.executeJob(instructions)

//        println("test menu V-2 stops (${stops.size}):")
//        stops.forEach { it.print() }

        assertEquals(18, stops.size, "expected 18 stops")
        assertEquals('V', stops.get(0).rotor1RingStellung)
        assertEquals('C', stops.get(0).rotor2RingStellung)
        assertEquals('U', stops.get(0).rotor3RingStellung)
        assertEquals("Y", stops.get(0).getPotentialSteckerPartnersString())
    }

    /**
     US 6812 Bombe Report 1944 - chapter 3
     test menu VI
     https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
     http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_VI.txt

                       Input I
       1   2   3   4  /
     A---I---H---F---Y
                     |
                     |5
           8   7   6 |
         Q---L---D---K

                       Input II
           9  10   11 /
         T---W---U---O
                     |
                     |12
               14 13 |
             C---P---S

     1 ZZA        8 ZAF
     2 ZZD        9 ZAD
     3 ZAD       10 ZZR
     4 ZAI       11 ZAJ
     5 ZZB       12 ZAE
     6 ZZO       13 ZAY
     7 ZZK       14 ZAK
     */
    @Test
    fun usBombeReport1944_testMenu_VI() {
        val instructions = BombeJobInstructions(
            // the menu
            //         1     2     3     4     5     6     7     8
            listOf("A-ZZA-I-ZZD-H-ZAD-F-ZAI-Y-ZZB-K-ZZO-D-ZZK-L-ZAF-Q",
                //  9     10    11    12    13    14
                "T-ZAD-W-ZZR-U-ZAJ-O-ZAE-S-ZAY-P-ZAK-C"),
            listOf('Y', 'O'),
            // left, middle, right rotor
            listOf(DrumType.I, DrumType.II, DrumType.III),
            ReflectorType.B,
        )

        val bombe = Bombe(instructions.deriveBombeConstructionParameters())
        val operator = ExpertBombeOperator(bombe)
        val stops = operator.executeJob(instructions)

//        println("test menu VI stops (${stops.size}):")
//        stops.forEach { it.print() }

        assertEquals(44, stops.size, "expected 44 stops")
        assertEquals('E', stops.get(0).rotor1RingStellung)
        assertEquals('V', stops.get(0).rotor2RingStellung)
        assertEquals('Z', stops.get(0).rotor3RingStellung)
        assertEquals("E", stops.get(0).getPotentialSteckerPartnersString())
    }

        /**
     US 6812 Bombe Report 1944 - chapter 3
     test menu VII
     https://www.codesandciphers.org.uk/documents/bmbrpt/index.htm
     http://www.jfbouch.fr/crypto/enigma/bombe/us6812/test_VII.txt

       1   2   3   4   5   6   7   8
     Q---N---?---O---?---K---?---V---?
                                /|   |
                             13/ |10 |9
           18  17  16  15  14 /  |   |
         M---?---Y---?---Z---?   ?   A
                                 |11
      26   27                    | 12
     D---C---B                   J---I

       19  20  24
     W---?---P---?
            /|   |
         23/ |21 |25
          /  |   |
         ?---S   E
          22

        1     2     3     4     5     6     7     8     9
     Q-AGP-N-ENF-?-EMI-O-ENH-?-EMK-K-ENL-?-EMO-V-ENK-?-EMN-A
        10    11    12         13    14    15    16    17    18
     V-ENM-?-EMP-J-CIQ-I    V-EMT-?-ENQ-Z-ENN-?-EMQ-Y-ENG-?-EMJ-M
        19    20    21    22    23    24    25        26    27
     W-ENR-?-EMU-P-CIR-S-EML-?-ENI-P-EMR-?-ENO-E   D-CIP-C-AGN-B

     AGP  1       ENK  8        ENN 15        EML 22
     ENF  2       EMN  9        EMQ 16        ENI 23
     EMI  3       ENM 10        ENG 17        EMR 24
     ENH  4       EMP 11        EMJ 18        ENO 25
     EMK  5       CIQ 12        ENR 19        CIP 26
     ENL  6       EMT 13        EMU 20        AGN 27
     EMO  7       ENQ 14        CIR 21
    */
    @Test
    fun usBombeReport1944_testMenu_VII() {
        val instructions = BombeJobInstructions(
            // the menu
            //         1     2     3     4     5     6     7     8     9
            listOf("Q-AGP-N-ENF-?-EMI-O-ENH-?-EMK-K-ENL-?-EMO-V-ENK-?-EMN-A",
            //        10    11    12         13    14    15    16    17    18
                  "V-ENM-?-EMP-J-CIQ-I", "V-EMT-?-ENQ-Z-ENN-?-EMQ-Y-ENG-?-EMJ-M",
            //        19    20    21    22    23    24    25        26    27
                  "W-ENR-?-EMU-P-CIR-S-EML-?-ENI-P-EMR-?-ENO-E", "D-CIP-C-AGN-B"),
            'V',
            'A',
            // left, middle, right rotor
            listOf(listOf(DrumType.V, DrumType.IV, DrumType.III)),
            ReflectorType.B,
        )

        val bombe = Bombe(instructions.deriveBombeConstructionParameters())
        val operator = ExpertBombeOperator(bombe)
        val stops = operator.executeJob(instructions)
//        println("test menu VII stops (${stops.size}):")
//        stops.forEach { it.print() }

        assertEquals(14, stops.size, "expected 14 stops")
        assertEquals('C', stops.get(0).rotor1RingStellung)
        assertEquals('K', stops.get(0).rotor2RingStellung)
        assertEquals('Y', stops.get(0).rotor3RingStellung)
        assertEquals("H", stops.get(0).getPotentialSteckerPartnersString())
    }
}