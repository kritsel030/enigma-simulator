// Renders a Letchworth enigma as an SVG image
class LWEnigmaSVGRenderer {

    constructor(enigma, bombeRenderer) {
        this.enigma = enigma
        this.reflectorRenderer = new LWReflectorSVGRenderer(this.enigma.reflector)
        this.drum1Renderer = new LWDrumSVGRenderer(this.enigma, 1)
        this.drum2Renderer = new LWDrumSVGRenderer(this.enigma, 2)
        this.drum3Renderer = new LWDrumSVGRenderer(this.enigma, 3)
        this.plugboardRenderer = new LWPlugboardSVGRenderer(this.enigma.plugboard, this.enigma.getAlphabetSize())
        this.keyAndLightboardRenderer = new LWKeyAndLightboardSVGRenderer(enigma.getAlphabetSize())
//        this.encipherPathRenderer = new EncipherPathSVGRenderer(this.reflectorRenderer)
        this.bombeRenderer = bombeRenderer
        this.bombe = this.bombeRenderer.bombe
        this.animationDisabled = false
    }

    draw(parent, id, variant, first, last, x, y  ) {
        this.id = id
        this.drawSimple(parent, id, variant, first, last, x, y)
//        this.drawLayered(svg)
    }

    drawSimple(parent, enigmaId, variant, first, last, x, y) {
        let ys = yValues(variant)
        let group = addGroupNode (parent, enigmaId, x, y)

        if (renderReflector(variant)) {
            this.reflectorRenderer.draw(group, variant, reflectorXOffset(variant, first, last), ys.reflectorY)
        }

        // wires between plugboard and (virtual) keyboard
        if (renderKeyOrLightboard(variant, first, last, true)) {
            this.drawPlugboardToKeyboardConnections(group, variant, "left", first, last, true, alphabetSize)
        }
        if (renderKeyOrLightboard(variant, first, last, false)) {
            this.drawPlugboardToKeyboardConnections(group, variant, "right", first, last, false, alphabetSize)
        }

        // wires to/from horizontal connectors 
        if (renderHorizontalConnector(variant, first, last, true) ) {
            this.drawHorConnectorInputOutput(group, variant, "left", first, last, true, horConnectorXOffset(variant, first, last, true), ys.horConnectorY + CONNECTOR_HEIGHT, alphabetSize)
        }
        if (renderHorizontalConnector(variant, first, last, false)) {
            this.drawHorConnectorInputOutput(group, variant, "right", first, last, false, horConnectorXOffset(variant, first, last, false), ys.horConnectorY + CONNECTOR_HEIGHT, alphabetSize)
        }

        // wires to/from vertical connectors
        if (renderVerticalConnector(variant, first, last, true)) {
            this.drawVertConnectorInputOutput(group, variant, "left", first, last, true, vertConnectorXOffset(variant, first, last, true), ys.vertConnectorY, alphabetSize)
        }
        if (renderVerticalConnector(variant, first, last, false)) {
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

        // draw plugboard(s)
        if (renderPlugboard(variant, first, last, true)) {
            this.plugboardRenderer.draw(group, `${group.id}_plugboard_left`, variant, first, last, true, plugboardXOffset(variant, first, last, true), ys.plugboardY )
        }
        if (renderPlugboard(variant, first, last, false)) {
            this.plugboardRenderer.draw(group, `${group.id}_plugboard_right`, variant, first, last, false, plugboardXOffset(variant, first, last, false), ys.plugboardY )
        }

        // draw keyboard/lightboard
        if (renderKeyOrLightboard(variant, first, last, true)) {
            let leftXOffset = keyboardXOffset(variant, first, last, true)
            let leftProps = keyOrLightboardProperties(variant, first, last, true)
            this.keyAndLightboardRenderer.draw(group, `${group.id}_keyboard_left`, variant, leftProps, leftXOffset, ys.keyboardY, this.enigma.plugboardInputId, this.enigma.plugboardOutputId)
            // add the onclick event handler to the group of keys
            if (leftProps.variant == "clickableKeyboard") {
                group.addEventListener('click', bombeRenderer.handleKeyClick.bind(bombeRenderer), false)
            } 
        }
        if (renderKeyOrLightboard(variant, first, last, false)) {
            let rightProps = keyOrLightboardProperties(variant, first, last, false)
            let rightXOffset = keyboardXOffset(variant, first, last, false)
            this.keyAndLightboardRenderer.draw(group, `${group.id}_keyboard_right`, variant, rightProps, rightXOffset, ys.keyboardY, this.enigma.plugboardInputId, this.enigma.plugboardOutputId)
        }

        // draw a label
        let labelY = 10
        if (renderKeyOrLightboard(variant, first, last, false) || renderKeyOrLightboard(variant, first, last, true)) {
            labelY += ys.keyboardY + 2*PLUGBOARD_HEIGHT + COMPONENT_DISTANCE
        } else if (renderPlugboard(variant, first, last, false) || renderPlugboard(variant, first, last, true)){
            labelY += ys.plugboardY + PLUGBOARD_HEIGHT
        } else if (renderHorizontalConnector(variant, first, last, false) || renderHorizontalConnector(variant, first, last, true)){
            labelY += ys.horConnectorY + CONNECTOR_HEIGHT + 4 * COMPONENT_DISTANCE
        } else {
            labelY = ys.vertConnectorY + CONNECTOR_WIDTH + 2*COMPONENT_DISTANCE
        }

        // labels are horizontally centered based on the x property
        // add labels underneath each enigma
        let labelX = enigmaCenterXOffset(variant, first, last)
        addTextNode(group, `scrambler ${enigmaId+1}`, `scrambler_${enigmaId+1}_label`, "componentLabelSmall", labelX, labelY)
        addTextNode(group, `start +${this.bombe.scramblerOffsets[enigmaId]}`, `scrambler_${enigmaId+1}_position_label`, "componentLabel", labelX, labelY+COMPONENT_DISTANCE)
    
        // label the cable coming into the first enigma
        if (first && !renderKeyOrLightboard(variant, first, last, true)) {
            // labels are horizontally centered based on the x property
            let cableLabelX = - 0.5*VERTICAL_CONNECTOR_GAP
            let cableLabelY = 0
            
            if (renderHorizontalConnector(variant, first, last, true)) {
                if (renderPlugboard(variant, first, last, true)) {
                    cableLabelY = ys.plugboardY + PLUGBOARD_HEIGHT + 0.5*alphabetSize*WIRE_DISTANCE + 2*COMPONENT_DISTANCE
                } else {
                    cableLabelY = ys.horConnectorY + CONNECTOR_HEIGHT + WIRE_DISTANCE + 0.5*alphabetSize*WIRE_DISTANCE + 2*COMPONENT_DISTANCE
                }
            } else if (renderVerticalConnector(variant, first, last, true)) {
                cableLabelY = ys.vertConnectorY + CONNECTOR_WIDTH + 4*COMPONENT_DISTANCE
            }
            addTextNode(group, `${this.bombe.menuLetters[0]}-cable`, `cable_0`, "componentLabel", cableLabelX, cableLabelY)
        }

        // label the cable to the next enigma
        if (!renderKeyOrLightboard(variant, first, last, false) || keyOrLightboardProperties(variant, first, last, false).virtual) {
            // labels are horizontally centered based on the x property
            let cableLabelX = enigmaWidth(variant, first, last) + 0.5*VERTICAL_CONNECTOR_GAP
            let cableLabelY = 0
            
            if (renderHorizontalConnector(variant, first, last, false)) {
                if (renderPlugboard(variant, first, last, false)) {
                    cableLabelY = ys.plugboardY + PLUGBOARD_HEIGHT + 0.5*alphabetSize*WIRE_DISTANCE + 2*COMPONENT_DISTANCE
                } else {
                    cableLabelY = ys.horConnectorY + CONNECTOR_HEIGHT + WIRE_DISTANCE + 0.5*alphabetSize*WIRE_DISTANCE + COMPONENT_DISTANCE
                }
            } else if (renderVerticalConnector(variant, first, last, false)) {
                cableLabelY = ys.vertConnectorY + CONNECTOR_WIDTH + 4*COMPONENT_DISTANCE
            }
            addTextNode(group, `${this.bombe.menuLetters[enigmaId+1]}-cable`, `cable_${enigmaId+1}`, "componentLabel", cableLabelX, cableLabelY)
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

    resetKeyboard() {
        this.pressedKeyId = null
        this.lightedKeyId = null
        this.enigma.reset()
    }

}
