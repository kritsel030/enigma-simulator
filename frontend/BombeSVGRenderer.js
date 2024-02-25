// Renders a bombe consisting of several enigmas
class BombeSVGRenderer {

    // constructor(enigmaList){
    constructor(bombe) {    
        this.variant = "scrambler_multi_line_scanning"
        this.bombe = bombe
        this.lwEnigmaRenderers = []

        for (let i = 0; i < bombe.scramblers.length; i++)  {
            this.lwEnigmaRenderers.push(new LWEnigmaSVGRenderer(bombe.scramblers[i], this))
        }

        this.indicatorDrum1Renderer = new LWDrumSVGRenderer(null, null, bombe.indicatorDrums[0])
        this.indicatorDrum2Renderer = new LWDrumSVGRenderer(null, null, bombe.indicatorDrums[1])
        this.indicatorDrum3Renderer = new LWDrumSVGRenderer(null, null, bombe.indicatorDrums[2])

        this.pathRenderer = new BombePathSVGRenderer(bombe)
    }

    // parentId: the id of the HTML element which acts as the container for the SVG node
    init(parentId) {
        this.svgParentId = parentId
        this.redrawBombe("init")

        // document.addEventListener('click', this.handleClick.bind(this))
    }

    redrawBombe(caller = "<unknown>") {
        console.log("redrawBombe called by " + caller)
        const div = document.getElementById(this.svgParentId)

        // remove any existing SVG element of this div
        // (we're assuming it will contain max 1 child element)
        if (div.lastChild) {
            div.removeChild(div.lastChild)
        }

        // create the svg element
        let svg = document.createElementNS(SVG_NS, "svg");
        svg.setAttribute("width", "1000");
        svg.setAttribute("height", "500");
        svg.style.cssText = 'border: 1px solid black'
        svg.id = "bombe"
        div.appendChild(svg)

        this.draw(svg)
    }

    draw(svg) {
        let ys = yValues(this.variant)

        let previousFirst = null
        let previousLast = null
        let x = LEFT_MARGIN
        for (let i=0; i<this.lwEnigmaRenderers.length; i++) {
            let first = i==0
            let last = i==this.lwEnigmaRenderers.length-1
            x += first ? 0 : enigmaWidth(this.variant, previousFirst, previousLast) + enigmaGap(this.variant, previousFirst, previousLast)
            this.lwEnigmaRenderers[i].draw(svg, i, this.variant,  first, last, x, TOP_MARGIN)
            previousFirst = first
            previousLast = last
        }

         // wires from bombe output to bombe input
        if (renderOutputToInputWires(this.variant)) {
            for (let i=0; i<alphabetSize; i++) {
                let path = SVGPathService.outputToInputPath(i, this.variant, this.lwEnigmaRenderers.length)
                addPathNode (svg, path, `feedback_${i}`, "wire")
            }
        }

        // indicator drums
        let xIndicator = LEFT_MARGIN + this.lwEnigmaRenderers.length * enigmaWidth(this.variant, false, false) + (this.lwEnigmaRenderers.length-1)*VERTICAL_CONNECTOR_GAP + 0.5*VERTICAL_CONNECTOR_GAP + 6*COMPONENT_DISTANCE
        let group = addGroupNode (svg, "indicatordrums", xIndicator, TOP_MARGIN)
        this.indicatorDrum1Renderer.draw(group, `${group.id}_drum1`, this.variant, 0, ys.drum1Y, true)
        this.indicatorDrum2Renderer.draw(group, `${group.id}_drum2`, this.variant, 0, ys.drum2Y, true)
        this.indicatorDrum3Renderer.draw(group, `${group.id}_drum3`, this.variant, 0, ys.drum3Y, true)

        // electrical path
        this.pathRenderer.draw(svg, this.variant, {}, LEFT_MARGIN, TOP_MARGIN)

        // proceed with path buttons
        if (renderProceedWithPathButtons(this.variant)) {
            let nextButton = addGroupNode(svg, "nextButton")
            nextButton.addEventListener('click', this.handleNextPathSegmentClick.bind(this), false) 
            addRectangleNode (nextButton, `${nextButton.id}_rect`, "pathButton", LEFT_MARGIN+100, TOP_MARGIN, 20, 20) 
            addTextNode(nextButton, "&#x25B6;", `${nextButton.id}_text`, "pathButton", LEFT_MARGIN+100+10, TOP_MARGIN+10)
            
            let completePathButton = addGroupNode(svg, "completePathButton")
            completePathButton.addEventListener('click', this.handleCompletePathClick.bind(this), false)
            addRectangleNode (completePathButton, `${completePathButton.id}_rect`, "pathButton", LEFT_MARGIN+100+20+10, TOP_MARGIN, 40, 20) 
            addTextNode(completePathButton, "&#x25B6;&#x25B6;|", `${completePathButton.id}_text`, "pathButton", LEFT_MARGIN+100+20+10+20, TOP_MARGIN+10)
        }
    }

