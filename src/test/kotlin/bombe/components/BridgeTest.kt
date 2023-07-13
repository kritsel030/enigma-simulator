package bombe.components

import bombe.Bombe
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BridgeTest {

    @Test
    fun passCurrentInbound_viaJack() {
        val bridge = Bridge("label", Bombe())
        val contactId = 'X'
        bridge.jack.passCurrentInbound(contactId)
        // the current should flow to all connectors
        bridge.connectors.forEach {
            run {
                assertTrue(it.isContactActive(contactId))
                assertEquals(bridge.jack.readContacts(), it.readContacts())
            }
        }
    }

    @Test
    fun passCurrentInbound_viaInPlug() {
        val bridge = Bridge("label", Bombe())
        val contactId = 'X'
        bridge.inPlug.passCurrentInbound(contactId)
        // the current should flow to all connectors
        bridge.connectors.forEach {
            run {
                assertTrue(it.isContactActive(contactId))
                assertEquals(bridge.inPlug.readContacts(), it.readContacts())
            }
        }
    }

    @Test
    fun passCurrentInbound_viaOutPlug() {
        val bridge = Bridge("label", Bombe())
        val contactId = 'X'
        bridge.outPlug.passCurrentInbound(contactId)
        // the current should flow to all connectors
        bridge.connectors.forEach {
            run {
                assertTrue(it.isContactActive(contactId))
                assertEquals(bridge.outPlug.readContacts(), it.readContacts())
            }
        }
    }
}