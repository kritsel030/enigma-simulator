class Plugboard {

    _wiringTable

    // e.g. AC-DK-GI-JX-OE-XZ
    constructor(wiringDefinition, alphabetSize = 26) {
        this.wiringDefinition = wiringDefinition
        this.alphabetSize = alphabetSize
        this.init()
    }

    init() {
        // initialize the result as if every letter is self-steckered
        let result = [];
        for (let i = 0; i < this.alphabetSize; i++) {
            result.push(0)
        }

        // split returns 1 empty string element when executed on an empty string
        if (this.wiringDefinition.length > 0) {    
            let pairs = this.wiringDefinition.split("-")
            for (let i = 0; i < pairs.length; i++) {
                const id1 = charToId(pairs[i][0])
                const id2 = charToId(pairs[i][1])
                if (id1 > this.alphabetSize)
                    throw "plugboard mapping 'pairs[i]' not allowed, '" + id1 + "' not within alphabetSize of " + this.alphabetSize
                if (id2 > this.alphabetSize)
                    throw "plugboard mapping 'pairs[i]' not allowed, '" + id2 + "' not within alphabetSize of " + this.alphabetSize
                result[id1] = id2 - id1
                result[id2] = id1 - id2
            }
        }
        this._wiringTable = result
    }

    getNormalizedWiringTable() {
        return this._wiringTable
    }

    encipherConnection(inputChannel) {
        return this.normalize(inputChannel + this._wiringTable[inputChannel])
    }

    normalize(input)  {
        return (input + this.alphabetSize) % this.alphabetSize
    }
}