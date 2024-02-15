class RotorRenderer {

    constructor(rotor) {
        this.rotor = rotor
    }

    draw(ctx, xOffset, yOffset, frameIndex, frameTotal, clearPreviousRendering = false) {
        if (clearPreviousRendering) {
            ctx.clearRect(xOffset - 10, yOffset, COMPONENT_WIDTH + 20, BORDER_HEIGHT + 20)
        }
        this.drawBackground(ctx, xOffset, yOffset, frameIndex, frameTotal)
        this.drawWiring(ctx, xOffset, yOffset, frameIndex, frameTotal)
        this.drawBorder(ctx, xOffset, yOffset, frameIndex, frameTotal)
        this.drawLabels(ctx, xOffset, yOffset, frameIndex, frameTotal)
        this.drawOuterRing(ctx, xOffset, yOffset, frameIndex, frameTotal)
    }

    drawBackground(ctx, xOffset, yOffset, frameIndex = 0, frameTotal = 1) {
        let frameStep = (SINGLE_HEIGHT / frameTotal) * frameIndex
        yOffset += frameStep

        setComponentBackgroundState(ctx)
        ctx.fillRect(xOffset, yOffset, COMPONENT_WIDTH, BORDER_HEIGHT);
    }

    drawWiring(ctx, xOffset, yOffset, frameIndex = 0, frameTotal = 1) {
        let frameStep = (SINGLE_HEIGHT / frameTotal) * frameIndex
        yOffset += frameStep

        let wiring = this.rotor.getNormalizedWiringTableRtoL()

        setWiringState(ctx)

        ctx.beginPath()
        // p = display position index (each rotor has 26 vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor
        for(let p = 0; p<this.rotor.alphabetSize; p++) {
            ctx.moveTo(xOffset + COMPONENT_WIDTH, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT)
            let rightRotorContactId = displayIndexToId2(p, this.rotor)
            let leftRotorContactId = (rightRotorContactId + wiring[rightRotorContactId] + this.rotor.alphabetSize) % this.rotor.alphabetSize
            let leftRotorContactPos = idToDisplayIndex2(leftRotorContactId, this.rotor)
            let y = yOffset + LEADING_HEIGHT + leftRotorContactPos * SINGLE_HEIGHT
            ctx.lineTo(xOffset, y)
        }
        ctx.stroke()
    }

    drawBorder(ctx, xOffset, yOffset, frameIndex = 0, frameTotal = 1) {
        let frameStep = (SINGLE_HEIGHT / frameTotal) * frameIndex
        yOffset += frameStep

        setComponentBorderState(ctx)
        ctx.beginPath();

        // left border
        borderPath(ctx, xOffset, yOffset, this.rotor.alphabetSize)
        // right border
        borderPath(ctx, xOffset + COMPONENT_WIDTH, yOffset, this.rotor.alphabetSize)

        ctx.stroke()
    }

    drawLabels(ctx, xOffset, yOffset, frameIndex = 0, frameTotal = 1) {
        let frameStep = (SINGLE_HEIGHT / frameTotal) * frameIndex
        yOffset += frameStep

        setLabelState(ctx)
        // p = display position index (each rotor has 26 vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor
        for (let p=0; p<this.rotor.alphabetSize; p++) {
            // left column
            ctx.fillText(
                idToNumberToken(displayIndexToId2(p, this.rotor)),
                xOffset - 2*CONNECTOR_RADIUS,
                yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT + 3)
            // right column
            ctx.fillText(
                idToNumberToken(displayIndexToId2(p, this.rotor)),
                xOffset + COMPONENT_WIDTH - 2*CONNECTOR_RADIUS,
                yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT + 3)
        }
    }

    drawOuterRing(ctx, xOffset, yOffset, frameIndex = 0, frameTotal = 1) {
        let origYOffset = yOffset
        let frameStep = (SINGLE_HEIGHT / frameTotal) * frameIndex
        yOffset += frameStep

        // outer ring background
        ctx.fillStyle = 'rgba(169,169,169,0.75)';
        ctx.fillRect(xOffset+10, yOffset, 20, BORDER_HEIGHT);

        // outer ring labels
        ctx.fillStyle = 'rgb(0, 0, 0)';
        ctx.font = 'bold 12px arial'
        // p = display position index (each rotor has 26 vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor
        for (let p=0; p<this.rotor.alphabetSize; p++) {
            ctx.fillText(
                idToCharToken((displayIndexToId(p, this.rotor.alphabetSize) + this.rotor.getNormalizedPosition()) % this.rotor.alphabetSize),
                xOffset + 15,
                yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT + 3)
        }

        // outer ring position 'window'
        ctx.strokeStyle = 'rgb(0,0,0)'
        ctx.strokeRect(xOffset + 9, origYOffset + LEADING_HEIGHT + A_POSITION*SINGLE_HEIGHT - 11, 22, 22)

        // outer ring notch
        ctx.fillStyle = 'rgb(0, 0, 0)';
        let displayIndex = (A_POSITION + this.rotor.getNormalizedPosition() - this.rotor.getNormalizedTurnoverPosition() + this.rotor.alphabetSize) % this.rotor.alphabetSize
        ctx.beginPath()
        ctx.moveTo(xOffset + 10, yOffset + LEADING_HEIGHT + displayIndex*SINGLE_HEIGHT - 10)
        ctx.lineTo(xOffset + 14, yOffset + LEADING_HEIGHT + displayIndex*SINGLE_HEIGHT - 10 + 0.5*SINGLE_HEIGHT)
        ctx.lineTo(xOffset + 10, yOffset + LEADING_HEIGHT + displayIndex*SINGLE_HEIGHT + 10)
        ctx.fill()

        // mark ring setting with a red dot
        // note: ringsetting = 6 means that the 6th position on the outer ring (visible value 'F' or '06', id = 5, 0-based) is aligned with the 1st connector (id = 0, 0-based)
        ctx.beginPath()
        ctx.fillStyle = 'rgb(144, 12, 63 )'
        ctx.arc(xOffset + 29, yOffset + LEADING_HEIGHT + idToDisplayIndex2(0, this.rotor) * SINGLE_HEIGHT, 3, Math.PI*2, false)
        ctx.fill()

    }
}
