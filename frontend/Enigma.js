class Enigma {

    constructor(reflector, rotor1, rotor2, rotor3, plugboard) {
        this.reflector = reflector
//        this.rotor1 = rotor1;
//        this.rotor2 = rotor2;
//        this.rotor3 = rotor3;
        this.plugboard = plugboard;
        this.rotorSteppingEnabled = true
        this.rotors = {}
        this.rotors[1] = rotor1
        this.rotors[2] = rotor2
        this.rotors[3] = rotor3
    }

    setRotor(position, rotor) {
        this.rotors[position] = rotor
    }

    getAlphabetSize() {
        return this.reflector.alphabetSize
    }

    setRotorSteppingEnabled(steppingEnabled) {
        this.rotorSteppingEnabled = steppingEnabled
    }

    // encipher the given character
    // returns an array:
    //  index 0: enciphered character
    //  index 1: array of intermediate results (all 0-based):
    //    0: plugboard inbound input connection
    //    1: plugboard inbound output connection
    //    2: rotor3 inbound output connection
    //    3: rotor2 inbound output connection
    //    4: rotor1 inbound output connection
    //    5: reflector outbound output connection
    //    6: rotor1 outbound output connection
    //    7: rotor2 outbound output connection
    //    8: rotor3 outbound output connection
    //    9: plugboard outbound output connection
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

    encipherWireId(inputWireId, stepRotors=true) {
        if (stepRotors === true) {
            this.stepRotors()
        }

        let wireMap = {}
        wireMap.inbound_KBtoPBWireId = inputWireId
        wireMap.inbound_PBtoR3WireId = this.plugboard.encipherConnection(wireMap.inbound_KBtoPBWireId)
        wireMap.inbound_R3toR2WireId = this.rotors[3].encipherRightToLeftContactChannel(wireMap.inbound_PBtoR3WireId)
        wireMap.inbound_R2toR1WireId = this.rotors[2].encipherRightToLeftContactChannel(wireMap.inbound_R3toR2WireId)
        wireMap.inbound_R1toReflWireId = this.rotors[1].encipherRightToLeftContactChannel(wireMap.inbound_R2toR1WireId)
        wireMap.outbound_ReflToR1WireId = this.reflector.encipherConnection(wireMap.inbound_R1toReflWireId)
        wireMap.outbound_R1ToR2WireId = this.rotors[1].encipherLeftToRightContactChannel(wireMap.outbound_ReflToR1WireId)
        wireMap.outbound_R2toR3WireId = this.rotors[2].encipherLeftToRightContactChannel(wireMap.outbound_R1ToR2WireId)
        wireMap.outbound_R3toPBWireId = this.rotors[3].encipherLeftToRightContactChannel(wireMap.outbound_R2toR3WireId)
        wireMap.outbound_PBtoKBWireId = this.plugboard.encipherConnection(wireMap.outbound_R3toPBWireId)

        return [wireMap.outbound_PBtoKBWireId, wireMap]
    }



    // step the rotor(s)
    stepRotors() {
        // console.log("Enigma.stepRotors()")
        if (this.rotorSteppingEnabled) {
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
}