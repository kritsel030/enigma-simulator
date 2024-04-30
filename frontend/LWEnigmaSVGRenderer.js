// Renders a Letchworth enigma as an SVG image
class LWEnigmaSVGRenderer {

    constructor(enigma, bombeRenderer) {
        this.enigma = enigma
        this.reflectorRenderer = new LWReflectorSVGRenderer(this.enigma.reflector)
        this.drum1Renderer = new LWDrumSVGRenderer(this.enigma, 1)
        this.drum2Renderer = new LWDrumSVGRenderer(this.enigma, 2)
        this.drum3Renderer = new LWDrumSVGRenderer(this.enigma, 3)
        this.plugboardRenderer = new LWPlugboardSVGRenderer(this.enigma.plugboard, this.enigma.getAlphabetSize())
        this.keyAndLightboardRenderer = new LWKeyAndLightboardSVGRenderer(enigma.index, enigma.getAlphabetSize())
        this.bombeRenderer = bombeRenderer
        this.bombe = this.bombeRenderer.bombe
        this.animationDisabled = false
    }

    drawBackground(parent, enigmaId, variant, first, last, x, y) {
        let group = addGroupNode (parent, enigmaId, x, y)
        let ys = yValues(variant)

        // reflector
        if (renderReflector(variant)) {
            this.reflectorRenderer.drawBackground(group, variant, first, last, reflectorXOffset(variant, first, last), ys.reflectorY)
        }

        // plugboard
        if (renderPlugboard(variant, first, last, true)) {
            this.plugboardRenderer.drawBackground(group, variant, plugboardXOffset(variant, first, last, true), ys.plugboardY )
        }
        if (renderPlugboard(variant, first, last, false)) {
            this.plugboardRenderer.drawBackground(group, variant, plugboardXOffset(variant, first, last, false), ys.plugboardY )
        }

        // label for the cable leading into / out of the enigma
        this.drawCableLabels(group, enigmaId, variant, first, last)
    }

