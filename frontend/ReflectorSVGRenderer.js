class ReflectorSVGRenderer {

    constructor(reflector) {
        this.reflector = reflector
        this.internalWireLengths = {}
    }

    draw(parent, variant, x, y) {
        let group = addGroupNode(parent, "reflector", x, y)
        this.drawBackground(group)
        this.drawWiring(group)
        this.drawBorder(group)
        this.drawConnectorLabels(group)
    }

    // background = everything behind the encipher path
    drawBackground(parent, x=0, y=0) {
        let path = `M ${x} ${y}`
        path += `h ${COMPONENT_WIDTH} `
        path += connectorSVGPath(true, this.reflector.alphabetSize)
        path += `h -${COMPONENT_WIDTH} Z`

        addPathNode (parent, path, "reflector_bg", "reflectorBG")
    }

    // foreground = everything in front of the encipher path
    drawForeground(svg, x, y) {
        let group = addGroupNode(svg, "reflector", x, y)
        this.drawWiring(group)
        this.drawBorder(group)
        this.drawConnectorLabels(group)
    }

    drawWiring (parent) {
        let wiring = this.reflector.getNormalizedWiringTable()

        let contactsProcessed = []
        for (let i=0; i < this.reflector.alphabetSize; i++) {
            contactsProcessed.push(0)
        }

        let connection = 0
        for (let i = 0; i < this.reflector.alphabetSize; i++) {
            let inputContactId = i
            if (contactsProcessed[inputContactId] === 0) {
                let inputContactPos = idToDisplayIndex(inputContactId, this.reflector.alphabetSize)
                let wiringStep = wiring[inputContactId]
                let outputContactId = (inputContactId + wiringStep + this.reflector.alphabetSize) % this.reflector.alphabetSize
                let outputContactPos = idToDisplayIndex(outputContactId, this.reflector.alphabetSize)

                contactsProcessed[inputContactId] = 1
                contactsProcessed[outputContactId] = 1

                let horDist = ((0.5 * this.reflector.alphabetSize - connection) * 5) + CONNECTOR_RADIUS
                this.internalWireLengths[inputContactId] = horDist
                this.internalWireLengths[outputContactId] = horDist

                let vertDist = ( (outputContactPos - inputContactPos) * SINGLE)
                let x = COMPONENT_WIDTH + CONNECTOR_RADIUS
                let y = LEADING_STRAIGHT + inputContactPos * SINGLE + CONNECTOR_RADIUS
                let path = `M ${x}, ${y} `
                path += `h ${-horDist} `
                path += `v ${vertDist} `
                path += `h ${horDist} `
                addPathNode (parent, path, `${parent.id}_wire_${i}`, "wire")
//                window.console.log (`[SVG] connect ${idToCharToken(inputContactId)} at ${y} to ${idToCharToken(outputContactId, this.reflector.alphabetSize)} at ${y+vertDist} `)

                connection++
            }
        }
    }

    drawBorder(parent) {
        let path = "M 0 0 "
        path += `h ${COMPONENT_WIDTH} `
        path += connectorSVGPath(true, this.reflector.alphabetSize)
        path += `h -${COMPONENT_WIDTH} Z`

        addPathNode (parent, path, `${parent.id}_bg`, "border")
    }

   drawConnectorLabels(parent) {
        // p = display position index (each rotor has 26 vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor
        for (let p=0; p<this.reflector.alphabetSize; p++) {
            var label = idToCharToken(displayIndexToId(p, this.reflector.alphabetSize))
            var x = COMPONENT_WIDTH - CONNECTOR_RADIUS
            var y = LEADING_STRAIGHT + p*SINGLE + 1.5*CONNECTOR_RADIUS
            addTextNode (parent, label, `${parent.id}_${label}`, "connectorLabel", x, y)
        }
    }

}
