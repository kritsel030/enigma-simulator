class Enigma {

    constructor(reflector, rotor1, rotor2, rotor3, plugboard) {
        this.reflector = reflector
        this.rotor1 = rotor1;
        this.rotor2 = rotor2;
        this.rotor3 = rotor3;
        this.plugboard = plugboard;
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
            this.step()
        }

        let normalizedResult = this.encipherNormalized(charToId(inputChar), false)
        return [idToCharToken(normalizedResult[0]), normalizedResult[1]]
    }

    encipherNormalized(inputConnection, stepRotors=true) {
        if (stepRotors === true) {
            this.step()
        }

        let plugboardInboundInput = inputConnection
        let plugboardInboundOutput = this.normalize(this.plugboard.encipherConnection(plugboardInboundInput))
        let rotor3InboundOutput = this.normalize(this.rotor3.encipherRightToLeftContactChannel(plugboardInboundOutput))
        let rotor2InboundOutput = this.normalize(this.rotor2.encipherRightToLeftContactChannel(rotor3InboundOutput))
        let rotor1InboundOutput = this.normalize(this.rotor1.encipherRightToLeftContactChannel(rotor2InboundOutput))
        let reflectorOutboundOutput = this.normalize(this.reflector.encipherConnection(rotor1InboundOutput))
        let rotor1OutboundOutput = this.normalize(this.rotor1.encipherLeftToRightContactChannel(reflectorOutboundOutput))
        let rotor2OutboundOutput = this.normalize(this.rotor2.encipherLeftToRightContactChannel(rotor1OutboundOutput))
        let rotor3OutboundOutput = this.normalize(this.rotor3.encipherLeftToRightContactChannel(rotor2OutboundOutput))
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


    // step the rotor(s)
    step() {
        let steppingRotors = this.calculateStep()
        if (steppingRotors[0]) {
            this.rotor1.step()
        }
        if (steppingRotors[1]) {
            this.rotor2.step()
        }
        if (steppingRotors[2]) {
            this.rotor3.step()
        }
    }

    // determine which rotors are going to step on the next key press
    calculateStep() {
        // the rightmost rotor steps by default
        let steppingRotors = [false, false, true]

        // when the rightmost rotor is in the pre-turnover position, it will make the middle rotor step
        if (this.rotor3.inPreTurnoverPosition()) {
            steppingRotors[1] = true
        }

        // when the middle rotor is in the pre-turnover position, it will make the leftmost rotor and itself step
        if (this.rotor2.inPreTurnoverPosition()) {
            steppingRotors[1] = true
            steppingRotors[0] = true
        }
        return steppingRotors
    }

    normalize(input)  {
        return (input + 26) % 26
    }
}