    drawForeground(parent, enigmaId, variant, first, last, x, y) {
        let group = addGroupNode (parent, enigmaId, x, y)
        let ys = yValues(variant)
//        let group = addGroupNode (parent, enigmaId, x, y)
        let enigmaWireStatus = bombe.pathFinder.enigmaWireStatuses[enigmaId]

        // label underneath the enigma
        this.drawEnigmaLabel(group, enigmaId, variant, first, last)

        // reflector
        if (renderReflector(variant)) {
            this.reflectorRenderer.drawForeground(group, variant, first, last, reflectorXOffset(variant, first, last), ys.reflectorY)
        }

        // draw the 3 drums
        if (renderDrums(variant)) {
            let xOffsetInbound = drumXOffset(variant, first, last, true, enigmaId)
            let side = renderDrumsIntegrated(variant) ? "center" : "left"
            this.drum1Renderer.draw(group, `${group.id}_drum1_${side}`, variant, xOffsetInbound, ys.drum1Y)
            this.drum2Renderer.draw(group, `${group.id}_drum2_${side}`, variant, xOffsetInbound, ys.drum2Y)
            this.drum3Renderer.draw(group, `${group.id}_drum3_${side}`, variant, xOffsetInbound, ys.drum3Y)
            if (renderDrumsSeparate(variant)) {
                let xOffsetOutbound = drumXOffset(variant, first, last, false, enigmaId)
                side = "right"
                this.drum1Renderer.draw(group, `${group.id}_drum1_${side}`, variant, xOffsetOutbound, ys.drum1Y)
                this.drum2Renderer.draw(group, `${group.id}_drum2_${side}`, variant, xOffsetOutbound, ys.drum2Y)
                this.drum3Renderer.draw(group, `${group.id}_drum3_${side}`, variant, xOffsetOutbound, ys.drum3Y)
            }
        }

        // draw horizontal input/output connector
        if (renderHorizontalConnector(variant, first, last, true)) {
            this.drawHorizontalConnector(group, "left", horConnectorXOffset(variant, first, last, true), ys.horConnectorY)
        }
        if (renderHorizontalConnector(variant, first, last, false)) {
            this.drawHorizontalConnector(group, "right", horConnectorXOffset(variant, first, last, false), ys.horConnectorY)
        }

        // draw vertical input/output connector
        if (renderVerticalConnector(variant, first, last, true)) {
            this.drawVerticalConnector(group, "left", vertConnectorXOffset(variant, first, last, true, enigmaId), ys.vertConnectorY)
        }
        if (renderVerticalConnector(variant, first, last, false)) {
            this.drawVerticalConnector(group, "right", vertConnectorXOffset(variant, first, last, false, enigmaId), ys.vertConnectorY)
        }

        if (renderPlugboard(variant, first, last, true)) {
            this.plugboardRenderer.drawForeground(group, variant, first, last, true, plugboardXOffset(variant, first, last, true), ys.plugboardY )
        }
        if (renderPlugboard(variant, first, last, false)) {
            this.plugboardRenderer.drawForeground(group, variant, first, last, false, plugboardXOffset(variant, first, last, false), ys.plugboardY )
        }

        // draw keyboard/lightboard
        if (renderKeyOrLightboard(variant, first, last, true)) {
            let leftXOffset = keyboardXOffset(variant, first, last, true)
            let leftProps = keyOrLightboardProperties(variant, first, last, true)
            let plugboardInputId = enigmaWireStatus.activePaths.length > 0 ? enigmaWireStatus.activePaths[0].inboundPbInputContactId : null
            let plugboardOutputId = null
            this.keyAndLightboardRenderer.draw(group, `${group.id}_keyboard_left`, variant, leftProps, leftXOffset, ys.keyboardY, plugboardInputId, plugboardOutputId)
            // add the onclick event handler to the group of keys
            if (leftProps.variant == "clickableKeyboard") {
                group.addEventListener('click', bombeRenderer.handleKeyClick.bind(bombeRenderer), false)
            } 
        }
        if (renderKeyOrLightboard(variant, first, last, false)) {
            let rightProps = keyOrLightboardProperties(variant, first, last, false)
            let rightXOffset = keyboardXOffset(variant, first, last, false)
            let plugboardInputId = null
            let plugboardOutputId = enigmaWireStatus.activePaths.length > 0 ? enigmaWireStatus.activePaths[0].outboundPbOutputContactId : null
            this.keyAndLightboardRenderer.draw(group, `${group.id}_keyboard_right`, variant, rightProps, rightXOffset, ys.keyboardY, plugboardInputId, plugboardOutputId)
        }

        // draw letters on input and output wires
        this.drawWireLetters(group, enigmaId, variant, first, last)
    }

