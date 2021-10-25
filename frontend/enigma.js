class Enigma {

    constructor(reflector, rotor1, rotor2, rotor3, plugboard) {
        this.reflector = reflector
        this.rotor1 = rotor1;
        this.rotor2 = rotor2;
        this.rotor3 = rotor3;
        this.plugboard = plugboard;
    }

    // determine which rotors are going to step on the next key press
    calculateStep() {
        // the rightmost rotor steps by default
        let steppingRotors = [false, false, true]

        // when the rightmost rotor is in the pre-turnover position, it will make the middle rotor step
        if (rotor3.inPreTurnoverPosition()) {
            steppingRotors[1] = true
        }

        // when the middle rotor is in the pre-turnover position, it will make the leftmost rotor and itself step
        if (rotor2.inPreTurnoverPosition()) {
            steppingRotors[1] = true
            steppingRotors[0] = true
        }
        return steppingRotors
    }

    step() {
        let steppingRotors = this.calculateStep()
        if (steppingRotors[0]) {
            rotor1.step()
        }
        if (steppingRotors[1]) {
            rotor2.step()
        }
        if (steppingRotors[2]) {
            rotor3.step()
        }
    }
}