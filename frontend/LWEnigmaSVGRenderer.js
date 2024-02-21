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

        // vertical wires downwards from a horizontal connector
        if (renderHorizontalConnector(variant, first, last, true)) {
            this.drawConnectorToPlugboardConnections(group, "left", horConnectorXOffset(variant, first, last, true), ys.horConnectorY + CONNECTOR_HEIGHT, alphabetSize)
        }
        if (renderHorizontalConnector(variant, first, last, false)) {
            this.drawConnectorToPlugboardConnections(group, "right", horConnectorXOffset(variant, first, last, false), ys.horConnectorY + CONNECTOR_HEIGHT, alphabetSize)
        }

        // wires downwards (vertically or diagonally) from plugboard to keyboard
        if (renderKeyOrLightboard(variant, first, last, true) || keyOrLightboardType(variant, first, last, true)=="integrated") {
            this.drawPlugboardToKeyboardConnections(group, variant, "left", first, last, true, plugboardXOffset(variant, first, last, true), ys.plugboardY + PLUGBOARD_HEIGHT, alphabetSize)
        }
        if (renderKeyOrLightboard(variant, first, last, false)) {
            this.drawPlugboardToKeyboardConnections(group, variant, "right", first, last, false, plugboardXOffset(variant, first, last, false), ys.plugboardY + PLUGBOARD_HEIGHT, alphabetSize)
        }

        // wires between enigmas between horizontal connectors
        if (variant!="variantG"){
            let connY = ys.interEnigmaConnectionsY
            if (! renderKeyOrLightboard(variant, first, last, true) && renderHorizontalConnector(variant, first, last, true) && keyOrLightboardType(variant, first, last, true)!="integrated") {
                this.drawInterEnigmaHorizontalConnectorConnections(group, variant, "left", plugboardXOffset(variant, first, last, true), connY, alphabetSize)
            }
            if (! renderKeyOrLightboard(variant, first, last, false) && renderHorizontalConnector(variant, first, last, false)) {
                this.drawInterEnigmaHorizontalConnectorConnections(group, variant, "right", plugboardXOffset(variant, first, last, false), connY, alphabetSize)
            }
        }

        // wires between enigmas between vertical connectors
        if (renderVerticalConnector(variant, first, last, true)) {
            this.drawInterEnigmaVerticalConnectorConnections(group, "left", vertConnectorXOffset(variant, first, last, true) - 0.5*VERTICAL_CONNECTOR_GAP, ys.vertConnectorY, alphabetSize)
        }
        if (renderVerticalConnector(variant, first, last, false)) {
            this.drawInterEnigmaVerticalConnectorConnections(group, "right", vertConnectorXOffset(variant, first, last, false) + CONNECTOR_HEIGHT, ys.vertConnectorY, alphabetSize)
        }

        // wires in a scrambler
        // each wire's start is a letchworth enigma input, and the end is the enigma's output
        // (a letchworth enigma being an enigma without a plugboard)
        if (renderScramblerWires(variant)) {
            let yOffset = ys.vertConnectorY + 0.5 * WIRE_DISTANCE
            let xOffset = vertConnectorXOffset(variant, first, last, true) + CONNECTOR_HEIGHT
            for (let i=0; i<alphabetSize; i++) {
                let inputId = i
                let fromY = yOffset + inputId * WIRE_DISTANCE
                // do not step rotors and skip the plugboard
                let outputId = this.enigma.encipherWireId(inputId, false, true)[0]
                let toX = xOffset + SCRAMBLER_WIDTH
                let toY = yOffset + outputId * WIRE_DISTANCE
                let path = `M ${xOffset} ${fromY} L ${toX} ${toY} `
                addPathNode (group, path, `scrambler_${enigmaId}_wire_${i}`, "wire")
            }
        } 

        // draw cables between reflector, drums and connectors
        if (renderDrums(variant)) {
            // cable from reflector to horizontal connector or drum 3
            let toY= renderDrumsSeparate(variant) ? ys.horConnectorY : ys.drum3Y + DRUM_RADIUS
            this.drawVerticalCable(group, "left", verticalCableXOffset(variant, first, last, true), REFLECTOR_HEIGHT, toY)
            this.drawVerticalCable(group, "right", verticalCableXOffset(variant, first, last, false), REFLECTOR_HEIGHT, toY)

            // cable from drum 3 to connector - diagonal
            let startX = drumXOffset(variant, first, last, true)
            if (renderHorizontalConnector(variant, first, last, true) && renderDrumsIntegrated(variant)) {
                // diagonal cable, left side on to lower left, we start from the center of the drum
                this.drawCable(group, "left", startX+DRUM_RADIUS, ys.drum3Y+DRUM_RADIUS, startX-COMPONENT_DISTANCE, ys.horConnectorY+UNIT)
            }
            if (renderHorizontalConnector(variant, first, last, false) && renderDrumsIntegrated(variant)) {
                // diagonal cable, right side on to right, we start from the center of the drum
                this.drawCable(group, "right", startX+DRUM_RADIUS, ys.drum3Y+DRUM_RADIUS, startX+2*DRUM_RADIUS+COMPONENT_DISTANCE, ys.horConnectorY+UNIT)
            }

            // horizontal cable from drum 3 to vertical connector
            let horCableY = ys.drum3Y + DRUM_RADIUS
            if (renderVerticalConnector(variant, first, last, true)) {
                let fromX = vertConnectorXOffset(variant, first, last, true) + CONNECTOR_HEIGHT
                this.drawCable(group, "left", fromX, horCableY, fromX+COMPONENT_DISTANCE, horCableY)
            }
            if (renderVerticalConnector(variant, first, last, false)) {
                let fromX = vertConnectorXOffset(variant, first, last, false) - COMPONENT_DISTANCE
                this.drawCable(group, "right", fromX, horCableY, fromX+COMPONENT_DISTANCE, horCableY)
            }
        }

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

        if (renderHorizontalConnector(variant, first, last, true)) {
            this.drawHorizontalConnector(group, "left", horConnectorXOffset(variant, first, last, true), ys.horConnectorY)
        }
        if (renderHorizontalConnector(variant, first, last, false)) {
            this.drawHorizontalConnector(group, "right", horConnectorXOffset(variant, first, last, false), ys.horConnectorY)
        }

        if (renderVerticalConnector(variant, first, last, true)) {
            this.drawVerticalConnector(group, "left", vertConnectorXOffset(variant, first, last, true, enigmaId), ys.vertConnectorY)
        }
        if (renderVerticalConnector(variant, first, last, false)) {
            this.drawVerticalConnector(group, "right", vertConnectorXOffset(variant, first, last, false, enigmaId), ys.vertConnectorY)
        }

        let doRenderPlugboardInbound = renderPlugboard(variant, first, last, true)
        if (doRenderPlugboardInbound) {
            this.plugboardRenderer.draw(group, `${group.id}_plugboard_left`, variant, first, last, plugboardXOffset(variant, first, last, true), ys.plugboardY )
        }
        let doRenderPlugboardOutbound = renderPlugboard(variant, first, last, false)
        if (doRenderPlugboardOutbound) {
            this.plugboardRenderer.draw(group, `${group.id}_plugboard_right`, variant, first, last, plugboardXOffset(variant, first, last, false), ys.plugboardY )
        }

        if (renderKeyOrLightboard(variant, first, last, true)) {
            let leftXOffset = keyboardXOffset(variant, first, last, true)
            let leftType = keyOrLightboardType(variant, first, last, true)
            this.keyAndLightboardRenderer.draw(group, `${group.id}_keyboard_left`, variant, leftType, leftXOffset, ys.keyboardY, this.pressedKeyId, this.lightedKeyId)
            if (leftType == "clickableKeyboard") {
                group.addEventListener('click', bombeRenderer.handleKeyClick.bind(bombeRenderer), false)
            }
            
        }
        if (renderKeyOrLightboard(variant, first, last, false)) {
            let rightType = keyOrLightboardType(variant, first, last, false)
            let rightXOffset = keyboardXOffset(variant, first, last, false)
            this.keyAndLightboardRenderer.draw(group, `${group.id}_keyboard_right`, variant, rightType, rightXOffset, ys.keyboardY, this.pressedKeyId, this.lightedKeyId)
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

    drawConnectorToPlugboardConnections(parent, side, x, y, alphabetSize) {
        let group = addGroupNode(parent, `${parent.id}_pbwires_${side}`, x, y)
        let fromY = 0
        for(let i = 0; i<alphabetSize; i++) {
            let position = idToDisplayIndex(i, alphabetSize)
            let x = 0.5*WIRE_DISTANCE + position*WIRE_DISTANCE
            addPathNode (group, `M ${x} ${0} v ${CONN_TO_PB_DISTANCE}`, `${group.id}_${i}`, "wire")
        }
    }

    drawPlugboardToKeyboardConnections(parent, variant, side, first, last, inbound, x, y, alphabetSize) {
        let group = addGroupNode(parent, `${parent.id}_keywires_${side}`, x, y)
        let fromY = 0
        for(let i = 0; i<alphabetSize; i++) {
            let position = idToDisplayIndex(i, alphabetSize)
            let fromX = 0.5*WIRE_DISTANCE + position*WIRE_DISTANCE
            let shiftedKeyboard = keyOrLightboardType(variant, first, last, inbound) == "integrated"
            let toX = !shiftedKeyboard ? 0 : (inbound ? - (0.5 * (PLUGBOARD_WIDTH + HORIZONTAL_CONNECTOR_GAP)) : (0.5 * (PLUGBOARD_WIDTH + HORIZONTAL_CONNECTOR_GAP)))
            let toY = !shiftedKeyboard ?  PB_TO_KB_DISTANCE + (i%2) * KEY_SHIFT : PB_TO_KB_DISTANCE
            addPathNode (group, `M ${fromX} ${fromY} l ${toX} ${toY}`, `${group.id}_${i}`, "wire")
        }
    }

    drawInterEnigmaHorizontalConnectorConnections(parent, variant, side, x, y, alphabetSize) {
        let group = addGroupNode(parent, `${parent.id}_pbtopb_${side}`, x, y)
        let startY = 0
        for(let i = 0; i<alphabetSize; i++) {
            let startX = 0.5*WIRE_DISTANCE + i*WIRE_DISTANCE
            let v = 0.5*i*WIRE_DISTANCE + WIRE_DISTANCE
            let h = 0.5 * (PLUGBOARD_WIDTH + ENIGMA_DISTANCE)
            if (side == "left") h = -h
            addPathNode (group, `M ${startX} ${startY} v ${v} h ${h}`, `${group.id}_${i}`, "wire")
        }
    }

    drawInterEnigmaVerticalConnectorConnections(parent, side, x, y, alphabetSize) {
        let group = addGroupNode(parent, `${parent.id}_conntoconn_${side}`, x, y)
        let h = 0.5*VERTICAL_CONNECTOR_GAP
        for(let i = 0; i<alphabetSize; i++) {
            let startY = 0.5*WIRE_DISTANCE + i*WIRE_DISTANCE
            addPathNode (group, `M ${0} ${startY} h ${h}`, `${group.id}_${i}`, "wire")
        }
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
