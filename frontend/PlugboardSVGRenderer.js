class PlugboardSVGRenderer {

    constructor(plugboard, alphabetSize = 26) {
        this.plugboard = plugboard
        this.alphabetSize = alphabetSize
    }

    draw(svg, x, y) {
        let group = addGroupNode(svg, "plugboard", x, y)

        this.drawBackground(group)
        this.drawWiring(group)
        this.drawBorders(group)
        this.drawConnectorLabels(group)
    }

    // background = everything behind the encipher path
    drawBackground(group, x=0, y=0) {
        let path = `M ${x} ${y}`
        path += `h ${COMPONENT_WIDTH} `
        path += connectorSVGPath(true, alphabetSize)
        path += `h -${COMPONENT_WIDTH} `
        path += connectorSVGPath(false, alphabetSize)

        addPathNode (group, path, "plugboardBG", "plugboardBG")
    }

    // foreground = everything in front of the encipher path
    drawForeground(svg, x, y) {
        var group = addGroupNode(svg, "plugboard", x, y)

        this.drawWiring(group)
        this.drawBorders(group)
        this.drawConnectorLabels(group)
    }

    drawWiring(group) {
        let contactsProcessed = []
        for (let i=0; i < alphabetSize; i++) {
            contactsProcessed.push(0)
        }

        let wiring = this.plugboard.getNormalizedWiringTable()

        // draw 'steckered' letters
        // c = contact id (0-based, 26 in total)
        let fromX = COMPONENT_WIDTH + CONNECTOR_RADIUS
        let toX = CONNECTOR_RADIUS
        for(let c = 0; c<alphabetSize; c++) {
            if (contactsProcessed[c] === 0) {
                let position1 = idToDisplayIndex(c, alphabetSize)
                let contactId2 = (c + wiring[c] + alphabetSize) % alphabetSize
                contactsProcessed[contactId2] = 1
                let position2 = idToDisplayIndex(contactId2, alphabetSize)

                let y1 = LEADING_STRAIGHT + position1 * SINGLE + CONNECTOR_RADIUS
                let y2 = LEADING_STRAIGHT + position2 * SINGLE + CONNECTOR_RADIUS
                addPathNode (group, `M ${fromX} ${y1} h -${CONNECTOR_RADIUS} L ${toX} ${y2}`, `${group.id}_wire_${c}`, "wire")
                addPathNode (group, `M ${fromX} ${y2} h -${CONNECTOR_RADIUS} L ${toX} ${y1}`, `${group.id}_wire_${c}_inv`, "wire")
            }
        }
    }

    drawBorders(group) {
        let path = `M 0 0 h ${COMPONENT_WIDTH} ${connectorSVGPath(true, this.alphabetSize)} h -${COMPONENT_WIDTH} ${connectorSVGPath(false, this.alphabetSize)} Z`
        addPathNode (group, path, `${group.id}_border`, "border")
    }

    drawConnectorLabels(group) {
        // p = display position index (each rotor has 26 vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor
        for (let p=0; p<this.alphabetSize; p++) {
            var label = idToCharToken(displayIndexToId(p, this.alphabetSize))
            var y =  LEADING_STRAIGHT + p*SINGLE + 1.5*CONNECTOR_RADIUS
            // left column
            addTextNode (group, label, `${group.id}_left_${label}`, "connectorLabel", -CONNECTOR_RADIUS + 0.5*UNIT, y)

            // right column
            addTextNode (group, label, `${group.id}_right_${label}`, "connectorLabel", COMPONENT_WIDTH-CONNECTOR_RADIUS + 0.5*UNIT, y)
        }
    }
}