class Reflector {

    _wiringTable

    constructor(type, alphabetSize = 26) {
        this.type = type
        this.initType(alphabetSize)
    }

    initType(alphabetSize) {
        let result = [];
        let wiringTable = this.getWiringTable()
        if (wiringTable.length != alphabetSize)
            throw "Reflector " + this.type + " has alphabet size " + wiringTable.length + ", expected " + alphabetSize
        for (let i=0; i < wiringTable.length; i++) {
            // 65 = A
            if (this.type === 'I' && i === 1) {
                console.log('internal wiring pos ' + i + ': ' + (wiringTable.charCodeAt(i) - 65 + i))
            }
            result.push(wiringTable.charCodeAt(i) - (65 + i))
        }

        this._wiringTable = result
        this.alphabetSize = wiringTable.length
    }

    // based on https://en.wikipedia.org/wiki/Enigma_rotor_details
    getWiringTable() {
        switch (this.type) {
            // military enigma reflector types
            case 'B':
                return 'YRUHQSLDPXNGOKMIEBFZCWVJAT'
                break;
            case 'C':
                return 'FVPJIAOYEDRZXWGCTKUQSBNMHL'
                break

            // 6 letter alphabet show case reflector
            case 'DEMO':
                return 'FEDCBA'
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
        return (input + this.alphabetSize) % this.alphabetSize
    }
}
