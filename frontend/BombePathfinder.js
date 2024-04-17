// Finds the electrical path in a bombe, based on an input

class BombePathfinder {

    constructor(bombe) {
        this.bombe = bombe
        this.enigmaWireStatuses = []
        for(let i=0; i < this.bombe.scramblers.length; i++) {
            this.enigmaWireStatuses.push(new EnigmaWireStatus())
        }
        this.reset()
    }

    reset() {
//        console.log("BombePathFinder.reset")
        for(let i=0; i < this.enigmaWireStatuses.length; i++) {
            this.enigmaWireStatuses[i].reset()
        }

        this.activeLetterCableWires = []
        this.activeLetterCableWires2 = []
        for (let i=0; i < this.bombe.menuLetters.length; i++) {
            this.activeLetterCableWires2[this.bombe.menuLetters[i]] = []
        }
        this.activeLetterCableWires2["#"] = []
        this.activeDiagonalBoardConnections = []
        this.activeCycleCableWireIds = []
    }

    // keyboard input is the same as plugboard input
    processKeyboardInput(enigmaIndex, keyId, variant) {
//        console.log(`processKeyboardInput(${enigmaIndex}, ${keyId}, ${variant})`)
        let inboundPbInputContactId = keyId

        // encipher the pressed key on each enigma
        // using one enigma's out put as the next enigma's input
        let enigma = this.bombe.scramblers[enigmaIndex]

//        for (let i=0; i<this.bombe.scramblers.length; i++) {
//            let enigma = this.bombe.scramblers[i]
        while (enigma != null) {
            let enigmaWireStatus = this.enigmaWireStatuses[enigma.index]
            let encipherResult = enigma.encipherWireId(inboundPbInputContactId, false)
            let outboundPbOutputContactId = encipherResult[0]
//            console.log(`enigmaIndex=${enigma.index}, inboundPbInputContactId=${inboundPbInputContactId}, outboundPbOutputContactId=${outboundPbOutputContactId})`)

            enigmaWireStatus.addEncipherPathViaPlugboard(
                inboundPbInputContactId,
                encipherResult[1].inbound_PBtoR3WireId,
                encipherResult[1].outbound_R3toPBWireId,
                outboundPbOutputContactId)

            // mark active wires in letter cables
            // 'cA' means that the 'c' wire in the 'A' cable is active
            if (enigma.first) {
                let inputWireLetter = idToCharToken(encipherResult[1].inbound_PBtoR3WireId).toLowerCase()
                let inputCableLetter = enigma.inputLetter
                // old
                this.activeLetterCableWires.push(inputWireLetter.concat(inputCableLetter))
                // new
                this.activeLetterCableWires2[inputCableLetter].push(encipherResult[1].inbound_PBtoR3WireId)
            }

            let outputWireId = encipherResult[1].outbound_R3toPBWireId
            let outputWireLetter = idToCharToken(outputWireId).toLowerCase()
            let outputCableLetter = enigma.outputLetter
            if (enigma != this.bombe.cycleEndEnigma) {
                // old
                this.activeLetterCableWires.push(outputWireLetter.concat(outputCableLetter))
                // new
                this.activeLetterCableWires2[outputCableLetter].push(encipherResult[1].outbound_R3toPBWireId)
            } else {
                // new)
                this.activeLetterCableWires2["#"].push(outputWireId)
            }

            // prepare for next enigma in the list
            if (["variantA", "variantB"].includes(variant)) {
                enigma = null
            } else {
                enigma = enigma.next
                inboundPbInputContactId = outboundPbOutputContactId
            }
        }
    }

    completePath(variant) {
        let segmentAdded = true
        while (segmentAdded) {
            segmentAdded = bombeRenderer.bombe.pathFinder.processNextPathRequest(variant)
        }
    }

