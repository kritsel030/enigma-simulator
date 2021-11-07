class Plugboard {

    _wiringTable

    // e.g. AC-DK-GI-JX-OE-XZ
    constructor(wiringDefinition) {
        this.wiringDefinition = wiringDefinition
        this.init()
    }

    init() {
        // initialize the result as if every letter is self-wired
        let result = [];
        for (let i = 0; i < 26; i++) {
            result.push(0)
        }

        let pairs = this.wiringDefinition.split("-")
        for (let i = 0; i < pairs.length; i++) {
            const id1 = charToId(pairs[i][0])
            const id2 = charToId(pairs[i][1])
            result[id1] = id2 - id1
            result[id2] = id1 - id2
        }
        this._wiringTable = result
    }

    getNormalizedWiringTable() {
        return this._wiringTable
    }

    encipherConnection(inputChannel) {
        let result = this.normalize(inputChannel + this._wiringTable[inputChannel])
        //console.log("plugboard, input=" + inputChannel + ", output=" + result)
        return result
    }

    normalize(input)  {
        return (input + 26) % 26
    }
}