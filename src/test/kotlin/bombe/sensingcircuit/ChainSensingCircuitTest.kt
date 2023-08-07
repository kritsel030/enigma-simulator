package bombe.sensingcircuit

import bombe.Bombe
import bombe.MainCircuit
import bombe.components.DrumType
import bombe.operators.JuniorBombeOperator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChainSensingCircuitTest {

    @Test
    fun afterInitialization() {
        val bombe = Bombe()
        val chainSensingCircuit = bombe.sensingCircuit.chainSensingCircuits["1"]!!

        // no chain is on, so not a single coil of a single sense relay should be energized

        // checks for primary coils
        // expect primary coil of all sense relays to be connected to the bombes main circuit
        assertEquals(bombe.alphabetSize, chainSensingCircuit.sensingRelays.values.filter { it.primaryCoil.connectedTo is MainCircuit }.count(), "primary coils connected to MainCircuit")
        // expect 0 primary coils to be energized
        assertEquals(0, chainSensingCircuit.sensingRelays.values.filter{it.primaryCoil.isEnergized()}.count(), "energized primary coils")

        // checks for secondary coils
        // expect secondary coil of all sense relays to be connected to 'alphabetsize' different ChainInputContacts
        assertEquals(bombe.alphabetSize, chainSensingCircuit.sensingRelays.values.filter{it.secondaryCoil.connectedTo is ChainInputContact}.map{it.secondaryCoil.connectedTo}.toSet().size, "secondary coils connected to unique ChainInputContacts")
        // expect 0 secondary coils to be  energized
        assertEquals(0, chainSensingCircuit.sensingRelays.values.filter{it.secondaryCoil.isEnergized()}.count(), "energized secondary coils")

        // checks for the entire circuit
        // nothing energized yet, chainSensingCircuit is expected to be closed
        val noOfClosedRelays = chainSensingCircuit.sensingRelays.values.filter{it.operates() }.count()
        assertEquals(0, noOfClosedRelays)
        assertFalse(chainSensingCircuit.isOpen())
    }

    @Test
    fun mainCircuitEnergized_activeChain() {
        val bombe = Bombe()
        bombe.getChain(1)!!.switchOn()
        bombe.mainCircuit.powerUp()

        val activeChainSensingCircuit = bombe.sensingCircuit.chainSensingCircuits["1"]!!

        // checks for primary coils
        // expect primary coil of all sense relays to be connected to the bombes main circuit
        assertEquals(bombe.alphabetSize, activeChainSensingCircuit.sensingRelays.values.filter { it.primaryCoil.connectedTo is MainCircuit }.count(), "primary coils connected to MainCircuit")
        // expect all primary coils to be energized
        assertEquals(bombe.alphabetSize, activeChainSensingCircuit.sensingRelays.values.filter{it.primaryCoil.isEnergized()}.count(), "energized primary coils")

        // checks for secondary coils
        // expect secondary coil of all sense relays to be connected to 'alphabetsize' different ChainInputContacts
        assertEquals(bombe.alphabetSize, activeChainSensingCircuit.sensingRelays.values.filter{it.secondaryCoil.connectedTo is ChainInputContact}.map{it.secondaryCoil.connectedTo}.toSet().size, "secondary coils connected to unique ChainInputContacts")
        // expect 0 secondary coils to be  energized (as no chainInputContact is energized)
        assertEquals(0, activeChainSensingCircuit.sensingRelays.values.filter{it.secondaryCoil.isEnergized()}.count(), "energized secondary coils")

        // checks for the entire circuit
        // all relays are expected to be operating => chainSensingCircuit is expected to be open
        val noOfOperatingRelays = activeChainSensingCircuit.sensingRelays.values.filter{it.operates() }.count()
        assertEquals(bombe.alphabetSize, noOfOperatingRelays, "operating relays")
        assertTrue(activeChainSensingCircuit.isOpen(), "chainSensingCircuit.isOpen")
    }

    @Test
    fun mainCircuitEnergized_activeChain_searchLetter() {
        val bombe = Bombe()
        bombe.getChain(1)!!.switchOn()
        bombe.getChain(1)!!.swichOnSearchLetter('A')
        bombe.mainCircuit.powerUp()
        val chainSensingCircuit = bombe.sensingCircuit.chainSensingCircuits["1"]!!

        // checks for primary coils
        // expect primary coil of all sense relays MINUS 1 to be connected to the bombes main circuit
        assertEquals(bombe.alphabetSize-1, chainSensingCircuit.sensingRelays.values.filter { it.primaryCoil.connectedTo is MainCircuit }.count(), "primary coils connected to MainCircuit")
        // expect primary coil of 1 sense relay to be unconnected
        assertEquals(1, chainSensingCircuit.sensingRelays.values.filter{it.primaryCoil.connectedTo == null}.count(), "primary coils unconnected")
        // expect 'primary coil of all sense relays MINUS 1 to be energized
        assertEquals(bombe.alphabetSize-1, chainSensingCircuit.sensingRelays.values.filter{it.primaryCoil.isEnergized()}.count(), "energized primary coils")

        // checks for secondary coils
        // expect secondary coil of all sense relays MINUS 1 to be connected to 'alphabetsize' different ChainInputContacts
        assertEquals(bombe.alphabetSize-1, chainSensingCircuit.sensingRelays.values.filter{it.secondaryCoil.connectedTo is ChainInputContact}.count(), "secondary coils connected to unique ChainInputContacts")
        // expect secondary coil of 1 sense relays to be unconnected
        assertEquals(1, chainSensingCircuit.sensingRelays.values.filter{it.secondaryCoil.connectedTo == null}.count(), "secondary coils unconnected")

        // expect 0 secondary coil to be energized
        assertEquals(0, chainSensingCircuit.sensingRelays.values.filter{it.secondaryCoil.isEnergized()}.count(), "energized secondary coils")

        // checks for the entire circuit
        // after 1 step, expecting all but 1 sense relay to be closed
        assertEquals(bombe.alphabetSize-1, chainSensingCircuit.sensingRelays.values.filter{it.operates() }.count(), "closed sense relays")
        // the OPEN sense relay should be the one for our search letter
        assertEquals('A', chainSensingCircuit.sensingRelays.filter { !it.value.operates() }.keys.first(), "open sense relay")
        assertTrue(chainSensingCircuit.isOpen())
    }


    @Test
    fun after1Step_withScramblers() {
        val bombe = Bombe()
        val operator = JuniorBombeOperator(bombe)

        val scrambler = bombe.getScrambler(1)!!;
        scrambler.placeDrums(listOf(DrumType.I, DrumType.II, DrumType.III))

        val chainInputLetter = 'X'
        val searchLetter = 'A'

        // X : chain input and scrambler input
        val commonsSet = operator.findFreeCommonsSet()
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack(chainInputLetter), commonsSet.getAvailableJack())
        operator.drawCableBetween(bombe.getChainJackPanel(1)!!.getInputJack(), commonsSet.getAvailableJack())
        operator.drawCableBetween(scrambler.getInputJack(), commonsSet.getAvailableJack())

        // B : scrambler output
        operator.drawCableBetween(bombe.getDiagonalBoardJackPanel(1)!!.getJack('Y'), scrambler.getOutputJack())

        bombe.getChain(1)!!.switchOn()
        bombe.getChain(1)!!.swichOnSearchLetter(searchLetter)
        bombe.start(1)

        val chainSensingCircuit = bombe.sensingCircuit.chainSensingCircuits["1"]!!

        // checks for primary coils
        // expect primary coil of all sense relays MINUS 1 to be connected to the bombes main circuit
        assertEquals(bombe.alphabetSize-1, chainSensingCircuit.sensingRelays.values.filter { it.primaryCoil.connectedTo is MainCircuit }.count(), "primary coils connected to MainCircuit")
        // expect primary coils of all sense relays MINUS 1 to be energized
        assertEquals(bombe.alphabetSize-1, chainSensingCircuit.sensingRelays.values.filter{it.primaryCoil.isEnergized()}.count(), "energized primary coils")

        // checks for secondary coils
        // expect secondary coil of all sense relays MINUS 1 to be connected to 'alphabetsize' different ChainInputContacts
        assertEquals(bombe.alphabetSize-1, chainSensingCircuit.sensingRelays.values.filter{it.secondaryCoil.connectedTo is ChainInputContact}.map{it.secondaryCoil.connectedTo}.toSet().size, "secondary coils connected to unique ChainInputContacts")
        // expect 0 secondary coils to be energized
        assertEquals(0, chainSensingCircuit.sensingRelays.values.filter{it.secondaryCoil.isEnergized()}.count(), "energized secondary coils")

        // checks for the entire circuit
        // after 1 step, expecting all but 1 sense relay to be closed
        assertEquals(bombe.alphabetSize-1, chainSensingCircuit.sensingRelays.values.filter{it.operates() }.count(), "closed sense relays")
        // the OPEN sense relay should be the one for our search letter
        assertEquals(searchLetter, chainSensingCircuit.sensingRelays.filter { !it.value.operates() }.keys.first(), "open sense relay")
        assertTrue(chainSensingCircuit.isOpen())
    }
}