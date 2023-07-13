package bombe.connectors

import bombe.components.DummyComponent
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlugTest {

    @Test
    fun constructor() {
        val dummyComponent = DummyComponent()
        val plug = Plug("label", dummyComponent)
        assertEquals(dummyComponent, plug.attachedTo, "the newly created plug must know to which component it is attached")
        assertTrue(plug in plug.attachedTo.connectors, "the newly created plug should be a member of its component's connectorlist")
    }

    @Test
    fun plugInto_ok() {
        val jack = Jack("label", "label", DummyComponent())
        val plug = Plug("label", DummyComponent())
        plug.plugInto(jack)
        assertEquals(jack, plug.pluggedInto())
        assertEquals(plug, jack.insertedPlug())
    }

    @Test
    fun plugInto_plugAlreadyConnected() {
        val jack1 = Jack("label", "label", DummyComponent())
        val jack2 = Jack("label", "label", DummyComponent())
        val plug = Plug("label", DummyComponent())
        plug.plugInto(jack1)
        // now try to plug into the second jack
        assertFailsWith<IllegalStateException> { plug.plugInto(jack2) }
    }

    @Test
    fun plugInto_jackAlreadyConnected() {
        val jack = Jack("label", "label", DummyComponent())
        val plug1 = Plug("label", DummyComponent())
        val plug2 = Plug("label", DummyComponent())
        plug1.plugInto(jack)
        // now try to plug a second plug into the same jack
        assertFailsWith<IllegalStateException> { plug2.plugInto(jack) }
    }

    @Test
    fun unplug_ok() {
        val jack = Jack("label", "label", DummyComponent())
        val plug = Plug("label", DummyComponent())
        plug.plugInto(jack)
        plug.unplug()
        assertNull(jack.insertedPlug())
        assertNull(plug.pluggedInto())
    }

    @Test
    fun unplug_notPluggedUp() {
        val plug = Plug("label", DummyComponent())
        assertFailsWith<IllegalStateException> { plug.unplug() }
    }
}