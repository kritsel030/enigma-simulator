//////////////////////////////////////////////////////////////////////////
// constants

const UNIT = 4
const CONNECTOR_RADIUS = UNIT
const STRAIGHT = 2*UNIT
const SINGLE = STRAIGHT + 2*CONNECTOR_RADIUS
const LEADING_STRAIGHT = STRAIGHT
const TRAILING_STRAIGHT = STRAIGHT


let SINGLE_HEIGHT = 20
let LEADING_HEIGHT = SINGLE_HEIGHT * 0.5
let TRAILING_HEIGHT = SINGLE_HEIGHT * 0.5

let COMPONENT_WIDTH = 75
let SPACING = 40

let LEFT_MARGIN = 100
let TOP_MARGIN = 30

// let BORDER_HEIGHT = LEADING_HEIGHT + SINGLE_HEIGHT * 25 + TRAILING_HEIGHT
//let BORDER_HEIGHT = LEADING_HEIGHT + SINGLE_HEIGHT * 5 + TRAILING_HEIGHT

let KEY_RADIUS = 10
let KEY_SHIFT = 30

//let A_POSITION = 11 // 0-based
//let A_POSITION = 3 // 0-based


//////////////////////////////////////////////////////////////////////////
// variables

// interpret query string (?reflector=B&rotor1=I-C-5)
const urlParams = new URLSearchParams(window.location.search);
// reflector
var reflectorParam
if (urlParams.get('reflector')) {
  reflectorParam = urlParams.get('reflector')
  console.log('reflector: ' + reflectorParam);
}

// rotor 1 (example value: II-D-5)
var rotor1TypeParam;
var rotor1PositionParam;
var rotor1RingSettingParam
if (urlParams.has('rotor1') ) {
  if (urlParams.get('rotor1').split('-').length = 3) {
    rotor1ParamElements = urlParams.get('rotor1').split('-');
    rotor1TypeParam = rotor1ParamElements[0];
    rotor1PositionParam = rotor1ParamElements[1];
    rotor1RingSettingParam = rotor1ParamElements[2];
    console.log('rotor 1 type: ' + rotor1TypeParam);
    console.log('rotor 1 position: ' + rotor1PositionParam);
    console.log('rotor 1 ringSetting: ' + rotor1RingSettingParam);
  }
}

// rotor 2 
var rotor2TypeParam;
var rotor2PositionParam;
var rotor2RingSettingParam
if (urlParams.has('rotor2') ) {
  if (urlParams.get('rotor2').split('-').length = 3) {
    rotor2ParamElements = urlParams.get('rotor2').split('-');
    rotor2TypeParam = rotor2ParamElements[0];
    rotor2PositionParam = rotor2ParamElements[1];
    rotor2RingSettingParam = rotor2ParamElements[2];
    console.log('rotor 2 type: ' + rotor2TypeParam);
    console.log('rotor 2 position: ' + rotor2PositionParam);
    console.log('rotor 2 ringSetting: ' + rotor2RingSettingParam);
  }
}

// rotor 3 
var rotor3TypeParam;
var rotor3PositionParam;
var rotor3RingSettingParam
if (urlParams.has('rotor3') ) {
  if (urlParams.get('rotor3').split('-').length = 3) {
    rotor3ParamElements = urlParams.get('rotor3').split('-');
    rotor3TypeParam = rotor3ParamElements[0];
    rotor3PositionParam = rotor3ParamElements[1];
    rotor3RingSettingParam = rotor3ParamElements[2];
    console.log('rotor 3 type: ' + rotor3TypeParam);
    console.log('rotor 3 position: ' + rotor3PositionParam);
    console.log('rotor 3 ringSetting: ' + rotor3RingSettingParam);
  }
}

//plugboard
var plugboardParam
if (urlParams.has('plugboard') ) {
  plugboardParam = urlParams.get('plugboard')
  console.log('plugboard: ' + plugboardParam);
}

// alphabet size
//var alphabetSize = 26
var alphabetSize = 6
if (urlParams.has('alphabetSize') ) {
  alphabetSize = parseFloat(urlParams.get('alphabetSize'))
  console.log('alphabetSize: ' + alphabetSize);
}

