class BombeEnigma extends Enigma {

    constructor(reflector, rotor1, rotor2, rotor3, plugboard, inputLetter, outputLetter, index, first, lastInMenu, lastInCycle) {
        super(reflector, rotor1, rotor2, rotor3, plugboard)
        this.inputLetter = inputLetter
        this.outputLetter = outputLetter
        this.index = index
        this.first = first
        this.lastInMenu = lastInMenu
        this.lastInCycle = lastInCycle
        this.next = null
        this.previous = null
    }

    isLast(variant) {
        if (["scrambler_diagonal_board", "scrambler_full_menu"].includes(variant)) {
            return this.lastInMenu
        } else {
            return this.lastInCycle
        }
    }



}