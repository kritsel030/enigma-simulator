//////////////////////////////////////////////////////////////////////////
// constants

let SINGLE_HEIGHT = 20
let LEADING_HEIGHT = SINGLE_HEIGHT * 0.5
let TRAILING_HEIGHT = SINGLE_HEIGHT * 0.5

let COMPONENT_WIDTH = 75
let SPACING = 40

let LEFT_MARGIN = 100
let TOP_MARGIN = 10

let BORDER_HEIGHT = LEADING_HEIGHT + SINGLE_HEIGHT * 25 + TRAILING_HEIGHT

let KEY_RADIUS = 10
let KEY_SHIFT = 20

let A_POSITION = 11 // 0-based

const CONNECTOR_RADIUS = 4

//////////////////////////////////////////////////////////////////////////
// variables

let reflector = new Reflector('B')
let rotor1 = new Rotor('I', 'A', 6)
let rotor2 = new Rotor('II', 'D', 5)
let rotor3 = new Rotor('III', 'V', 3)
let plugboard = new Plugboard('AC-DK-GI-JX-OE-XZ')

let enigma = new Enigma (reflector, rotor1, rotor2, rotor3, plugboard)

let canvas;
let ctx;
let raf;
let running = false

let steppingRotors = [false, false, false]

let animationStartTime;
let animationLeadTime = 1000 // in ms
let frameTotal = 20
let prevFrameIndex = -1
let framePeriodInMs = animationLeadTime / frameTotal

//////////////////////////////////////////////////////////////////////////
// init

function init() {
    console.log("init")
    console.log("framePeriodInMs: " + framePeriodInMs)
    canvas = document.getElementById("enigma")
    ctx = canvas.getContext('2d')
    draw()

    canvas.addEventListener('click', function(e) {
        if (!running) {
            console.log("start animation")
            animationStartTime = new Date();
            raf = window.requestAnimationFrame(startAnimation);
            running = true;
        }
    });

    canvas.addEventListener('mouseout', function(e) {
        console.log("stop animation")
        window.cancelAnimationFrame(raf);
        running = false;
    });

}

//////////////////////////////////////////////////////////////////////////
// animate

function startAnimation() {
    steppingRotors = enigma.calculateStep()
    console.log ("steppingRotors")
    console.log(steppingRotors)
    drawAnimatedRotorStep()
}

function drawAnimatedRotorStep() {
    const now = new Date();
    let msSinceStart = now.getTime() - animationStartTime.getTime()
    if ( msSinceStart > animationLeadTime) {
        console.log("animation done")
        window.cancelAnimationFrame(raf);
        running = false
        enigma.step()
        console.log("new rotor positions: " + rotor1.position + " " + rotor2.position + " " + rotor3.position)
        prevFrameIndex = -1
        // clear entire canvas
        ctx.clearRect(0 , 0, 1000, 1000)
        // redraw entire enigma
        draw()
    } else {
        let frameIndex = Math.floor(msSinceStart / framePeriodInMs)
        if (prevFrameIndex !== frameIndex) {
            //console.log("new frame index " + frameIndex + " (" + msSinceStart + " ms since start)")
            prevFrameIndex = frameIndex
            // ctx.clearRect(LEFT_MARGIN + 1 * (COMPONENT_WIDTH + SPACING) - 10, TOP_MARGIN, COMPONENT_WIDTH + 20, BORDER_HEIGHT + 20)
            // drawRotor(rotor1, ctx, LEFT_MARGIN + 1 * (COMPONENT_WIDTH + SPACING), TOP_MARGIN, frameIndex, frameTotal)
            if (steppingRotors[0]) {
                drawRotor(rotor1, ctx, LEFT_MARGIN + 1 * (COMPONENT_WIDTH + SPACING), TOP_MARGIN, frameIndex, frameTotal, true)
            }
            if (steppingRotors[1]) {
                drawRotor(rotor2, ctx, LEFT_MARGIN + 2 * (COMPONENT_WIDTH + SPACING), TOP_MARGIN, frameIndex, frameTotal, true)
            }
            if (steppingRotors[2]) {
                drawRotor(rotor3, ctx, LEFT_MARGIN + 3 * (COMPONENT_WIDTH + SPACING), TOP_MARGIN, frameIndex, frameTotal, true)
            }
        }
        raf = window.requestAnimationFrame(drawAnimatedRotorStep);
    }
}