    drawWiring(parent, enigmaId, variant, first, last, x, y) {
        let group = addGroupNode (parent, enigmaId, x, y)
        let ys = yValues(variant)

        // wires between plugboard and (virtual) keyboard
        if (renderKeyOrLightboard(variant, first, last, true)) {
            this.drawPlugboardToKeyboardConnections(group, variant, "left", first, last, true, alphabetSize)
        }
        if (renderKeyOrLightboard(variant, first, last, false)) {
            this.drawPlugboardToKeyboardConnections(group, variant, "right", first, last, false, alphabetSize)
        }

        // wires to/from horizontal connectors
        if (renderHorConnectorInputOutput(variant, first, last, true) ) {
            this.drawHorConnectorInputOutput(group, variant, "left", first, last, true, horConnectorXOffset(variant, first, last, true), ys.horConnectorY + CONNECTOR_HEIGHT, alphabetSize)
        }
        if (renderHorConnectorInputOutput(variant, first, last, false)) {
            this.drawHorConnectorInputOutput(group, variant, "right", first, last, false, horConnectorXOffset(variant, first, last, false), ys.horConnectorY + CONNECTOR_HEIGHT, alphabetSize)
        }

        // wires to/from vertical connectors
        if (renderVertConnectorInputOutput(variant, first, last, true)) {
            this.drawVertConnectorInputOutput(group, variant, "left", first, last, true, vertConnectorXOffset(variant, first, last, true), ys.vertConnectorY, alphabetSize)
        }
        if (renderVertConnectorInputOutput(variant, first, last, false)) {
            this.drawVertConnectorInputOutput(group, variant, "right", first, last, false, vertConnectorXOffset(variant, first, last, false) + CONNECTOR_HEIGHT, ys.vertConnectorY, alphabetSize)
        }

        // diagonal input control wires connecting to the horizontal input wires of the input vertical connector of the first enigma
        if (renderInputControlWires(variant, first)) {
            this.drawInputControl(group, variant)
        }

        // wires in a scrambler
        // each wire's start is a letchworth enigma input, and the end is the enigma's output
        // (a letchworth enigma being an enigma without a plugboard)
        if (renderScramblerWires(variant)) {
            this.drawScramblerWires(group, variant, first, last)
        }

        // draw plugboard(s)
        if (renderPlugboard(variant, first, last, true)) {
            this.plugboardRenderer.drawWiring(group, variant, first, last, true, plugboardXOffset(variant, first, last, true), ys.plugboardY )
        }
        if (renderPlugboard(variant, first, last, false)) {
            this.plugboardRenderer.drawWiring(group, variant, first, last, false, plugboardXOffset(variant, first, last, false), ys.plugboardY )
        }

        // draw cables between reflector, drums and connectors
        if (renderDrums(variant)) {
            // cable through scrambler (connecting drums and reflector)
            let scramblerCablePath = SVGPathService.drumsAndReflectorCablePath(variant, first, last)
            addPathNode (group, scramblerCablePath, `${group.id}_cable_scrambler`, "cable")
            // cable from input connector to drum3
            let connectorCablePathLeft = SVGPathService.connectorCablePath(variant, first, last, true)
            addPathNode (group, connectorCablePathLeft, `${group.id}_cable_connector_left`, "cable")
            // cable from drum3 to output connector
            let connectorCablePathRight = SVGPathService.connectorCablePath(variant, first, last, false)
            addPathNode (group, connectorCablePathRight, `${group.id}_cable_connector_right`, "cable")
        }
    }

    drawEnigmaLabel(parent, enigmaId, variant, first, last) {
        let ys = yValues(variant)
        let labelY = TOP_MARGIN
        if (renderKeyOrLightboard(variant, first, last, false) || renderKeyOrLightboard(variant, first, last, true)) {
            labelY += ys.keyboardY + 60
//        } else if (renderPlugboard(variant, first, last, false) || renderPlugboard(variant, first, last, true)){
//            labelY += ys.plugboardY
        } else if (renderHorizontalConnector(variant, first, last, false) || renderHorizontalConnector(variant, first, last, true)){
            labelY += ys.plugboardY + 4*COMPONENT_DISTANCE
        } else {
            labelY += ys.vertConnectorY + CONNECTOR_WIDTH + 2*COMPONENT_DISTANCE
        }

        // labels are horizontally centered based on the x property
        // add labels underneath each enigma
        let labelX = enigmaCenterXOffset(variant, first, last)
        addTextNode(parent, `enigma ${enigmaId+1}`, `scrambler_${enigmaId+1}_label`, "componentLabelSmall", labelX, labelY)
        addTextNode(parent, `start +${this.bombe.scramblerOffsets[enigmaId]}`, `scrambler_${enigmaId+1}_position_label`, "componentLabel", labelX, labelY+20)
    }

    drawCableLabels(parent, enigmaId, variant, first, last) {
        // label the cable coming into the first enigma
        if (first) {
            // labels are horizontally centered based on the x property
            let cableLabelY = this.calculateCableLabelY(variant, first, last, true)
            let cableLabelX = 0
            if (renderHorizontalConnector(variant, first, last, true)) {
                cableLabelX = -15
            } else if (renderVerticalConnector(variant, first, last, true)) {
                cableLabelX = -100
            }
            addTextNode(parent, `${this.bombe.menuLetters[0]}`, `cable_0`, "cableLabel", cableLabelX, cableLabelY)
        }

        // label the cable leading to the next enigma
        // labels are horizontally centered based on the x property
        let cableLabelY = this.calculateCableLabelY(variant, first, last, false)
        let cableLabelX = 0
        if (renderHorizontalConnector(variant, first, last, false)) {
            cableLabelX = enigmaWidth(variant, first, last) + 0.5*HORIZONTAL_CONNECTOR_GAP
        } else if (renderVerticalConnector(variant, first, last, false)) {
            cableLabelX = enigmaWidth(variant, first, last) + 0.5*VERTICAL_CONNECTOR_GAP
        }
        addTextNode(parent, `${this.bombe.menuLetters[enigmaId+1]}`, `cable_${enigmaId+1}`, "cableLabel", cableLabelX, cableLabelY)
    }