    processNextPathRequest(variant) {
//        console.log(`processNextPathRequest(${variant})`)
        if (this.findAndProcessUnprocessedEnigmaInput()) {
            // console.log("unprocessed enigma input found and processed")
            return true
        }

        if (this.findAndProcessUnprocessedEnigmaOutput()) {
            // console.log("unprocessed enigma output found and processed")
            return true
        }

        // add the scrambler outputIds of the 'cycleEndEnigma' to the scrambler inputIds of the 'cycleStartEnigma'
        if (this.findAndProcessNewCycleFeedbackPath()) {
            // console.log("new cycle feedback wire to activate found")
            return true
        }

        // add the scrambler inputIds of the 'cycleStartEnigma' to the unprocessed scrambler outputIds of the 'cycleEndEnigma'
        if (this.findAndProcessNewCycleFeedforwardPath()) {
            console.log("new cycle feedforward wire to activate found")
            return true
        }

        if (renderDiagonalBoard(variant)) {
            // diagonal board FEEDBACK
            // when the result would be 'cE' it means that the 'c' wire in the 'E' cable is active,
            // while the corresponding 'e' wire in the 'C' cable is not
            // so it is time to activate the CE diagonal board wire
            // and mark the 'e' contact for the enigma which has 'C' as its input as un unprocessed contact
            let wireInCableToActivate = this.findDiagonalBoardConnectionToActivate(false)
//            console.log("find diagonal board feed-back: " + wireInCableToActivate)
            if (wireInCableToActivate != null) {
                // console.log("new feed-back DB conn to activate found: " + wireInCableToActivate)
                let contactId = charToId(wireInCableToActivate[0].toUpperCase())
                let cableLetter = wireInCableToActivate[1]
                this.activeDiagonalBoardConnections.push(wireInCableToActivate.toUpperCase())
                this.activeDiagonalBoardConnections.push(wireInCableToActivate[1].concat(wireInCableToActivate[0]).toUpperCase())
                this.activeLetterCableWires2[cableLetter].push(contactId)
    //            console.log("cableLetter: " + cableLetter)
    //            console.log(this.bombe.scramblersByInputLetterMap)
                let leftEnigma = this.bombe.scramblersByOutputLetterMap[cableLetter]
                if (leftEnigma != null) {
                    // the previous enigma will receive another output
                    if (!this.enigmaWireStatuses[leftEnigma.index].scramblerOutputContactIds.includes(contactId)) {
                        this.enigmaWireStatuses[leftEnigma.index].unprocessedScramblerOutputContactId = contactId
                    }
                }
                let rightEnigma = this.bombe.scramblersByInputLetterMap[cableLetter]
                if (rightEnigma != null) {
                    // the next enigma will receive another input
                    if (!this.enigmaWireStatuses[rightEnigma.index].scramblerInputContactIds.includes(contactId)) {
                        this.enigmaWireStatuses[rightEnigma.index].unprocessedScramblerInputContactId = contactId
                    }
                }
                return true
            }

            // diagonal board FEEDFORWARD
            // when the result would be 'cE' it means that the 'c' wire in the 'E' cable is active,
            // while the corresponding 'e' wire in the 'C' cable is not
            // so it is time to activate the CE diagonal board wire
            // and mark the 'e' contact for the enigma which has 'C' as its input as un unprocessed contact
            wireInCableToActivate = this.findDiagonalBoardConnectionToActivate(true)
//            console.log("find diagonal board feed-forward: " + wireInCableToActivate)
            if (wireInCableToActivate != null) {
                // console.log("new feed-forward DB conn to activate found: " + wireInCableToActivate)
                let contactId = charToId(wireInCableToActivate[0].toUpperCase())
                let cableLetter = wireInCableToActivate[1]
                this.activeDiagonalBoardConnections.push(wireInCableToActivate.toUpperCase())
                this.activeDiagonalBoardConnections.push(wireInCableToActivate[1].concat(wireInCableToActivate[0]).toUpperCase())
                this.activeLetterCableWires2[cableLetter].push(contactId)
    //            console.log("cableLetter: " + cableLetter)
    //            console.log(this.bombe.scramblersByInputLetterMap)
                let leftEnigma = this.bombe.scramblersByOutputLetterMap[cableLetter]
                if (leftEnigma != null) {
                    // the previous enigma will receive another output
                    if (!this.enigmaWireStatuses[leftEnigma.index].scramblerOutputContactIds.includes(contactId)) {
                        this.enigmaWireStatuses[leftEnigma.index].unprocessedScramblerOutputContactId = contactId
                    }
                }
                let rightEnigma = this.bombe.scramblersByInputLetterMap[cableLetter]
                if (rightEnigma != null) {
                    // the next enigma will receive another input
                    if (!this.enigmaWireStatuses[rightEnigma.index].scramblerInputContactIds.includes(contactId)) {
                        this.enigmaWireStatuses[rightEnigma.index].unprocessedScramblerInputContactId = contactId
                    }
                }
                return true
            }
        }
        console.log("done, no more paths to draw")
        return false
    }

