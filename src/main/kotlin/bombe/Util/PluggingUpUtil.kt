package bombe.Util

import bombe.components.Bridge
import bombe.components.CommonsSet
import bombe.components.DiagonalBoard
import bombe.connectors.CablePlug
import bombe.connectors.DiagonalBoardJack
import bombe.connectors.Jack

class PluggingUpUtil {
    companion object {
        fun findConnectedDiagonalBoardJack(jack: Jack) : DiagonalBoardJack? {
            if (jack.pluggedUpBy() != null) {
                if (jack.pluggedUpBy() is CablePlug) {
                    // this jack has a cable plugged into it
                    // find the component on the other end of the cable
                    val otherComponentJack = ((jack.pluggedUpBy() as CablePlug).getOppositePlug().pluggedInto()) as Jack
                    val otherComponent = otherComponentJack!!.attachedTo
                    if (otherComponent is DiagonalBoard) {
                        return otherComponentJack as DiagonalBoardJack
                    } else if (otherComponent is CommonsSet) {
                        // find the jack of this CommonsSet which is plugged into the DiagonalBoard
                        val commonsJackConnectedToDiagonalBoard = (otherComponent as CommonsSet).jacks().filter {
                            it.pluggedUpBy() != null && (it.pluggedUpBy() as CablePlug).getOppositePlug()
                                .pluggedInto()!!.attachedTo is DiagonalBoard
                        }.firstOrNull()
                        if (commonsJackConnectedToDiagonalBoard == null)
                            return null
                        else return (commonsJackConnectedToDiagonalBoard.pluggedUpBy() as CablePlug).getOppositePlug()
                            .pluggedInto() as DiagonalBoardJack
                    }
                } else {
                    // this jack has a bridge plugged into it,
                    // find the diagonal board jack ultimately connected to the bridge's jack
                    return findConnectedDiagonalBoardJack((jack.pluggedUpBy()!!.attachedTo as Bridge).jack)
                }
            }
            // we should actually never get here...
            return null
        }

    }
}