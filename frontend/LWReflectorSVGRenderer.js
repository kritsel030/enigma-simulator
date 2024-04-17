class LWReflectorSVGRenderer {

    constructor(reflector) {
        this.reflector = reflector
        this.internalWireLengths = {}
    }

    draw(parent, variant, x=0, y=0) {
        let group = addGroupNode(parent, `${parent.id}_reflector`, x, y)
        this.drawBackground(group, variant)
        this.drawForeground(group, variant)
    }

    // background = everything behind the encipher path
    drawBackground(parent, variant, x=0, y=0) {
        let startX = reflectorXOffset(variant)
        let width = renderDrumsSeparate(variant) ? REFLECTOR_BROAD_WIDTH : REFLECTOR_WIDTH
        let path = `M ${startX} ${0}`
        path += `h ${width} `
        path += `v ${REFLECTOR_HEIGHT } `
        path += `h -${width} Z`

        addPathNode (parent, path, "reflector_bg", "reflectorBG")
    }

    // foreground = everything in front of the encipher path
//    drawForeground(parent, variant, x, y) {
//        let group = addGroupNode(parent, "reflector", x, y)
//        this.drawBorder(group, variant)
//    }

    drawForeground(group, variant) {
        let startX = reflectorXOffset(variant)
        let width = renderDrumsSeparate(variant) ? REFLECTOR_BROAD_WIDTH : REFLECTOR_WIDTH
        let path = `M ${startX} ${0}`
        path += `h ${width} `
        path += `v ${REFLECTOR_HEIGHT } `
        path += `h -${width} Z`
        addPathNode (group, path, `${group.id}_border`, "border")
    }

}
