class LWReflectorSVGRenderer {

    constructor(reflector) {
        this.reflector = reflector
        this.internalWireLengths = {}
    }

    draw(parent, variant, x=0, y=0) {
        // the broad reflector needs to start at another x value than a normal reflector
        //x = variant=="variantA" ? x : x + 0.5*(REFLECTOR_BROAD_WIDTH-REFLECTOR_WIDTH)
        //x = renderReflectorNarrow(variant) ? x + 0.5*(REFLECTOR_BROAD_WIDTH-REFLECTOR_WIDTH) : x
        let group = addGroupNode(parent, `${parent.id}_reflector`, x, y)
        this.drawBackground(group, variant)
        this.drawBorder(group, variant)
    }

    // background = everything behind the encipher path
    drawBackground(parent, variant, x=0, y=0) {
        let width = variant=="variantA" ? REFLECTOR_BROAD_WIDTH : REFLECTOR_WIDTH
        let path = `M ${0} ${0}`
        path += `h ${width} `
        path += `v ${REFLECTOR_HEIGHT } `
        path += `h -${width} Z`

        addPathNode (parent, path, "reflector_bg", "reflectorBG")
    }

    // foreground = everything in front of the encipher path
    drawForeground(parent, variant, x, y) {
        let group = addGroupNode(parent, "reflector", x, y)
        this.drawBorder(group, variant)
    }

    drawBorder(group, variant) {
        let width = variant=="variantA" ? REFLECTOR_BROAD_WIDTH : REFLECTOR_WIDTH
        let path = `M ${0} ${0}`
        path += `h ${width} `
        path += `v ${REFLECTOR_HEIGHT } `
        path += `h -${width} Z`
        addPathNode (group, path, `${group.id}_border`, "border")
    }

}
