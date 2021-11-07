class Reflector {

    _wiringTable

    constructor(type) {
        this.type = type
        this.initType()
    }

    initType() {
        let result = [];
        let wiringTable = this.getWiringTable()
        for (let i=0; i < wiringTable.length; i++) {
            // 65 = A
            if (this.type === 'I' && i === 1) {
                console.log('internal wiring pos ' + i + ': ' + (wiringTable.charCodeAt(i) - 65 + i))
            }
            result.push(wiringTable.charCodeAt(i) - (65 + i))
        }

        this._wiringTable = result
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

    getNormalizedWiringTable() {
        return this._wiringTable
    }

    encipherConnection(inputChannel) {
        let result = this.normalize(inputChannel + this._wiringTable[inputChannel])
        //console.log("reflector, input=" + inputChannel + ", output=" + result)
        return result
    }

    normalize(input)  {
        return (input + 26) % 26
    }
}
