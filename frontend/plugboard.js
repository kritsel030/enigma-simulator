class Plugboard {

    // e.g. AC-DK-GI-JX-OE-XZ
    constructor(connections) {
        this.connections = connections
    }

    getInternalWiringTable() {
        // initialize the result as if every letter is self-wired
        let result = [];
        for (let i = 0; i < 26; i++) {
            result.push[0]
        }

        let pairs = this.connections.split("-")
        for (let i = 0; i < pairs.length; i++) {
            const id1 = charToId(pairs[i][0])
            const id2 = charToId(pairs[i][1])
            result[id1] = id2 - id1
            result[id2] = id1 - id2
        }
        return result
    }
}