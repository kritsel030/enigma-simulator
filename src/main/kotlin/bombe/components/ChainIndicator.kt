package bombe.components

interface ChainIndicator {

    fun readIndicatorRelays() : Map<Char, Boolean>
}