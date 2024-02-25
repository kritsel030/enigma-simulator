class BombePathSVGRenderer {

    constructor(bombe) {
        this.bombe = bombe
        this.enigmaList = bombe.scramblers
    }

    draw(parent, variant) {
        let pathGroup = addGroupNode(parent, `${parent.id}_path`, 0, 0)
            
        let previousFirst = null
        let previousLast = null
        let x = LEFT_MARGIN
        for (let i=0; i<this.enigmaList.length; i++) {
            let first = i == 0
            let last = i == this.enigmaList.length-1
            x += first ? 0 : enigmaWidth(variant, previousFirst, previousLast) + enigmaGap(variant, previousFirst, previousLast)
            let group = addGroupNode (pathGroup, i, x, TOP_MARGIN)
            let enigma = this.enigmaList[i]

            if (enigma.plugboardInputId != null) {
                this.drawEnigmaPath(enigma, i, group, variant, first, last, enigma.plugboardInputId, null) 
            } else if (enigma.scramblerInputIds != null) {
                for (let i=0; i < enigma.scramblerInputIds.length; i++) {
                    this.drawEnigmaPath(enigma, i, group, variant, first, last, null, enigma.scramblerInputIds[i]) 
                }
            }

            // feedback output to input
            previousFirst = first
            previousLast = last
        }

        if (renderOutputToInputWires(variant)) {
            let lastEnigma = this.enigmaList[this.enigmaList.length-1]
            for (let i = 0; i < lastEnigma.scramblerOutputIds.length; i++) {
                let outputId = lastEnigma.scramblerOutputIds[i]
                let path = SVGPathService.outputToInputPath(outputId, variant, this.bombe.scramblers.length)
                addPathNode (parent, path, `${parent.id}_path_output_to_input`, "electricalPath") 
            }
        }
    }

    // only one of the inputs will have a value
    drawEnigmaPath(enigma, enigmaId, group, variant, first, last, plugboardInputId, scramblerInputId) { 
        // console.log(`BombePathSVGRenderer.drawEnigmaPath(enigmaId=${enigmaId}, variant=${variant}, first=${first}, last=${last}, plugboardInputId=${plugboardInputId}, scramblerInputId=${scramblerInputId}) `)  
        let encipherResult = null
        if (plugboardInputId!= null) {
            encipherResult = enigma.encipherWireId(plugboardInputId, false, false)    
        } else {
            encipherResult = enigma.encipherWireId(scramblerInputId, false, true)  
        }
        let wireMap = encipherResult[1]
        let ys = yValues(variant)

        // inbound: path from inputControl
        if (renderInputControlWires(variant, first)) {
            if (this.bombe.inputControlIds.includes(wireMap.inbound_PBtoR3WireId)) {
                let path = SVGPathService.inputControlPathPlusStartCoordinate(wireMap.inbound_PBtoR3WireId, variant, first)[0]
                if (path) addPathNode (group, path, `${parent.id}_enigma${enigmaId}_path_input_control`, "electricalPath") 
            }
        }

        // inbound: path between plugboard and (virtual) keyboard
        if (renderKeyOrLightboard(variant, first, last, true)) {
            this.drawPrePath(group, variant, enigma, enigmaId, first, last, wireMap, ys.keyboardY, ys.plugboardY, 0)
        }
        // outbound: path between plugboard and (virtual) keyboard
        if (renderKeyOrLightboard(variant, first, last, false)) {
            this.drawPostPath(group, variant, enigma, enigmaId, first, last, wireMap, ys.keyboardY, ys.plugboardY, 0) 
        }

        // inbound: input to horizontal connector
        if (renderHorizontalConnector(variant, first, last, true)) {
            let path = SVGPathService.horConnectorInputOutputPath(variant, first, last, true, wireMap.inbound_PBtoR3WireId, true)
            addPathNode (group, path, `${parent.id}_enigma${enigmaId}_path_connector_input`, "electricalPath") 
        }
        // outbound: output from horizontal connector
        if (renderHorizontalConnector(variant, first, last, false)) {
            let path = SVGPathService.horConnectorInputOutputPath(variant, first, last, false, wireMap.outbound_R3toPBWireId, true)
            addPathNode (group, path, `${parent.id}_enigma${enigmaId}_path_connector_output`, "electricalPath") 
        }

        // inbound: input to vertical connector
        if (renderVerticalConnector(variant, first, last, true)) {
            let path = SVGPathService.vertConnectorInputOutputPath(variant, first, last, true, wireMap.inbound_PBtoR3WireId, true)
            addPathNode (group, path, `${parent.id}_enigma${enigmaId}_path_connector_input`, "electricalPath") 
        }
        // outbound: output from vertical connector
        if (renderVerticalConnector(variant, first, last, false)) {
            let path = SVGPathService.vertConnectorInputOutputPath(variant, first, last, false, wireMap.outbound_R3toPBWireId, true)
            addPathNode (group, path, `${parent.id}_enigma${enigmaId}_path_connector_output`, "electricalPath") 
        }

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
            let path = SVGPathService.scramblerPath(wireMap.inbound_PBtoR3WireId, wireMap.outbound_R3toPBWireId, variant, first, last)
            addPathNode (group, path, `${parent.id}_enigma${enigmaId}_path_scrambler`, "electricalPath") 
        }
    }

    // draw the first (black) section of the inbound path: from keyboard to exitpoint of the plugboard
    drawPrePath(parent, variant, enigma, enigmaIndex, first, last, wireMap, keyboardY, plugboardY, xOffset) {
        // inbound: keyboard to plugboard in
        let kbPath = SVGPathService.plugboardToKeyboardPath(wireMap.inbound_KBtoPBWireId, variant, first, last, true)
        addPathNode (parent, kbPath, `${parent.id}_enigma${enigmaIndex}_path_pre_key`, "electricalPath_pre_post") 
        let pbPath = SVGPathService.plugboardPath(wireMap.inbound_PBtoR3WireId, wireMap.inbound_KBtoPBWireId, variant, first, last, true, true)
        // inbound: plugboard in to plugboard out
        addPathNode (parent, pbPath, `${parent.id}_enigma${enigmaIndex}_path_pre_plugboard`, "electricalPath_pre_post") 
    }

    // draw the last (black) section of the outbound path: from inputpoint of the plugboard to keyboard
    drawPostPath(parent, variant, enigma, enigmaIndex, first, last, wireMap, keyboardY, plugboardY, xOffset) {
        // outboung: keyboard to plugboard out
        let kbPath = SVGPathService.plugboardToKeyboardPath(wireMap.outbound_PBtoKBWireId, variant, first, last, false)
        addPathNode (parent, kbPath, `${parent.id}_enigma${enigmaIndex}_path_pre_key`, "electricalPath_pre_post") 
        let pbPath = SVGPathService.plugboardPath(wireMap.outbound_R3toPBWireId, wireMap.outbound_PBtoKBWireId, variant, first, last, false, true)
        // outboard: plugboard in to plugboard out
        addPathNode (parent, pbPath, `${parent.id}_enigma${enigmaIndex}_path_pre_plugboard`, "electricalPath_pre_post") 

    }

    // get the x coordinate for the diagonal board wire which starts in the given wire in the given cable
    dbXs(wireLetter, cableLetter) {
        let cableIndices = []
        let startPos = 0
        while (true) {
            let cableIndex = this.bombe.menuLetters.indexOf(cable, startPos)
            if (cableIndex >= 0){
                cableIndices.push(cableIndex)
            } else {
                break
            }
            startPos = cableIndex+1
        }
    }

    // get the y coordinate for the bottom of the diagonal board wire which starts in the given wire in the given cable
    dbBottomY(wireLetter, cableLetter) {
    
    } 

    // get the y coordinate for the top of the diagonal board wire which starts in the given wire in the given cable
    dbTopX(wireLetter, cableLetter) {

    }   
    

}
