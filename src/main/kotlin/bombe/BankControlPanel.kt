package bombe

interface BankControlPanel {

    fun switchOn()
    fun switchOff()
    fun isOn() : Boolean

    fun setContactToActivate(contact: Char)
    fun getContactToActivate() : Char?
}