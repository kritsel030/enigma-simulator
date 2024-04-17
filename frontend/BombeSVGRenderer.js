// Renders a bombe consisting of several enigmas
class BombeSVGRenderer {

    // constructor(enigmaList){
    constructor(bombe) {    
        // this.variant = "scrambler_multi_line_scanning"
        this.variant = "variantA"
        this.autoCompletePathOnInputActivation = false
        this.autoActivateInputOnRotorPositionChange = false
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
        svg.setAttribute("width", "1200");
        svg.setAttribute("height", "500");
        svg.style.cssText = 'border: 1px solid black'
        svg.id = "bombe"
        div.appendChild(svg)

        this.draw(svg)
    }

    draw(svg) {
        this.drawBackground(svg)

        this.drawWiring(svg)

        // electrical path
        this.pathRenderer.draw(svg, this.variant, {}, LEFT_MARGIN, TOP_MARGIN)

        this.drawForeground(svg)

    }

    drawBackground(parent) {
        let ys = yValues(this.variant)
        // diagonal board container
        if (renderDiagonalBoard(this.variant)) {
            let leftX = LEFT_MARGIN + vertConnectorXOffset(this.variant, true, false, true) - (alphabetSize+3) * WIRE_DISTANCE - 4*WIRE_DISTANCE
            let dbWidth = this.lwEnigmaRenderers.length * enigmaWidth(this.variant, false, false) + (this.lwEnigmaRenderers.length-1)*VERTICAL_CONNECTOR_GAP + 2*(alphabetSize+3)*WIRE_DISTANCE + 4*WIRE_DISTANCE
            addRectangleNode(parent, "diagonalBoard", "diagonalBoard", leftX, ys.diagonalBoard, dbWidth, DIAGONAL_BOARD_HEIGHT + WIRE_DISTANCE, 2*UNIT, 2*UNIT)
        }

        // probe
        if (renderOutputToInputWires(this.variant)) {
            let probeX = LEFT_MARGIN - 4*WIRE_DISTANCE
            let probeY = TOP_MARGIN + ys.vertConnectorY - 0.5*WIRE_DISTANCE
            addRectangleNode (parent, "probe", "probe", probeX, probeY, 2*WIRE_DISTANCE, COMPONENT_SIZE + 2*WIRE_DISTANCE, UNIT, UNIT)
        }

        // scramblers/engimas
        let noOfScramblers = numberOfScramblersToDisplay(this.variant, this.bombe.menuLetters)
        for (let i=0; i<noOfScramblers; i++) {
            let first = i==0
            let last = i==noOfScramblers-1
            let x = scramblerAbsoluteXOffset(this.variant, i, this.bombe.menuLetters)
            this.lwEnigmaRenderers[i].drawBackground(parent, i, this.variant, first, last, x, TOP_MARGIN)
        }
    }

