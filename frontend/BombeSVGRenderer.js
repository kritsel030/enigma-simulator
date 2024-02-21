// Renders a bombe consisting of several enigmas
class BombeSVGRenderer {

    constructor(enigmaList){
        this.variant = "variantA"
        this.lwEnigmaRenderers = []
        for (let i = 0; i < enigmaList.length; i++)  {
            this.lwEnigmaRenderers.push(new LWEnigmaSVGRenderer(enigmaList[i]))
        }
        let drum1 = new Rotor('SCII', 'E', 1, alphabetSize)
        let drum2 = new Rotor('SCIII', 'B', 1, alphabetSize)
        let drum3 = new Rotor('SCI', 'B', 1, alphabetSize)
        this.indicatorDrum1Renderer = new LWDrumSVGRenderer(null, null, drum1)
        this.indicatorDrum2Renderer = new LWDrumSVGRenderer(null, null, drum2)
        this.indicatorDrum3Renderer = new LWDrumSVGRenderer(null, null, drum3)
        this.pathRenderer = new BombePathSVGRenderer(enigmaList)
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
            this.lwEnigmaRenderers[i].draw(svg, `enigma${i}`, this.variant,  first, last, x, TOP_MARGIN)
            previousFirst = first
            previousLast = last
        }

        if (this.variant=="scrambler_multi_line_scanning") {
            // wires from bombe output to bombe input
            let yOffset = TOP_MARGIN + ys.vertConnectorY + 0.5 * WIRE_DISTANCE
            let xOffset = LEFT_MARGIN + this.lwEnigmaRenderers.length * enigmaWidth(this.variant, false, false) + (this.lwEnigmaRenderers.length-1)*VERTICAL_CONNECTOR_GAP + 0.5*VERTICAL_CONNECTOR_GAP
            for (let i=0; i<alphabetSize; i++) {
                let fromY = yOffset + i * WIRE_DISTANCE
                let h1 = i*WIRE_DISTANCE
                let v = 2*i* WIRE_DISTANCE + 3*WIRE_DISTANCE
                let h2 = this.lwEnigmaRenderers.length * enigmaWidth(this.variant, false, false) + (this.lwEnigmaRenderers.length-1)*VERTICAL_CONNECTOR_GAP + 2*0.5*VERTICAL_CONNECTOR_GAP + 2*i* WIRE_DISTANCE 
                let path = `M ${xOffset} ${fromY} h ${h1} v -${v} h -${h2} v ${v} h ${h1}`
                addPathNode (svg, path, `feedback_${i}`, "wire-dashed")
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
    }

    handleKeyClick (event) {
        // handle key mouse click
        // (a keyGroup is a group of SVG elements which together constitute a single keyboard key)
        let clickedKeyGroup = event.target.closest('.keyGroup')
        if (!clickedKeyGroup) return;

        // remove any path if it exists
//        this.encipherPathRenderer.removePath()

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
            enigmaRenderer.pressedKeyId = inputId
            enigmaRenderer.lightedKeyId = outputId
            enigma.plugboardInputId = inputId
            enigma.plugboardOutputId = outputId
            enigma.scramblerInputId = null
            enigma.scramblerOutputId = null
            // prepare for next enigma in the list
            inputId = outputId
        }
        this.redrawBombe("key press " + pressedLetter)
    }

    reset(event) {
        for (let i=0; i<this.lwEnigmaRenderers.length; i++) {
            let enigmaRenderer = this.lwEnigmaRenderers[i]
            enigmaRenderer.pressedKeyId = null
            enigmaRenderer.lightedKeyId = null
            enigmaRenderer.enigma.reset()
        }
    }

}