    // add the scrambler outputIds of the 'cycleEndEnigma' to the unprocessed scrambler inputIds of the 'cycleStartEnigma'
    findAndProcessNewCycleFeedbackPath() {
//        console.log("findAndProcessNewCycleFeedbackPath")
        let cycleStartEnigma = this.bombe.cycleStartEnigma
        let cycleEndEnigma = this.bombe.cycleEndEnigma
        let cycleStartEnigmaWireStatus = this.enigmaWireStatuses[cycleStartEnigma.index]
        let cycleEndEnigmaWireStatus = this.enigmaWireStatuses[cycleEndEnigma.index]

        for (let i=0; i < cycleEndEnigmaWireStatus.scramblerOutputContactIds.length; i++){
            let cycleEndOutputId = cycleEndEnigmaWireStatus.scramblerOutputContactIds[i]
            if (!this.activeCycleCableWireIds.includes(cycleEndOutputId)) {
                if (! cycleStartEnigmaWireStatus.scramblerInputContactIds.includes(cycleEndOutputId)) {
                    cycleStartEnigmaWireStatus.unprocessedScramblerInputContactId = cycleEndOutputId
                }
                this.activeCycleCableWireIds.push(cycleEndOutputId)
                return true
            }
        }
        return false
    }

    // add the scrambler inputIds of the 'cycleStartEnigma' to the unprocessed scrambler outputIds of the 'cycleEndEnigma'
    findAndProcessNewCycleFeedforwardPath() {
//        console.log("findAndProcessNewCycleFeedbackPath")
        let cycleStartEnigma = this.bombe.cycleStartEnigma
        let cycleEndEnigma = this.bombe.cycleEndEnigma
        let cycleStartEnigmaWireStatus = this.enigmaWireStatuses[cycleStartEnigma.index]
        let cycleEndEnigmaWireStatus = this.enigmaWireStatuses[cycleEndEnigma.index]

        for (let i=0; i < cycleStartEnigmaWireStatus.scramblerInputContactIds.length; i++){
            let cycleStartOutputId = cycleStartEnigmaWireStatus.scramblerInputContactIds[i]
            if (!this.activeCycleCableWireIds.includes(cycleStartOutputId)) {
                if (! cycleEndEnigmaWireStatus.scramblerOutputContactIds.includes(cycleStartOutputId)) {
                    cycleEndEnigmaWireStatus.unprocessedScramblerOutputContactId = cycleStartOutputId
                }
                this.activeCycleCableWireIds.push(cycleStartOutputId)
                return true
            }
        }
        return false
    }


    findAndProcessUnprocessedEnigmaInput() {
//        console.log("findAndProcessUnprocessedEnigmaInput")
        for (let i=0; i<this.enigmaWireStatuses.length; i++) {
            let enigmaWireStatus = this.enigmaWireStatuses[i]
            if (enigmaWireStatus.unprocessedScramblerInputContactId != null) {
                let enigma = this.bombe.scramblers[i]
                this.processEnigmaInput(enigma, enigmaWireStatus.unprocessedScramblerInputContactId)
                enigmaWireStatus.unprocessedScramblerInputContactId = null
                return true
            }
        }
        return false
    }

