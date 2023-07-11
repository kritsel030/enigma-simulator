package bombe

class IndicatorDrum(val bombe:Bombe) {

    var position = 'A'.plus(bombe.alphabetSize -1)
        private set

    // rotate counter clock-wise: after A comes Z
    fun rotate() {
        val zeroBasedPosition = position.code - 'A'.code
        position = Char(((zeroBasedPosition-1+bombe.alphabetSize) % bombe.alphabetSize) + 'A'.code)
    }

}