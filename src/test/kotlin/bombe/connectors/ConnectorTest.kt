package bombe.connectors

import bombe.components.DummyComponent
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConnectorTest {

    // 'connectTo' and 'disconnect' have test cases in the 'PlugTest' class,
    // as the Plug subclass is the only class actually using those methods

    @Test
    fun isContactActive() {
        val plug = Plug("label", DummyComponent())
        val contactId = 'X'
        var contactActive = plug.isContactActive(contactId)
        assertFalse(contactActive)
        plug.passCurrentInbound(contactId)
        contactActive = plug.isContactActive(contactId)
        assertTrue(contactActive)
    }

    @Test
    fun passCurrentInbound_ok() {
        val plug = Plug("label", DummyComponent())
        val contactId = 'X'
        val currentPassed = plug.passCurrentInbound(contactId)
        assertTrue(currentPassed)
        assertTrue(plug.isContactActive(contactId))
    }

    @Test
    fun passCurrentInbound_unconnected() {
        val plug = Plug("label", DummyComponent())
        val contactId = 'X'
        val currentPassed = plug.passCurrentInbound(contactId)
        assertTrue(currentPassed)
        assertTrue(plug.isContactActive(contactId))
        // no exception is expected, it would be strange to try and pass a current onto an unconnected
        // connector, but it will not trigger an exception either
    }

    @Test
    fun passCurrentInbound_contactAlreadyActive() {
        val jack = Jack("label", "label", DummyComponent())
        val plug = Plug("label", DummyComponent())
        plug.plugInto(jack)
        val contactId = 'X'

        // now activate the same contact of the same plug twice
        plug.passCurrentInbound(contactId)
        val currentPassed = plug.passCurrentInbound(contactId)
        assertFalse(currentPassed, "an already active contact should not continue to pass the current")
    }

    @Test
    fun passCurrentOutbound_ok () {
        val jack = Jack("label", "label", DummyComponent())
        val plug = Plug("label", DummyComponent())
        plug.plugInto(jack)

        // now activate a contact in the jack, this should pass through to the plug
        val contactId = 'X'
        val currentPassed = jack.passCurrentOutbound(contactId)
        assertTrue(currentPassed)
        assertTrue(jack.isContactActive(contactId))
        assertTrue(plug.isContactActive(contactId))
        assertEquals(jack.readContacts(), plug.readContacts())
    }

    @Test
    fun passCurrentOutbound_unconnected() {
        val plug = Plug("label", DummyComponent())
        val contactId = 'X'
        val currentPassed = plug.passCurrentOutbound(contactId)
        assertTrue(currentPassed)
        assertTrue(plug.isContactActive(contactId))
        // no exception is expected (e.g. a CommonsSet should be able to pass a current coming in via one of its connectors
        // to all its other connectors, regardless of which ones are plugged up, without triggering an exception
    }

    @Test
    fun passCurrentOutound_contactAlreadyActive() {
        val component = DummyComponent()
        val jack = Jack("label", "label", component)

        // now activate the same contact in the jack twice
        val contactId = 'X'
        jack.passCurrentOutbound(contactId)
        val currentPassed = jack.passCurrentOutbound(contactId)
        assertFalse(currentPassed, "an already active contact should not continue to pass the current")
    }

}