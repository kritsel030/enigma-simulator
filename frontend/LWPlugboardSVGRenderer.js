class LWPlugboardSVGRenderer {

    constructor(plugboard, alphabetSize = 26) {
        this.plugboard = plugboard
        this.alphabetSize = alphabetSize
    }

    draw(parent, groupId, variant, first, last, inbound, x, y) {
        let group = addGroupNode(parent, groupId, x, y)

        this.drawBackground(group, variant)
        this.drawWiring(group, variant, first, last, inbound)
        this.drawForeground(group)
    }

    // background = everything behind the encipher path
    drawBackground(group, variant, x=0, y=0) {
        let path = `M ${x} ${y} `
        path += `h ${PLUGBOARD_WIDTH} `
        path += `v ${PLUGBOARD_HEIGHT} `
        path += `h -${PLUGBOARD_WIDTH} `
        addPathNode (group, path, "plugboardBG", "plugboardBG")
    }

    // foreground = everything in front of the encipher path
    drawForeground(parent, variant, first, last, inbound, x, y) {
        var group = addGroupNode(parent, "plugboard", x, y)

        //this.drawWiring(group, variant, first, last, inbound)
        this.drawBorders(group)
        //this.drawConnectorLabels(group)
    }

    drawWiring(parent, variant, first, last, inbound, x, y) {
        var group = addGroupNode(parent, "wiring", x, y)
        let contactsProcessed = []
        for (let i=0; i < alphabetSize; i++) {
            contactsProcessed.push(0)
        }

        let wiring = this.plugboard.getNormalizedWiringTable()

        // draw a wire between connected letters
        // c = contact id (0-based, 26 in total)
        for(let c = 0; c<alphabetSize; c++) {
                let topIndex = idToDisplayIndex(c, alphabetSize)
                let outputContactId = (c + wiring[c] + alphabetSize) % alphabetSize
                let bottomIndex = idToDisplayIndex(outputContactId, alphabetSize)
                let path = SVGPathService.plugboardPath(topIndex, bottomIndex, variant, first, last, inbound)
                addPathNode (group, path, `${group.id}_wire_${c}`, "wire")
        }
    }

    drawBorders(group) {
        let path = `M ${0} ${0}`
        path += `h ${PLUGBOARD_WIDTH} `
        path += `v ${PLUGBOARD_HEIGHT } `
        path += `h -${PLUGBOARD_WIDTH} Z`
        addPathNode (group, path, `${group.id}_border`, "border")
    }
}