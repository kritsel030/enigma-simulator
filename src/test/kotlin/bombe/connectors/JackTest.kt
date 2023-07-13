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

    @Test
    fun verifyCableTo_ok() {
        val jack = Jack("external", "label", DummyComponent())
        val otherJack = Jack("other", "other", DummyComponent())
        val cable = Cable("label", Bombe())
        cable.leftPlug.plugInto(jack)
        cable.rightPlug.plugInto(otherJack)
        val verificationErrors = jack.verifyCableTo(listOf(DummyComponent::class.java.simpleName))
        assertEquals(0, verificationErrors.size, "expected no verification errors")
    }

    @Test
    fun verifyCableTo_notPluggedUp() {
        val jack = Jack("external", "label", DummyComponent())
        val verificationErrors = jack.verifyCableTo(listOf(Scrambler::class.java.simpleName))
        assertEquals(1, verificationErrors.size, "expected 'jack not plugged up' error")
    }

    @Test
    fun verifyCableTo_notPluggedUpToCable() {
        val jack = Jack("external", "label", DummyComponent())
        val cable = Cable("label", Bombe())
        cable.leftPlug.plugInto(jack) // intentionally leave other plug unplugged
        val verificationErrors = jack.verifyCableTo(listOf(DummyComponent::class.java.simpleName))
        assertEquals(1, verificationErrors.size, "expected 'cable not connected' error")
    }

    @Test
    fun verifyCableTo_notReachingExpectedType() {
        val jack = Jack("external", "label", DummyComponent())
        val otherJack = Jack("other", "other", DummyComponent())
        val cable = Cable("label", Bombe())
        cable.leftPlug.plugInto(jack)
        cable.rightPlug.plugInto(otherJack)
        // intentionally specify a different type than DummyComponent
        val verificationErrors = jack.verifyCableTo(listOf(Scrambler::class.java.simpleName))
        assertEquals(1, verificationErrors.size, "expected 'not connected to Scrambler' error")
    }
}