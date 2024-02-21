class LWPlugboardSVGRenderer {

    constructor(plugboard, alphabetSize = 26) {
        this.plugboard = plugboard
        this.alphabetSize = alphabetSize
    }

    draw(parent, groupId, variant, first, last, x, y) {
        let group = addGroupNode(parent, groupId, x, y)

        this.drawBackground(group, variant)
        this.drawWiring(group, variant)
        this.drawBorders(group, variant)
    }

    // background = everything behind the encipher path
    drawBackground(group, variant, x=0, y=0) {
        let path = `M ${x} ${y}`
        path += `h ${PLUGBOARD_WIDTH} `
        path += `v ${PLUGBOARD_HEIGHT} `
        path += `h -${PLUGBOARD_WIDTH} `
        addPathNode (group, path, "plugboardBG", "plugboardBG")
    }

    // foreground = everything in front of the encipher path
    drawForeground(parent, variant, x, y) {
        var group = addGroupNode(parent, "plugboard", x, y)

        this.drawWiring(group)
        this.drawBorders(group)
        this.drawConnectorLabels(group)
    }

    drawWiring(group, variant) {
        let contactsProcessed = []
        for (let i=0; i < alphabetSize; i++) {
            contactsProcessed.push(0)
        }

        let wiring = this.plugboard.getNormalizedWiringTable()

        // draw a wire between connected letters
        // c = contact id (0-based, 26 in total)
//        let fromX = COMPONENT_WIDTH + CONNECTOR_RADIUS
//        let toX = CONNECTOR_RADIUS
        let fromY = 0
        let toY = PLUGBOARD_HEIGHT
        for(let c = 0; c<alphabetSize; c++) {
            if (contactsProcessed[c] === 0) {
                let position1 = idToDisplayIndex(c, alphabetSize)
                let contactId2 = (c + wiring[c] + alphabetSize) % alphabetSize
                contactsProcessed[contactId2] = 1
                let position2 = idToDisplayIndex(contactId2, alphabetSize)

                let x1 = 0.5*WIRE_DISTANCE + position1*WIRE_DISTANCE
                let x2 = 0.5*WIRE_DISTANCE + position2*WIRE_DISTANCE

                addPathNode (group, `M ${x1} ${fromY} L ${x2} ${toY}`, `${group.id}_wire_${c}`, "wire")
                if (x1 != x2) {
                    addPathNode (group, `M ${x2} ${fromY} L ${x1} ${toY}`, `${group.id}_wire_${c}_inv`, "wire")
                }
            }
        }
    }

    drawBorders(group, variant) {
        let path = `M ${0} ${0}`
        path += `h ${PLUGBOARD_WIDTH} `
        path += `v ${PLUGBOARD_HEIGHT } `
        path += `h -${PLUGBOARD_WIDTH} Z`
        addPathNode (group, path, `${group.id}_border`, "border")
    }
}