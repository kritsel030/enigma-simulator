// helper methods to generate the 'd' attribute of SVG path elements
class SVGPathService {

    // wireId: value between 0 and 5
    static plugboardToKeyboardPath(wireId, variant, first, last, inbound, pathStart='M' ) {
        let xOffset = plugboardXOffset(variant, first, last, inbound)
        let yOffset = yValues(variant).plugboardY + PLUGBOARD_HEIGHT
        let position = idToDisplayIndex(wireId, alphabetSize)
        let fromX = xOffset + 0.5*WIRE_DISTANCE + position*WIRE_DISTANCE
        let path = `${pathStart} ${fromX} ${yOffset} `

        let keyBoardProps = keyOrLightboardProperties(variant, first, last, inbound)
        if (!keyBoardProps.virtual) {
            // vertical or diagonal path to the keyboard
            let toX = !keyBoardProps.positionShifted ? 0 : (inbound ? - (0.5 * (PLUGBOARD_WIDTH + HORIZONTAL_CONNECTOR_GAP)) : (0.5 * (PLUGBOARD_WIDTH + HORIZONTAL_CONNECTOR_GAP)))
            let toY = !keyBoardProps.positionShifted ?  PB_TO_KB_DISTANCE + (wireId%2) * KEY_SHIFT : PB_TO_KB_DISTANCE
            path += `l ${toX} ${toY} `
        } else {
            // path with an angle to the virtual keyboard
            let v = 0.5*wireId*WIRE_DISTANCE + WIRE_DISTANCE 
            let h = 0.5 * (PLUGBOARD_WIDTH + ENIGMA_DISTANCE)
            if (inbound) h = -h
            path += ` v ${v} h ${h} `
        }
        return path
    }

    static plugboardPath(topWireId, bottomWireId, variant, first, last, inbound, determinePosition=false) {
        let xOffset = determinePosition ? plugboardXOffset(variant, first, last, inbound) : 0
        let yOffset = determinePosition ? yValues(variant).plugboardY : 0
        let topY = yOffset + 0
        let bottomY = topY + PLUGBOARD_HEIGHT
        let topX = xOffset + 0.5*WIRE_DISTANCE + topWireId*WIRE_DISTANCE
        let bottomX = xOffset + 0.5*WIRE_DISTANCE + bottomWireId*WIRE_DISTANCE
        return `M ${topX} ${topY} L ${bottomX} ${bottomY}`
    }

    static horConnectorInputOutputPath(variant, first, last, inbound, wireId, determinePosition=false) {
        let xOffset = determinePosition ? horConnectorXOffset(variant, first, last, inbound) : 0
        let x = xOffset + 0.5*WIRE_DISTANCE + wireId*WIRE_DISTANCE
        let y = determinePosition ? yValues(variant).horConnectorY + CONNECTOR_HEIGHT: 0
        let path = `M ${x} ${y} `

        if (renderPlugboard(variant, first, last, inbound) || (first && inbound) || (last && !inbound)) {
            path += `v ${CONN_TO_PB_DISTANCE} `
        } else {
            let extraV = 0.5*wireId*WIRE_DISTANCE + WIRE_DISTANCE 
            let h = 0.5 * (PLUGBOARD_WIDTH + HORIZONTAL_CONNECTOR_GAP)
            if (inbound) h = -h
            path += `v ${extraV} h ${h} `
        }
        return path 
    }

    static vertConnectorInputOutputPath(variant, first, last, inbound, wireId, determinePosition=false) {
        let x = determinePosition ? vertConnectorXOffset(variant, first, last, inbound) : 0
        if (!inbound && determinePosition) {
            x += CONNECTOR_HEIGHT
        }
        let yOffset = determinePosition ? yValues(variant).vertConnectorY : 0
        let y = yOffset + 0.5*WIRE_DISTANCE + wireId* WIRE_DISTANCE
        let h = 0.5 * VERTICAL_CONNECTOR_GAP
        if (inbound) h = -h
        let path = `M ${x} ${y} h ${h} `
        return path 
    }

    static scramblerPath(inputWireId, outputWireId, variant, first, last) {
        let yOffset = yValues(variant).vertConnectorY
        let xOffset = vertConnectorXOffset(variant, first, last, true) + CONNECTOR_HEIGHT
        let fromY = yOffset + 0.5*WIRE_DISTANCE + inputWireId * WIRE_DISTANCE
        let toX = xOffset + SCRAMBLER_WIDTH
        let toY = yOffset + 0.5*WIRE_DISTANCE + outputWireId * WIRE_DISTANCE
        return `M ${xOffset} ${fromY} L ${toX} ${toY} `
    }