    calculateCableLabelY(variant, first, last, inbound) {
        let ys = yValues(variant)
        if (renderHorizontalConnector(variant, first, last, inbound)) {
            return TOP_MARGIN + ys.plugboardY + PLUGBOARD_HEIGHT
        } else if (renderVerticalConnector(variant, first, last, inbound)) {
            return TOP_MARGIN + ys.vertConnectorY + 16*UNIT
        }
    }

    // draw the connector to access the enigma
    drawHorizontalConnector(group, side, x, y) {
        let path = `M ${x} ${y}`
        path += `h ${CONNECTOR_WIDTH} `
        path += `v ${CONNECTOR_HEIGHT} `
        path += `h -${CONNECTOR_WIDTH} Z`
        addPathNode (group, path, `${parent.id}_connector_${side}`, "connector")
    }

    drawVerticalConnector(group, side, x, y) {
        let path = `M ${x} ${y}`
        path += `h ${CONNECTOR_HEIGHT} `
        path += `v ${CONNECTOR_WIDTH} `
        path += `h -${CONNECTOR_HEIGHT} Z`
        addPathNode (group, path, `${parent.id}_connector_${side}`, "connector")
    }

    drawPlugboardToKeyboardConnections(parent, variant, side, first, last, inbound, alphabetSize) {
        let group = addGroupNode(parent, `${parent.id}_pbTokb_${side}`)
        for(let i = 0; i<alphabetSize; i++) {
            let path = SVGPathService.plugboardToKeyboardPath(i, variant, first, last, inbound)
            addPathNode (group, path, `${group.id}_pbTokb_${i}`, "wire")
        }
    }

    // draw the input/output wires of a horizontal connector
    drawHorConnectorInputOutput(parent, variant, side, first, last, inbound, x, y, alphabetSize) {
        let group = addGroupNode(parent, `${parent.id}_connwires_${side}`, x, y)
        for(let i = 0; i<alphabetSize; i++) {
            let path = SVGPathService.horConnectorInputOutputPath(variant, first, last, inbound, i)
            addPathNode (group, path, `${group.id}_${i}`, "wire")
        }
    }

     // draw the input/output wires of a vertical connector
     drawVertConnectorInputOutput(parent, variant, side, first, last, inbound, x, y, alphabetSize) {
        let group = addGroupNode(parent, `${parent.id}_connwires_${side}`, x, y)
        for(let i = 0; i<alphabetSize; i++) {
            let path = SVGPathService.vertConnectorInputOutputPath(variant, first, last, inbound, i)
            addPathNode (group, path, `${group.id}_${i}`, "wire")
        }
    }   

    drawScramblerWires(parent, variant, first, last) {
        let group = addGroupNode(parent, `${parent.id}_scrambler`)
        for (let i=0; i<alphabetSize; i++) {
            let inputId = i
            let outputId = this.enigma.encipherWireId(inputId, false, true)[0]
            let path = SVGPathService.scramblerPath(i, outputId, variant, first, last)
            addPathNode (group, path, `${group.id}_wire_${i}`, "wire")
        }
    }

    drawInputControl(parent, variant) {
        let group = addGroupNode(parent, `${parent.id}_inputcontrol`)
        for (let i=0; i < alphabetSize; i++) {
            let pathAndStartCoordinate = SVGPathService.inputControlPathPlusStartCoordinate(i, variant)
            // let y = ys.vertConnectorY + 0.5*WIRE_DISTANCE + i*WIRE_DISTANCE
            // let distance = (alphabetSize - i + 1) * WIRE_DISTANCE
            // let path = `M ${x} ${y} l -${distance} ${distance}`
            if (pathAndStartCoordinate[0]) {
                addPathNode (group, pathAndStartCoordinate[0], `inputcontrol_${i}`, "wire")
            }
            addCircleNode(group, `input_${i}`, "activate", 1.5*UNIT, pathAndStartCoordinate[1].x, pathAndStartCoordinate[1].y)
            addTextNode(group, "+", `input_${i}`, "activate", pathAndStartCoordinate[1].x, pathAndStartCoordinate[1].y+1)

            // wire letter
            addTextNode(group, idToCharToken(i).toLowerCase(), `input_${i}_letter`, "wireLetter", pathAndStartCoordinate[1].x, pathAndStartCoordinate[1].y+16)
        }
        group.addEventListener('click', bombeRenderer.handleInputControlClick.bind(bombeRenderer), false)
    }


