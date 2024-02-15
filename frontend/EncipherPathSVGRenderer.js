class EncipherPathSVGRenderer {

    constructor(reflectorSVGRenderer) {
        this.reflectorSVGRenderer = reflectorSVGRenderer
    }

    // add a container node to the svg, where we can later add the actual enciphered path to
    // in terms of z-order, this container sits between the layers which need to appear
    // behind the enciphered path and the layers which need to appear in front of it
    addPathContainer(svg) {
        addGroupNode (svg, "encipherPathContainer")
    }

    drawEncipherPath(encipherWireMap, animationEndCallback, alphabetSize=26) {
        let group = document.getElementById("encipherPathContainer")

        let path = this.determineSvgPathData(encipherWireMap, LEFT_MARGIN, TOP_MARGIN, alphabetSize)
        // console.log("encipherPath: " + path)

        // https://jakearchibald.com/2013/animated-line-drawing-svg/
        var pathNode = addPathNode (group, path, "encipherPath", "encipherPath")
        var length = pathNode.getTotalLength();
        // Clear any previous transition
        pathNode.style.transition = pathNode.style.WebkitTransition = 'none';
        // Set up the starting positions
        pathNode.style.strokeDasharray = length + ' ' + length;
        pathNode.style.strokeDashoffset = length;
        // Trigger a layout so styles are calculated & the browser
        // picks up the starting position before animating
        pathNode.getBoundingClientRect();
        // Define our transition
        pathNode.style.transition = pathNode.style.WebkitTransition = 'stroke-dashoffset 1s ease-in-out';
        // Go!
        pathNode.style.strokeDashoffset = '0';

        pathNode.addEventListener("transitionend", animationEndCallback);
    }

    removePath(svg) {
        let path = document.getElementById("encipherPath")
        if (path) path.remove()
    }

    determineSvgPathData(encipherWireMap, xOffset, yOffset, alphabetSize) {
        yOffset += LEADING_STRAIGHT + CONNECTOR_RADIUS
        // inbound: path starting point: keyboard
        // X needs to take into account the shifted positions of half of the keyboard keys
        let inbound_KB_out_X = xOffset + 5 * (COMPONENT_WIDTH + SPACING) + (idToDisplayIndex(encipherWireMap.inbound_KBtoPBWireId, alphabetSize) % 2) * KEY_SHIFT
        let inbound_KBtoPB_Y = yOffset + idToDisplayIndex(encipherWireMap.inbound_KBtoPBWireId, alphabetSize) * SINGLE
        let path = `M ${inbound_KB_out_X} ${inbound_KBtoPB_Y} `

        // inbound: keyboard to plugboard
        let inbound_PB_in_X = xOffset + 4 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH + CONNECTOR_RADIUS
        path += `H ${inbound_PB_in_X} `

        // inbound: plugboard-in to plugboard-out (start with a short horizontal wire)
        path += `h -${CONNECTOR_RADIUS}`
        let inbound_PB_out_X = xOffset + 4 * (COMPONENT_WIDTH + SPACING) + CONNECTOR_RADIUS
        let inbound_PBtoR3_Y = yOffset + idToDisplayIndex(encipherWireMap.inbound_PBtoR3WireId, alphabetSize) * SINGLE
        path += `L ${inbound_PB_out_X} ${inbound_PBtoR3_Y} `

        // inbound: plugboard to rotor3
        let inbound_R3_in_X = xOffset + 3 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH + CONNECTOR_RADIUS
        path += `H ${inbound_R3_in_X}`

        // inbound: rotor3-in to rotor3-out (start with a short horizontal wire)
        path += `h -${CONNECTOR_RADIUS}`
        let inbound_R3_out_X = xOffset + 3 * (COMPONENT_WIDTH + SPACING) + CONNECTOR_RADIUS
        let inbound_R3toR2_Y = yOffset + idToDisplayIndex(encipherWireMap.inbound_R3toR2WireId, alphabetSize) * SINGLE
        path += `L ${inbound_R3_out_X} ${inbound_R3toR2_Y} `

        // inbound: rotor3 to rotor2
        let inbound_R2_in_X = xOffset + 2 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH + CONNECTOR_RADIUS
        path += `H ${inbound_R2_in_X}`

        // inbound: rotor2-in to rotor2-out (start with a short horizontal wire)
        path += `h -${CONNECTOR_RADIUS}`
        let inbound_R2_out_X = xOffset + 2 * (COMPONENT_WIDTH + SPACING) + CONNECTOR_RADIUS
        let inbound_R2toR1_Y = yOffset + idToDisplayIndex(encipherWireMap.inbound_R2toR1WireId, alphabetSize) * SINGLE
        path += `L ${inbound_R2_out_X} ${inbound_R2toR1_Y} `

        // inbound: rotor2 to rotor1
        let inbound_R1_in_X = xOffset + 1 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH + CONNECTOR_RADIUS
        path += `H ${inbound_R1_in_X}`

        // inbound: rotor1-in to rotor1-out (start with a short horizontal wire)
        path += `h -${CONNECTOR_RADIUS}`
        let inbound_R1_out_X = xOffset + 1 * (COMPONENT_WIDTH + SPACING) + CONNECTOR_RADIUS
        let inbound_R1toRefl_Y = yOffset + idToDisplayIndex(encipherWireMap.inbound_R1toReflWireId, alphabetSize) * SINGLE
        path += `L ${inbound_R1_out_X} ${inbound_R1toRefl_Y} `

        // inbound: rotor1 to reflector (taking into account how the wiring is drawn by the reflector renderer)
        let internalWireLength = this.reflectorSVGRenderer.internalWireLengths[encipherWireMap.inbound_R1toReflWireId]
        let inbound_Refl_in_X = xOffset + COMPONENT_WIDTH + CONNECTOR_RADIUS - internalWireLength
        path += `H ${inbound_Refl_in_X}`

        // within reflector
        let outbound_ReflToR1_Y = yOffset + idToDisplayIndex(encipherWireMap.outbound_ReflToR1WireId, alphabetSize) * SINGLE
        path += `V ${outbound_ReflToR1_Y}`

        // outbound: reflector to rotor1 (includes the horizontal part within the reflector of length 4*UNIT)
        let outbound_Refl_out_X = xOffset + 1 * (COMPONENT_WIDTH + SPACING) + CONNECTOR_RADIUS
        path += `H ${outbound_Refl_out_X}`

        // outbound: rotor1-in to rotor1-out (end with a short horizontal wire)
        let outbound_R1_out_X = xOffset + 1 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
        let outbound_R1ToR2_Y = yOffset + idToDisplayIndex(encipherWireMap.outbound_R1ToR2WireId, alphabetSize) * SINGLE
        path += `L ${outbound_R1_out_X} ${outbound_R1ToR2_Y} `
        path += `h ${CONNECTOR_RADIUS}`

        // outbound: rotor1 to rotor2
        let outbound_R2_in_X = xOffset + 2 * (COMPONENT_WIDTH + SPACING) + CONNECTOR_RADIUS
        path += `H ${outbound_R2_in_X}`

        // outbound: rotor2-in to rotor2-out (end with a short horizontal wire)
        let outbound_R2_out_X = xOffset + 2 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
        let outbound_R2ToR3_Y = yOffset + idToDisplayIndex(encipherWireMap.outbound_R2toR3WireId, alphabetSize) * SINGLE
        path += `L ${outbound_R2_out_X} ${outbound_R2ToR3_Y} `
        path += `h ${CONNECTOR_RADIUS}`

        // outbound: rotor2 to rotor3
        let outbound_R3_in_X = xOffset + 3 * (COMPONENT_WIDTH + SPACING) + CONNECTOR_RADIUS
        path += `H ${outbound_R3_in_X}`

        // outbound: rotor3-in to rotor3-out (end with a short horizontal wire)
        let outbound_R3_out_X = xOffset + 3 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
        let outbound_R3ToPB_Y = yOffset + idToDisplayIndex(encipherWireMap.outbound_R3toPBWireId, alphabetSize) * SINGLE
        path += `L ${outbound_R3_out_X} ${outbound_R3ToPB_Y} `
        path += `h ${CONNECTOR_RADIUS}`

        // outbound: rotor3 to plugboard
        let outbound_PB_in_X = xOffset + 4 * (COMPONENT_WIDTH + SPACING) + CONNECTOR_RADIUS
        path += `H ${outbound_PB_in_X}`

        // outbound: plugboard-in to plugboard-out (end with a short horizontal wire)
        let outbound_PB_out_X = xOffset + 4 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
        let outbound_PBToKB_Y = yOffset + idToDisplayIndex(encipherWireMap.outbound_PBtoKBWireId, alphabetSize) * SINGLE
        path += `L ${outbound_PB_out_X} ${outbound_PBToKB_Y} `
        path += `h ${CONNECTOR_RADIUS}`

        // outbound: plugboard to keyboard
        // X needs to take into account the shifted positions of half of the keyboard keys
        let outbound_KB_out_X = xOffset + 5 * (COMPONENT_WIDTH + SPACING) + (idToDisplayIndex(encipherWireMap.outbound_PBtoKBWireId, alphabetSize) % 2) * KEY_SHIFT
        path += `H ${outbound_KB_out_X}`

        return path
    }


    // add a path segment to the array of pathSegments
    // each path segment is itself an array:
    //   0: name of the segment
    //   1: x-coordinate of segment endpoint
    //   2: y-coordinate of segment endpoint
    addPathSegment(pathSegments, name, x, y) {
        pathSegments.push([name, x, y])
    }

}
