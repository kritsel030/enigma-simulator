package bombe.components

import bombe.Bombe
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CableTest {

    @Test
    fun passCurrentInbound_viaRightPlug() {
        val cable = Cable("label", Bombe())
        val contactId = 'X'
        cable.rightPlug.passCurrentInbound(contactId)
        // the current should flow to the other plug
        assertTrue(cable.leftPlug.isContactActive(contactId))
        assertEquals(cable.rightPlug.readContacts(), cable.leftPlug.readContacts())
    }

    @Test
    fun passCurrentInbound_viaLeftPlug() {
        val cable = Cable("label", Bombe())
        val contactId = 'X'
        cable.leftPlug.passCurrentInbound(contactId)
        // the current should flow to the other plug
        assertTrue(cable.rightPlug.isContactActive(contactId))
        assertEquals(cable.rightPlug.readContacts(), cable.leftPlug.readContacts())
    }

}