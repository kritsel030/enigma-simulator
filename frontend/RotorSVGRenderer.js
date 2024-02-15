class RotorSVGRenderer {

    svgGroup

    constructor(enigma, rotorNo) {
        this.enigma = enigma
        this.rotorNo = rotorNo
    }

    draw(svg, groupId, x, y) {
        let group = addGroupNode(svg, groupId, x, y)
        this.nodesToAnimate = [group]

        this.drawBackground(group, groupId, x, y, false)
        this.drawWiring(group)
        this.drawBorders(group)
        this.drawConnectorLabels(group)
        this.drawOuterRing(group)

        // the rotorwindow is not a child element of the group but of the root svg node
        // because it must stay stationary when the group moves
        this.drawRotorWindow(svg, x, y)
    }

    // background = everything behind the encipher path
    drawBackground(parent, rotorId, x, y, createOwnGroup=true) {
//        this.nodesToAnimate = []
        // console.log(`RotorSVGRenderer.drawBackground(parent = ${parent.id}, rotorId = ${rotorId}, x = ${x}, y = ${y})`)
        if (createOwnGroup) {
            var group = addGroupNode(parent, `${rotorId}_BG_group`, x, y)
            this.nodesToAnimate = [group]
        } else {
            var group = parent
        }
        let path = "M 0 0 "
        path += `h ${COMPONENT_WIDTH} `
        path += connectorSVGPath(true, this.enigma.rotors[this.rotorNo].alphabetSize)
        path += `h -${COMPONENT_WIDTH} `
        path += connectorSVGPath(false, this.enigma.rotors[this.rotorNo].alphabetSize)

        addPathNode (group, path, `${rotorId}_BG`, "rotorBG")
    }

    // foreground = everything in front of the encipher path
    drawForeground(parent, rotorId, x, y) {
        let group = addGroupNode(parent, rotorId, x, y)
        this.nodesToAnimate.push(group)

        this.drawWiring(group)
        this.drawBorders(group)
        this.drawConnectorLabels(group)
        this.drawOuterRing(group)
        this.drawRingSettingMarker(group)
        this.drawTurnoverNotchMarker(group)

        // the rotorwindow is not a child element of the group but of the root svg node
        // because it must stay stationary when the group moves
        this.drawRotorWindow(parent, x, y)
    }

    drawWiring(parent) {
        let wiring = this.enigma.rotors[this.rotorNo].getNormalizedWiringTableRtoL()

        // p = display position index (each rotor has 26 vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor
        var fromX = COMPONENT_WIDTH + CONNECTOR_RADIUS
        var toX = CONNECTOR_RADIUS
        for(let p = 0; p<this.enigma.rotors[this.rotorNo].alphabetSize; p++) {
            var fromY = LEADING_STRAIGHT + CONNECTOR_RADIUS + p*SINGLE
            let rightRotorContactId = displayIndexToId2(p, this.enigma.rotors[this.rotorNo])
            let leftRotorContactId = (rightRotorContactId + wiring[rightRotorContactId] + this.enigma.rotors[this.rotorNo].alphabetSize) % this.enigma.rotors[this.rotorNo].alphabetSize
            let leftRotorContactPos = idToDisplayIndex2(leftRotorContactId, this.enigma.rotors[this.rotorNo])
            let toY = LEADING_STRAIGHT + CONNECTOR_RADIUS + leftRotorContactPos * SINGLE

            addPathNode (parent, `M ${fromX} ${fromY} L ${fromX-CONNECTOR_RADIUS} ${fromY} L ${toX} ${toY}`, `${parent.id}_wire_${p}`, "wire")
        }
    }

    drawBorders(parent) {
        // draw the rotor's left connectors
        addPathNode (parent, `M 0 0 ${connectorSVGPath(true, this.enigma.rotors[this.rotorNo].alphabetSize)}`, `${parent.id}_left`, "border")

        // draw the rotor's right connectors
        addPathNode (parent, `M ${COMPONENT_WIDTH} 0 ${connectorSVGPath(true, this.enigma.rotors[this.rotorNo].alphabetSize)}`, `${parent.id}_left`, "border")
    }

    drawConnectorLabels(parent) {
        // p = display position index (each rotor has 26 vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor
        for (let p=0; p<this.enigma.rotors[this.rotorNo].alphabetSize; p++) {
            var label = idToCharToken(displayIndexToId2(p, this.enigma.rotors[this.rotorNo]))
            var y =  LEADING_STRAIGHT + p*SINGLE + 1.5*CONNECTOR_RADIUS
            // left column
            addTextNode (parent, label, `${parent.id}_left_${label}`, "connectorLabel", -CONNECTOR_RADIUS, y)

            // right column
            addTextNode (parent, label, `${parent.id}_right_${label}`, "connectorLabel", COMPONENT_WIDTH-CONNECTOR_RADIUS, y)
        }
    }

    drawOuterRing(parent) {
        // outer ring background
        addPathNode (parent, `M ${2.5*UNIT} 0 h ${5*UNIT} v ${COMPONENT_HEIGHT} h -${5*UNIT}`, `${parent.id}_outerring`, "outerRing")

        // outer ring labels
        // p = display position index (each rotor has <alphabetSize> vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor (for a 26 letter rotor)
        for (let p=0; p<this.enigma.rotors[this.rotorNo].alphabetSize; p++) {
            var label = idToCharToken((displayIndexToId(p, this.enigma.rotors[this.rotorNo].alphabetSize) + this.enigma.rotors[this.rotorNo].getNormalizedPosition()) % this.enigma.rotors[this.rotorNo].alphabetSize)
            addTextNode (parent, label, `${parent.id}_${label}`, "rotorLabel", 4*UNIT, LEADING_STRAIGHT + p*SINGLE + 2*UNIT)
        }
    }

    drawRingSettingMarker(parent) {
        // find the position of the A contact
        let displayIndex = idToDisplayIndex2 (0, this.enigma.rotors[this.rotorNo])

        let rightX = 0 + COMPONENT_WIDTH
        let y = LEADING_STRAIGHT + (displayIndex * SINGLE)

        // draw a red connector arc on the right-hand side of the rotor
        addPathNode (parent, `M ${rightX} ${y} a ${CONNECTOR_RADIUS} ${CONNECTOR_RADIUS} 0 0 1 0 ${2*CONNECTOR_RADIUS}`, `${parent.id}_ringsetting_left`, "ringSettingMarker")
    }

    drawTurnoverNotchMarker(parent) {
        let notchPositionIndex = (     A_POSITION + this.enigma.rotors[this.rotorNo].getNormalizedPosition() - this.enigma.rotors[this.rotorNo].getNormalizedTurnoverPosition() + this.enigma.rotors[this.rotorNo].alphabetSize) % this.enigma.rotors[this.rotorNo].alphabetSize
//        let notchPositionIndex = (19 + A_POSITION + this.enigma.rotors[this.rotorNo].getNormalizedPosition() - this.enigma.rotors[this.rotorNo].getNormalizedTurnoverPosition() + this.enigma.rotors[this.rotorNo].alphabetSize) % this.enigma.rotors[this.rotorNo].alphabetSize

        let x = 2.5*UNIT
        let y = LEADING_STRAIGHT + (notchPositionIndex * SINGLE) - 0.5*STRAIGHT

        // draw a black triangle on the left hand side of the rotor's outer ring
//        addPathNode (parent, `M ${x} ${y} v ${SINGLE} h -${UNIT}`, `${parent.id}_notch`, "notch")
        addPathNode (parent, `M ${x} ${y} v ${SINGLE} l ${UNIT} -${0.5*SINGLE}`, `${parent.id}_notch`, "notch")
    }

    drawRotorWindow(parent, xOffset, yOffset) {
        // outer ring position 'window'
        // this becomes a child of the parent of the group, as it shouldn't move together with the group
        addPathNode (parent, `M ${xOffset + 2.2*UNIT} ${yOffset + LEADING_STRAIGHT + A_POSITION*SINGLE - 1.9*UNIT} h ${5.5*UNIT} v ${5.5*UNIT} h -${5.5*UNIT} Z`, `${parent.id}_window`, "rotorWindow")
    }

    // TODO: change based on
    // https://css-tricks.com/smil-is-dead-long-live-smil-a-guide-to-alternatives-to-smil-features/
    animateStep(callbackOnAnimationEnd = null) {
        // A rotor consists of 2 SVG elements
        // 1. the background (which appears behind the enciphered path)
        // 2. the foreground (which appears in front of the enciphered path)
        // These two elements need to animate together
        for (let i = 0; i < this.nodesToAnimate.length; i++) {
            let animateNode = document.createElementNS(SVG_NS, "animateMotion");
            animateNode.id = `${this.nodesToAnimate[i].id}_animate`
            animateNode.setAttribute("path", `M0,0 0 ${SINGLE}`)
            animateNode.setAttribute("begin", "0s")
            animateNode.setAttribute("dur", "1s")
            animateNode.setAttribute("repeatCode", "1")
            animateNode.setAttribute("fill", "freeze")

            if (callbackOnAnimationEnd && i == 0) {
                // only add the event listener to 1 of the nodes to animate for this rotor
                animateNode.addEventListener("endEvent", callbackOnAnimationEnd);
            }
            //console.log("add animateMotion to " + this.nodesToAnimate[i].id)
            this.nodesToAnimate[i].appendChild(animateNode)
        }
    }

}
