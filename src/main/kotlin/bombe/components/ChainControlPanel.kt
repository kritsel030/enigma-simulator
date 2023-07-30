package bombe.components

interface ChainControlPanel {

    fun getId() : Int

    fun switchOn()
    fun switchOff()
    fun isOn() : Boolean

    fun setContactToActivate(contact: Char)
    fun getContactToActivate() : Char?
}