    // start in drum 3 on the left, upwards to reflector, horizontally in reflector, and down towards drum 3 on the right
    static drumsAndReflectorCablePath(variant, first, last) {
        let ys = yValues(variant)
        let drum3LeftX = verticalCableXOffset(variant, first, last, true)
        let drum3RightX = verticalCableXOffset(variant, first, last, false)
        let drum3Y= renderDrumsSeparate(variant) ? ys.horConnectorY : ys.drum3Y + DRUM_RADIUS
        let reflectorY = ys.reflectorY + 0.5*REFLECTOR_HEIGHT

        return `M ${drum3LeftX} ${drum3Y} V ${reflectorY} H ${drum3RightX}, V ${drum3Y}`
    }

    // cable path between vertical/horizontal connector and drum
    static connectorCablePath(variant, first, last, inbound) {
        let ys = yValues(variant)
        if (renderVerticalConnector(variant, first, last, inbound)) {
            // a horizontal cable from reflector to drum
            let horCableY = ys.drum3Y + DRUM_RADIUS
            let x = vertConnectorXOffset(variant, first, last, inbound) 
            if (inbound) x += CONNECTOR_HEIGHT
            let h = inbound ? COMPONENT_DISTANCE : -COMPONENT_DISTANCE
            return `M ${x} ${horCableY} h ${h} `

        } else if (renderHorizontalConnector(variant, first, last, inbound)) {
            let startX = drumXOffset(variant, first, last, true) + DRUM_RADIUS
            let startY = ys.drum3Y + DRUM_RADIUS
            if (renderDrumsIntegrated(variant)) {
                // diagonal cable, from center of the drum to center of the horizontal connector
                let toY = ys.horConnectorY+UNIT // extra unit is necessary for correct layout
                let toX = horConnectorXOffset(variant, first, last, inbound) + 0.5*CONNECTOR_WIDTH
                return `M ${startX} ${startY} L ${toX} ${toY} `
            } else {
                // vertical cable from drum3 to plugboard
                let toY = ys.horConnectorY
                return `M ${startX} ${startY} V ${toY} `
            }
        }
    }

    static outputToInputPath(wireId, variant, noOfEnigmas) {
        let ys = yValues(variant)
        let yOffset = TOP_MARGIN + ys.vertConnectorY + 0.5 * WIRE_DISTANCE
        let xOffset = LEFT_MARGIN + noOfEnigmas * enigmaWidth(variant, false, false) + (noOfEnigmas-1)*VERTICAL_CONNECTOR_GAP + 0.5*VERTICAL_CONNECTOR_GAP
        let fromY = yOffset + wireId * WIRE_DISTANCE
        let h1 = wireId*WIRE_DISTANCE
        let v = 2*wireId* WIRE_DISTANCE + 3*WIRE_DISTANCE
        let h2 = noOfEnigmas * enigmaWidth(variant, false, false) + (noOfEnigmas-1)*VERTICAL_CONNECTOR_GAP + 2*0.5*VERTICAL_CONNECTOR_GAP + 2*wireId* WIRE_DISTANCE 
        return `M ${xOffset} ${fromY} h ${h1} v -${v} h -${h2} v ${v} h ${h1}`
    }

    static inputControlPathPlusStartCoordinate(wireId, variant) {
        let horizontalConnector = renderHorizontalConnector(variant, true, false, true)
        let ys = yValues(variant)
        let x = horizontalConnector ?
            horConnectorXOffset(variant, true, false, true) + 0.5*WIRE_DISTANCE + wireId*WIRE_DISTANCE : 
            vertConnectorXOffset(variant, true, false, true) - 0.5*VERTICAL_CONNECTOR_GAP
        let y = horizontalConnector ? ys.horConnectorY + CONNECTOR_HEIGHT + CONN_TO_PB_DISTANCE : ys.vertConnectorY + 0.5*WIRE_DISTANCE + wireId*WIRE_DISTANCE
        let distance = (alphabetSize - wireId + 1) * WIRE_DISTANCE 
        let startCoordinate = {x: horizontalConnector? x : x-distance, y: horizontalConnector ? y : y+distance}
        // in the horizontal connector scenario, no additional wires need to be drawn, 
        // we only need to at what coordinate to add the '+' button
        let path = horizontalConnector ? null : `M ${x} ${y} l -${distance} ${distance} `
        return [path, startCoordinate]
    }
}