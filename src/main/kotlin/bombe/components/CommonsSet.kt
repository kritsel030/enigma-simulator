package bombe.components

import bombe.Bombe
import bombe.connectors.CablePlug
import bombe.connectors.Jack
import java.lang.IllegalStateException

/**
 * A CommonsSet is a set of Jacks which are all interconnected. A contact being activated on of the Jacks
 * being attached to a CommonsSet will result int the same contact being activated in all other Jacks
 * which are attached to this CommonSet.
 *
 * It is used whenever there is a junction in the menu which needs to be set-up on a bombe:
 * a letter with more links than just a single input and output.
 */

// On an actual bombe, the set of Jacks attached to the same CommonsSet would all carry an identical
// label which contains the ID of the CommonsSet
fun externalLabel(id: Int) : String {
    return "CO$id"
}

/**
 * - id: unique ID for this CommonsSet
 * - bombe: the bombe machine this CommonsSet belongs to
 */
class CommonsSet (val id: Int, bombe : Bombe) : PassThroughComponent(externalLabel(id), bombe){

    // each Jack constructor registers the Jack with the component it is attached to,
    // so please do not remove these member initializers
    private val jack1 = Jack(externalLabel(id), "jack-1", this)
    private val jack2 = Jack(externalLabel(id), "jack-2", this)
    private val jack3 = Jack(externalLabel(id), "jack-3", this)
    private val jack4 = Jack(externalLabel(id), "jack-4", this)
    private val jack5 = Jack(externalLabel(id), "jack-5", this)

    fun jacks() : List<Jack> {
        return connectors as List<Jack>
    }

    // ******************************************************************************************************
    // Features needed to plug up a bombe based on a menu

    // When setting up a bombe based on a menu, we need a way of requesting an available
    // (= not yet claimed) CommonsSet from the total set of CommonsSet instances available
    // on a bombe. These fields and methods support this feature.
    // As a CommonsSet is always used to represent a certain letter in the menu,
    // we also register that letter for debugging/informational purposes.
    var claimedForLetter : Char? = null
    fun isAvailable() : Boolean {
        return claimedForLetter == null
    }
    fun claimFor(letter:Char) {
        claimedForLetter = letter
    }

    // When setting up a bombe based on a menu, we need a way of requesting an available (=unplugged) Jack
    // of a particular CommonsSet
    fun getAvailableJack() : Jack {
        try {
            return jacks().filter { it.pluggedUpBy() == null }.first() as Jack
        } catch (ex:NoSuchElementException) {
            throw IllegalStateException("[CommonsSet ${label}] Trying to use more than 5 Jacks from this CommonsSet.")
        }
    }

    // ******************************************************************************************************
    // Features needed to verify correct plugging up of a bombe
    /**
     * either 0 jacks are plugged up, or at least 2 (so not 1)
     * when there are plugged up jacks, exactly 1 should be connected to the DiagonalBoard
     */
    fun verifyConnections() : List<String> {
        val errors = mutableListOf<String>()
        val pluggedUpJacks = jacks().filter{it.pluggedUpBy() != null}.toList()
        if ( pluggedUpJacks.size == 1) {
            errors.add("$label has only 1 plugged up jack, this cannot be correct")
        }
        if (pluggedUpJacks.size > 1) {
            val jacksConnectedToDB = pluggedUpJacks.filter { (it.pluggedUpBy() as CablePlug).getOppositePlug()!!.pluggedInto()!!.attachedTo is DiagonalBoard }.toList()
            if (jacksConnectedToDB.size != 1) {
                errors.add("$label has ${jacksConnectedToDB.size} jacks which are connected to the DiagonalBoard, expected 1")
            }
        }
        return errors
    }

}