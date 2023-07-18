package bombe

enum class BombeRunStrategy {

    // strategy used by the very first bombe prototype (VICTORY)
    // in 1 bombe run 1 potential stecker partner can be tested, with a single-loop menu
    SINGLE_LINE_SCANNING,

    // output is feedback again as input
    SIMULTANEOUS_SCANNING,

    // diagonal board
    DIAGONAL_BOARD
}