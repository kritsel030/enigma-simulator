package bombe

interface ChainControlPanel {

    fun switchOn()
    fun switchOff()
    fun isOn() : Boolean

    fun setContactToActivate(contact: Char)
    fun getContactToActivate() : Char?
}