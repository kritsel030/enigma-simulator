package bombe.sensingcircuit

import bombe.Bombe
import bombe.EnergySource
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse


class DifferentialRelayTest {

    @Test
    fun constructor() {
        val bombe = Bombe()
        val energySource1 = EnergySource()
        val energySource2 = EnergySource()
        val relay = DifferentialRelay(bombe.mainCircuit)
        // just want to test that this doesn't throw any exceptions
    }

    fun isClosed_inactive_inactive() {
        val energySource1 = EnergySource()
        val energySource2 = EnergySource()
        val relay = createRelay(energySource1, energySource2)
        assertFalse(relay.operates())
    }

    fun isClosed_active_active() {
        val energySource1 = EnergySource()
        val energySource2 = EnergySource()
        val relay = createRelay(energySource1, energySource2)
        energySource1.energize()
        energySource2.energize()
        assertFalse(relay.operates())
    }

    fun isClosed_active_inactive() {
        val energySource1 = EnergySource()
        val energySource2 = EnergySource()
        val relay = createRelay(energySource1, energySource2)
        energySource1.energize()
        assertFalse(relay.operates())
    }

    fun isClosed_inactive_active() {
        val energySource1 = EnergySource()
        val energySource2 = EnergySource()
        val relay = createRelay(energySource1, energySource2)
        energySource2.energize()
        assertFalse(relay.operates())
    }




    private fun createRelay(energySource1: EnergySource, energySource2: EnergySource) : DifferentialRelay{
        val bombe = Bombe()
        val relay = DifferentialRelay(bombe.mainCircuit)
        relay.primaryCoil.connectTo(energySource1)
        relay.secondaryCoil.connectTo(energySource2)
        return relay
    }
}