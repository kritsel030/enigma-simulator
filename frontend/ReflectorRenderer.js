class ReflectorRenderer {

    constructor(reflector) {
        this.reflector = reflector
    }

    draw (reflector, ctx, xOffset, yOffset) {
        this.drawBackground(ctx, xOffset, yOffset)
        this.drawWiring(ctx, xOffset, yOffset)
        this.drawBorder (ctx, xOffset, yOffset)
        this.drawLabels(ctx, xOffset, yOffset)
    }

    drawBackground (ctx, xOffset, yOffset) {
        setComponentBackgroundState(ctx)
        ctx.fillRect(xOffset, yOffset, COMPONENT_WIDTH, BORDER_HEIGHT);
    }

    drawWiring (ctx, xOffset, yOffset) {
        let wiring = this.reflector.getNormalizedWiringTable()
        setWiringState(ctx)

        let contactsProcessed = []
        for (let i=0; i < 26; i++) {
            contactsProcessed.push(0)
        }

        ctx.beginPath()
        let connection = 0
        for (let i = 0; i < 26; i++) {
            let inputContactId = i
            if (contactsProcessed[inputContactId] === 0) {
                let inputContactPos = idToDisplayIndex(inputContactId)
                let wiringStep = wiring[inputContactId]
                let outputContactId = (inputContactId + wiringStep + 26) % 26
                let outputContactPos = idToDisplayIndex(outputContactId)

                contactsProcessed[inputContactId] = 1
                contactsProcessed[outputContactId] = 1

                // start line at rotor contact
                ctx.moveTo(xOffset + COMPONENT_WIDTH, yOffset + LEADING_HEIGHT + inputContactPos * SINGLE_HEIGHT)
                // go left
                ctx.lineTo(xOffset + COMPONENT_WIDTH - ((13 - connection) * 5), yOffset + LEADING_HEIGHT + inputContactPos * SINGLE_HEIGHT)
                // go vertical (can be down or up)
                ctx.lineTo(xOffset + COMPONENT_WIDTH - ((13 - connection) * 5), yOffset + LEADING_HEIGHT + outputContactPos * SINGLE_HEIGHT)
                // go right
                ctx.lineTo(xOffset + COMPONENT_WIDTH, yOffset + LEADING_HEIGHT + outputContactPos * SINGLE_HEIGHT)

                connection++
            }
        }
        ctx.stroke()
    }

    drawBorder (ctx, xOffset, yOffset) {
        // border
        setComponentBorderState(ctx)

        ctx.beginPath();

        // define left border
        ctx.moveTo(xOffset, yOffset);
        ctx.lineTo(xOffset, yOffset + BORDER_HEIGHT);

        // define right border
        borderPath(ctx, xOffset + COMPONENT_WIDTH, yOffset)

        ctx.stroke()
    }

    drawLabels(ctx, xOffset, yOffset) {
        setLabelState(ctx)
        for (let i=0; i<26; i++) {
            // write tokens
            ctx.fillText(idToCharToken(displayIndexToId(i)), xOffset + COMPONENT_WIDTH - 2*CONNECTOR_RADIUS + 2, yOffset + LEADING_HEIGHT + i*SINGLE_HEIGHT + 3)
        }
    }

}
