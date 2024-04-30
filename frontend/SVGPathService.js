// helper methods to generate the 'd' attribute of SVG path elements
class SVGPathService {

    // wireId: value between 0 and 5
    static plugboardToKeyboardPath(wireId, variant, first, last, inbound, pathStart='M' ) {
//        console.log(`SVGPathService.plugboardToKeyboardPath(wireId=${wireId}), variant=${variant}, first=${first}, last=${last}, inbound=${inbound})`)
        let xOffset = plugboardXOffset(variant, first, last, inbound)
        let yOffset = yValues(variant).plugboardY + PLUGBOARD_HEIGHT
        let position = idToDisplayIndex(wireId, alphabetSize)
        //console.log(`position=${position}, wireId=${wireId}, alphabetSize=${alphabetSize}`)
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
            let h = 0.5 * (PLUGBOARD_WIDTH + HORIZONTAL_CONNECTOR_GAP)
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
            let extraV = 0.5*wireId*WIRE_DISTANCE + 6*UNIT
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
        let toX = xOffset + SCHEMA_ENIGMA_WIDTH
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

    // voltage feedback: connect output from the last enigma in the cycle to the first enigma in the cycle
   static outputToInputPath2(wireId, variant, lastEnigmaInCycleIndex) {
        let ys = yValues(variant)
        // start: right-hand side of right vertical connector
        let yStartOffset = TOP_MARGIN + ys.vertConnectorY
        let yStart = yStartOffset + 0.5*WIRE_DISTANCE + wireId * WIRE_DISTANCE
        let xStart = LEFT_MARGIN +
            (lastEnigmaInCycleIndex+1) * enigmaWidth(variant, false, false) +
            lastEnigmaInCycleIndex*VERTICAL_CONNECTOR_GAP

        let xRight = xStart + 2*WIRE_DISTANCE + (alphabetSize-wireId-1)*WIRE_DISTANCE
        let yDown = yStartOffset + alphabetSize*WIRE_DISTANCE + WIRE_DISTANCE + (alphabetSize-wireId-1)*WIRE_DISTANCE + 4*COMPONENT_DISTANCE
        let horSpaceToConnector = (["scrambler_multi_line_scanning", "scrambler_full_menu"].includes(variant)) ? alphabetSize*WIRE_DISTANCE : (alphabetSize+3)*WIRE_DISTANCE
        let xLeft = LEFT_MARGIN + vertConnectorXOffset(variant, true, false, true) - horSpaceToConnector - (alphabetSize-wireId-1)*WIRE_DISTANCE
        let xRight2 = LEFT_MARGIN

        return `M ${xStart} ${yStart} H ${xRight} V ${yDown} H ${xLeft} V ${yStart} H ${xRight2}`

    }

    static inputControlPathPlusStartCoordinate(wireId, variant) {
        let horizontalConnector = renderHorizontalConnector(variant, true, false, true)
        let ys = yValues(variant)
        let xStart = horizontalConnector ?
            horConnectorXOffset(variant, true, false, true)  + 0.5*WIRE_DISTANCE + wireId*WIRE_DISTANCE :
            vertConnectorXOffset(variant, true, false, true) 
        let yStart = horizontalConnector ?
            ys.horConnectorY + CONNECTOR_HEIGHT + CONN_TO_PB_DISTANCE :
            ys.vertConnectorY + 0.5*WIRE_DISTANCE + wireId*WIRE_DISTANCE
        
        let h = horizontalConnector ? 0 : -0.5*VERTICAL_CONNECTOR_GAP
        if (["scrambler_multi_line_scanning", "scrambler_full_menu"].includes(variant))
            h = -(alphabetSize+3)*WIRE_DISTANCE -5*WIRE_DISTANCE
        else if (["scrambler_diagonal_board"].includes(variant))
            h = -(alphabetSize*2 + 3) * WIRE_DISTANCE

        let hToStart = horizontalConnector ? 0 : (alphabetSize-wireId) * WIRE_DISTANCE 
        let yToStart = horizontalConnector ? 0 : (alphabetSize-wireId) * WIRE_DISTANCE
        let xEnd = xStart + h - hToStart
        let yEnd = yStart + yToStart   
        // let startCoordinate = {x: horizontalConnector? xStart : xStart-distance, y: horizontalConnector ? yStart : yStart+distance}
        let startCoordinate = {x: xEnd, y: yEnd}
         
        // in the horizontal connector scenario, no additional wires need to be drawn, 
        // we only need to know at what coordinate to add the '+' button
        let path = horizontalConnector ? null : `M ${xStart} ${yStart} h ${h} L ${xEnd} ${yEnd} `
        return [path, startCoordinate]
    }

    // if input = AB
    // then draw a diagonal board path from the a-wire in the B-cable to the b-wire in the A-cable
    static diagonalBoardPath(letter1, letter2, variant, menuLetters, activeLetterCableWires) {
        let fromWireId = charToId(letter1) // 0 (for A)
        let fromCableLetter = letter2      // B
//        let fromActive = activeLetterCableWires[fromCableLetter][fromWireId] != null
        let toWireId = charToId(letter2) // 1 (for B)
        let toCableLetter = letter1      // A
//        let toActive = activeLetterCableWires[toCableLetter][toWireId] != null

        // check if the 'from' cable is depicted to the right of the 'to' cable in the visualisation
        // meaning we will be drawing a forward diagonalBoard path from aB to bA
        let forward = menuLetters.indexOf(fromCableLetter) < menuLetters.indexOf(toCableLetter)

//        console.log(`fromWireId=${fromWireId}, fromCableLetter=${fromCableLetter}, toWireId=${toWireId}, toCableLetter=${toCableLetter}`)
        let startX = dbX(fromCableLetter, fromWireId, menuLetters)
        let startY = wireY(fromWireId)
        let dbY1 = dbY(fromWireId)

        let dbY2 = dbY(toWireId)
        let endX = dbX(toCableLetter, toWireId, menuLetters)
        let endY = wireY(toWireId)

        return `M ${startX} ${startY} V ${dbY1} L ${endX} ${dbY2} V ${endY}`
    }
}