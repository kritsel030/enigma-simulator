class EnigmaRenderer {

    // id 0..25 represents A..Z
    pressedKeyId

    // id 0..25 represents A..Z
    lightedKeyId

    // array of indices which represent the path from pressed key to lighted key (all 0-based)
    //    0: plugboard inbound input connection = pressedKeyId
    //    1: plugboard inbound output connection
    //    2: rotor3 inbound output connection
    //    3: rotor2 inbound output connection
    //    4: rotor1 inbound output connection
    //    5: reflector outbound output connection
    //    6: rotor1 outbound output connection
    //    7: rotor2 outbound output connection
    //    8: rotor3 outbound output connection
    //    9: plugboard outbound output connection = lightedKeyId
    encipherPath

    // animation durations in ms
    buttonDownDuration = 0
    rotorSteppingDuration = 1000
    pathDuration = 1000

    rotorFrameTotal = 20
    rotorPrevFrameIndex = -1
    rotorFramePeriodInMs = this.rotorSteppingDuration / this.rotorFrameTotal

    animationStartTime;
    buttonDownStartTime;
    rotorSteppingStartTime;
    pathStartTime;

    // 0: no active animation
    // 1: animate button down
    // 2: animate rotor stepping
    // 3: animate encipherPath
    // 4: light up key
    animationPhase

    constructor(enigma) {
        this.enigma = enigma
        this.reflectorRenderer = new ReflectorRenderer(this.enigma.reflector)
        this.rotor1Renderer = new RotorRenderer(this.enigma.rotor1)
        this.rotor2Renderer = new RotorRenderer(this.enigma.rotor2)
        this.rotor3Renderer = new RotorRenderer(this.enigma.rotor3)
        this.plugboardRenderer = new PlugboardRenderer(this.enigma.plugboard)
        this.keysRenderer = new KeysRenderer()
    }

    setEncipherPath(encipherPath, xOffset, yOffset) {
        this.encipherPath = encipherPath
        this.pathSegments = this.calculatePathSegments(encipherPath, xOffset, yOffset)
    }

    draw(ctx, frameTime) {
        // layer 1: backgrounds
        this.reflectorRenderer.drawBackground(ctx, LEFT_MARGIN, TOP_MARGIN)
        this.rotor1Renderer.drawBackground(ctx, LEFT_MARGIN + 1*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor2Renderer.drawBackground(ctx, LEFT_MARGIN + 2*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor3Renderer.drawBackground(ctx, LEFT_MARGIN + 3*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.plugboardRenderer.drawBackground(ctx, LEFT_MARGIN + 4*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)

        // layer 2: wiring
        this.reflectorRenderer.drawWiring(ctx, LEFT_MARGIN, TOP_MARGIN)
        this.rotor1Renderer.drawWiring(ctx, LEFT_MARGIN + 1*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor2Renderer.drawWiring(ctx, LEFT_MARGIN + 2*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor3Renderer.drawWiring(ctx, LEFT_MARGIN + 3*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.plugboardRenderer.drawWiring(ctx, LEFT_MARGIN + 4*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)

        // layer 2: connections (same layer as wiring)
        for (let i = 0; i < 4; i++) {
            drawConnectionColumn(ctx, LEFT_MARGIN + i * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH, LEFT_MARGIN + (i + 1) * (COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        }
        drawKeyConnectionColumn(ctx, LEFT_MARGIN + 4 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH, LEFT_MARGIN + (4 + 1) * (COMPONENT_WIDTH + SPACING), TOP_MARGIN)

        // layer 3: encipher path (when there is an active path to be drawn path
        this.drawPath(ctx, frameTime)

        // layer 4: component borders
        this.reflectorRenderer.drawBorder(ctx, LEFT_MARGIN, TOP_MARGIN)
        this.rotor1Renderer.drawBorder(ctx, LEFT_MARGIN + 1*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor2Renderer.drawBorder(ctx, LEFT_MARGIN + 2*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor3Renderer.drawBorder(ctx, LEFT_MARGIN + 3*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.plugboardRenderer.drawBorder(ctx, LEFT_MARGIN + 4*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)

        // layer 4: connection labels
        this.reflectorRenderer.drawLabels(ctx, LEFT_MARGIN, TOP_MARGIN)
        this.rotor1Renderer.drawLabels(ctx, LEFT_MARGIN + 1*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor2Renderer.drawLabels(ctx, LEFT_MARGIN + 2*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor3Renderer.drawLabels(ctx, LEFT_MARGIN + 3*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.plugboardRenderer.drawLabels(ctx, LEFT_MARGIN + 4*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)

        // layer 4: keys (same layer as component borders)
        this.keysRenderer.draw(ctx, LEFT_MARGIN + 5*(COMPONENT_WIDTH + SPACING), TOP_MARGIN, this.pressedKeyId, this.lightedKeyId)

        // layer 4: outer ring of rotors (same layer as component borders)
        this.rotor1Renderer.drawOuterRing(ctx, LEFT_MARGIN + 1*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor2Renderer.drawOuterRing(ctx, LEFT_MARGIN + 2*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor3Renderer.drawOuterRing(ctx, LEFT_MARGIN + 3*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    }

    drawPath(ctx, frameTime) {
        if (this.encipherPath) {
            setPathState(ctx)

            // determine which path segments are to be drawn, given the frameTime
            let pathSegmentsToBeDrawn = this.pathSegments.length
            if (frameTime) {
                pathSegmentsToBeDrawn = 0
                let totalLength = this.pathSegments[this.pathSegments.length-1][4]
                let lengthToBeDrawn = totalLength * ( (frameTime - this.pathStartTime) / this.pathDuration)
                for (let ps = 0; ps < this.pathSegments.length; ps++) {
                    if (this.pathSegments[ps][4] >= lengthToBeDrawn) {
                        pathSegmentsToBeDrawn = ps
                        break
                    }
                }
            }
            ctx.beginPath()

            // first pathSegment: starting point
            ctx.moveTo(this.pathSegments[0][1], this.pathSegments[0][2])

            for (let i=1; i<pathSegmentsToBeDrawn; i++) {
                ctx.lineTo(this.pathSegments[i][1], this.pathSegments[i][2])
            }

            // stroke
            ctx.stroke()
        }
    }

    // based on an array of encipher datapoints, create an array of pathSegments
    // each path segment is itself an array:
    //   0: name of the segment
    //   1: x-coordinate of segment endpoint
    //   2: y-coordinate of segment endpoint
    //   3: length of segment endpoint
    //   4: total length of all segments up to (and including) this segment
    calculatePathSegments(encipherPath, xOffset, yOffset) {
        let pathSegments = []

        // console.log('encipherPath:')
        // console.log(encipherPath)
        // inbound: key
        let pbXRight = xOffset + 4 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
        let keyXLeft_inbound = pbXRight + SPACING + (idToDisplayIndex(encipherPath[0]) % 2) * KEY_SHIFT
        let pbY_inbound = yOffset + LEADING_HEIGHT + idToDisplayIndex(encipherPath[0]) * SINGLE_HEIGHT
        this.addPathSegment(pathSegments,"key (inbound)", keyXLeft_inbound, pbY_inbound)

        // inbound: plugboard input
        this.addPathSegment(pathSegments, "plugboard in (inbound)", pbXRight, pbY_inbound)

        // inbound: plugboard output
        let pbXLeft = xOffset + 4 * (COMPONENT_WIDTH + SPACING)
        let pbYLeft_inbound = yOffset + LEADING_HEIGHT + idToDisplayIndex(encipherPath[1]) * SINGLE_HEIGHT
        this.addPathSegment(pathSegments, "plugboard out (inbound)", pbXLeft, pbYLeft_inbound)

        // inbound: rotor 3 input
        let rotor3XRight = xOffset + 3 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
        this.addPathSegment(pathSegments, "rotor 3 input (inbound)", rotor3XRight, pbYLeft_inbound)

        // inbound: rotor 3 output
        let rotor3XLeft = xOffset + 3 * (COMPONENT_WIDTH + SPACING)
        let rotor3YLeft_inbound = yOffset + LEADING_HEIGHT + idToDisplayIndex(encipherPath[2]) * SINGLE_HEIGHT
        this.addPathSegment(pathSegments, "rotor 3 output (inbound)", rotor3XLeft, rotor3YLeft_inbound)

        // inbound: rotor 2 input
        let rotor2XRight = xOffset + 2 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
        this.addPathSegment(pathSegments, "rotor 2 input (inbound)", rotor2XRight, rotor3YLeft_inbound)

        // inbound: rotor 2 output
        let rotor2XLeft = xOffset + 2 * (COMPONENT_WIDTH + SPACING)
        let rotor2YLeft_inbound = yOffset + LEADING_HEIGHT + idToDisplayIndex(encipherPath[3]) * SINGLE_HEIGHT
        this.addPathSegment(pathSegments, "rotor 2 output (inbound)", rotor2XLeft, rotor2YLeft_inbound)

        // inbound: rotor 2 input
        let rotor1XRight = xOffset + 1 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
        this.addPathSegment(pathSegments, "rotor 1 input (inbound)", rotor1XRight, rotor2YLeft_inbound)

        // wiring in rotor 1 (inbound)
        let rotor1XLeft = xOffset + 1 * (COMPONENT_WIDTH + SPACING)
        let rotor1YLeft_inbound = yOffset + LEADING_HEIGHT + idToDisplayIndex(encipherPath[4]) * SINGLE_HEIGHT
        this.addPathSegment(pathSegments, "rotor 1 output (inbound)", rotor1XLeft, rotor1YLeft_inbound)

        // inbound: reflector input
        let reflectorX = xOffset + COMPONENT_WIDTH
        this.addPathSegment(pathSegments, "reflector input (inbound)", reflectorX, rotor1YLeft_inbound)

        // wiring in reflector
        // 1. right-to-left
        let reflectorX_internal = reflectorX - 25
        this.addPathSegment(pathSegments, "reflector internal A", reflectorX_internal, rotor1YLeft_inbound)
        // 2. vertical
        let reflectorY_outbound = yOffset + LEADING_HEIGHT + idToDisplayIndex(encipherPath[5]) * SINGLE_HEIGHT
        this.addPathSegment(pathSegments, "reflector internal B", reflectorX_internal, reflectorY_outbound)
        // 3. left-to-right
        this.addPathSegment(pathSegments, "reflector output (outbound)", reflectorX, reflectorY_outbound)

        // reflector to rotor 1 (always a horizontal line)
        this.addPathSegment(pathSegments, "rotor 1 input (outbound)", rotor1XLeft, reflectorY_outbound)

        // wiring in rotor 1 (outbound)
        let rotor1YRight_outbound = yOffset + LEADING_HEIGHT + idToDisplayIndex(encipherPath[6]) * SINGLE_HEIGHT
        this.addPathSegment(pathSegments, "rotor 1 output (outbound)", rotor1XRight, rotor1YRight_outbound)

        // rotor 1 to rotor 2 (outbound, always a horizontal line)
        this.addPathSegment(pathSegments, "rotor 2 input (outbound)", rotor2XLeft, rotor1YRight_outbound)

        // wiring in rotor 2 (outbound)
        let rotor2YRight_outbound = yOffset + LEADING_HEIGHT + idToDisplayIndex(encipherPath[7]) * SINGLE_HEIGHT
        this.addPathSegment(pathSegments, "rotor 2 output (outbound)", rotor2XRight, rotor2YRight_outbound)

        // rotor 2 to rotor 3 (outbound, always a horizontal line)
        this.addPathSegment(pathSegments, "rotor 3 input (outbound)", rotor3XLeft, rotor2YRight_outbound)

        // wiring in rotor 3 (outbound)
        let rotor3YRight_outbound = yOffset + LEADING_HEIGHT + idToDisplayIndex(encipherPath[8]) * SINGLE_HEIGHT
        this.addPathSegment(pathSegments, "rotor 3 output (outbound)", rotor3XRight, rotor3YRight_outbound)

        // rotor 3 to plugboard (outbound, always a horizontal line)
        this.addPathSegment(pathSegments, "plugboard input (outbound)", pbXLeft, rotor3YRight_outbound)

        // wiring in plugboard (outbound)
        let pbYRight_outbound = yOffset + LEADING_HEIGHT + idToDisplayIndex(encipherPath[9]) * SINGLE_HEIGHT
        this.addPathSegment(pathSegments, "plugboard output (outbound)", pbXRight, pbYRight_outbound)

        // plugboard to key/light (outbound, always a horizontal line)
        let keyXLeft_outbound = pbXRight + SPACING + (idToDisplayIndex(encipherPath[9]) % 2) * KEY_SHIFT
        this.addPathSegment(pathSegments, "key (outbound)", keyXLeft_outbound, pbYRight_outbound)

        return pathSegments
    }

    // add a path segment to the array of pathSegments
    // each path segment is itself an array:
    //   0: name of the segment
    //   1: x-coordinate of segment endpoint
    //   2: y-coordinate of segment endpoint
    //   3: length of segment endpoint
    //   4: total length of all segments up to (and including) this segment
    addPathSegment(pathSegments, name, x, y) {
        let length = 0
        let totalLength = 0
        if (pathSegments.length !== 0) {
            length = this.segmentLength(pathSegments[pathSegments.length - 1][1], pathSegments[pathSegments.length - 1][2], x, y)
            totalLength = pathSegments[pathSegments.length-1][4] + length
        }
        pathSegments.push([name, x, y, length, totalLength])
    }

    segmentLength (startX, startY, endX, endY) {
        return Math.sqrt(Math.pow(Math.abs(endX-startX), 2) + Math.pow(Math.abs(endY-startY), 2))
    }

}
