package bombe.components

import bombe.Bombe
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiagonalBoardTest {

    @Test
    fun getJack() {
        val db = DiagonalBoard(1, Bombe())
        for (i in 0..db.bombe.alphabetSize - 1) {
            val letter = 'A'.plus(i)
            val letterJack = db.getJack(letter)
        }
        // nothing to assert, mainly checking that getJack doesn't throw any errors
    }

    @Test
    fun passCurrent() {
        val db = DiagonalBoard(1, Bombe())
        val inputContactId = 'X'
        val inputJackLetter = 'B'
        val inputJack = db.getJack(inputJackLetter)
        inputJack.passCurrentInbound(inputContactId)

        db._jacks.forEach { letter, jack ->
            // when contact X is activated in jack B,
            // the result should be that contact B gets activated in jack X
            // all other contacts besides these two should be inactive
            run {
                if (letter == inputJackLetter) {
                    // nothing to check
                } else if (letter == inputContactId) {
                    assertTrue(
                        jack.isContactActive(inputJackLetter),
                        "contact $inputJackLetter in jack $letter should be active"
                    )
                    for (i in 0..db.bombe.alphabetSize - 1) {
                        val alphabetLetter = 'A'.plus(i)
                        if (alphabetLetter != inputJackLetter) {
                            assertFalse(
                                jack.isContactActive(alphabetLetter),
                                "contact $alphabetLetter in jack $letter should be inactive"
                            )
                        }
                    }
                } else {
                    for (i in 0..db.bombe.alphabetSize - 1) {
                        val alphabetLetter = 'A'.plus(i)
                        assertFalse(
                            jack.isContactActive(alphabetLetter),
                            "contact $alphabetLetter in jack $letter should be inactive"
                        )
                    }
                }
            }
        }
    }
}
