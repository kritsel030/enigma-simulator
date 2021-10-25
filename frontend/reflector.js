class Reflector {

    constructor(type) {
        this.type = type
    }

    // based on https://en.wikipedia.org/wiki/Enigma_rotor_details
    getWiringTable() {
        switch (this.type) {
            case 'B':
                return 'YRUHQSLDPXNGOKMIEBFZCWVJAT'
                break;
            case 'C':
                return 'FVPJIAOYEDRZXWGCTKUQSBNMHL'
                break
        }
    }

    getInternalWiringTable() {
        let result = [];
        let wiringTable = this.getWiringTable()
        for (let i=0; i < wiringTable.length; i++) {
            // 65 = A
            if (this.type === 'I' && i === 1) {
                console.log('internal wiring pos ' + i + ': ' + (wiringTable.charCodeAt(i) - 65 + i))
            }
            result.push(wiringTable.charCodeAt(i) - (65 + i))
        }
        return result
    }
}
