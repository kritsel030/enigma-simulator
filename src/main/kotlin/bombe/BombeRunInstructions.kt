package bombe

import enigma.components.ReflectorType
import enigma.components.RotorType

class BombeRunInstructions (
    val menu: List<String>,
    val centralLetter: Char,
    // in the order as they would appear from left to right on an Enigma machine
    val rotorConfigurations: List<List<RotorType>>,
    val reflectorType: ReflectorType = ReflectorType.B,
    val activateContact: Char = 'A') {

    var parsedMenu = mutableListOf<List<MenuLink>>()
        private set
    init{
        // parse menu
        for (menuString in menu) {
            val menuChain = mutableListOf<MenuLink>()
            parsedMenu.add(menuChain)
            // example input D-3-K-8-E-2-B
            // needs to be turned into these menu links (each link represented as a MenuLink)
            // D-3-K
            // K-8-E
            // E-2-B
            val menuElements = menuString.split("-")
            // start processing from the start, and proceed by 2 elements for each cycle
            for (i in 0 .. menuElements.size-3 step 2) {
                // i-th element should be a single Char
                var index = i
                require(isChar(menuElements[index])) {"menu error at ${index+1}-th element: found ${menuElements[index]}, expected a single Char"}
                val inputChar = menuElements[index][0]
                // i+1-th element should be an Int
                index = i+1
                require(isInt(menuElements[index])) {"menu error  at ${index+1}-th element: found ${menuElements[index]}, expected a single Char"}
                val menuPosition = menuElements[index].toInt()
                // i+2-th element should be a single Char
                index = i+2
                require(isChar(menuElements[index])) {"menu error at ${index+1}-th element: found ${menuElements[index]}, expected a single Char"}
                val outputChar = menuElements[index][0]
                // add a link (a Triple) to the menuChain
                menuChain.add(MenuLink(parsedMenu.map{it->it.size}.sum()+1,1,inputChar, menuPosition, outputChar))
            }
        }
    }

    private fun isChar(string:String) : Boolean {
        return string.length == 1 && string[0].isUpperCase()
    }

    private fun isInt(string: String) : Boolean {
        return string.all { char -> char.isDigit() }
    }
}