package bombe.sensingcircuit

import bombe.components.Chain

/**
 * Sources
 * -------------------------------------------------------------------------------------------
 * Turing's treatise on the Enigma - chapter 6
 * https://www.ugr.es/~aquiran/cripto/museo/turing/turchap6.pdf
 * pages 110, 111
 * "We can detect whether this [= current fails to reach all other points on the E line
 * of the diagonal board] happens by connecting the points of the E line through
 * differential relays to the other pole of our current supply, and putting the 'on' points
 * of the relays in parallel with one another and in series whe the stop mechanism.
 * Normally current will flow through all the differential relays and they will not move.
 * When one reaches a position that might be correct the current fails to reach one
 * of these relays, and the current permanently flowing in the other coil of the relay
 * causes it to close, and bring the stopping mechanism into play."
 * [the part below about 'Mostly what will happen...' and 'Another possibility...' only serves
 *  as explanation, the bombe's sensing circuit does not distinguish between these situations]
 * "Mostly what will happen is that there will be just one relay which closes, and this be
 * one connected to a point of the diagonal board which corresponds to a Stecker which
 * is possibly correct; more accurately, if this Stecker is not correct the position
 * is not correct.
 * Another possibility is that all relays close except the one connected to the point
 * at which the current enters the diagonal board, and this point then corresponds to
 * the only possible Stecker."
 *
 * -----------------------------------------------------------------------------------------------
 * Spider Number 3 - Goldon Welchman - Autumn 1940
 * https://www.joelgreenberg.co.uk/_files/ugd/bbe412_691c3b0212d64e4dbcc1ebf180bc50bd.pdf
 *
 * from section "TERMINOLOGY":
 * "The spider has four INPUTS. [...] Each spider input is a set of 26 terminals or LINES.
 * When the spider is in action the main current enters the spider at one of the lines of an input,
 * and this line is known as the CURRENT ENTRY LINE. [...]
 * Each input is associated with a set of 26 switches, which control the current entry line,
 * and with a set of 26 sensing relays, whose action can cause the spider to stop."
 *
 * from section "THE SENSING RELAYS"
 * "Each sensing relay has two windings, in opposite directions, and the relay is “up”, i.e.
 * the relay switches operate, when current passes through one coil but not through the other.
 * When current passes through both coils or through neither coil the switches do not operate,
 * and the relay is in its normal or “down” position. The negative terminals of both coils are
 * connected to the negative side of the main spider circuit, shown by the green line in the
 * diagram."
 * [the part below about connections of the positive ends of both coils is implemented in
 *  Chain.switchOffSearchletter and Chain.switchSwitchOnSearchLetter]
 * "Normally the positive end of the primary coil is connected to the positive side of the
 * main circuit, shown by the red line, and the positive end of the secondary coil is connected to
 * the corresponding line of the input. But a current entry switch breaks these two connections
 * and connects the line of the input to the positive side of the main circuit.
 * For example in the diagram the A current entry switch is on, so current enters the spider on the
 * a-line of the input, and the A sensing relay is out of action and therefore down.
 * If the input is full, all the lines of the input are connected to the positive side of the main circuit,
 * so all the sensing relays are down.
 * On the other hand if grouping occurs, at least one of the sensing relays is up.
 * The sensing circuit, which detects grouping, is extremely simple. The positive and
 * negative sides, shown by brown and violet lines in the diagram, are connected whenever one
 * of the sensing relays is up."
 */

class ChainSensingCircuit(val chain:Chain) {

    val sensingRelays = mutableMapOf<Char, DifferentialRelay>()
    init {
        // associate a SensingRelay with each input contact of the chain
        chain.chainInputContacts.values.forEach { sensingRelays.put(it.contactId, SwitchControlledDifferentialRelay(chain.bombe.mainCircuit, chain.chainSwitch)) }
    }

    // When at least one of the (differential) sensing relays is closed, it means that the current injected in the
    // 'search letter' contact of the chain's input, did not manage to reach all other contacts of
    // the chain's input. This means a potential correct enigma setting has been found.
    fun isOpen() : Boolean {
        return sensingRelays.values.filter{it.operates()}.count() > 0
    }
}