    drawWiring(svg) {
        let ys = yValues(this.variant)
         // wires from bombe output to bombe input
        if (renderOutputToInputWires(this.variant)) {
            for (let i=0; i<alphabetSize; i++) {
//                let path = SVGPathService.outputToInputPath(i, this.variant, this.lwEnigmaRenderers.length)
                let path = SVGPathService.outputToInputPath2(i, this.variant, this.bombe.cycleEndEnigma.index)
                addPathNode (svg, path, `feedback_${i}`, "wire")
            }
        }

        // diagonal board wires
        if (renderDiagonalBoard(this.variant)) {
            // let dbBottomY = DIAGONAL_BOARD_HEIGHT
            let dbBottomY = ys.diagonalBoard + DIAGONAL_BOARD_HEIGHT - WIRE_DISTANCE

            // A-cable
            for (let i=0; i<ALPHABET_SIZE; i++) {
                let path = `M ${dbX("A", i, this.bombe.menuLetters)} ${wireY(i)} V ${dbBottomY} `
                addPathNode (svg, path, `test`, "wire")
            }

            // C-cable
            for (let i=0; i<ALPHABET_SIZE; i++) {
                let path = `M ${dbX("C", i, this.bombe.menuLetters)} ${wireY(i)} V ${dbBottomY} `
                addPathNode (svg, path, `test`, "wire")
            }

            // E-cable
            for (let i=0; i<ALPHABET_SIZE; i++) {
                let path = `M ${dbX("E", i, this.bombe.menuLetters)} ${wireY(i)} V ${dbBottomY} `
                addPathNode (svg, path, `test`, "wire")
            }

            // F-cable
            for (let i=0; i<ALPHABET_SIZE; i++) {
                //let path = `M ${dbX("F", i, this.bombe.menuLetters)} ${wireY(i)} V ${dbBottomY} `
                let startX = scramblerAbsoluteXOffset(this.variant, 3, this.bombe.menuLetters) + vertConnectorXOffset(this.variant, false, false, false, 3)
                let path = `M ${startX} ${wireY(i)} H ${dbX("F", i, this.bombe.menuLetters)} V ${dbBottomY} `
                addPathNode (svg, path, `test`, "wire")
            }

            let acDBPath = SVGPathService.diagonalBoardPath('A', 'C', this.variant, this.bombe.menuLetters)
            addPathNode (svg, acDBPath, `test`, "wire")

            let aeDBPath = SVGPathService.diagonalBoardPath('A', 'E', this.variant, this.bombe.menuLetters)
            addPathNode (svg, aeDBPath, `test`, "wire")

            let afDBPath = SVGPathService.diagonalBoardPath('A', 'F', this.variant, this.bombe.menuLetters)
            addPathNode (svg, afDBPath, `test`, "wire")

            let ceDBPath = SVGPathService.diagonalBoardPath('C', 'E', this.variant, this.bombe.menuLetters)
            addPathNode (svg, ceDBPath, `test`, "wire")

            let cfDBPath = SVGPathService.diagonalBoardPath('C', 'F', this.variant, this.bombe.menuLetters)
            addPathNode (svg, cfDBPath, `test`, "wire")

            let efDBPath = SVGPathService.diagonalBoardPath('E', 'F', this.variant, this.bombe.menuLetters)
            addPathNode (svg, efDBPath, `test`, "wire")
        }

        // scramblers/engimas
        let noOfScramblers = numberOfScramblersToDisplay(this.variant, this.bombe.menuLetters)
        for (let i=0; i<noOfScramblers; i++) {
            let first = i==0
            let last = i==noOfScramblers-1
            let x = scramblerAbsoluteXOffset(this.variant, i, this.bombe.menuLetters)
            this.lwEnigmaRenderers[i].drawWiring(svg, i, this.variant, first, last, x, TOP_MARGIN)
        }

    }

    drawForeground(svg) {
        let ys = yValues(this.variant)
        // indicator drums
        // let xIndicator = LEFT_MARGIN + this.lwEnigmaRenderers.length * enigmaWidth(this.variant, false, false) + (this.lwEnigmaRenderers.length-1)*VERTICAL_CONNECTOR_GAP + 0.5*VERTICAL_CONNECTOR_GAP + 6*COMPONENT_DISTANCE
        let xIndicator = 20
        let group = addGroupNode (svg, "indicatordrums", xIndicator, TOP_MARGIN)
        this.indicatorDrum1Renderer.draw(group, `${group.id}_drum1`, this.variant, 0, ys.drum1Y, true)
        this.indicatorDrum2Renderer.draw(group, `${group.id}_drum2`, this.variant, 0, ys.drum2Y, true)
        this.indicatorDrum3Renderer.draw(group, `${group.id}_drum3`, this.variant, 0, ys.drum3Y, true)

        // buttons next to key indicators to advance or step back all drums on the same row
        let xDrumButtons = xIndicator + DRUM_WIDTH + COMPONENT_DISTANCE
        let drumButtonsGroup = addGroupNode (svg, "drumButtons", xDrumButtons, TOP_MARGIN)
        let drumNumbers = [1, 2, 3]
        for (let i=0; i < drumNumbers.length; i++) {
            let drumNo = drumNumbers[i]
            this.drawDrumButtons(drumNo, drumButtonsGroup, this.variant)
        }

        // scramblers/engimas
        let noOfScramblers = numberOfScramblersToDisplay(this.variant, this.bombe.menuLetters)
        for (let i=0; i<noOfScramblers; i++) {
            let first = i==0
            let last = i==noOfScramblers-1
            let x = scramblerAbsoluteXOffset(this.variant, i, this.bombe.menuLetters)
            this.lwEnigmaRenderers[i].drawForeground(svg, i, this.variant, first, last, x, TOP_MARGIN)
        }
    }

