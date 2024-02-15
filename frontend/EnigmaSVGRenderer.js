class EnigmaSVGRenderer {

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

//    rotorFrameTotal = 20
//    rotorPrevFrameIndex = -1
//    rotorFramePeriodInMs = this.rotorSteppingDuration / this.rotorFrameTotal

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
        this.reflectorRenderer = new ReflectorSVGRenderer(this.enigma.reflector)
        this.rotor1Renderer = new RotorSVGRenderer(this.enigma, 1)
        this.rotor2Renderer = new RotorSVGRenderer(this.enigma, 2)
        this.rotor3Renderer = new RotorSVGRenderer(this.enigma, 3)
        this.plugboardRenderer = new PlugboardSVGRenderer(this.enigma.plugboard, enigma.getAlphabetSize())
        this.keyboardRenderer = new KeyboardSVGRenderer(enigma.getAlphabetSize())
        this.encipherPathRenderer = new EncipherPathSVGRenderer(this.reflectorRenderer)
    }

    init(parentId) {
        this.svgParentId = parentId
        this.redrawEnigma("init")

        document.addEventListener('click', this.handleClick.bind(this))
    }

    redrawEnigma(caller = "<unknown>") {
    //    console.log("redrawEnigma called by " + caller)
        const div = document.getElementById(this.svgParentId)

        // remove any existing SVG element of this div
        // (we're assuming it will contain max 1 child element)
        if (div.lastChild) {
            div.removeChild(div.lastChild)
        }

        // create the svg element
        let svg = document.createElementNS(SVG_NS, "svg");
        svg.setAttribute("width", "1000");
        svg.setAttribute("height", "600");
        svg.style.cssText = 'border: 1px solid black'
        svg.id = "enigma"
        div.appendChild(svg)

        this.draw(svg)
    }

    draw(svg) {
//        this.drawSimple(svg)
        this.drawLayered(svg)
    }

    drawLayered(svg) {
        // step 1: first draw all component backgrounds (to appear behind the encipher path)
        this.drawComponentLabels(svg, LEFT_MARGIN, 20)
        this.reflectorRenderer.drawBackground(svg, LEFT_MARGIN, TOP_MARGIN)
        this.rotor1Renderer.drawBackground(svg, "rotor1", LEFT_MARGIN + 1*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor2Renderer.drawBackground(svg, "rotor2", LEFT_MARGIN + 2*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor3Renderer.drawBackground(svg, "rotor3", LEFT_MARGIN + 3*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.plugboardRenderer.drawBackground(svg, LEFT_MARGIN + 4*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)

        // step 2: add a container (SVG group) to hold the encipher path
        this.encipherPathRenderer.addPathContainer(svg)

        // step 3: draw everything else (to appear in front of the encipher path)
        this.drawConnectionColumn(svg, "column1", LEFT_MARGIN + 1*COMPONENT_WIDTH, SPACING, TOP_MARGIN, enigma.getAlphabetSize())
        this.drawConnectionColumn(svg, "column2", LEFT_MARGIN + 2*COMPONENT_WIDTH + SPACING, SPACING, TOP_MARGIN, enigma.getAlphabetSize())
        this.drawConnectionColumn(svg, "column3", LEFT_MARGIN + 3*COMPONENT_WIDTH + 2*SPACING, SPACING, TOP_MARGIN, enigma.getAlphabetSize())
        this.drawConnectionColumn(svg, "column4", LEFT_MARGIN + 4*COMPONENT_WIDTH + 3*SPACING, SPACING, TOP_MARGIN, enigma.getAlphabetSize())
        this.drawKeyConnectionColumn(svg, "column5", LEFT_MARGIN + 5*COMPONENT_WIDTH + 4*SPACING, SPACING, TOP_MARGIN, enigma.getAlphabetSize())

        this.reflectorRenderer.drawForeground(svg, LEFT_MARGIN, TOP_MARGIN)
        this.rotor1Renderer.drawForeground(svg, "rotor1", LEFT_MARGIN + 1*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor2Renderer.drawForeground(svg, "rotor2", LEFT_MARGIN + 2*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor3Renderer.drawForeground(svg, "rotor3", LEFT_MARGIN + 3*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.plugboardRenderer.drawForeground(svg, LEFT_MARGIN + 4*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.keyboardRenderer.draw(svg, LEFT_MARGIN + 5*(COMPONENT_WIDTH + SPACING), TOP_MARGIN, this.pressedKeyId, this.lightedKeyId)
    }

    drawSimple(svg) {
        this.drawConnectionColumn(svg, "column1", LEFT_MARGIN + 1*COMPONENT_WIDTH, SPACING, TOP_MARGIN, enigma.getAlphabetSize())
        this.drawConnectionColumn(svg, "column2", LEFT_MARGIN + 2*COMPONENT_WIDTH + SPACING, SPACING, TOP_MARGIN, enigma.getAlphabetSize())
        this.drawConnectionColumn(svg, "column3", LEFT_MARGIN + 3*COMPONENT_WIDTH + 2*SPACING, SPACING, TOP_MARGIN, enigma.getAlphabetSize())
        this.drawConnectionColumn(svg, "column4", LEFT_MARGIN + 4*COMPONENT_WIDTH + 3*SPACING, SPACING, TOP_MARGIN, enigma.getAlphabetSize())
        this.drawKeyConnectionColumn(svg, "column5", LEFT_MARGIN + 5*COMPONENT_WIDTH + 4*SPACING, SPACING, TOP_MARGIN, enigma.getAlphabetSize())

        this.reflectorRenderer.draw(svg, LEFT_MARGIN, TOP_MARGIN)
        this.rotor1Renderer.draw(svg, "rotor1", LEFT_MARGIN + 1*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor2Renderer.draw(svg, "rotor2", LEFT_MARGIN + 2*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.rotor3Renderer.draw(svg, "rotor3", LEFT_MARGIN + 3*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.plugboardRenderer.draw(svg, LEFT_MARGIN + 4*(COMPONENT_WIDTH + SPACING), TOP_MARGIN)
        this.keyboardRenderer.draw(svg, LEFT_MARGIN + 5*(COMPONENT_WIDTH + SPACING), TOP_MARGIN, this.pressedKeyId, this.lightedKeyId)

        // draw the enciphered path on top
        this.encipherPathRenderer.addPathContainer(svg)
    }

    drawComponentLabels(svg, x, y) {
        let group = addGroupNode(svg, "componentLabels", x, y)
        addTextNode (group, "Reflector", "reflector-label", "componentLabel")
        addTextNode (group, `Rotor ${this.enigma.rotors[1].type}`, "rotor1-label", "componentLabel", 1 * (COMPONENT_WIDTH + SPACING))
        addTextNode (group, `Rotor ${this.enigma.rotors[2].type}`, "rotor2-label", "componentLabel", 2 * (COMPONENT_WIDTH + SPACING))
        addTextNode (group, `Rotor ${this.enigma.rotors[3].type}`, "rotor3-label", "componentLabel", 3 * (COMPONENT_WIDTH + SPACING))
        addTextNode (group, "Plugboard", "plugboard-label", "componentLabel", 4 * (COMPONENT_WIDTH + SPACING))
        addTextNode (group, "Key- & lampboard", "keyboard-label", "componentLabel", 5 * (COMPONENT_WIDTH + SPACING))
    }

    handleClick (event) {
        // handle key mouse click
        // (a keyGroup is a group of SVG elements which together constitute a single keyboard key)
        let clickedKeyGroup = event.target.closest('.keyGroup')
        if (!clickedKeyGroup) return;

        // remove any path if it exists
        this.encipherPathRenderer.removePath()

        // the last character of the keyGroup id is the letter on the key
        let pressedLetter = clickedKeyGroup.id.slice(-1)
        let pressedKeyId = charToId(pressedLetter)

        this.pressedKeyId = pressedKeyId
        this.lightedKeyId = null

        // only redraw the keyboard to indicate the pressed key
        // --> has a terrible effect on the rotor animation, not smooth at all
        //     and redraw of the keyboard tends to be delayed until after the rotor animation is done
        // let svg = document.getElementById("enigma")
        // this.keyboardRenderer.draw(svg, LEFT_MARGIN + 5*(COMPONENT_WIDTH + SPACING), TOP_MARGIN, this.pressedKeyId, enigmaSVGRenderer.lightedKeyId)

        // redraw the entire thing
        // --> rotor animation remains smooth
        // animation step 1: mark the pressed key
        this.redrawEnigma("key press")

        // invoke animation step 2: step the rotor(s)
        if (this.enigma.rotorSteppingEnabled) {
            this.animateRotorStepping()
        } else {
            // immediately proceed to the steps after the rotor stepping animation
            this.rotorSteppingAnimationDone()
        }
    }

    // animation step 2: step the rotor(s)
    // (the end of this animation step triggers animation step 3)
    animateRotorStepping() {
        let steppingRotors = enigma.determineSteppingRotors()
        if (steppingRotors[0]) {
            this.rotor1Renderer.animateStep()
        }
        if (steppingRotors[1]) {
            this.rotor2Renderer.animateStep()
        }
        if (steppingRotors[2]) {
            // callback only needed on the right most rotor which always steps
            this.rotor3Renderer.animateStep(this.rotorSteppingAnimationDone.bind(this))
        }
    }

    // animation steps 3 (complete redraw with stepped rotors) and 4 (animate the enciphered path)
    rotorSteppingAnimationDone() {
        // console.log("rotorSteppingAnimationDone")
        if (this.enigma.rotorSteppingEnabled) {
            // step the rotors of the internal enigma so we're ready to redraw the enigma in the new rotor state
            enigma.stepRotors()
            // animation step 3: redraw the stepped rotors in their new position
            this.redrawEnigma("rotorSteppingAnimationDone")
        }

        // let the internal enigma encipher the input (do not step the rotors again, as we've already done that)
        //console.log("pressedKeyId: " + enigmaSVGRenderer.pressedKeyId)
        let encipherResult = this.enigma.encipherWireId(this.pressedKeyId, false)
        //console.log ("set lightedKeyId to " + encipherResult[0])
        this.lightedKeyId = encipherResult[0]

        // animation step 4: animate the encipher path
        // (the end of this animation step triggers animation step 5)
        let svg = document.getElementById("enigma")
        this.encipherPathRenderer.drawEncipherPath(encipherResult[1], this.encipherPathAnimationDone.bind(this), this.enigma.getAlphabetSize())
    }

    // animation step 5: light up the key representing the encipher result
    encipherPathAnimationDone() {
        //console.log("encipherPathAnimationDone")
        let svg = document.getElementById("enigma")
        this.keyboardRenderer.draw(svg, LEFT_MARGIN + 5*(COMPONENT_WIDTH + SPACING), TOP_MARGIN, this.pressedKeyId, this.lightedKeyId)
    }

    drawConnectionColumn(svg, columnId, xLeft, width, y, alphabetSize=26) {
        // p = display position index (each component has 26 vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor
        let group = addGroupNode(svg, columnId, xLeft, y)
        for(let p = 0; p<alphabetSize; p++) {
            addPathNode (group, `M ${CONNECTOR_RADIUS} ${LEADING_STRAIGHT + p*SINGLE + CONNECTOR_RADIUS} h ${width}`, `${columnId}_${p}`, "wire")
        }
    }

    drawKeyConnectionColumn(svg, columnId, xLeft, width, y, alphabetSize=26) {
        // p = display position index (each component has 26 vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor
        let group = addGroupNode(svg, columnId, xLeft, y)
        for(let p = 0; p<alphabetSize; p++) {
            addPathNode (group, `M ${CONNECTOR_RADIUS} ${LEADING_STRAIGHT + p*SINGLE + CONNECTOR_RADIUS} h ${width + (p%2)*KEY_SHIFT}`, `${columnId}_${p}`, "wire")
        }
    }

}