function draw() {
    draw_componentAtATime(ctx)
    //draw_layerAtATime(ctx)
}

function draw_componentAtATime(ctx) {
    for (let i = 0; i < 4; i++) {
        drawConnectionColumn(ctx, LEFT_MARGIN + i * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH, LEFT_MARGIN + (i + 1) * (COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    }
    drawKeyConnectionColumn(ctx, LEFT_MARGIN + 4 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH, LEFT_MARGIN + (4 + 1) * (COMPONENT_WIDTH + SPACING), TOP_MARGIN)

    drawReflector (reflector, ctx, LEFT_MARGIN, TOP_MARGIN)

    drawRotor(rotor1, ctx, LEFT_MARGIN + 1*(COMPONENT_WIDTH + SPACING), TOP_MARGIN, 0, 1)

    drawRotor(rotor2, ctx, LEFT_MARGIN + 2*(COMPONENT_WIDTH + SPACING), TOP_MARGIN, 0, 1)

    drawRotor(rotor3, ctx, LEFT_MARGIN + 3*(COMPONENT_WIDTH + SPACING), TOP_MARGIN, 0, 1)

    drawPlugboard(plugboard, ctx, LEFT_MARGIN + 4*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)

    drawKeys(ctx, LEFT_MARGIN + 5*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
}

function draw_layerAtATime(ctx) {

    // layer 1: backgrounds
    drawReflectorBackground(ctx, LEFT_MARGIN, TOP_MARGIN)
    drawRotorBackground(ctx, LEFT_MARGIN + 1*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    drawRotorBackground(ctx, LEFT_MARGIN + 2*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    drawRotorBackground(ctx, LEFT_MARGIN + 3*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    drawPlugboardBackground(ctx, LEFT_MARGIN + 4*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)

    // layer 2: wiring
    drawReflectorWiring(reflector, ctx, LEFT_MARGIN, TOP_MARGIN)
    drawRotorWiring(rotor1, ctx, LEFT_MARGIN + 1*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    drawRotorWiring(rotor2, ctx, LEFT_MARGIN + 2*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    drawRotorWiring(rotor3, ctx, LEFT_MARGIN + 3*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    drawPlugboardWiring(plugboard, ctx, LEFT_MARGIN + 4*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)

    // layer 2: connections (same layer as wiring)
    for (let i = 0; i < 4; i++) {
        drawConnectionColumn(ctx, LEFT_MARGIN + i * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH, LEFT_MARGIN + (i + 1) * (COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    }
    drawKeyConnectionColumn(ctx, LEFT_MARGIN + 4 * (COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH, LEFT_MARGIN + (4 + 1) * (COMPONENT_WIDTH + SPACING), TOP_MARGIN)

    // layer 3: connection path
    drawPath(ctx, LEFT_MARGIN, TOP_MARGIN, [0, 1, 20, 22, 15, 5, 8, 24, 21, 3])

    // layer 4: component borders
    drawReflectorBorder(ctx, LEFT_MARGIN, TOP_MARGIN)
    drawRotorBorder(ctx, LEFT_MARGIN + 1*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    drawRotorBorder(ctx, LEFT_MARGIN + 2*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    drawRotorBorder(ctx, LEFT_MARGIN + 3*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    drawPlugboardBorder(ctx, LEFT_MARGIN + 4*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)

    // layer 4: connection tokens
    drawReflectorLabels(ctx, LEFT_MARGIN, TOP_MARGIN)
    drawRotorLabels(rotor1, ctx, LEFT_MARGIN + 1*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    drawRotorLabels(rotor2, ctx, LEFT_MARGIN + 2*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    drawRotorLabels(rotor3, ctx, LEFT_MARGIN + 3*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    drawPlugboardLabels(ctx, LEFT_MARGIN + 4*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)

    // layer 4: keys (same layer as component borders)
    drawKeys(ctx, LEFT_MARGIN + 5*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)

    // layer 4: outer ring of rotors (same layer as component borders)
    drawRotorOuterRing(rotor1, ctx, LEFT_MARGIN + 1*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    drawRotorOuterRing(rotor2, ctx, LEFT_MARGIN + 2*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
    drawRotorOuterRing(rotor3, ctx, LEFT_MARGIN + 3*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
}

//////////////////////////////////////////////////////////////////////////
// reflector drawing

function drawReflector (reflector, ctx, xOffset, yOffset) {
    drawReflectorBackground(ctx, xOffset, yOffset)
    drawReflectorWiring(reflector, ctx, xOffset, yOffset)
    drawReflectorBorder (ctx, xOffset, yOffset)
    drawReflectorLabels(ctx, xOffset, yOffset)
}

function drawReflectorBackground (ctx, xOffset, yOffset) {
    setComponentBackgroundState(ctx)
    ctx.fillRect(xOffset, yOffset, COMPONENT_WIDTH, BORDER_HEIGHT);
}

function drawReflectorWiring (reflector, ctx, xOffset, yOffset) {
    let wiring = reflector.getInternalWiringTable()
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

function drawReflectorBorder (ctx, xOffset, yOffset) {
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

function drawReflectorLabels(ctx, xOffset, yOffset) {
    setLabelState(ctx)
    for (let i=0; i<26; i++) {
        // write tokens
        ctx.fillText(idToCharToken(displayIndexToId(i)), xOffset + COMPONENT_WIDTH - 2*CONNECTOR_RADIUS + 2, yOffset + LEADING_HEIGHT + i*SINGLE_HEIGHT + 3)
    }
}

//////////////////////////////////////////////////////////////////////////
// rotor drawing

function drawRotor(rotor, ctx, xOffset, yOffset, frameIndex, frameTotal, clearPreviousRendering = false) {
    if (clearPreviousRendering) {
        ctx.clearRect(xOffset - 10, yOffset, COMPONENT_WIDTH + 20, BORDER_HEIGHT + 20)
    }
    drawRotorBackground(ctx, xOffset, yOffset, frameIndex, frameTotal)
    drawRotorWiring(rotor, ctx, xOffset, yOffset, frameIndex, frameTotal)
    drawRotorBorder(ctx, xOffset, yOffset, frameIndex, frameTotal)
    drawRotorLabels(rotor, ctx, xOffset, yOffset, frameIndex, frameTotal)
    drawRotorOuterRing(rotor, ctx, xOffset, yOffset, frameIndex, frameTotal)
}

function drawRotorBackground(ctx, xOffset, yOffset, frameIndex, frameTotal) {
    let frameStep = (SINGLE_HEIGHT / frameTotal) * frameIndex
    yOffset += frameStep

    setComponentBackgroundState(ctx)
    ctx.fillRect(xOffset, yOffset, COMPONENT_WIDTH, BORDER_HEIGHT);
}

function drawRotorWiring(rotor, ctx, xOffset, yOffset, frameIndex, frameTotal) {
    let frameStep = (SINGLE_HEIGHT / frameTotal) * frameIndex
    yOffset += frameStep

    let wiring = rotor.getInternalWiringTable()

    setWiringState(ctx)

    ctx.beginPath()
    for(let i = 0; i<26; i++) {
        ctx.moveTo(xOffset + COMPONENT_WIDTH, yOffset + LEADING_HEIGHT + i*SINGLE_HEIGHT)

        let rightRotorContactPos = i
        let rightRotorContactId = displayIndexToId2(rightRotorContactPos, rotor)
        let wiringStep = wiring[rightRotorContactId]
        let leftRotorContactId = (rightRotorContactId + wiringStep + 26) % 26
        let leftRotorContactPos = idToDisplayIndex2(leftRotorContactId, rotor)
        // if (rotor.type === 'I' && i == 10) {
        //     console.log('rightRotorContactPos: ' + rightRotorContactPos)
        //     console.log('rightRotorContactId:  ' + rightRotorContactId)
        //     console.log('wiringStep:           ' + wiringStep)
        //     console.log('leftRotorContactId:   ' + leftRotorContactId)
        //     console.log('leftRotorContactPos:  ' + leftRotorContactPos)
        // }
        let y = yOffset + LEADING_HEIGHT + leftRotorContactPos * SINGLE_HEIGHT
        ctx.lineTo(xOffset, y)
    }
    ctx.stroke()
}

function drawRotorBorder(ctx, xOffset, yOffset, frameIndex, frameTotal) {
    let frameStep = (SINGLE_HEIGHT / frameTotal) * frameIndex
    yOffset += frameStep

    setComponentBorderState(ctx)
    ctx.beginPath();

    // left border
    borderPath(ctx, xOffset, yOffset)
    // right border
    borderPath(ctx, xOffset + COMPONENT_WIDTH, yOffset)

    ctx.stroke()
}

function drawRotorLabels(rotor, ctx, xOffset, yOffset, frameIndex, frameTotal) {
    let frameStep = (SINGLE_HEIGHT / frameTotal) * frameIndex
    yOffset += frameStep

    setLabelState(ctx)
    for (let i=0; i<26; i++) {
        // left column
        ctx.fillText(idToNumberToken(displayIndexToId2(i, rotor)), xOffset - 2*CONNECTOR_RADIUS, yOffset + LEADING_HEIGHT + i*SINGLE_HEIGHT + 3)
        // right column
        ctx.fillText(idToNumberToken(displayIndexToId2(i, rotor)), xOffset + COMPONENT_WIDTH - 2*CONNECTOR_RADIUS, yOffset + LEADING_HEIGHT + i*SINGLE_HEIGHT + 3)
    }
}

function drawRotorOuterRing(rotor, ctx, xOffset, yOffset, frameIndex, frameTotal) {
    let origYOffset = yOffset
    let frameStep = (SINGLE_HEIGHT / frameTotal) * frameIndex
    yOffset += frameStep

    // outer ring background
    ctx.fillStyle = 'rgba(169,169,169,0.75)';
    ctx.fillRect(xOffset+10, yOffset, 20, BORDER_HEIGHT);

    // outer ring labels
    ctx.fillStyle = 'rgb(0, 0, 0)';
    ctx.font = 'bold 12px arial'
    for (let i=0; i<26; i++) {
        ctx.fillText(idToCharToken((displayIndexToId(i) + rotor.getInternalPosition()) % 26), xOffset + 15, yOffset + LEADING_HEIGHT + i*SINGLE_HEIGHT + 3)
    }

    // outer ring position 'window'
    ctx.strokeStyle = 'rgb(0,0,0)'
    ctx.strokeRect(xOffset + 9, origYOffset + LEADING_HEIGHT + A_POSITION*SINGLE_HEIGHT - 11, 22, 22)

    // outer ring notch
    ctx.fillStyle = 'rgb(0, 0, 0)';
    let displayIndex = (A_POSITION + rotor.getInternalPosition() - (rotor.getTurnoverPosition().charCodeAt(0) - 65) + 26) % 26
    ctx.beginPath()
    ctx.moveTo(xOffset + 10, yOffset + LEADING_HEIGHT + displayIndex*SINGLE_HEIGHT - 10)
    ctx.lineTo(xOffset + 14, yOffset + LEADING_HEIGHT + displayIndex*SINGLE_HEIGHT - 10 + 0.5*SINGLE_HEIGHT)
    ctx.lineTo(xOffset + 10, yOffset + LEADING_HEIGHT + displayIndex*SINGLE_HEIGHT + 10)
    ctx.fill()

    // mark ring setting with a red dot
    // note: ringsetting = 6 means that the 6th character on the outer ring ('06' or 'F') is aligned with the 1st connector (whose id is 0)
    ctx.beginPath()
    ctx.fillStyle = 'rgb(144, 12, 63 )'
    ctx.arc(xOffset + 29, yOffset + LEADING_HEIGHT + idToDisplayIndex2(0, rotor) * SINGLE_HEIGHT, 3, Math.PI*2, false)
    ctx.fill()

}

//////////////////////////////////////////////////////////////////////////
// plugboard drawing

function drawPlugboard(plugboard, ctx, xOffset, yOffset) {
    drawPlugboardBackground(ctx, xOffset, yOffset)
    drawPlugboardWiring(plugboard, ctx, xOffset, yOffset)
    drawPlugboardBorder(ctx, xOffset, yOffset)
    drawPlugboardLabels(ctx, xOffset, yOffset)
}

function drawPlugboardBackground(ctx, xOffset, yOffset) {
    setComponentBackgroundState(ctx)
    ctx.fillRect(xOffset, yOffset, COMPONENT_WIDTH, BORDER_HEIGHT);
}

function drawPlugboardWiring(plugboard, ctx, xOffset, yOffset) {
    let contactsProcessed = []
    for (let i=0; i < 26; i++) {
        contactsProcessed.push(0)
    }

    let wiring = plugboard.getInternalWiringTable()

    setWiringState(ctx)
    ctx.beginPath()

    // draw paired letters
    for(let i = 0; i<26; i++) {
        if (contactsProcessed[i] === 0) {
            let position1 = idToDisplayIndex(i)
            let contactId2 = (i + wiring[i] + 26) % 26
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

function drawPlugboardBorder(ctx, xOffset, yOffset) {
    setComponentBorderState(ctx)

    ctx.beginPath();

    borderPath(ctx, xOffset, yOffset)
    borderPath(ctx, xOffset + COMPONENT_WIDTH, yOffset)

    ctx.stroke()
}

function drawPlugboardLabels(ctx, xOffset, yOffset) {
    setLabelState(ctx)
    for (let i=0; i<26; i++) {
        // left column
        ctx.fillText(idToCharToken(displayIndexToId(i)), xOffset - 2*CONNECTOR_RADIUS + 2, yOffset + LEADING_HEIGHT + i*SINGLE_HEIGHT + 3)
        // right column
        ctx.fillText(idToCharToken(displayIndexToId(i)), xOffset + COMPONENT_WIDTH - 2*CONNECTOR_RADIUS + 2, yOffset + LEADING_HEIGHT + i*SINGLE_HEIGHT + 3)
    }
}

//////////////////////////////////////////////////////////////////////////
// keys drawing

function drawKeys(ctx, xOffset, yOffset) {
    for (let i=0; i<26; i++) {
        // draw circle
        ctx.beginPath();
        ctx.arc(xOffset + KEY_RADIUS + (i%2)*KEY_SHIFT, yOffset + LEADING_HEIGHT-8 + i*SINGLE_HEIGHT + KEY_RADIUS, KEY_RADIUS, 0, Math.PI * 2, true);
        ctx.stroke()

        // write letter
        ctx.fillStyle = 'rgb(0, 0, 0)';
        ctx.font = 'bold 12px arial'
        ctx.fillText(idToCharToken(displayIndexToId(i)), xOffset + KEY_RADIUS-4 + (i%2)*KEY_SHIFT, yOffset + LEADING_HEIGHT + 6 + i*SINGLE_HEIGHT)
    }
}

//////////////////////////////////////////////////////////////////////////
// connection column drawing

function drawConnectionColumn(ctx,  xLeft, xRight, yOffset) {

    setWiringState(ctx)
    ctx.beginPath()

    for(let i = 0; i<26; i++) {
        ctx.moveTo(xLeft, yOffset + LEADING_HEIGHT + i*SINGLE_HEIGHT)
        ctx.lineTo(xRight, yOffset + LEADING_HEIGHT + i*SINGLE_HEIGHT)
    }
    ctx.stroke()
}

function drawKeyConnectionColumn(ctx,  xLeft, xRight, yOffset) {
    setWiringState(ctx)

    ctx.beginPath()
    for(let i = 0; i<26; i++) {
        ctx.moveTo(xLeft , yOffset + LEADING_HEIGHT + i*SINGLE_HEIGHT)
        ctx.lineTo(xRight + (i%2)*20, yOffset + LEADING_HEIGHT + i*SINGLE_HEIGHT)
    }
    ctx.stroke()
}

//////////////////////////////////////////////////////////////////////////
// connection path drawing

function drawPath(ctx, xOffset, yOffset, points) {
    setPathState(ctx)
    ctx.beginPath()


    // key to plug board (always a horizontal line)
    let keyXLeft = xOffset + 4*(COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
    let keyXRight_inbound = keyXLeft + SPACING + (points[0]%2)*KEY_SHIFT
    let keyY_inbound = yOffset + LEADING_HEIGHT + displayIndexToId(points[0]) * SINGLE_HEIGHT
    console.log("path, key to plugboard:  (" + keyXRight_inbound + ", " + keyY_inbound + ") to (" + keyXLeft + ", " + keyY_inbound + ")")
    ctx.moveTo(keyXRight_inbound, keyY_inbound)
    ctx.lineTo(keyXLeft, keyY_inbound)

    // wiring in plugboard
    let plugboardXLeft = xOffset + 4*(COMPONENT_WIDTH + SPACING)
    let plugboardYLeft_inbound = yOffset + LEADING_HEIGHT + displayIndexToId(points[1]) * SINGLE_HEIGHT
    ctx.lineTo(plugboardXLeft, plugboardYLeft_inbound)

    // plugboard to rotor 3 (always a horizontal line)
    let rotor3XRight = xOffset + 3*(COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
    ctx.lineTo(rotor3XRight, plugboardYLeft_inbound)

    // wiring in rotor 3
    let rotor3XLeft = xOffset + 3*(COMPONENT_WIDTH + SPACING)
    let rotor3Left_inbound = yOffset + LEADING_HEIGHT + displayIndexToId(points[2]) * SINGLE_HEIGHT
    ctx.lineTo(rotor3XLeft, rotor3Left_inbound)

    // rotor 3 to rotor 2 (always a horizontal line)
    let rotor2XRight = xOffset + 2*(COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
    ctx.lineTo(rotor2XRight, rotor3Left_inbound)

    // wiring in rotor 2
    let rotor2XLeft = xOffset + 2*(COMPONENT_WIDTH + SPACING)
    let rotor2Left_inbound = yOffset + LEADING_HEIGHT + displayIndexToId(points[3]) * SINGLE_HEIGHT
    ctx.lineTo(rotor2XLeft, rotor2Left_inbound)

    // rotor 2 to rotor 1 (always a horizontal line)
    let rotor1XRight_inbound = xOffset + 1*(COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
    ctx.lineTo(rotor1XRight_inbound, rotor2Left_inbound)

    // wiring in rotor 1 (inbound)
    let rotor1XLeft = xOffset + 1*(COMPONENT_WIDTH + SPACING)
    let rotor1YLeft_inbound = yOffset + LEADING_HEIGHT + displayIndexToId(points[4]) * SINGLE_HEIGHT
    ctx.lineTo(rotor1XLeft, rotor1YLeft_inbound)

    // rotor 1 to reflector (always a horizontal line)
    let reflectorX_border = xOffset + COMPONENT_WIDTH
    ctx.lineTo(reflectorX_border, rotor1YLeft_inbound)

    // wiring in reflector
    // 1. right-to-left
    let reflectorX_internal = reflectorX_border - 25
    ctx.lineTo(reflectorX_internal, rotor1YLeft_inbound)
    // 2. vertical
    let reflectorY_outbound = yOffset + LEADING_HEIGHT + displayIndexToId(points[5]) * SINGLE_HEIGHT
    ctx.lineTo(reflectorX_internal, reflectorY_outbound)
    // 3. left-to-right
    ctx.lineTo(reflectorX_border, reflectorY_outbound)

    // reflector to rotor 1 (always a horizontal line)
    let rotor1XLeft_outbound = xOffset + 1*(COMPONENT_WIDTH + SPACING)
    ctx.lineTo(rotor1XLeft_outbound, reflectorY_outbound)

    // wiring in rotor 1 (outbound)
    let rotor1XRight_outbound = xOffset + 1*(COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
    let rotor1YRight_outbound = yOffset + LEADING_HEIGHT + displayIndexToId(points[6]) * SINGLE_HEIGHT
    ctx.lineTo(rotor1XRight_outbound, rotor1YRight_outbound)

    // rotor 1 to rotor 2 (outbound, always a horizontal line)
    let rotor2XLeft_outbound = xOffset + 2*(COMPONENT_WIDTH + SPACING)
    ctx.lineTo(rotor2XLeft_outbound, rotor1YRight_outbound)

    // wiring in rotor 2 (outbound)
    let rotor2XRight_outbound = xOffset + 2*(COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
    let rotor2YRight_outbound = yOffset + LEADING_HEIGHT + displayIndexToId(points[7]) * SINGLE_HEIGHT
    ctx.lineTo(rotor2XRight_outbound, rotor2YRight_outbound)

    // rotor 2 to rotor 3 (outbound, always a horizontal line)
    let rotor3XLeft_outbound = xOffset + 3*(COMPONENT_WIDTH + SPACING)
    ctx.lineTo(rotor3XLeft_outbound, rotor2YRight_outbound)

    // wiring in rotor 3 (outbound)
    let rotor3XRight_outbound = xOffset + 3*(COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
    let rotor3YRight_outbound = yOffset + LEADING_HEIGHT + displayIndexToId(points[8]) * SINGLE_HEIGHT
    ctx.lineTo(rotor3XRight_outbound, rotor3YRight_outbound)

    // rotor 3 to plugboard (outbound, always a horizontal line)
    let plugboardXLeft_outbound = xOffset + 4*(COMPONENT_WIDTH + SPACING)
    ctx.lineTo(plugboardXLeft_outbound, rotor3YRight_outbound)

    // wiring in plugboard (outbound)
    let plugboardXRight_outbound = xOffset + 4*(COMPONENT_WIDTH + SPACING) + COMPONENT_WIDTH
    let plugboardYRight_outbound = yOffset + LEADING_HEIGHT + displayIndexToId(points[9]) * SINGLE_HEIGHT
    ctx.lineTo(plugboardXRight_outbound, plugboardYRight_outbound)

    // plugboard to key/light (outbound, always a horizontal line)
    let keyXRight_outbound = xOffset + 5*(COMPONENT_WIDTH + SPACING)
    ctx.lineTo(keyXRight_outbound, plugboardYRight_outbound)

    // stroke
    ctx.stroke()
}

//////////////////////////////////////////////////////////////////////////
// border helper function

function borderPath(ctx, xOffset, yOffset) {
    // start position
    ctx.moveTo(xOffset, yOffset);
    // leading bit
    ctx.lineTo(xOffset, yOffset + LEADING_HEIGHT - CONNECTOR_RADIUS);
    // halfround connector bits
    for (let i = 0; i < 26; i++) {
        ctx.arc(xOffset, yOffset + LEADING_HEIGHT + i * SINGLE_HEIGHT, CONNECTOR_RADIUS, Math.PI * 1.5, Math.PI * 0.5, false)
        ctx.moveTo(xOffset, yOffset + LEADING_HEIGHT + i * SINGLE_HEIGHT + 2 * CONNECTOR_RADIUS)
        ctx.lineTo(xOffset, yOffset + LEADING_HEIGHT + i * SINGLE_HEIGHT + CONNECTOR_RADIUS);
    }
    // trailing bit
    ctx.lineTo(xOffset, yOffset + LEADING_HEIGHT + 25 * SINGLE_HEIGHT + TRAILING_HEIGHT);
}

//////////////////////////////////////////////////////////////////////////
// drawing state helper functions

function setWiringState(ctx) {
    ctx.lineWidth = 1
    ctx.strokeStyle = 'rgb(169,169,169)'
}

function setComponentBackgroundState(ctx) {
    ctx.fillStyle = 'rgb(255, 255, 204)';
}

function setComponentBorderState(ctx) {
    ctx.strokeStyle = 'rgb(0,0,0)'
    ctx.lineWidth = 2
}

function setPathState(ctx) {
    ctx.lineWidth = 3
    ctx.strokeStyle = 'rgb(144, 12, 63 )'
}

function setLabelState(ctx) {
    ctx.fillStyle = 'rgb(0, 0, 0)';
    ctx.font = 'bold 8px arial'
}

//////////////////////////////////////////////////////////////////////////
// helper functions


function displayIndexToId (displayIndex) {
    return (26 - displayIndex + A_POSITION) % 26
}

function displayIndexToId2 (displayIndex, rotor) {
    return (26 - displayIndex + A_POSITION + rotor.getInternalPosition() - rotor.getInternalRingSetting() + 26) % 26
}

function idToDisplayIndex (connectionId) {
    return (A_POSITION - connectionId + 26) %26
}

function idToDisplayIndex2 (connectionId, rotor) {
    return (A_POSITION - connectionId + 26 + rotor.getInternalPosition() - rotor.getInternalRingSetting() +26) %26
}

function charToId(char) {
    return char.charCodeAt(0) - 'A'.charCodeAt(0)
}

function idToCharToken(connectionId) {
    return String.fromCharCode(connectionId + 'A'.charCodeAt(0))
}

function idToNumberToken(connectionId) {
    return (connectionId+1).toLocaleString('nl-NL', {minimumIntegerDigits: 2})
}


