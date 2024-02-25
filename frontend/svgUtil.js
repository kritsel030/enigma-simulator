const SVG_NS = "http://www.w3.org/2000/svg";

function addGroupNode (parent, id, translateX=0, translateY=0) {
    let groupNode = document.createElementNS(SVG_NS, "g");
    groupNode.id = id
    groupNode.setAttribute("transform", `translate(${translateX}, ${translateY})`)
    parent.appendChild(groupNode)
    return groupNode
}

function addTextNode (parent, text, id, clazz, x=0, y=0, transform=null) {
    let textNode = document.createElementNS(SVG_NS, "text");
    textNode.id = id
    textNode.setAttribute("x", x)
    textNode.setAttribute("y", y)
    textNode.setAttribute("class", clazz)
    if (transform) {
        textNode.setAttribute("transform", transform)
    }
    textNode.innerHTML = text
    parent.appendChild(textNode)
    return textNode
}

function addPathNode (parent, path, id, clazz) {
    let pathNode = document.createElementNS(SVG_NS, "path");
    pathNode.id = id
    pathNode.setAttribute("d", path)
    pathNode.setAttribute("class", clazz)
    parent.appendChild(pathNode)
    return pathNode
}

function addCircleNode (parent, id, clazz, radius, x, y) {
    let circleNode = document.createElementNS(SVG_NS, "circle");
    circleNode.id = id
    circleNode.setAttribute("class", clazz)
    circleNode.setAttribute("r", radius)
    circleNode.setAttribute("cx", x)
    circleNode.setAttribute("cy", y)
    parent.appendChild(circleNode)
    return circleNode
}

function addRectangleNode (parent, id, clazz, x, y, width, height, rx=0, ry=0) {
    let rectNode = document.createElementNS(SVG_NS, "rect");
    rectNode.id = id
    rectNode.setAttribute("class", clazz)
    rectNode.setAttribute("x", x)
    rectNode.setAttribute("y", y)
    rectNode.setAttribute("width", width)
    rectNode.setAttribute("height", height)
    rectNode.setAttribute("rx", rx)
    rectNode.setAttribute("ry", ry)
    
    parent.appendChild(rectNode)
    return rectNode
}