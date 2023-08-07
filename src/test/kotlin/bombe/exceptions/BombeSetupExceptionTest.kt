package bombe.exceptions

import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class BombeSetupExceptionTest {

    @Test
    fun bombeCheckTest() {
        val thrown = assertFailsWith<BombeSetupException> { bombeCheck(false, "name", "code", "sub", "message") }
    }
}