let BORDER_HEIGHT = LEADING_HEIGHT + SINGLE_HEIGHT * (alphabetSize-1) + TRAILING_HEIGHT
let COMPONENT_HEIGHT = LEADING_STRAIGHT + (alphabetSize-1) * (2*CONNECTOR_RADIUS + STRAIGHT) + 2*CONNECTOR_RADIUS + TRAILING_STRAIGHT
let A_POSITION = alphabetSize/2 - 1 // 0-based

// 26 letter enigma
//let _reflector = new Reflector(reflectorParam ?? 'B', alphabetSize)
//let _rotor1 = new Rotor(rotor1TypeParam ?? 'I', rotor1PositionParam ?? 'A', rotor1RingSettingParam ?? 6, alphabetSize)
//let _rotor2 = new Rotor(rotor2TypeParam ?? 'II', rotor2PositionParam ?? 'D', rotor2RingSettingParam ?? 5, alphabetSize)
//let _rotor3 = new Rotor(rotor3TypeParam ?? 'III', rotor3PositionParam ?? 'U', rotor3RingSettingParam ?? 3, alphabetSize)
//let _plugboard = new Plugboard(plugboardParam ?? 'AC-DK-GI-JB-OE-XZ', alphabetSize)

// 6 letter enigma
let _reflector = new Reflector('SC', alphabetSize)
_rotor1 = new Rotor('SCII', 'E', 1, alphabetSize)
_rotor2 = new Rotor('SCIII', 'B', 1, alphabetSize)
_rotor3 = new Rotor('SCI', 'B', 1, alphabetSize)
let _plugboard = new Plugboard('AE', alphabetSize)

let enigma = new Enigma (_reflector, _rotor1, _rotor2, _rotor3, _plugboard)

let enigmaRenderer = new EnigmaRenderer(enigma)
let enigmaSVGRenderer = new EnigmaSVGRenderer(enigma)

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

function initFormFields() {
    // rotor types
    document.getElementById("rotor1Type").value = enigma.rotors[1].type
    document.getElementById("rotor2Type").value = enigma.rotors[2].type
    document.getElementById("rotor3Type").value = enigma.rotors[3].type

    // rotor ring settings
    document.getElementById("rotor1RingSetting").value = String.fromCharCode(enigma.rotors[1].ringSetting - 1 + 'A'.charCodeAt(0))
    document.getElementById("rotor2RingSetting").value = String.fromCharCode(enigma.rotors[2].ringSetting - 1 + 'A'.charCodeAt(0))
    document.getElementById("rotor3RingSetting").value = String.fromCharCode(enigma.rotors[3].ringSetting - 1 + 'A'.charCodeAt(0))

    // rotor start positions
    document.getElementById("rotor1StartPosition").value = enigma.rotors[1].startPosition
    document.getElementById("rotor2StartPosition").value = enigma.rotors[2].startPosition
    document.getElementById("rotor3StartPosition").value = enigma.rotors[3].startPosition
}

// parentId: id of the html element which will contain the SVG
function initSVG(parentId) {
    enigmaSVGRenderer.init(parentId)
}

//////////////////////////////////////////////////////////////////////////
// UI input handlers

function handleRotor1Type(event) {
    handleRotorType(event, 1, "handleRotor1Type")
}

function handleRotor2Type(event) {
    handleRotorType(event, 2, "handleRotor2Type")
}

function handleRotor3Type(event) {
    handleRotorType(event, 3, "handleRotor3Type")
}

function handleRotorType(event, rotorNo, trigger) {
    enigma.setRotor(rotorNo, new Rotor(event.target.value, 'A', 1, 26))
    enigmaSVGRenderer.resetKeyboard()
    enigmaSVGRenderer.redrawEnigma(trigger)
    initFormFields()
}

function handleRotor1RingSetting(event) {
    console.log("handleRotor1RingSetting")
    handleRotorRingSetting(event, 1, "handleRotor1RingSetting")
}

function handleRotor2RingSetting(event) {
    console.log("handleRotor2RingSetting")
    handleRotorRingSetting(event, 2, "handleRotor2RingSetting")
}

function handleRotor3RingSetting(event) {
    console.log("handleRotor3RingSetting")
    handleRotorRingSetting(event, 3, "handleRotor3RingSetting")
}

