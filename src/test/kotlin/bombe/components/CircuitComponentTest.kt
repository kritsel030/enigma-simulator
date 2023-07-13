package bombe.components

import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class CircuitComponentTest {

    @Test
    fun constructor() {
        val dummyComponent = DummyComponent()
        assertNotNull(dummyComponent.connectors)
        assertNotNull(dummyComponent.bombe)
    }
}