    drawDrumButtons(drumNo, group, variant) {
        let ys = yValues(variant)
        let proceedButton = addGroupNode (group, `advance_${drumNo}`)
        proceedButton.setAttribute("class", "drumButtonGroup")
        proceedButton.addEventListener('click', this.handleAdvanceDrums.bind(this), false)
        let y = 2 * COMPONENT_DISTANCE
        if (drumNo == 1) y += ys.drum1Y 
        if (drumNo == 2) y += ys.drum2Y 
        if (drumNo == 3) y += ys.drum3Y 
        addRectangleNode (proceedButton, `${proceedButton.id}_rect`, "pathButton", 0, y, 10, 10) 
        addTextNode(proceedButton, "+", `${proceedButton.id}_text`, "pathButton", 5, y + 6)
        
        let stepBackButton = addGroupNode (group, `stepback_${drumNo}`)
        stepBackButton.setAttribute("class", "drumButtonGroup")
        stepBackButton.addEventListener('click', this.handleStepBackDrums.bind(this), false)
        addRectangleNode (stepBackButton, `${stepBackButton.id}_rect`, "pathButton", 0, y + 15, 10, 10) 
        addTextNode(stepBackButton, "-", `${stepBackButton.id}_text`, "pathButton", 5, y + 5 + 15)
    }

    handleKeyClick (event) {
        // handle key mouse click
        // (a keyGroup is a group of SVG elements which together constitute a single keyboard key)
        let clickedKeyGroup = event.target.closest('.keyGroup')
        if (!clickedKeyGroup) return;

        this.pressedKeyId = Number(clickedKeyGroup.getAttribute("keyId"))
        let pressedLetter = clickedKeyGroup.getAttribute("keyLetter")
        let enigmaIndex = clickedKeyGroup.getAttribute("enigmaIndex")

        if (enigmaIndex == 0) {
            this.reset()
        }
        this.bombe.pathFinder.processKeyboardInput(enigmaIndex, this.pressedKeyId, this.variant)
        this.redrawBombe("key press " + pressedLetter + " on enigma " + enigmaIndex)
    }

    handleInputControlClick(event) {
        let clickedInputControl = event.target.closest('.activate')
        if (!clickedInputControl) return;

        this.reset()
        let clickedInputWire = clickedInputControl.id.slice(-1)
        let scramblerInputId = Number(clickedInputWire)
        this.bombe.inputControlIds.push(scramblerInputId)

        //this.calculateScramblerInputsAndOutputs(scramblerInputId)
        this.bombe.pathFinder.processEnigmaInput(bombe.scramblersByIndexMap[0], scramblerInputId)
        if (this.autoCompletePathOnInputActivation) {
            this.bombe.pathFinder.completePath(this.variant)
        }
        this.redrawBombe("input activation " + clickedInputWire)
    }

    handleAdvanceDrums(event) {
        let drumButtonGroup = event.target.closest('.drumButtonGroup')
        if (!drumButtonGroup) return;

        let drumNo = Number(drumButtonGroup.id.slice(-1))
        this.bombe.advanceIndicatorDrums(drumNo-1)

        this.reset()
        if (this.autoActivateInputOnRotorPositionChange) {
            let wireId = 2 // c-wire
            this.bombe.inputControlIds.push(wireId)
            this.bombe.pathFinder.processEnigmaInput(bombe.scramblersByIndexMap[0], wireId)
            if (this.autoCompletePathOnInputActivation) {
                this.bombe.pathFinder.completePath(this.variant)
            }
        }
        this.redrawBombe("advance drums " + drumNo)
    }

    handleStepBackDrums(event) {
        let drumButtonGroup = event.target.closest('.drumButtonGroup')
        if (!drumButtonGroup) return;

        let drumNo = Number(drumButtonGroup.id.slice(-1))
        this.bombe.turnBackIndicatorDrums(drumNo-1)

        this.reset()
        if (this.autoActivateInputOnRotorPositionChange) {
            let wireId = 2 // c-wire
            this.bombe.inputControlIds.push(wireId)
            this.bombe.pathFinder.processEnigmaInput(bombe.scramblersByIndexMap[0], wireId)
            if (this.autoCompletePathOnInputActivation) {
                this.bombe.pathFinder.completePath(this.variant)
            }
        }
        this.redrawBombe("step back drums " + drumNo)
    }

    reset(event) {
        this.bombe.inputControlIds = []
        this.bombe.pathFinder.reset()
    }

}
