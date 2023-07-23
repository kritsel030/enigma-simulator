package bombe.operators

import bombe.Bombe
import bombe.components.Cable
import bombe.components.DummyComponent
import bombe.components.Scrambler
import bombe.connectors.Jack
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MediorBombeOperatorTest {

    @Test
    fun verifyCableTo_ok() {
        val bombe = Bombe()
        val jack = Jack("external", "label", DummyComponent(bombe))
        val otherJack = Jack("other", "other", DummyComponent(bombe))
        val cable = Cable("label", Bombe())
        cable.leftPlug.plugInto(jack)
        cable.rightPlug.plugInto(otherJack)

        val operator = MediorBombeOperator(bombe)

        val verificationErrors = operator.verifyCableTo(jack, listOf(DummyComponent::class.java.simpleName))
        assertEquals(0, verificationErrors.size, "expected no verification errors")
    }

    @Test
    fun verifyCableTo_notPluggedUp() {
        val bombe = Bombe()
        val jack = Jack("external", "label", DummyComponent(bombe))

        val operator = MediorBombeOperator(bombe)

        val verificationErrors = operator.verifyCableTo(jack, listOf(Scrambler::class.java.simpleName))
        assertEquals(1, verificationErrors.size, "expected 'jack not plugged up' error")
    }

    @Test
    fun verifyCableTo_notPluggedUpToCable() {
        val bombe = Bombe()
        val jack = Jack("external", "label", DummyComponent(bombe))
        val cable = Cable("label", Bombe())
        cable.leftPlug.plugInto(jack) // intentionally leave other plug unplugged

        val operator = MediorBombeOperator(bombe)

        val verificationErrors = operator.verifyCableTo(jack, listOf(DummyComponent::class.java.simpleName))
        assertEquals(1, verificationErrors.size, "expected 'cable not connected' error")
    }

    @Test
    fun verifyCableTo_notReachingExpectedType() {
        val bombe = Bombe()
        val jack = Jack("external", "label", DummyComponent(bombe))
        val otherJack = Jack("other", "other", DummyComponent(bombe))
        val cable = Cable("label", Bombe())
        cable.leftPlug.plugInto(jack)
        cable.rightPlug.plugInto(otherJack)

        val operator = MediorBombeOperator(bombe)

        // intentionally specify a different type than DummyComponent
        val verificationErrors = operator.verifyCableTo(jack, listOf(Scrambler::class.java.simpleName))
        assertEquals(1, verificationErrors.size, "expected 'not connected to Scrambler' error")
    }
}