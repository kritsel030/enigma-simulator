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
let KEY_SHIFT = 30

let A_POSITION = 11 // 0-based

const CONNECTOR_RADIUS = 4

//////////////////////////////////////////////////////////////////////////
// variables

let _reflector = new Reflector('B')
let _rotor1 = new Rotor('I', 'A', 6)
let _rotor2 = new Rotor('II', 'D', 5)
let _rotor3 = new Rotor('III', 'U', 3)
let _plugboard = new Plugboard('AC-DK-GI-JX-OE-XZ')

let enigma = new Enigma (_reflector, _rotor1, _rotor2, _rotor3, _plugboard)

let enigmaRenderer = new EnigmaRenderer(enigma)

let canvas;
let ctx;
let raf;
let running = false

let steppingRotors = [false, false, false]

//let animationLeadTime = 1000 // in ms
// let frameTotal = 20
// let prevFrameIndex = -1
// let framePeriodInMs = animationLeadTime / frameTotal

// let pressedKey
// let lightedKey
// let buttonCenters

let encryptionPoints

//////////////////////////////////////////////////////////////////////////
// init

function init() {
    console.log("init")
    //console.log("framePeriodInMs: " + framePeriodInMs)
    canvas = document.getElementById("enigma")
    ctx = canvas.getContext('2d')
    draw()

    canvas.addEventListener('click', function(e) {
        // start with a reset of the canvas
        // pressedKey = -1
        // lightedKey = -1
        ctx.clearRect(0 , 0, 1000, 1000)
        draw()

        const buttonCenters = enigmaRenderer.keysRenderer.buttonCenters
        if (!running) {
            // https://stackoverflow.com/questions/17130395/real-mouse-position-in-canvas
            let pressedKeyPosition = -1
            let rect = canvas.getBoundingClientRect()
            let x = e.clientX - rect.left;
            let y = e.clientY - rect.top;
            for (let i=0; i<26; i++) {
                //console.log("button center " + i + ": " + buttonCenters[i][0] + ", " + buttonCenters[i][1])
                if (buttonCenters[i][0]-(KEY_RADIUS) <= x && x <= buttonCenters[i][0]+KEY_RADIUS &&
                    buttonCenters[i][1]-(KEY_RADIUS) <= y && y <= buttonCenters[i][1]+KEY_RADIUS) {
                    pressedKeyPosition = i
                    break
                }
            }
            if (pressedKeyPosition >= 0) {
                let clickedLetter = idToCharToken(displayIndexToId(pressedKeyPosition))
                console.log("click on " + clickedLetter + " ,start animation")
                // // redraw the canvas with the pushed button
                // ctx.clearRect(0 , 0, 1000, 1000)
                // draw(pressedKeyId)
                // // set the points for the path, and start the rotor animation, which ends with the path drawn in red
                // encryptionPoints = [displayIndexToId(pressedKey), 1, 18, 22, 15, 5, 8, 24, 21, displayIndexToId(20)]
                // animationStartTime = new Date();
                startAnimation(displayIndexToId(pressedKeyPosition));
            } else {
                console.log("click was not on a button")
            }
        }
    });

    // canvas.addEventListener('mouseout', function(e) {
    //     console.log("stop animation")
    //     window.cancelAnimationFrame(raf);
    //     running = false;
    // });

}

//////////////////////////////////////////////////////////////////////////
// animate

function startAnimation(pressedKeyId) {
    // clear the canvas
    ctx.clearRect(0 , 0, 1000, 1000)

    // reset animation params
    const now = new Date().getTime()
    enigmaRenderer.pressedKeyId = pressedKeyId
    enigmaRenderer.encipherPath = null
    enigmaRenderer.lightedKeyId = null
    enigmaRenderer.animationStartTime = null
    enigmaRenderer.buttonDownStartTime = null
    enigmaRenderer.rotorSteppingStartTime = null
    enigmaRenderer.pathStartTime = null
    enigmaRenderer.animationPhase = 1
    enigmaRenderer.rotorPrevFrameIndex = -1

    // and let's kick off the actual animation
    running = true;
    raf = window.requestAnimationFrame(drawAnimatedFrame);

    //encryptionPoints = [displayIndexToId(pressedKey), 1, 18, 22, 15, 5, 8, 24, 21, displayIndexToId(20)]
}

