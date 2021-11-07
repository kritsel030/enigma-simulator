class PlugboardRenderer {

    constructor(plugboard) {
        this.plugboard = plugboard
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
        for (let i=0; i < 26; i++) {
            contactsProcessed.push(0)
        }

        let wiring = this.plugboard.getNormalizedWiringTable()

        setWiringState(ctx)
        ctx.beginPath()

        // draw 'steckered' letters
        // c = contact id (0-based, 26 in total)
        for(let c = 0; c<26; c++) {
            if (contactsProcessed[c] === 0) {
                let position1 = idToDisplayIndex(c)
                let contactId2 = (c + wiring[c] + 26) % 26
                contactsProcessed[contactId2] = 1
                let position2 = idToDisplayIndex(contactId2)

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

        borderPath(ctx, xOffset, yOffset)
        borderPath(ctx, xOffset + COMPONENT_WIDTH, yOffset)

        ctx.stroke()
    }

    drawLabels(ctx, xOffset, yOffset) {
        setLabelState(ctx)
        // p = display position index (each rotor has 26 vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor
        for (let p=0; p<26; p++) {
            // left column
            ctx.fillText(idToCharToken(displayIndexToId(p)), xOffset - 2*CONNECTOR_RADIUS + 2, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT + 3)
            // right column
            ctx.fillText(idToCharToken(displayIndexToId(p)), xOffset + COMPONENT_WIDTH - 2*CONNECTOR_RADIUS + 2, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT + 3)
        }
    }

}