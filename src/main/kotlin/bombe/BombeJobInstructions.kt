package bombe

import bombe.components.DrumType
import enigma.components.ReflectorType
import java.lang.IllegalArgumentException
import kotlin.math.sin

class BombeJobInstructions private constructor (
    val menu: List<String>,

    // single input
    // * searchletter + activateContact has a value
    // * drumConfigurations can be a list of values
    // double input
    // * doubleInputCrossSearchLetters has a value
    // * drumConfigurations has a single value
    // this behaviour is guaranteed by the public constructors
    val singleInput: Boolean,
    searchLetter: Char?,
    activateContact: Char?,
    doubleInputCrossSearchLetters: List<Char>?,

    // in the order as they would appear from left to right on an Enigma machine
    val drumConfigurations: List<List<DrumType>>,
    val reflectorType: ReflectorType = ReflectorType.B,

    val bombeTemplate: BombeTemplate,
    val bombeStrategy: BombeRunStrategy
    ) {

    // constructor for single input (searchLetter and activateContact are set)
    constructor(menu: List<String>, searchLetter: Char, activateContact: Char, drumConfigurations: List<List<DrumType>>, reflectorType: ReflectorType, bombeStrategy: BombeRunStrategy = BombeRunStrategy.DIAGONAL_BOARD, bombeTemplate: BombeTemplate = BombeTemplate.MAGIC) : this(menu, true, searchLetter, activateContact, null, drumConfigurations, reflectorType, bombeTemplate, bombeStrategy)

    // constructor for double input (doubleInputCrossSearchLetters is set)
    constructor(menu: List<String>, doubleInputCrossSearchLetters: List<Char>, drumConfiguration: List<DrumType>, reflectorType: ReflectorType, bombeStrategy: BombeRunStrategy = BombeRunStrategy.DIAGONAL_BOARD, bombeTemplate: BombeTemplate = BombeTemplate.MAGIC) : this(menu, false, null, null, doubleInputCrossSearchLetters, listOf(drumConfiguration), reflectorType, bombeTemplate, bombeStrategy)

    // US 6812 Bombe Report 1944, chapter 1, page 25, section 'Double Input'
    // (https://www.codesandciphers.org.uk/documents/bmbrpt/usbmbrpt.pdf)
    // "The search keys used for double input menus must be arranged to cross search, that is the search key for
    // the main menu has the same letter designation as the input letter for the auxiliary chain and vice versa.
    // This practise has been adopted to give the best distribution of current due to the association
    // of the two search points through the diagonal board.
    // double input
    var chain1SearchLetter : Char? = null
    var chain1ActivateContact : Char? = null
    var chain2SearchLetter : Char? = null
    var chain2ActivateContact : Char? = null
    init {
        if (singleInput) {
            // single input
            chain1SearchLetter = searchLetter
            chain1ActivateContact = activateContact
        } else {
            // double input
            chain1SearchLetter = doubleInputCrossSearchLetters!![0]
            chain1ActivateContact = doubleInputCrossSearchLetters[1]
            chain2SearchLetter = chain1ActivateContact
            chain2ActivateContact = chain1SearchLetter
        }
    }

    var parsedMenu = mutableListOf<List<MenuLink>>()
        private set
    init{
        // when double input is used, our job instructions can only support a single drum configuration
        // (in reality two configurations could be plugged up on a single bombe, by using the auxiliary chain,
        //  but this simulator is not so sophisticated to support that)
        check(singleInput || drumConfigurations.size == 1) {"in a double input job we can only support 1 drum configuration, this job has specified ${drumConfigurations.size} drum configurations"}

        // check to see if all drumConfigurations are of the same length (all 3 or 4)
        val drumConfigLengths = drumConfigurations.map{it.size}.toSet()
        require(drumConfigLengths.size == 1) { "all drum configurations in the instructions should have the same length"}
        val numberOfDrumsInConfig = drumConfigLengths.toList()[0]!!
        require(numberOfDrumsInConfig in 3..4) { "all drum configurations should specify 3 or 4 drum types (currently they specify $numberOfDrumsInConfig)"}

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
                require(isCharOrQuestionMark(menuElements[index])) {"menu error at ${index+1}-th element: found ${menuElements[index]}, expected a single letter or '?'"}
                val inputChar = menuElements[index][0]
                // i+1-th element should be an Int or a string consisting of capital letters representing drum orientations
                index = i+1
                var menuPosition: Int? = null
                var menuDrumOrientations: String? = null
                if (isInt(menuElements[index])) {
                    menuPosition = menuElements[index].toInt()
                } else if (isDrumOrientations(menuElements[index], numberOfDrumsInConfig)) {
                    menuDrumOrientations = menuElements[index]
                } else {
                    throw IllegalArgumentException("menu error  at ${index + 1}-th element: found ${menuElements[index]}, expected an int or a string of $numberOfDrumsInConfig capital letters")
                }
                // i+2-th element should be a single Char
                index = i+2
                require(isCharOrQuestionMark(menuElements[index])) {"menu error at ${index+1}-th element: found ${menuElements[index]}, expected a single letter or '?'"}
                val outputChar = menuElements[index][0]
                // add a link (a Triple) to the menuChain
                menuChain.add(MenuLink(parsedMenu.map{it->it.size}.sum()+1,1,inputChar, menuPosition, menuDrumOrientations, outputChar))
            }
        }
    }

    private fun isCharOrQuestionMark(string:String) : Boolean {
        return string.length == 1 && (string[0].isUpperCase() || string.equals("?"))
    }

    private fun isInt(string: String) : Boolean {
        return string.all { char -> char.isDigit() }
    }

    private fun isDrumOrientations(string: String, numberOfDrumsInConfig: Int) : Boolean {
        return string.length == numberOfDrumsInConfig && string.filter{it.isUpperCase()}.length == string.length
    }

    fun deriveBombeConstructionParameters() : BombeConstructionParameters {
        // bombe construction parameters can be derived from the job instructions
        // but the instructions can also indicate that a specific bombe template is to be used
        // (e.g. 'ATLANTA' which will produce a bombe with the ATLANTA dimensions
        if (bombeTemplate != BombeTemplate.MAGIC) {
            return BombeConstructionParameters.getBombeConstructionParameters(bombeTemplate)
        }
        // we can create a bombe with dimensions (link number of banks) which are specifically targeted to suit the instructions
        // noOfChains = 2 in case of double input, in case of single output it is the same as the number of drum configurations
        val noOfChains = if (singleInput) drumConfigurations.size else 2
        // noOfBanks is equal to the number of drum configurations
        val noOfBanks = drumConfigurations.size
        // noOfScramblersPerBank = total number of menuLinks in all menuSegments
        val noOfScramblersPerBank = parsedMenu.sumOf { it.size }
        // noOfRotorsPerScrambler = number of drum types in the first configuration (all drum configurations need to have the samen length)
        val noOfRotorsPerScrambler = drumConfigurations[0].size
        return  BombeConstructionParameters(26,  noOfChains, noOfBanks, noOfScramblersPerBank, noOfRotorsPerScrambler, 5)
    }
}