    findAndProcessUnprocessedEnigmaOutput() {
//        console.log("findAndProcessUnprocessedEnigmaOutput")
        for (let i=0; i<this.enigmaWireStatuses.length; i++) {
            let enigmaWireStatus = this.enigmaWireStatuses[i]
            if (enigmaWireStatus.unprocessedScramblerOutputContactId != null) {
                let enigma = this.bombe.scramblers[i]
                this.processEnigmaOutput(enigma, enigmaWireStatus.unprocessedScramblerOutputContactId)
                enigmaWireStatus.unprocessedScramblerOutputContactId = null
                return true
            }
        }
        return false
    }

    // based on new input for a scrambler/enigma, calculate new activePaths for all subsequent scramblers/enigmas
    processEnigmaInput(startEnigma, startInputId) {
        let scramblerInputContactId = startInputId
        let enigma = startEnigma
        let pathAdded = false
        while (enigma != null) {
            let enigmaWireStatus = this.enigmaWireStatuses[enigma.index]
            if (!enigmaWireStatus.scramblerInputContactIds.includes(scramblerInputContactId)) {
                pathAdded = true
                // false: do no step rotors
                // true: skip plugboard
                let encipherResult = enigma.encipherWireId(scramblerInputContactId, false, true)
                let scramblerOutputContactId = encipherResult[0]
                enigmaWireStatus.addEncipherPath(scramblerInputContactId, scramblerOutputContactId)

                // mark active wires in letter cables
                // 'cA' means that the 'c' wire in the 'A' cable is active
                if (enigma.first) {
                    let inputWireLetter = idToCharToken(scramblerInputContactId).toLowerCase()
                    let inputCableLetter = enigma.inputLetter
                    // old
                    this.activeLetterCableWires.push(inputWireLetter.concat(inputCableLetter))
                    // new
                    this.activeLetterCableWires2[inputCableLetter].push(scramblerInputContactId)
                }

                let outputWireLetter = idToCharToken(scramblerOutputContactId).toLowerCase()
                let outputCableLetter = enigma.outputLetter
                //old
                this.activeLetterCableWires.push(outputWireLetter.concat(outputCableLetter))
                // new
                this.activeLetterCableWires2[outputCableLetter].push(scramblerOutputContactId)

                // prepare for next iteration
                scramblerInputContactId = scramblerOutputContactId
                enigma = enigma.next
            } else {
                break
            }
        }
        return pathAdded
    }

    // based on new output for a scrambler/enigma, calculate new activePaths for all preceeding scramblers/enigmas
    processEnigmaOutput(startEnigma, startOutputId) {
        let scramblerOutputContactId = startOutputId
        let enigma = startEnigma
        let pathAdded = false
        while (enigma != null) {
            let enigmaWireStatus = this.enigmaWireStatuses[enigma.index]
            if (!enigmaWireStatus.scramblerOutputContactIds.includes(scramblerOutputContactId)) {
                pathAdded = true
                // false: do no step rotors
                // true: skip plugboard
                let encipherResult = enigma.encipherWireId(scramblerOutputContactId, false, true)
                let scramblerInputContactId = encipherResult[0]
                enigmaWireStatus.addEncipherPath(scramblerInputContactId, scramblerOutputContactId)

                // mark active wires in letter cables
                // 'cA' means that the 'c' wire in the 'A' cable is active
                if (enigma.first) {
                    let outputWireLetter = idToCharToken(scramblerOutputContactId).toLowerCase()
                    let outputCableLetter = enigma.outputLetter
                    // old
                    this.activeLetterCableWires.push(outputWireLetter.concat(outputCableLetter))
                    // new
                    this.activeLetterCableWires2[outputCableLetter].push(scramblerOutputContactId)
                }

                let inputWireLetter = idToCharToken(scramblerInputContactId).toLowerCase()
                let inputCableLetter = enigma.inputLetter
                //old
                this.activeLetterCableWires.push(inputWireLetter.concat(inputCableLetter))
                // new
                this.activeLetterCableWires2[inputCableLetter].push(scramblerInputContactId)

                // prepare for next iteration
                scramblerOutputContactId = scramblerInputContactId
                enigma = enigma.previous
            } else {
                break
            }
        }
        return pathAdded
    }


