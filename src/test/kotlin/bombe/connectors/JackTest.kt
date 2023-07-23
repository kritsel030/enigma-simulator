package bombe.connectors

import bombe.Bombe
import bombe.components.Cable
import bombe.components.DummyComponent
import bombe.components.Scrambler
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class JackTest {

    @Test
    fun constructor() {
        val dummyComponent = DummyComponent()
        val jack = Jack("external", "label", dummyComponent)
        assertEquals(dummyComponent, jack.attachedTo, "the newly created jack must know to which component it is attached")
        assertTrue(jack in jack.attachedTo.connectors, "the newly created jack should be a member of its component's connectorlist")
    }


}