    handleKeyClick (event) {
        // handle key mouse click
        // (a keyGroup is a group of SVG elements which together constitute a single keyboard key)
        let clickedKeyGroup = event.target.closest('.keyGroup')
        if (!clickedKeyGroup) return;

        // the last character of the keyGroup id is the letter on the key
        let pressedLetter = clickedKeyGroup.id.slice(-1)
        let pressedKeyId = charToId(pressedLetter)
        this.pressedKeyId = pressedKeyId
        // console.log(`key ${pressedLetter} got clicked`)

        // encipher the pressed key on each enigma
        // using one enigma's out put as the next enigma's input
        // (do not use rotor stepping)
        let inputId = pressedKeyId
        for (let i=0; i<this.lwEnigmaRenderers.length; i++) {
            let enigmaRenderer = this.lwEnigmaRenderers[i]
            let enigma = enigmaRenderer.enigma
            let outputId = enigma.encipherWireId(inputId, false)[0]
            enigma.plugboardInputId = inputId
            enigma.plugboardOutputId = outputId
            enigma.scramblerInputId = null
            enigma.scramblerOutputId = null
            // prepare for next enigma in the list
            inputId = outputId
        }
        this.redrawBombe("key press " + pressedLetter)
    }

    handleInputControlClick(event) {
        let clickedInputControl = event.target.closest('.activate')
        if (!clickedInputControl) return;

        let clickedInputWire = clickedInputControl.id.slice(-1)
        let scramblerInputId = Number(clickedInputWire)
        this.bombe.inputControlIds.push(scramblerInputId)

        this.calculateScramblerInputsAndOutputs(scramblerInputId)
    
        this.redrawBombe("input activation " + clickedInputWire)
    }

    handleNextPathSegmentClick(event) {
        this.calculateNextPathSegment()
        this.redrawBombe("next path segment")
    }

    handleCompletePathClick(event) {
        let inputAdded = true
        while (inputAdded) {
            inputAdded = this.calculateNextPathSegment()
        }
        this.redrawBombe("complete the path")
    }

    calculateNextPathSegment() {
        // add the scrambler outputIds of the last scrambler to the scramler inputIds of the first scrambler
        let result = this.addLastScramblerOutputIdsToFirstScramblerInputIds()

        // recalculate all scrambler inputs and outputs based on the inputs of the first scrambler
        let firstEnigma = this.bombe.scramblers[0]
        for (let i2=0; i2 < firstEnigma.scramblerInputIds.length; i2++){
            this.calculateScramblerInputsAndOutputs(firstEnigma.scramblerInputIds[i2])
        }
        return result
    }

    // add the scrambler outputIds of the last scrambler to the scramler inputIds of the first scrambler
    addLastScramblerOutputIdsToFirstScramblerInputIds() {
        let inputAdded = false
        let firstEnigma = this.bombe.scramblers[0]
        let lastEnigma = this.bombe.scramblers[this.bombe.scramblers.length-1]
        for (let i=0; i < lastEnigma.scramblerOutputIds.length; i++){
            let outputId = lastEnigma.scramblerOutputIds[i]
            if (!firstEnigma.scramblerInputIds.includes(outputId)) {
                firstEnigma.addScramblerInputId(outputId)
                inputAdded = true
            }
        }
        return inputAdded
    }

    // recalculate all scrambler inputs and outputs based on an input of the first scrambler
    calculateScramblerInputsAndOutputs(firstInputId) {
        let scramblerInputId = firstInputId
        let scramblerOutputId = null
        for (let i=0; i<this.lwEnigmaRenderers.length; i++) {
            let enigmaRenderer = this.lwEnigmaRenderers[i]
            let enigma = enigmaRenderer.enigma
            // false: do no step rotors
            // true: skip plugboard
            scramblerOutputId = enigma.encipherWireId(scramblerInputId, false, true)[0]
            enigma.plugboardInputId = null
            enigma.plugboardOutputId = null
            enigma.addScramblerInputId(scramblerInputId)
            enigma.addScramblerOutputId(scramblerOutputId)
            // prepare for next enigma in the list
            scramblerInputId = scramblerOutputId
        }
    }

    reset(event) {
        this.bombe.inputControlIds = []
        for (let i=0; i<this.lwEnigmaRenderers.length; i++) {
            let enigmaRenderer = this.lwEnigmaRenderers[i]
            enigmaRenderer.enigma.reset()
        }
    }

}