function handleRotorRingSetting(event, rotorNo, trigger) {
    if (event.target.value.length > 0) {
        let ringSetting = event.target.value.charAt(0)
        if (ringSetting.toLowerCase() != ringSetting.toUpperCase()) {
            // now we are sure ringSetting is a letter
            // convert to uppercase in case the user entered a lower case letter
            ringSetting = ringSetting.toUpperCase()
            // translate into a number ( A --> 1)
            let numericRingSetting = ringSetting.charCodeAt(0) - 65 + 1
            console.log(ringSetting + " - " + numericRingSetting)
            enigma.rotors[rotorNo].ringSetting = numericRingSetting
        } else {
            console.log(event.target.value + " is not a valid ring setting")
            enigma.rotors[rotorNo].ringSetting = 1
            document.getElementById(event.target.id).value = "A"
        }
    } else {
        enigma.rotors[rotorNo].ringSetting = 1
        document.getElementById(event.target.id).value = "A"
    }
    enigmaSVGRenderer.resetKeyboard()
    enigmaSVGRenderer.redrawEnigma(trigger)
    initFormFields()
}

function handleRotor1StartPosition(event) {
    console.log("handleRotor1StartPosition")
    handleRotorStartPosition(event, 1, "handleRotor1StartPosition")
}

function handleRotor2StartPosition(event) {
    console.log("handleRotor2StartPosition")
    handleRotorStartPosition(event, 2, "handleRotor2StartPosition")
}

function handleRotor3StartPosition(event) {
    console.log("handleRotor3StartPosition")
    handleRotorStartPosition(event, 3, "handleRotor3StartPosition")
}

function handleRotorStartPosition(event, rotorNo, trigger) {
    if (event.target.value.length > 0) {
        let startPos = event.target.value.charAt(0)
        if (startPos.toLowerCase() != startPos.toUpperCase()) {
            // now we are sure startPos is a letter
            // convert to uppercase in case the user entered a lower case letter
            startPos = startPos.toUpperCase()
            enigma.rotors[rotorNo].startPosition = startPos
            document.getElementById(event.target.id).value = startPos
        } else {
            console.log(event.target.value + " is not a valid start position")
            enigma.rotors[rotorNo].startPosition = "A"
            document.getElementById(event.target.id).value = "A"
        }
    } else {
        enigma.rotors[rotorNo].startPosition = "A"
        document.getElementById(event.target.id).value = "A"
    }
    enigmaSVGRenderer.resetKeyboard()
    enigmaSVGRenderer.redrawEnigma(trigger)
    initFormFields()
}

function handleDisableRotorStepping(event) {
     // console.log("handleDisableRotorStepping")
     enigma.rotorSteppingDisabled = event.target.checked
     // no redraw needed
}

function handleSkipAnimation(event) {
    // console.log("handleSkipAnimation")
    enigmaSVGRenderer.animationDisabled = event.target.checked
}

function initCanvas() {
    //console.log("framePeriodInMs: " + framePeriodInMs)
    canvas = document.getElementById("enigmaCanvas")
    if (canvas) {
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
                for (let i=0; i<alphabetSize; i++) {
                    //console.log("button center " + i + ": " + buttonCenters[i][0] + ", " + buttonCenters[i][1])
                    if (buttonCenters[i][0]-(KEY_RADIUS) <= x && x <= buttonCenters[i][0]+KEY_RADIUS &&
                        buttonCenters[i][1]-(KEY_RADIUS) <= y && y <= buttonCenters[i][1]+KEY_RADIUS) {
                        pressedKeyPosition = i
                        break
                    }
                }
                if (pressedKeyPosition >= 0) {
                    let clickedLetter = idToCharToken(displayIndexToId(pressedKeyPosition, alphabetSize))
                    console.log("click on " + clickedLetter + " ,start animation")
                    // // redraw the canvas with the pushed button
                    // ctx.clearRect(0 , 0, 1000, 1000)
                    // draw(pressedKeyId)
                    // // set the points for the path, and start the rotor animation, which ends with the path drawn in red
                    // encryptionPoints = [displayIndexToId(pressedKey), 1, 18, 22, 15, 5, 8, 24, 21, displayIndexToId(20)]
                    // animationStartTime = new Date();
                    startAnimation(displayIndexToId(pressedKeyPosition, alphabetSize));
                } else {
                    console.log("click was not on a button")
                }
            }
        });

    }

}

