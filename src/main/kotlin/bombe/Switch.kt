package bombe

class Switch (label: String) {

    private var on : Boolean = false

    fun switchOn() {
        on = true
    }

    fun switchOff() {
        on = false
    }

    fun isOn() : Boolean{
        return on
    }
}