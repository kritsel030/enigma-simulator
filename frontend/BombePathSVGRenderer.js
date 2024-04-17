class BombePathSVGRenderer {

    constructor(bombe) {
        this.bombe = bombe
        this.enigmaList = bombe.scramblers
        this.enigmaStatusesList = bombe.pathFinder.enigmaWireStatuses
    }

    draw(parent, variant) {
        //console.log(`BombePathSVGRenderer.draw(variant=${variant} `)
//        console.log(this.enigmaList)
        let pathGroup = addGroupNode(parent, `${parent.id}_path`, 0, 0)

        let noOfScramblers = numberOfScramblersToDisplay(variant, this.bombe.menuLetters)
        for (let i=0; i<noOfScramblers; i++) {
            let first = i == 0
            let last = i == noOfScramblers-1
            let x = scramblerAbsoluteXOffset(variant, i, this.bombe.menuLetters)
            let group = addGroupNode (pathGroup, i, x, TOP_MARGIN)
            let enigma = this.enigmaList[i]
            let enigmaStatus = this.enigmaStatusesList[i]

            for (let p=0; p < enigmaStatus.activePaths.length; p++) {
                let activePath = enigmaStatus.activePaths[p]
                this.drawEnigmaPath(activePath, i, group, variant, first, last)
            }
        }

        if (renderOutputLetterCableWires(variant)) {
            let ys = yValues(variant)
            for (let i=0; i < this.bombe.menuLetters.length; i++) {
                let menuLetter = this.bombe.menuLetters[i]
                if (menuLetter != this.bombe.cycleLetter) {
                    if (this.bombe.pathFinder.activeLetterCableWires2[menuLetter].length > 0) {
                        // find the index of the enigma which has this letter as its output letter
                        let enigma = this.bombe.scramblersByOutputLetterMap[menuLetter]
                        let enigmaIndex = enigma.index
                        // the variant controls the number of enigmas shown (full menu or only the cycle part)
                        if (enigmaIndex < numberOfScramblersToDisplay(variant, this.bombe.menuLetters)) {
                            if (renderVerticalConnector(variant, false, false, false)) {
                                let x =
                                    scramblerAbsoluteXOffset(variant, enigmaIndex, this.bombe.menuLetters) +
                                    vertConnectorXOffset(variant, enigma.first, enigma.isLast(variant), false, enigmaIndex) +
                                    CONNECTOR_HEIGHT
                                for (let a=0; a < this.bombe.pathFinder.activeLetterCableWires2[menuLetter].length; a++) {
                                    let wireId = this.bombe.pathFinder.activeLetterCableWires2[menuLetter][a]
                                    let y = TOP_MARGIN + ys.vertConnectorY + 0.5*WIRE_DISTANCE + wireId*WIRE_DISTANCE
//                                    let h = enigma.isLast(variant) ? (variant=="scrambler_diagonal_board" ? WIRE_DISTANCE : 0.5*VERTICAL_CONNECTOR_GAP) : VERTICAL_CONNECTOR_GAP
                                    let h = enigma.isLast(variant) ? 2*WIRE_DISTANCE + wireId*WIRE_DISTANCE : VERTICAL_CONNECTOR_GAP
                                    let path = `M ${x} ${y} h ${h}`
                                    addPathNode (parent, path, `${parent.id}_${menuLetter}_${wireId}`, "electricalPath")
                                }
                            }
                        }
                    }
                }
            }

            // output cable of the enigma at the end of the cycle
            if (this.bombe.pathFinder.activeLetterCableWires2["#"].length > 0) {
                // find the index of the enigma which has this letter as its output letter
                let enigma = this.bombe.cycleEndEnigma
                let enigmaIndex = enigma.index
                if (renderVerticalConnector(variant, enigma.first, enigma.isLast(variant), false)) {
                    let x =
                        scramblerAbsoluteXOffset(variant, enigmaIndex, this.bombe.menuLetters) +
                        vertConnectorXOffset(variant, false, false, false, enigmaIndex) +
                        CONNECTOR_HEIGHT

                    for (let a=0; a < this.bombe.pathFinder.activeLetterCableWires2["#"].length; a++) {
                        let wireId = this.bombe.pathFinder.activeLetterCableWires2["#"][a]
                        let y = TOP_MARGIN + ys.vertConnectorY + 0.5*WIRE_DISTANCE + wireId*WIRE_DISTANCE
                        let h = this.bombe.cycleEndEnigma.isLast(variant) ? 0.5*VERTICAL_CONNECTOR_GAP : VERTICAL_CONNECTOR_GAP
                        if (variant == "scrambler_multi_line_scanning") {
                            h = WIRE_DISTANCE
                        }
                        let path = `M ${x} ${y} h ${h}`
                        addPathNode (parent, path, `${parent.id}_CYCLE_END_${wireId}`, "electricalPath")
                    }
                }
            }
        }

        if (renderOutputToInputWires(variant)) {
            for (let i = 0; i < this.bombe.pathFinder.activeCycleCableWireIds.length; i++) {
                let wireId = this.bombe.pathFinder.activeCycleCableWireIds[i]
                let path = SVGPathService.outputToInputPath2(wireId, variant, this.bombe.cycleEndEnigma.index)
                addPathNode (parent, path, `${parent.id}_path_output_to_input_${wireId}`, "electricalPath")
            }
        }

        if (renderDiagonalBoard(variant)) {
            for (let i = 0; i < this.bombe.pathFinder.activeDiagonalBoardConnections.length; i++) {
                let dbConn = this.bombe.pathFinder.activeDiagonalBoardConnections[i]
                let path = SVGPathService.diagonalBoardPath(dbConn[0], dbConn[1], variant, this.bombe.menuLetters)
                addPathNode (parent, path, `${parent.id}_dbpath_${dbConn}`, "electricalPath")
            }
        }
    }

    drawEnigmaPath(activePath, enigmaId, group, variant, first, last) {
//        console.log(`BombePathSVGRenderer.drawEnigmaPath(enigmaId=${enigmaId}, variant=${variant}, first=${first}, last=${last}) `)
        let ys = yValues(variant)

        // inbound: path from inputControl
        if (renderInputControlWires(variant, first)) {
            if (this.bombe.inputControlIds.includes(activePath.scramblerInputContactId)) {
                let path = SVGPathService.inputControlPathPlusStartCoordinate(activePath.scramblerInputContactId, variant, first)[0]
                if (path) addPathNode (group, path, `${parent.id}_enigma${enigmaId}_path_input_control`, "electricalPath")
            }
        }

        // inbound: path between plugboard and (virtual) keyboard
        if (renderKeyOrLightboard(variant, first, last, true)) {
            this.drawPrePath(group, variant, enigmaId, first, last, activePath, ys.keyboardY, ys.plugboardY, 0)
        }
        // outbound: path between plugboard and (virtual) keyboard
        if (renderKeyOrLightboard(variant, first, last, false)) {
            this.drawPostPath(group, variant, enigmaId, first, last, activePath, ys.keyboardY, ys.plugboardY, 0)
        }

        // inbound: input to horizontal connector
        if (renderHorizontalConnector(variant, first, last, true)) {
            let path = SVGPathService.horConnectorInputOutputPath(variant, first, last, true, activePath.scramblerInputContactId, true)
            addPathNode (group, path, `${parent.id}_enigma${enigmaId}_path_connector_input`, "electricalPath")
        }
        // outbound: output from horizontal connector
        if (renderHorizontalConnector(variant, first, last, false)) {
            let path = SVGPathService.horConnectorInputOutputPath(variant, first, last, false, activePath.scramblerOutputContactId, true)
            addPathNode (group, path, `${parent.id}_enigma${enigmaId}_path_connector_output`, "electricalPath")
        }

        // inbound: input to vertical connector
//        if (renderVertConnectorInputOutput(variant, first, last, true)) {
//            let path = SVGPathService.vertConnectorInputOutputPath(variant, first, last, true, activePath.scramblerInputContactId, true)
//            addPathNode (group, path, `${parent.id}_enigma${enigmaId}_path_connector_input`, "electricalPath")
//        }
//        // outbound: output from vertical connector
//        if (renderVertConnectorInputOutput(variant, first, last, false)) {
//            let path = SVGPathService.vertConnectorInputOutputPath(variant, first, last, false, activePath.scramblerOutputContactId, true)
//            addPathNode (group, path, `${parent.id}_enigma${enigmaId}_path_connector_output`, "electricalPath")
//        }

        // inbound: connector to drum3
        if (renderDrums(variant)) {
            let inboundConnectorInputPath = SVGPathService.connectorCablePath(variant, first, last, true, true)
            addPathNode (group, inboundConnectorInputPath, `${parent.id}_enigma${enigmaId}_path_connector_to_drum3`, "electricalPath")
            // outbound: connector to drum3
            let outboundConnectourOutputPath = SVGPathService.connectorCablePath(variant, first, last, false, true)
            addPathNode (group, outboundConnectourOutputPath, `${parent.id}_enigma${enigmaId}_path_connector_to_drum3`, "electricalPath")
        }

        // wire in cable connecting drums and reflector
        if (renderDrums(variant)) {
            let path = SVGPathService.drumsAndReflectorCablePath(variant, first, last)
            addPathNode (group, path, `${parent.id}_enigma${enigmaId}_path_drums_and_reflector`, "electricalPath")
        }

        // scrambler
        if (renderScramblerWires(variant)) {
            let path = SVGPathService.scramblerPath(activePath.scramblerInputContactId, activePath.scramblerOutputContactId, variant, first, last)
            addPathNode (group, path, `${parent.id}_enigma${enigmaId}_path_scrambler`, "electricalPath")
        }
    }

    drawPrePath(parent, variant, enigmaIndex, first, last, activePath, keyboardY, plugboardY, xOffset) {
        //console.log(`BombePathSVGRenderer.drawPrePath(enigmaIndex=${enigmaIndex}, variant=${variant}, first=${first}, last=${last}) `)
        // inbound: keyboard to plugboard in
        let kbPath = SVGPathService.plugboardToKeyboardPath(activePath.inboundPbInputContactId, variant, first, last, true)
        addPathNode (parent, kbPath, `${parent.id}_enigma${enigmaIndex}_path_pre_key`, "electricalPath_pre_post")
        let pbPath = SVGPathService.plugboardPath(activePath.scramblerInputContactId, activePath.inboundPbInputContactId, variant, first, last, true, true)
        // inbound: plugboard in to plugboard out
        addPathNode (parent, pbPath, `${parent.id}_enigma${enigmaIndex}_path_pre_plugboard`, "electricalPath_pre_post")
    }

    drawPostPath(parent, variant, enigmaIndex, first, last, activePath, keyboardY, plugboardY, xOffset) {
        // outboung: keyboard to plugboard out
        let kbPath = SVGPathService.plugboardToKeyboardPath(activePath.outboundPbOutputContactId, variant, first, last, false)
        addPathNode (parent, kbPath, `${parent.id}_enigma${enigmaIndex}_path_pre_key`, "electricalPath_pre_post")
        let pbPath = SVGPathService.plugboardPath(activePath.scramblerOutputContactId, activePath.outboundPbOutputContactId, variant, first, last, false, true)
        // outboard: plugboard in to plugboard out
        addPathNode (parent, pbPath, `${parent.id}_enigma${enigmaIndex}_path_pre_plugboard`, "electricalPath_pre_post")
    }

}
