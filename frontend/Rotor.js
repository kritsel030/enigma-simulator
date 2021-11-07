class Rotor {

    static A_ascii = 65

    _position
    _ringSetting
    _wiringTableRtoL
    _wiringTableLtoR
    _turnoverPosition

    constructor(type, position, ringSetting) {
        this.type = type
        this.position = position;
        this.ringSetting = ringSetting;
        this.initType()
    }

    initType() {
        let wiringTable = this.getWiringTable()

        // compute normalized wiring table Left to Right
        this._wiringTableRtoL = [];
        for (let i=0; i < wiringTable.length; i++) {
            // 65 = A
            this._wiringTableRtoL.push(wiringTable.charCodeAt(i) - Rotor.A_ascii - i)
        }

        // compute normalized wiring table Right to Left
        this._wiringTableLtoR = [];
        for (let i=0; i < 26; i++) {
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
    }

    // ****************************************************************************
    // getters and setters

    set position (position) {
        this._position = position.charCodeAt(0) - 65
    }

    get position() {
        return String.fromCharCode(this._position + 65)
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
            case 'I':
                return 'EKMFLGDQVZNTOWYHXUSPAIBRCJ'
                break;
            case 'II':
                return 'AJDKSIRUXBLHWTMCQGZNPYFVOE'
                break
            case 'III':
                return 'BDFHJLCPRTXVZNYEIWGAKMUSQO'
                break
        }
    }

    getTurnoverPosition() {
        switch (this.type) {
            case 'I':
                return 'R'
                break;
            case 'II':
                return 'F'
                break
            case 'III':
                return 'W'
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

    step() {
        this._position = (this._position + 1) % 26
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
        return (input + 26) % 26
    }
}
