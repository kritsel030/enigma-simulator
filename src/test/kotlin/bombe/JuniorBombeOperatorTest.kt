package bombe

import bombe.components.DummyComponent
import bombe.connectors.CablePlug
import bombe.connectors.Jack
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JuniorBombeOperatorTest {

    @Test
    fun createAndConnectCable() {
        val bombe = Bombe(3, 1,1, 1)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        val component1 = DummyComponent(bombe)
        val jack1 = Jack("label", "label", component1)
        val component2 = DummyComponent(bombe)
        val jack2 = Jack("label", "label", component2)

        val cable = operator.drawCableBetween(jack1, jack2)
        assertNotNull(cable)
        assertEquals(jack2, (jack1.pluggedUpBy() as CablePlug).getOppositePlug().pluggedInto())
    }

    @Test
    fun createAndConnectBridge() {
        val bombe = Bombe(3, 1,2, 1)
        val operator = JuniorBombeOperator()
        operator.setBombe(bombe)

        val scrambler1 = bombe.getBank(1).getScrambler(1)
        val scrambler2 = bombe.getBank(1).getScrambler(2)

        val bridge = operator.attachBridgeTo(scrambler1!!.outputJack, scrambler2!!.inputJack)
        assertNotNull(bridge)
        assertNotNull(scrambler1.outputJack.pluggedUpBy())
        assertNotNull(scrambler2.inputJack.pluggedUpBy())

    }
}