//////////////////////////////////////////////////////////////////////////
// read form values
function readFormValues() {
    let rotor1Type = document.getElementById('rotor1Type').value
    if (rotor1Type.length > 0) {

    }
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

        let steppingRotors = enigma.determineSteppingRotors()
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

function drawConnectionColumn(ctx,  xLeft, xRight, yOffset, alphabetSize=26) {

    setWiringState(ctx)
    ctx.beginPath()

    // p = display position index (each component has 26 vertical positions)
    // 0 = position at the top of the rotor
    // 25 = position at the bottom of the rotor
    for(let p = 0; p<alphabetSize; p++) {
        ctx.moveTo(xLeft, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT)
        ctx.lineTo(xRight, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT)
    }
    ctx.stroke()
}

function drawKeyConnectionColumn(ctx,  xLeft, xRight, yOffset, alphabetSize=26) {
    setWiringState(ctx)

    ctx.beginPath()
    // p = display position index (each component has 26 vertical positions)
    // 0 = position at the top of the rotor
    // 25 = position at the bottom of the rotor
    for(let p = 0; p<alphabetSize; p++) {
        ctx.moveTo(xLeft , yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT)
        ctx.lineTo(xRight + (p%2)*KEY_SHIFT, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT)
    }
    ctx.stroke()
}


//////////////////////////////////////////////////////////////////////////
// border helper function

function borderPath(ctx, xOffset, yOffset, alphabetSize=26) {
    // start position
    ctx.moveTo(xOffset, yOffset);
    // leading bit
    ctx.lineTo(xOffset, yOffset + LEADING_HEIGHT - CONNECTOR_RADIUS);
    // halfround connector bits
    // p = display position index (each component has 26 vertical positions)
    // 0 = position at the top of the rotor
    // 25 = position at the bottom of the rotor
    for (let p = 0; p < alphabetSize; p++) {
        ctx.arc(xOffset, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT, CONNECTOR_RADIUS, Math.PI * 1.5, Math.PI * 0.5, false)
        ctx.moveTo(xOffset, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT + 2 * CONNECTOR_RADIUS)
        ctx.lineTo(xOffset, yOffset + LEADING_HEIGHT + p*SINGLE_HEIGHT + CONNECTOR_RADIUS);
    }
    // trailing bit
    ctx.lineTo(xOffset, yOffset + LEADING_HEIGHT + (alphabetSize-1)*SINGLE_HEIGHT + TRAILING_HEIGHT);
}

function connectorSVGPath(down=true, alphabetSize=26) {
    let vDown = `v ${STRAIGHT} `
    let arcDown = `a ${CONNECTOR_RADIUS} ${CONNECTOR_RADIUS} 0 0 1 0 ${2*CONNECTOR_RADIUS} `
    let vUp = `v -${STRAIGHT} `
    let arcUp = `a ${CONNECTOR_RADIUS} ${CONNECTOR_RADIUS} 0 1 0 0 -${2*CONNECTOR_RADIUS} `

    var path = ""
    for (let p = 0; p < alphabetSize; p++) {
        if (down) {
            path += (vDown + arcDown)
        } else {
            path += (vUp + arcUp)
        }
    }
    if (down) {
        path += (vDown)
    } else {
        path += (vUp)
    }
    return path
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


function displayIndexToId (displayIndex, alphabetSize=26) {
    return (alphabetSize - displayIndex + A_POSITION) % alphabetSize
}

function displayIndexToId2 (displayIndex, rotor) {
    return (alphabetSize - displayIndex + A_POSITION + rotor.getNormalizedPosition() - rotor.getNormalizedRingSetting() + rotor.alphabetSize) % rotor.alphabetSize
}

function idToDisplayIndex (connectionId, alphabetSize=26) {
    return (A_POSITION - connectionId + alphabetSize) % alphabetSize
}

function idToDisplayIndex2 (connectionId, rotor) {
    return (A_POSITION - connectionId + rotor.alphabetSize + rotor.getNormalizedPosition() - rotor.getNormalizedRingSetting() + rotor.alphabetSize) % rotor.alphabetSize
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



