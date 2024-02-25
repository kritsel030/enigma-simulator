class LWDrumSVGRenderer {

    constructor(enigma, rotorNo, indicatorDrum) {
        this.enigma = enigma
        this.rotorNo = rotorNo
        this.indicatorDrum = indicatorDrum
    }

    draw(parent, groupId, variant, x, y, indicatorDrumStyle=false) {
        let group = addGroupNode(parent, groupId, x, y)
        this.nodesToAnimate = [group]

        this.drawShape(group, variant, indicatorDrumStyle)
        this.drawLetters(group, variant)
        if (!indicatorDrumStyle) {
            this.drawDrumType(group, variant)
        }

        // the positionMarker is not a child element of the group but of the parent node
        // because it must stay stationary when the group moves
        this.drawPositionMarker(parent, variant, x, y)
    }

    drawShape(parent, variant, indicatorDrumStyle) {
        let drum = this.getRotor()
        let classPostfix = indicatorDrumStyle ? "_indicator" : ""
        let x = DRUM_RADIUS
        let y = DRUM_RADIUS
        let outerRadius = (renderDrumsSeparate(variant) && !indicatorDrumStyle) ? DRUM_RADIUS - UNIT : DRUM_RADIUS
        let outerclass = indicatorDrumStyle ? "drumOuter_indicator" : `drumOuter_${drum.type}`
        addCircleNode (parent, `${parent.id}_outer`, outerclass, outerRadius, x, y)
        addCircleNode (parent, `${parent.id}_letterRing`, `drumLetterring${classPostfix}`, DRUM_RADIUS - 2*UNIT, x, y)
        addCircleNode (parent, `${parent.id}_inner`, `drumInner${classPostfix}`, DRUM_RADIUS - 4*UNIT, x, y)
    }

    // draw letters counter clock wise within the letter ring
    // rotate each letter according to its circular position on the ring
    // (letter at the top is not rotated, letter at the bottom is rotated 180 degrees)
    drawLetters(parent, variant) {
        let drum = this.getRotor()
        let alphabetSize = drum.alphabetSize
        // p = display position index (each drum has 26 circular positions)
        // 0 = position the position marker is pointing at (at 45 degrees)
        // 25 = position to the right of it
        for (let p=0; p < alphabetSize; p++) {
            var label = idToCharToken(displayIndexToId2(p, drum))
            let fullCircle = 2 * Math.PI
            let positionRadians = (-45 * fullCircle/360 + p * (fullCircle/alphabetSize) + fullCircle) % fullCircle
            let textRotationDegrees = (45 + p * (360/alphabetSize) + 360) % 360
            let x = DRUM_RADIUS + Math.cos(positionRadians) * (DRUM_RADIUS - 3.25*UNIT)
            let y = DRUM_RADIUS + Math.sin(positionRadians) * (DRUM_RADIUS - 3.25*UNIT)
            addTextNode (parent, label, `${parent.id}_label`, "drumLetter", 0, 0, `translate(${x},${y}) rotate(${textRotationDegrees})`)
            // draw a dot below the 'A' to highlight it
            if (label == "A") {
                x = DRUM_RADIUS + Math.cos(positionRadians) * (DRUM_RADIUS - 5*UNIT)
                y = DRUM_RADIUS + Math.sin(positionRadians) * (DRUM_RADIUS - 5*UNIT)
                addCircleNode (parent, `${parent.id}_A_dot`, "A_dot", 0.5*UNIT, x, y)
            }
        }
    }

    // draw the drum's type in the center of the rum, rotated according to the drum's position
    // (upright when the drum is set to A)
    drawDrumType(parent, variant) {
        let drum = this.getRotor()
        // drumPosition = 0 -> drum set to A, max = alphabetsize-1
        let drumPosition = drum.getNormalizedPosition()
        let textRotationDegrees = (drumPosition * (360/drum.alphabetSize) + 360) % 360
        // when we're dealing with a showcase drum, remove the 'SC' from the type
        let label = drum.type.startsWith("SC") ? drum.type.substring(2) : drum.type
        addTextNode (parent, label, `${parent.id}_drumtype`, "drumType", 0, 0, `translate(${DRUM_RADIUS},${DRUM_RADIUS}) rotate(${textRotationDegrees})`)
    }

    drawPositionMarker(parent, variant, x, y) {
        let fullCircle = 2 * Math.PI
        let startX = x + DRUM_RADIUS + Math.cos(-45 * fullCircle/360) * (DRUM_RADIUS + 0.5*UNIT)
        let startY = y + DRUM_RADIUS + Math.sin(-45 * fullCircle/360) * (DRUM_RADIUS + 0.5*UNIT)
        addPathNode (parent, `M ${startX} ${startY} h ${2*UNIT} l -${2*UNIT} -${2*UNIT}`, `${parent.id}_rotor${this.rotorNo}_marker`, "positionMarker")
    }

    getRotor() {
        if (this.indicatorDrum) return this.indicatorDrum
        else return this.enigma.rotors[this.rotorNo]
    }

}
