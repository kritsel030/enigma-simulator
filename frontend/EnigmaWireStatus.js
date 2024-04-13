class EnigmaWireStatus {

    constructor() {
        this.reset()
    }

    reset() {
        this.scramblerInputContactIds = []
        this.scramblerOutputContactIds = []
        this.activePaths = []
        this.unprocessedScramblerInputContactId = null
    }

    addEncipherPathViaPlugboard(inputPbInputContactId, scramblerInputContactId, scramblerOutputContactId, outputPbOutputContactId) {
        let activePath = {
            inboundPbInputContactId: inputPbInputContactId,
            scramblerInputContactId: scramblerInputContactId,
            scramblerOutputContactId: scramblerOutputContactId,
            outboundPbOutputContactId: outputPbOutputContactId}
        this.#addActivePath(activePath)
    }

    addEncipherPath(scramblerInputContactId, scramblerOutputContactId) {
        let activePath = {
            inboundPbInputContactId: null,
            scramblerInputContactId: scramblerInputContactId,
            scramblerOutputContactId: scramblerOutputContactId,
            outboundPbOutputContactId: null}
        this.#addActivePath(activePath)
    }

    #addActivePath(activePath) {
        this.activePaths.push(activePath)
        this.scramblerInputContactIds.push(activePath.scramblerInputContactId)
        this.scramblerOutputContactIds.push(activePath.scramblerOutputContactId)
    }
}