function drawAnimatedFrame(frameTime) {
    if (!enigmaRenderer.animationStartTime) {
        enigmaRenderer.animationStartTime = frameTime
    }

    // first phase: press down key (not yet animated now)
    if (enigmaRenderer.animationPhase === 1) {
        // first time in this phase? register start time.
        if (!enigmaRenderer.buttonDownStartTime) {
            console.log("start button down render phase")
            enigmaRenderer.buttonDownStartTime = frameTime
        }
        draw()
        // and on to the next screen repaint
        enigmaRenderer.animationPhase = 2
        raf = window.requestAnimationFrame(drawAnimatedFrame);
    }

    // second phase: rotor stepping animation
    else if (enigmaRenderer.animationPhase === 2) {
        // first time in this phase? register start time.
        if (!enigmaRenderer.rotorSteppingStartTime) {
            console.log("start rotor stepping render phase")
            enigmaRenderer.rotorSteppingStartTime = frameTime
        }

        let steppingRotors = enigma.calculateStep()
        let msSinceStart = frameTime - enigmaRenderer.rotorSteppingStartTime
        let frameIndex = Math.floor(msSinceStart / enigmaRenderer.rotorFramePeriodInMs)
        if (enigmaRenderer.rotorPrevFrameIndex !== frameIndex && frameIndex < enigmaRenderer.rotorFrameTotal) {
            //console.log("new frame index " + frameIndex + " (" + msSinceStart + " ms since start)")
            enigmaRenderer.rotorPrevFrameIndex = frameIndex
            if (steppingRotors[0]) {
                enigmaRenderer.rotor1Renderer.draw(ctx, LEFT_MARGIN + 1 * (COMPONENT_WIDTH + SPACING), TOP_MARGIN, frameIndex, enigmaRenderer.rotorFrameTotal, true)
            }
            if (steppingRotors[1]) {
                enigmaRenderer.rotor2Renderer.draw(ctx, LEFT_MARGIN + 2 * (COMPONENT_WIDTH + SPACING), TOP_MARGIN, frameIndex, enigmaRenderer.rotorFrameTotal, true)
            }
            if (steppingRotors[2]) {
                enigmaRenderer.rotor3Renderer.draw(ctx, LEFT_MARGIN + 3 * (COMPONENT_WIDTH + SPACING), TOP_MARGIN, frameIndex, enigmaRenderer.rotorFrameTotal, true)
            }
        }
        // is this animation phase finished? then on to the next phase!
        if (frameIndex === enigmaRenderer.rotorFrameTotal - 1) {
            enigmaRenderer.animationPhase = 3
        }
        // and on to the next screen repaint
        raf = window.requestAnimationFrame(drawAnimatedFrame);
    }

    // third phase: path drawing
    else if (enigmaRenderer.animationPhase === 3) {
        // first time in this phase? register start time, and calculate the actual enciphering!
        if (!enigmaRenderer.pathStartTime) {
            console.log("start path render phase")
            enigmaRenderer.pathStartTime = frameTime
            let encipherResult = enigma.encipherNormalized(enigmaRenderer.pressedKeyId)
            enigmaRenderer.setEncipherPath(encipherResult[1], LEFT_MARGIN, TOP_MARGIN)
        }

        // clear the canvas
        ctx.clearRect(0 , 0, 1000, 1000)
        // and redraw
        draw(frameTime)

        // is this animation phase finished? then on to the next phase!
        if (frameTime > enigmaRenderer.pathStartTime + enigmaRenderer.pathDuration) {
            enigmaRenderer.animationPhase = 4
        }

        // and on to the next screen repaint
        raf = window.requestAnimationFrame(drawAnimatedFrame);
    }

    // fourth phase: light up the encipher result key
    else if (enigmaRenderer.animationPhase === 4) {
        console.log("animation done!")
        window.cancelAnimationFrame(raf);
        enigmaRenderer.animationPhase = 0
        // clear entire canvas
        ctx.clearRect(0, 0, 1000, 1000)
        // redraw entire enigma (including pressed key, path and lighted key)
        let encipherResult = enigma.encipherNormalized(enigmaRenderer.pressedKeyId, false)
        enigmaRenderer.lightedKeyId = encipherResult[0]
        running = false
        draw()
    }

}

function draw(frameTime) {
    enigmaRenderer.draw(ctx, frameTime)
}

//////////////////////////////////////////////////////////////////////////
// connection column drawing

function drawConnectionColumn(ctx,  xLeft, xRight, yOffset) {

    setWiringState(ctx)
    ctx.beginPath()

    // p = display position index (each component has 26 vertical positions)
    // 0 = position at the top of the rotor
    // 25 = position at the bottom of the rotor
    for(let p = 0; p<26; p++) {
        ctx.moveTo(xLeft, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT)
        ctx.lineTo(xRight, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT)
    }
    ctx.stroke()
}

function drawKeyConnectionColumn(ctx,  xLeft, xRight, yOffset) {
    setWiringState(ctx)

    ctx.beginPath()
    // p = display position index (each component has 26 vertical positions)
    // 0 = position at the top of the rotor
    // 25 = position at the bottom of the rotor
    for(let p = 0; p<26; p++) {
        ctx.moveTo(xLeft , yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT)
        ctx.lineTo(xRight + (p%2)*KEY_SHIFT, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT)
    }
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
    // p = display position index (each component has 26 vertical positions)
    // 0 = position at the top of the rotor
    // 25 = position at the bottom of the rotor
    for (let p = 0; p < 26; p++) {
        ctx.arc(xOffset, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT, CONNECTOR_RADIUS, Math.PI * 1.5, Math.PI * 0.5, false)
        ctx.moveTo(xOffset, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT + 2 * CONNECTOR_RADIUS)
        ctx.lineTo(xOffset, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT + CONNECTOR_RADIUS);
    }
    // trailing bit
    ctx.lineTo(xOffset, yOffset + LEADING_HEIGHT + 25*SINGLE_HEIGHT + TRAILING_HEIGHT);
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
    return (26 - displayIndex + A_POSITION + rotor.getNormalizedPosition() - rotor.getNormalizedRingSetting() + 26) % 26
}

function idToDisplayIndex (connectionId) {
    return (A_POSITION - connectionId + 26) %26
}

function idToDisplayIndex2 (connectionId, rotor) {
    return (A_POSITION - connectionId + 26 + rotor.getNormalizedPosition() - rotor.getNormalizedRingSetting() +26) %26
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



