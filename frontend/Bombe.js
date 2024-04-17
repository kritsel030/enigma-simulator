class Bombe {

    // menu: the menu string as received in the constructor
    menu

    // the drumTypes as received in the constructor
    drumTypes 

    // list of Enigmas, each configured according to drumTypes
    scramblers = []

    // the letters in the menu
    menuLetters = []

    // the numbers in the menu
    scramblerOffsets = []

    // list of Rotor, each representing an indicatorDrum 
    indicatorDrums = []

    // menu: String - a string formatted like A-2-C-1-E etc.
    // drumTypes: List<String> - a list of 3 drumTypes
    constructor(drumTypes, menu, plugboardString, alphabetSize) {
        // parse the menu string
        this.menuLetters.push(menu[0])
        for (let i=2; i < menu.length; i=i+4) {
            this.scramblerOffsets.push(Number(menu[i]))
            // console.log(i+2)
            this.menuLetters.push(menu[i+2])
        }

        // find the cycle letter in the menu (the letter which appears twice)
        // (shallow copy is sufficient)
        this.cycleLetter = null
        let menuLettersCopy = [...this.menuLetters]
        menuLettersCopy.sort()
        for (let c = 0; c < menuLettersCopy.length-1; c++) {
            if (menuLettersCopy[c] == menuLettersCopy[c+1]) {
                this.cycleLetter = menuLettersCopy[c]
                break
            }
        }

        // create a bank of scramblers fitted with the correct drumTypes, 
        // and all drum3 drums set to their correct relevant position as defined in the menu
        let noOfEnigmasNeeded = this.scramblerOffsets.length
        this.scramblersByIndexMap = {}
        this.scramblersByInputLetterMap = {}
        this.scramblersByOutputLetterMap = {}
        this.cycleStartEnigma = null
        this.cycleEndEnigma = null
        let previousScrambler = null
        for (let i=0; i < noOfEnigmasNeeded; i++) {
            let reflector = new Reflector('DEMO', alphabetSize)
            let drum1 = new Rotor(drumTypes[0], 'Z', 1, alphabetSize)
            let drum2 = new Rotor(drumTypes[1], 'Z', 1, alphabetSize)
            let drum3 = new Rotor(drumTypes[2], 'Z', 1, alphabetSize)
            drum3.step(this.scramblerOffsets[i])
            let plugboard = new Plugboard(plugboardString, alphabetSize)

            let first = i===0
            let inputLetter = (!first && this.menuLetters[i] == this.cycleLetter) ? '#' : this.menuLetters[i]
            let outputLetter = this.menuLetters[i+1] == this.cycleLetter ? '#' : this.menuLetters[i+1]

            let lastInMenu = i===noOfEnigmasNeeded-1
            let lastInCycle = i > 0 && outputLetter === '#'
            let scrambler = new BombeEnigma(reflector, drum1, drum2, drum3, plugboard, inputLetter, outputLetter, i, first, lastInMenu, lastInCycle)
            // add the newly created scrambler/enigma to several lists and maps which are needed later on
            this.scramblers.push(scrambler)
            this.scramblersByIndexMap[i] = scrambler
            this.scramblersByInputLetterMap[inputLetter] = scrambler
            this.scramblersByOutputLetterMap[outputLetter] = scrambler
            if (inputLetter === this.cycleLetter && this.cycleStartEnigma == null) {
                this.cycleStartEnigma = scrambler
            }
            if (outputLetter === '#' && this.cycleEndEnigma == null) {
                this.cycleEndEnigma = scrambler
            }
            if (previousScrambler != null) {
                previousScrambler.next = scrambler
                scrambler.previous = previousScrambler
            }
            // prepare for next iteration
            previousScrambler = scrambler
        }

        // create the indicator drums
        for (let i=0; i<drumTypes.length; i++) {
            this.indicatorDrums.push(new Rotor(drumTypes[i], 'Z', 1, alphabetSize))    
        }

        this.inputControlIds = []

        this.pathFinder = new BombePathfinder(this)
    }

    // set all drums with this drumNo to the given position
    // when drumNo = 3, the indicator drum is set to this position and each drum3 in the scramblers is set to the correct
    // position to maintain the relevant position
    // drumNo: 1, 2 or 3
    // position: alphabet letter
    setIndicatorDrumPosition(drumNo, position) {
        this.indicatorDrums[drumNo-1].position = position
        for (let i=0; i<this.scramblers.length; i++) {
            if (drumNo == 1 || drumNo == 2) {
                this.scramblers[i].rotors[drumNo].position = position
            } else {
                // set each rotor3 to the indicator drum + the offset
                this.scramblers[i].rotors[3].position = idToCharToken(normalize(charToId(this.indicatorDrums[drumNo-1].position) + this.scramblerOffsets[i], alphabetSize))
            }
        }
    }

    // advance all drums with this drumNo
    // indicator drum no: 0, 1, or 2
    advanceIndicatorDrums(drumNo) {
        this._advanceOrTunBackIndicatorDrum(drumNo, true)
    }

    // turn back all drums with this drumNo
    // indicator drum no: 0, 1, or 2  
    turnBackIndicatorDrums(drumNo) {
        this._advanceOrTunBackIndicatorDrum(drumNo, false)
    }

    // indicator drum no: 0, 1, or 2
    _advanceOrTunBackIndicatorDrum(drumNo, advance) {
        let newPosition = idToCharToken(normalize(charToId(this.indicatorDrums[drumNo].position) + (advance ? 1 : -1), alphabetSize ))
        this.setIndicatorDrumPosition(drumNo+1, newPosition)
    }
}