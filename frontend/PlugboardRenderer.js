class PlugboardRenderer {

    constructor(plugboard, alphabetSize = 26) {
        this.plugboard = plugboard
        this.alphabetSize = alphabetSize
    }

    draw(ctx, xOffset, yOffset) {
        this.drawBackground(ctx, xOffset, yOffset)
        this.drawWiring(ctx, xOffset, yOffset)
        this.drawBorder(ctx, xOffset, yOffset)
        this.drawLabels(ctx, xOffset, yOffset)
    }

    drawBackground(ctx, xOffset, yOffset) {
        setComponentBackgroundState(ctx)
        ctx.fillRect(xOffset, yOffset, COMPONENT_WIDTH, BORDER_HEIGHT);
    }

    drawWiring(ctx, xOffset, yOffset) {
        let contactsProcessed = []
        for (let i=0; i < alphabetSize; i++) {
            contactsProcessed.push(0)
        }

        let wiring = this.plugboard.getNormalizedWiringTable()

        setWiringState(ctx)
        ctx.beginPath()

        // draw 'steckered' letters
        // c = contact id (0-based, 26 in total)
        for(let c = 0; c<alphabetSize; c++) {
            if (contactsProcessed[c] === 0) {
                let position1 = idToDisplayIndex(c, alphabetSize)
                let contactId2 = (c + wiring[c] + alphabetSize) % alphabetSize
                contactsProcessed[contactId2] = 1
                let position2 = idToDisplayIndex(contactId2, alphabetSize)

                ctx.moveTo(xOffset + COMPONENT_WIDTH, yOffset + LEADING_HEIGHT + position1 * SINGLE_HEIGHT)
                ctx.lineTo(xOffset, yOffset + LEADING_HEIGHT + position2 * SINGLE_HEIGHT)

                ctx.moveTo(xOffset + COMPONENT_WIDTH, yOffset + LEADING_HEIGHT + position2 * SINGLE_HEIGHT)
                ctx.lineTo(xOffset, yOffset + LEADING_HEIGHT + position1 * SINGLE_HEIGHT)
            }
        }

        ctx.stroke()
    }

    drawBorder(ctx, xOffset, yOffset) {
        setComponentBorderState(ctx)

        ctx.beginPath();

        borderPath(ctx, xOffset, yOffset, alphabetSize)
        borderPath(ctx, xOffset + COMPONENT_WIDTH, yOffset, alphabetSize)

        ctx.stroke()
    }

    drawLabels(ctx, xOffset, yOffset) {
        setLabelState(ctx)
        // p = display position index (each rotor has 26 vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor
        for (let p=0; p<alphabetSize; p++) {
            // left column
            ctx.fillText(
                idToCharToken(displayIndexToId(p, alphabetSize)),
                xOffset - 2*CONNECTOR_RADIUS + 2,
                yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT + 3)
            // right column
            ctx.fillText(
                idToCharToken(displayIndexToId(p, alphabetSize)),
                xOffset + COMPONENT_WIDTH - 2*CONNECTOR_RADIUS + 2,
                yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT + 3)
        }
    }

}