    drawVerticalCable(group, side, x, fromY, toY) {
        addPathNode (group, `M ${x} ${fromY} V ${toY}`, `${group.id}_vertcable_${side}`, "cable")
    }
    
    drawHorizontalCable(group, side, x, fromY) {
        if (side=="left") {
            x = x - (COMPONENT_DISTANCE)
        } else {
            x = x + DRUM_WIDTH
        }
        let h = COMPONENT_DISTANCE
        addPathNode (group, `M ${x} ${fromY} h ${h}`, `${group.id}_horcable_${side}`, "cable")
    }

    drawCable(group, side, fromX, fromY, toX, toY) {
        addPathNode (group, `M ${fromX} ${fromY} L ${toX} ${toY}`, `${group.id}_cable_${side}`, "cable")
    }

//    drawWireLetters(parent, enigmaId, variant, first, last, x, y) {
    drawWireLetters(parent, enigmaId, variant, first, last) {
        let group = parent
        let ys = yValues(variant)
        // input connector
        if (renderHorizontalConnector(variant, first, last, true) ) {
            if (!renderInputControlWires(variant, first)) {
                let xOffset = horConnectorXOffset(variant, first, last, true)
                let y = ys.horConnectorY + CONNECTOR_HEIGHT + 12
                for (let i=0; i < alphabetSize; i++) {
                    let x = xOffset + 0.5*WIRE_DISTANCE + i*WIRE_DISTANCE
                    addCircleNode(group, `input_${i}_i`, "wireLetter", 1.5*UNIT, x, y)
                    addTextNode(group, idToCharToken(i).toLowerCase(), `input_${i}_i`, "wireLetter", x, y)
                }
            }

        } else if (first && renderVerticalConnector(variant, first, last, true) ) {
            let x = vertConnectorXOffset(variant, first, last, true) - 36
            let yOffset = ys.vertConnectorY
            for (let i=0; i < alphabetSize; i++) {
                let y = yOffset + 0.5*WIRE_DISTANCE + i*WIRE_DISTANCE
                addCircleNode(group, `input_${i}_i`, "wireLetter", 1.5*UNIT, x, y)
                addTextNode(group, idToCharToken(i).toLowerCase(), `input_${i}_i`, "wireLetter", x, y)
            }
        }
        // outbound connector
        if (renderHorizontalConnector(variant, first, last, false) ) {
            let xOffset = horConnectorXOffset(variant, first, last, false)
            let y = ys.horConnectorY + CONNECTOR_HEIGHT + 12
            for (let i=0; i < alphabetSize; i++) {
                let x = xOffset + 0.5*WIRE_DISTANCE + i*WIRE_DISTANCE
                addCircleNode(group, `input_${i}_i`, "wireLetter", 1.5*UNIT, x, y)
                addTextNode(group, idToCharToken(i).toLowerCase(), `input_${i}_i`, "wireLetter", x, y)
            }
        } else if (renderVerticalConnector(variant, first, last, false) ) {
            let x = vertConnectorXOffset(variant, first, last, false) + CONNECTOR_HEIGHT + 12
            let yOffset = ys.vertConnectorY
            for (let i=0; i < alphabetSize; i++) {
                let y = yOffset + 0.5*WIRE_DISTANCE + i*WIRE_DISTANCE
                addCircleNode(group, `input_${i}_i`, "wireLetter", 1.5*UNIT, x, y)
                addTextNode(group, idToCharToken(i).toLowerCase(), `input_${i}_i`, "wireLetter", x, y)
            }
        }
   }


    resetKeyboard() {
        this.pressedKeyId = null
        this.lightedKeyId = null
        this.enigma.reset()
    }

}
