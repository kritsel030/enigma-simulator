class Rotor {

    constructor(type, position, ringSetting) {
        this.type = type
        this.position = position;
        this.ringSetting = ringSetting;
    }

    step() {
        if (this.position === 'Z') {
            this.position = 'A'
        } else {
            this.position = String.fromCharCode(this.position.charCodeAt(0) + 1)
        }
    }

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

    getInternalTurnoverPosition() {
        return this.getTurnoverPosition().charCodeAt(0) - 65
    }

    inPreTurnoverPosition() {
        console.log("[" + this.type + "] + inPreTurnoverPosition: " + this.getInternalPosition() + " === " + this.getInternalTurnoverPosition() + " -1")
        return this.getInternalPosition() === (this.getInternalTurnoverPosition() - 1)
    }

    getInternalWiringTable() {
        let result = [];
        let wiringTable = this.getWiringTable()
        for (let i=0; i < wiringTable.length; i++) {
            // 65 = A
            result.push(wiringTable.charCodeAt(i) - (65 + i))
        }
        return result
    }

    getInternalPosition() {
        // A = 65
        return this.position.charCodeAt(0) - 65
    }

    getInternalRingSetting() {
        return this.ringSetting - 1
    }
}
