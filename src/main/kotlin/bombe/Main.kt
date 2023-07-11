package bombe

import enigma.components.RotorType

//fun main() {
////    val bombe = Bombe(8, 1, 3)
//    val bombe = Bombe(26, 1, 3)
////    val activeContact = 'V' // correct
//    val activeContact = 'G' // incorrect, nice results
//
//    // prepare run 1 - singe line scane
////    setUpBank(bombe, activeContact)
////    positionScramblers(bombe)
////    setUpMenuSingleLineScan(bombe)
////    bombe.run()
////
////    bombe.reset()
////    setUpBank(bombe, activeContact)
////    positionScramblers(bombe)
////    setUpSimultaneousScan(bombe)
////    bombe.run()
//
//    bombe.reset()
//    setUpBank(bombe, activeContact)
//    positionScramblers(bombe)
//    setUpWithDiagonalBoard(bombe)
//    bombe.run()
//
//}
//
//// set positions of scramblers on first bank
//// C --1--> F --5--> A --3--> C
//private fun setUpBank(bombe: Bombe, activeContact: Char) {
//    bombe.getBank(1).switchOn()
//    bombe.getBank(1).placeDrums(RotorType.I, RotorType.II, RotorType.III)
//    bombe.getBank(1).setContactToActivate(activeContact)
//}
//
//// set positions of scramblers on first bank
//// C --1--> F --5--> A --3--> C
//private fun positionScramblers(bombe:Bombe) {
//    bombe.getBank(1).getScrambler(1).setRelativePosition(1)
//    bombe.getBank(1).getScrambler(2).setRelativePosition(5)
//    bombe.getBank(1).getScrambler(3).setRelativePosition(3)
//}
//
//fun setUpMenuSingleLineScan(bombe:Bombe) {
//    // set-up a menu on the first bank
//    // C --1--> F --5--> A --3--> C
//    val bank = bombe.getBank(1)
//    bank.switchOn()
//
//    // A. connect the scramblers
//    // connect both the bank input to the 1st scrambler input
//    bombe.connectNewCable(bank.jack, bank.getScrambler(1).inputJack)
//    // connect the 1st scrambler to the 2nd
//    bombe.connectNewCable(bank.getScrambler(1).outputJack, bank.getScrambler(2).inputJack)
//    // connect the 2nd scrambler to the 3rd
//    bombe.connectNewCable(bank.getScrambler(2).outputJack, bank.getScrambler(3).inputJack)
//
//    // attach the bank's test register to the output of the 3rd scrambler
//    bank.connectTestRegisterTo(bank.getScrambler(3).outputJack)
//}
//
//fun setUpSimultaneousScan(bombe: Bombe) {
//    // set-up a menu on the first bank
//    // C --1--> F --5--> A --3--> C
//    val bank = bombe.getBank(1)
//
//    // A. connect the scramblers
//    // connect both the bank input and the 1st scrambler input to a commons
//    val commons1 = bombe.newCommons()
//    bombe.connectNewCable(bank.jack, commons1.jack1)
//    bombe.connectNewCable(bank.getScrambler(1).inputJack, commons1.jack2)
//    // connect the 1st scrambler to the 2nd
//    bombe.connectNewCable(bank.getScrambler(1).outputJack, bank.getScrambler(2).inputJack)
//    // connect the 2nd scrambler to the 3rd
//    bombe.connectNewCable(bank.getScrambler(2).outputJack, bank.getScrambler(3).inputJack)
//
//    // connect the 3rd scrambler to the commons the 1st scrambler is connected to
//    bombe.connectNewCable(bank.getScrambler(3).outputJack, commons1.jack3)
//
//    // no need to change the connector the bank's test register is connected to k)
//}
//
//fun setUpWithDiagonalBoard(bombe:Bombe) {
//    // set-up a menu on the first bank
//    // C --1--> F --5--> A --3--> C
//    val bank = bombe.getBank(1)
//
//    // A. connect the scramblers
//    // connect both the bank input, the 1st scrambler input, and the DiagonalBoard 'C' jack to a commons
//    val commons1 = bombe.newCommons()
//    bombe.connectNewCable(bank.jack, commons1.jack1)
//    bombe.connectNewCable(bank.getScrambler(1).inputJack, commons1.jack2)
//    bombe.connectNewCable(bombe.diagonalBoard.getJack('C'), commons1.jack3)
//    // bridge the 1st scrambler to the 2nd, and connect to the DiagonalBoard's 'F' jack
//    val bridgeF = bombe.connectNewBridge(bank.getScrambler(1).outputJack, bank.getScrambler(2).inputJack)
//    bombe.connectNewCable(bombe.diagonalBoard.getJack('F'), bridgeF.jack)
//    // bridge the 2nd scrambler to the 3rd, and connect to the DiagonalBoard's 'A' jack
//    val bridgeA = bombe.connectNewBridge(bank.getScrambler(2).outputJack, bank.getScrambler(3).inputJack)
//    bombe.connectNewCable(bombe.diagonalBoard.getJack('A'), bridgeA.jack)
//
//    // connect the 3rd scrambler to the commons we connected the 1st scrambler to
//    bombe.connectNewCable(bank.getScrambler(3).outputJack, commons1.jack4)
//
//    // no need to change the connector the bank's test register is connected to k)
//}
