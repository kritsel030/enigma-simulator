class Rotor {

    static A_ascii = 65

    _startPosition
    _position
    _ringSetting
    _wiringTableRtoL
    _wiringTableLtoR
    _turnoverPosition

    constructor(type, position, ringSetting, alphabetSize = 26) {
        this.type = type
        this.startPosition = position
        this.position = position;
        this.ringSetting = ringSetting;
        this.initType(alphabetSize)
    }

    initType(alphabetSize) {
        let wiringTable = this.getWiringTable()
        if (wiringTable.length != alphabetSize)
            throw "Rotor " + this.type + " has alphabet size " + wiringTable.length + ", expected " + alphabetSize

        // compute normalized wiring table Left to Right
        this._wiringTableRtoL = [];
        for (let i=0; i < wiringTable.length; i++) {
            // 65 = A
            this._wiringTableRtoL.push(wiringTable.charCodeAt(i) - Rotor.A_ascii - i)
        }

        // compute normalized wiring table Right to Left
        this._wiringTableLtoR = [];
        for (let i=0; i < wiringTable.length; i++) {
            this._wiringTableLtoR.push(0)
        }
        for (let i=0; i < wiringTable.length; i++) {
            // 65 = A
            let input = wiringTable.charCodeAt(i) - Rotor.A_ascii
            let output = Rotor.A_ascii + i - wiringTable.charCodeAt(i)
            this._wiringTableLtoR[input] = output
        }

        // compute normalized turnoverPosition
        this._turnoverPosition = this.getTurnoverPosition().charCodeAt(0) - Rotor.A_ascii

        this.alphabetSize = wiringTable.length
    }

    // ****************************************************************************
    // getters and setters

    set position (position) {
        this._position = position.charCodeAt(0) - 65
    }

    get position() {
        return String.fromCharCode(this._position + 65)
    }

    set startPosition (startPos) {
        this._startPosition = startPos
        this.position = startPos
    }

    get startPosition () {
        return this._startPosition
    }

    set ringSetting (ringSetting) {
        this._ringSetting = ringSetting - 1
    }

    get ringSetting() {
        return this._ringSetting + 1
    }

    // ****************************************************************************
    // getters

    // based on https://en.wikipedia.org/wiki/Enigma_rotor_details
    getWiringTable() {
        switch (this.type) {
            // military enigma rotor types
            case 'I':
                return 'EKMFLGDQVZNTOWYHXUSPAIBRCJ'
                break;
            case 'II':
                return 'AJDKSIRUXBLHWTMCQGZNPYFVOE'
                break
            case 'III':
                return 'BDFHJLCPRTXVZNYEIWGAKMUSQO'
                break
            case 'IV':
                return 'ESOVPZJAYQUIRHXLNFTGKDCMWB'
                break
            case 'V':
                return 'VZBRGITYUPSDNHLXAWMJQOFECK'
                break

            // 6 letter alphabet show case rotors
            case 'SCI':
                return 'CAFBDE'
                break
            case 'SCII':
                return 'CABEFD'
                break
            case 'SCIII':
                return 'EADFBC'
                break
        }
    }

    getTurnoverPosition() {
        switch (this.type) {
            // military enigma rotor types
            case 'I':
                return 'R'
                break;
            case 'II':
                return 'F'
                break
            case 'III':
                return 'W'
                break
            case 'IV':
                return 'K'
                break
            case 'V':
                return 'A'
                break

            // 6 letter alphabet show case rotors
            case 'SCI':
                return 'A'
                break
            case 'SCII':
                return 'D'
                break
            case 'SCIII':
                return 'E'
                break
        }
    }

    inPreTurnoverPosition() {
        return this._position === (this._turnoverPosition - 1)
    }

    // ****************************************************************************
    // normalized getters

    getNormalizedWiringTableLtoR() {
        return this._wiringTableLtoR
    }

    getNormalizedWiringTableRtoL() {
        return this._wiringTableRtoL
    }

    getNormalizedTurnoverPosition() {
        return this._turnoverPosition
    }

    getNormalizedPosition() {
        return this._position
    }

    getNormalizedRingSetting() {
        return this._ringSetting
    }

    // ****************************************************************************
    // magic

    step(steps = 1) {
        for (let i = 1; i <= steps; i++) {
            this._position = (this._position + 1) % this.alphabetSize
        }
    }

    encipherRightToLeftContactChannel(inputChannel) {
        let rightContact = this.normalize(inputChannel + this._position - this._ringSetting)
        let leftContact = this.normalize(rightContact + this._wiringTableRtoL[rightContact])
        let leftContactChannel = this.normalize(leftContact - this._position + this._ringSetting)
        //console.log("rotor " + this.type + ", direction=inbound, inputChannel=" + inputChannel + ", inputContact=" + rightContact + ", outputContact=" + leftContact + ", outputChannel=" + leftContactChannel)
        return leftContactChannel
    }

    encipherLeftToRightContactChannel(inputChannel) {
        let leftContact = this.normalize(inputChannel + this._position - this._ringSetting)
        let rightContact = this.normalize(leftContact + this._wiringTableLtoR[leftContact])
        let rightContactChannel = this.normalize(rightContact - this._position + this._ringSetting)
        //console.log("rotor " + this.type + ", direction=outbound, inputChannel=" + inputChannel + ", inputContact=" + leftContact + ", outputContact=" + rightContact + ", outputChannel=" + rightContactChannel)
        return rightContactChannel
    }

    normalize(input)  {
        return (input + 2*this.alphabetSize) % this.alphabetSize
    }
}
