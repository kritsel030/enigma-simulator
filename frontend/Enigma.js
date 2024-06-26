class Enigma {

    constructor(reflector, rotor1, rotor2, rotor3, plugboard) {
        this.reflector = reflector
        this.plugboard = plugboard;
        this.rotorSteppingDisabled = false
        this.rotors = {}
        this.rotors[1] = rotor1
        this.rotors[2] = rotor2
        this.rotors[3] = rotor3

        this.scramblerInputIds = []
        this.scramblerOutputIds = []
    }

    setRotor(position, rotor) {
        this.rotors[position] = rotor
    }

    getAlphabetSize() {
        return this.reflector.alphabetSize
    }

    addScramblerInputId(inputId) {
        if (!this.scramblerInputIds.includes(inputId))
            this.scramblerInputIds.push(inputId)
    }

    addScramblerOutputId(outputId) {
        if (!this.scramblerOutputIds.includes(outputId))
            this.scramblerOutputIds.push(outputId)
    }

    setRotorSteppingDisabled(steppingDisabled) {
        this.rotorSteppingDisabled = steppingDisabled
    }

    // encipher the given character
    // returns a two-element array:
    //  index 0: enciphered character
    //  index 1: object with active input and output contacts for plugboard, rotors and reflector
    encipher(inputChar, stepRotors=true) {
        if (stepRotors) {
            this.stepRotors()
        }

        let normalizedResult = this.encipherNormalized(charToId(inputChar), false)
        return [idToCharToken(normalizedResult[0]), normalizedResult[1]]
    }

    encipherNormalized(inputWireId, stepRotors=true) {
        if (stepRotors === true) {
            this.stepRotors()
        }

        let plugboardInboundInput = inputWireId
        let plugboardInboundOutput = this.normalize(this.plugboard.encipherConnection(plugboardInboundInput))
        let rotor3InboundOutput = this.normalize(this.rotors[3].encipherRightToLeftContactChannel(plugboardInboundOutput))
        let rotor2InboundOutput = this.normalize(this.rotors[2].encipherRightToLeftContactChannel(rotor3InboundOutput))
        let rotor1InboundOutput = this.normalize(this.rotors[1].encipherRightToLeftContactChannel(rotor2InboundOutput))
        let reflectorOutboundOutput = this.normalize(this.reflector.encipherConnection(rotor1InboundOutput))
        let rotor1OutboundOutput = this.normalize(this.rotors[1].encipherLeftToRightContactChannel(reflectorOutboundOutput))
        let rotor2OutboundOutput = this.normalize(this.rotors[2].encipherLeftToRightContactChannel(rotor1OutboundOutput))
        let rotor3OutboundOutput = this.normalize(this.rotors[3].encipherLeftToRightContactChannel(rotor2OutboundOutput))
        let plugboardOutboundOutput = this.normalize(this.plugboard.encipherConnection(rotor3OutboundOutput))

        return [plugboardOutboundOutput, [
            plugboardInboundInput,
            plugboardInboundOutput,
            rotor3InboundOutput,
            rotor2InboundOutput,
            rotor1InboundOutput,
            reflectorOutboundOutput,
            rotor1OutboundOutput,
            rotor2OutboundOutput,
            rotor3OutboundOutput,
            plugboardOutboundOutput
        ]]
    }

    encipherWireId(inputWireId, stepRotors=true, skipPlugboard=false) {
        if (stepRotors === true) {
            this.stepRotors()
        }

        let wireMap = {}
        if (!skipPlugboard) {
            wireMap.inbound_KBtoPBWireId = inputWireId
            wireMap.inbound_PBtoR3WireId = this.plugboard.encipherConnection(wireMap.inbound_KBtoPBWireId)
        } else {
            wireMap.inbound_PBtoR3WireId = inputWireId
        }
        wireMap.inbound_R3toR2WireId = this.rotors[3].encipherRightToLeftContactChannel(wireMap.inbound_PBtoR3WireId)
        wireMap.inbound_R2toR1WireId = this.rotors[2].encipherRightToLeftContactChannel(wireMap.inbound_R3toR2WireId)
        wireMap.inbound_R1toReflWireId = this.rotors[1].encipherRightToLeftContactChannel(wireMap.inbound_R2toR1WireId)
        wireMap.outbound_ReflToR1WireId = this.reflector.encipherConnection(wireMap.inbound_R1toReflWireId)
        wireMap.outbound_R1ToR2WireId = this.rotors[1].encipherLeftToRightContactChannel(wireMap.outbound_ReflToR1WireId)
        wireMap.outbound_R2toR3WireId = this.rotors[2].encipherLeftToRightContactChannel(wireMap.outbound_R1ToR2WireId)
        wireMap.outbound_R3toPBWireId = this.rotors[3].encipherLeftToRightContactChannel(wireMap.outbound_R2toR3WireId)
        if (!skipPlugboard) {
            wireMap.outbound_PBtoKBWireId = this.plugboard.encipherConnection(wireMap.outbound_R3toPBWireId)
        }

        if (!skipPlugboard) {
            return [wireMap.outbound_PBtoKBWireId, wireMap]
        } else {
            return [wireMap.outbound_R3toPBWireId, wireMap]
        }
    }



    // step the rotor(s)
    stepRotors() {
        // console.log("Enigma.stepRotors()")
        if (! this.rotorSteppingDisabled) {
            let steppingRotors = this.determineSteppingRotors()
            if (steppingRotors[0]) {
                this.rotors[1].step()
            }
            if (steppingRotors[1]) {
                this.rotors[2].step()
            }
            if (steppingRotors[2]) {
                this.rotors[3].step()
            }
        } else {
            console.log("rotor stepping disabled")
        }
    }

    // determine which rotors are going to step on the next key press
    determineSteppingRotors() {
        // the rightmost rotor steps by default
        let steppingRotors = [false, false, true]

        // when the rightmost rotor is in the pre-turnover position, it will make the middle rotor step
        if (this.rotors[3].inPreTurnoverPosition()) {
            steppingRotors[1] = true
        }

        // when the middle rotor is in the pre-turnover position, it will make the leftmost rotor and itself step
        if (this.rotors[2].inPreTurnoverPosition()) {
            steppingRotors[1] = true
            steppingRotors[0] = true
        }
        return steppingRotors
    }

    normalize(input)  {
        return (input + this.getAlphabetSize()) % this.getAlphabetSize()
    }

    reset() {
        this.plugboardInputId = null
        this.plugboardOutputId = null
        this.scramblerInputId = null
        this.scramblerOutputId = null
        this.scramblerInputIds = []
        this.scramblerOutputIds = []
    }

    resetRotors() {
        this.rotors[1].resetToStartPosition()
        this.rotors[2].resetToStartPosition()
        this.rotors[3].resetToStartPosition()
    }

    resetKeyboard() {
        this.pressedKeyId = null
        this.lightedKeyId = null
    }
}