    findForwardDiagonalBoardConnectionToActivate() {
        let availableConnections = ['AC', 'AE', 'AF', 'CA', 'CE', 'CF', 'EA', 'EC', 'EF', 'FA', 'FC', 'FE']

        for (let i=0; i < this.activeLetterCableWires.length; i++) {
            let activeLetterCableWire = this.activeLetterCableWires[i]
            let activeWireLetter = activeLetterCableWire[0]
            let cableLetter = activeLetterCableWire[1]
            if (
                availableConnections.includes(activeLetterCableWire.toUpperCase()) &&
                ! this.activeDiagonalBoardConnections.includes(activeLetterCableWire.toUpperCase()) &&
                this.bombe.menuLetters.indexOf(cableLetter) < this.bombe.menuLetters.indexOf(activeWireLetter.toUpperCase()) ) {
                return activeLetterCableWire.toUpperCase()
             }
        }
        return null
    }

    findBackwardDiagonalBoardConnectionToActivate() {
        let availableConnections = ['FA', 'FC', 'FE', 'AC', 'AE', 'AF', 'CA', 'CE', 'CF', 'EA', 'EC', 'EF']

        for (let i=0; i < this.activeLetterCableWires.length; i++) {
            let activeLetterCableWire = this.activeLetterCableWires[i]
            let activeWireLetter = activeLetterCableWire[0]
            let cableLetter = activeLetterCableWire[1]
//            console.log(this.bombe.menuLetters)
//            console.log(activeLetterCableWire + ", " + activeWireLetter + ", " + cableLetter)
            if (
                availableConnections.includes(activeLetterCableWire.toUpperCase()) &&
                ! this.activeDiagonalBoardConnections.includes(activeLetterCableWire.toUpperCase()) &&
                this.bombe.menuLetters.indexOf(cableLetter) > this.bombe.menuLetters.indexOf(activeWireLetter.toUpperCase()) ) {
                return activeLetterCableWire.toUpperCase()
             }
        }
        return null
    }

    // feedback: boolean
    findDiagonalBoardConnectionToActivate(feedforward) {
//        console.log("findDiagonalBoardConnectionToActivate")
        let availableConnections = ['FA', 'FC', 'FE', 'AC', 'AE', 'AF', 'CA', 'CE', 'CF', 'EA', 'EC', 'EF']

        for (let i=0; i < availableConnections.length; i++) {
            // e.g. FA
            let availableConn = availableConnections[i]

            if (!this.activeDiagonalBoardConnections.includes(availableConn)) {
                // 'fA'
                let letterCableWire1 = availableConn[0].toLowerCase().concat(availableConn[1])
                // 'aF'
                let letterCableWire2 = availableConn[1].toLowerCase().concat(availableConn[0])

                if (this.activeLetterCableWires.includes(letterCableWire1) && !this.activeLetterCableWires.includes(letterCableWire2)) {
                    if (feedforward) {
                        if (this.bombe.menuLetters.indexOf(letterCableWire1[1]) < this.bombe.menuLetters.indexOf(letterCableWire2[1]) ) {
                            return letterCableWire2
                        }
                    } else {
                        if (this.bombe.menuLetters.indexOf(letterCableWire1[1]) > this.bombe.menuLetters.indexOf(letterCableWire2[1]) ) {
                            return letterCableWire2
                        }
                    }
                }
                if (this.activeLetterCableWires.includes(letterCableWire2) && !this.activeLetterCableWires.includes(letterCableWire1)) {
                    if (feedforward) {
                        if (this.bombe.menuLetters.indexOf(letterCableWire2[1]) < this.bombe.menuLetters.indexOf(letterCableWire1[1]) ) {
                            return letterCableWire1
                        }
                    } else {
                        if (this.bombe.menuLetters.indexOf(letterCableWire2[1]) > this.bombe.menuLetters.indexOf(letterCableWire1[1]) ) {
                            return letterCableWire1
                        }
                    }
                }

                // both wires are already active
                if (this.activeLetterCableWires.includes(letterCableWire2) && this.activeLetterCableWires.includes(letterCableWire1)) {
                    return letterCableWire1
                }
            }
        }
        return null
    }


}