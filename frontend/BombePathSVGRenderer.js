class BombePathSVGRenderer {

    constructor(enigmaList) {
        this.enigmaList = enigmaList
    }

    draw(parent, variant, drawContext, x=0, y=0) {
        // determine y values of the various component types
        // copied from LWEnigmaSVGRenderer, needs a DRY approach
        let drum1Y = renderReflector(variant) ? REFLECTOR_HEIGHT + COMPONENT_DISTANCE : 0
        let drum2Y = drum1Y + DRUM_HEIGHT + DRUM_DISTANCE
        let drum3Y = drum2Y + DRUM_HEIGHT + DRUM_DISTANCE
        let connectorY = drum3Y + DRUM_HEIGHT + COMPONENT_DISTANCE
        let plugboardY = connectorY + CONNECTOR_HEIGHT + CONN_TO_PB_DISTANCE
        let keyboardY = plugboardY + PLUGBOARD_HEIGHT + PB_TO_KB_DISTANCE
        
        let group = addGroupNode(parent, `${parent.id}_path`, x, y)
            
        for (let i=0; i<this.enigmaList.length; i++) {
            let xOffset = i*(ENIGMA_WIDTH_BROAD + ENIGMA_DISTANCE)
            let enigma = this.enigmaList[i]
            let first = i == 0
            let last = i == this.enigmaList.length-1
            if (enigma.plugboardInputId != null || enigma.scramblerInputId != null) {
                // do not step the rotors!
                let encipherResult = enigma.encipherWireId(enigma.plugboardInputId, false)    
                let wireMap = encipherResult[1]
                let prePath = ""

                // inbound: key to top of plugboard
                if (["variantA", "variantB", "variantC", "variantD"].includes(variant) || (["variantE", "variantF"].includes(variant) && first)) {
                    this.drawPrePath(group, variant, enigma, i, first, last, wireMap, keyboardY, plugboardY, xOffset)
                }

                if (["variantE", "variantF", "variantG"].includes(variant)) {
                    this.drawToConnectorPath(group, variant, i, first, wireMap, plugboardY, xOffset)
                }

                // inbound: (from plugboard) into, through and out of connector (into the drum)
                if (["variantA", "variantB", "variantC", "variantD", "variantE"].includes(variant) || first) {
                    this.drawHorizontalConnectorInboundPath(group, variant, i, wireMap, drum3Y, connectorY, plugboardY, xOffset)
                }

                // inbound: left-hand side path through the drums
                this.drawDrumsPath(group, variant, i, true, drum1Y, drum3Y, xOffset) 

                // from top drum, through reflector back to the top drum
                if (renderReflector(variant)) {
                    this.drawReflectorPath(group, variant, i, drum1Y, xOffset) 
                }
            
                // outbound: right-hand side path through the drums
                this.drawDrumsPath(group, variant, i, false, drum1Y, drum3Y, xOffset) 

                // outbound: (from drum) into, through and out of connector (up to the plugboard)
                if (["variantA", "variantB", "variantC", "variantD", "variantE"].includes(variant) || last) {
                    this.drawHorizontalConnectorOutboundPath(group, variant, i, wireMap, drum3Y, connectorY, plugboardY, xOffset)
                }

                // outbound: top of plugboard to key/light
                if (["variantA", "variantB", "variantC", "variantD"].includes(variant) || (["variantE", "variantF"].includes(variant) && last)) {
                    this.drawPostPath(group, variant, enigma, i, first, last, wireMap, keyboardY, plugboardY, xOffset) 
                }

                if (["variantE", "variantF", "variantG"].includes(variant)) {
                    this.drawFromConnectorPath(group, variant, i, last, wireMap, plugboardY, xOffset)
                }
            }
        }
    }

    // draw the first (black) section of the inbound path: from keyboard to exitpoint of the plugboard
    drawPrePath(parent, variant, enigma, enigmaIndex, first, last, wireMap, keyboardY, plugboardY, xOffset) {
        let prePath = ""
        if (["variantA", "variantB", "variantC"].includes(variant) || first) {
            // inbound: start at the key
            let keyX = 0.5*WIRE_DISTANCE + wireMap.inbound_KBtoPBWireId*WIRE_DISTANCE
            if (variant=="variantC" && !first) {
                keyX -= 0.5*(PLUGBOARD_WIDTH + ENIGMA_DISTANCE)
            } 
            let keyY = ["variantA", "variantB"].includes(variant) ? keyboardY + (wireMap.inbound_KBtoPBWireId%2) * KEY_SHIFT : keyboardY
            prePath += `M ${xOffset + keyX} ${keyY} `
        } else {
            let i = wireMap.inbound_KBtoPBWireId
            let toX = xOffset + 0.5*WIRE_DISTANCE + i*WIRE_DISTANCE
            let startX = toX - 0.5 * (PLUGBOARD_WIDTH + ENIGMA_DISTANCE)
            let startY = plugboardY + PLUGBOARD_HEIGHT + WIRE_DISTANCE + 0.5*i*WIRE_DISTANCE
            let toY = plugboardY + PLUGBOARD_HEIGHT
            prePath += `M ${startX} ${startY} H ${toX} v -${0.5*i*WIRE_DISTANCE + WIRE_DISTANCE} `
        }
        // inbound: keyboard to plugboard in
        prePath += `L ${xOffset + 0.5*WIRE_DISTANCE + wireMap.inbound_KBtoPBWireId*WIRE_DISTANCE} ${plugboardY + PLUGBOARD_HEIGHT} `
        // inbound: plugboard in to plugboard out
        prePath += `L ${xOffset + 0.5*WIRE_DISTANCE + wireMap.inbound_PBtoR3WireId*WIRE_DISTANCE} ${plugboardY} `
        addPathNode (parent, prePath, `${parent.id}_enigma${enigmaIndex}_path_pre`, "electricalPath_pre_post") 
    }

    drawToConnectorPath(parent, variant, enigmaIndex, first, wireMap, plugboardY, xOffset) {
        let i = wireMap.inbound_PBtoR3WireId
        if (variant=="variantE" && !first) {
            let toX = xOffset + 0.5*WIRE_DISTANCE + i*WIRE_DISTANCE 
            let startX = toX - 0.5 * (PLUGBOARD_WIDTH + ENIGMA_DISTANCE)
            let startY = plugboardY + WIRE_DISTANCE + 0.5*i*WIRE_DISTANCE 
            let path = `M ${startX} ${startY} H ${toX} v -${0.5*i*WIRE_DISTANCE + WIRE_DISTANCE} `
            addPathNode (parent, path, `${parent.id}_enigma${enigmaIndex}_path_fromprevious`, "electricalPath") 
        }
    }

    // inbound path into, through and out of a horizontal connector (ends up in the middle of rotor3)
    drawHorizontalConnectorInboundPath(parent, variant, enigmaIndex, wireMap, drum3Y, connectorY, plugboardY, xOffset) {
        // start at plugbord out (top of plugboard)
        let path = `M ${xOffset + 0.5*WIRE_DISTANCE + wireMap.inbound_PBtoR3WireId*WIRE_DISTANCE} ${plugboardY} `
        // plugboard out to connector in
        path += `v -${CONN_TO_PB_DISTANCE} `
        // halfway up the connector
        path += `v -${0.5*CONNECTOR_HEIGHT} `
        // horizontally to the middle of the connector
        path += `H ${xOffset + 0.5*CONNECTOR_WIDTH}`
        // halfway up the connector
        path += `v -${0.5*CONNECTOR_HEIGHT} `
        // up to the center of the bottom drum (drum3)
        let drumX = variant=="variantA" ? xOffset + DRUM_RADIUS : xOffset + DRUM_WIDTH + 0.5*COMPONENT_DISTANCE
        path += `L ${drumX} ${drum3Y + DRUM_RADIUS} `
        addPathNode (parent, path, `${parent.id}_enigma${enigmaIndex}_path_connector_in`, "electricalPath") 
    }

    // draw the path connecting the three drums (starts in the middle of the top drum, ends in the middle of the lowest drum)
    drawDrumsPath(parent, variant, enigmaIndex, left, drum1Y, drum3Y, xOffset) {
        let x = xOffset
        if (variant=="variantA") {
            // drums in two separate columns
            if (left) x += DRUM_RADIUS
            else x += DRUM_WIDTH + COMPONENT_DISTANCE + DRUM_RADIUS
        } else {
            // one column of integrated drums, with two parallel cables running through them
            if (left) x += DRUM_WIDTH + 0.5*COMPONENT_DISTANCE - 2*UNIT
            else x += DRUM_WIDTH + 0.5*COMPONENT_DISTANCE + 2*UNIT
        }
        let path = `M ${x} ${drum1Y + DRUM_RADIUS} V ${drum3Y + DRUM_RADIUS}`
        let side = left ? "left" : "right"
        addPathNode (parent, path, `${parent.id}_enigma${enigmaIndex}_path_drums_${side}`, "electricalPath") 
    }

    // draw the path connecting the top drum and reflector (starts and ends in drum)
    drawReflectorPath(parent, variant, enigmaIndex, drum1Y, xOffset) {
        let startX = xOffset
        if (variant=="variantA") {
            // drums in two separate columns
            startX += DRUM_RADIUS
        } else {
            // one column of integrated drums, with two parallel cables running through them
            startX += DRUM_WIDTH + 0.5*COMPONENT_DISTANCE - 2*UNIT
        }
        // start halfway the drum
        let path = `M ${startX} ${drum1Y + DRUM_RADIUS} `
        // up halfway into the reflector
        path += `v -${DRUM_RADIUS + COMPONENT_DISTANCE + 0.5*REFLECTOR_HEIGHT} `
        let h = variant=="variantA" ? DRUM_RADIUS + COMPONENT_DISTANCE + DRUM_RADIUS : 4*UNIT
        // horizontally to the right
        path += `h ${h} ` 
        // down into the rotor
        path += `v ${DRUM_RADIUS + COMPONENT_DISTANCE + 0.5*REFLECTOR_HEIGHT} `
        addPathNode (parent, path, `${parent.id}_enigma${enigmaIndex}_path_reflector`, "electricalPath") 
    }


    // outbound path into, through and out of a horizontal connector (ends up in the middle of rotor3)
    drawHorizontalConnectorOutboundPath(parent, variant, enigmaIndex, wireMap, drum3Y, connectorY, plugboardY, xOffset) {
        xOffset += COMPONENT_SIZE + COMPONENT_DISTANCE
        // start at plugbord out (top of plugboard)
        let path = `M ${xOffset + 0.5*WIRE_DISTANCE + wireMap.outbound_R3toPBWireId*WIRE_DISTANCE} ${plugboardY} `
        // plugboard out to connector in
        path += `v -${CONN_TO_PB_DISTANCE} `
        // halfway up the connector
        path += `v -${0.5*CONNECTOR_HEIGHT} `
        // horizontally to the middle of the connector
        path += `H ${xOffset + 0.5*CONNECTOR_WIDTH}`
        // halfway up the connector
        path += `v -${0.5*CONNECTOR_HEIGHT} `
        // up to the center of the bottom drum (drum3)
        let drumX = variant=="variantA" ? xOffset + DRUM_RADIUS : xOffset - 0.5*COMPONENT_DISTANCE
        path += `L ${drumX} ${drum3Y + DRUM_RADIUS} `
        addPathNode (parent, path, `${parent.id}_enigma${enigmaIndex}_path_connector_out`, "electricalPath") 
    }

    // draw the last (black) section of the outbound path: from inputpoint of the plugboard to keyboard
    drawPostPath(parent, variant, enigma, enigmaIndex, first, last, wireMap, keyboardY, plugboardY, xOffset) {

        xOffset += COMPONENT_SIZE + COMPONENT_DISTANCE
        // outbound: start at the top of the plugboard
        let path= `M ${xOffset + 0.5*WIRE_DISTANCE + wireMap.outbound_R3toPBWireId*WIRE_DISTANCE} ${plugboardY} `
        // outbound: plugboard in (at the top) to plugboard out (at the bottom)
        path += `L ${xOffset + 0.5*WIRE_DISTANCE + wireMap.outbound_PBtoKBWireId*WIRE_DISTANCE} ${plugboardY + PLUGBOARD_HEIGHT}`

        if (["variantA", "variantB", "variantC"].includes(variant) || last) {
            let lightX = 0.5*WIRE_DISTANCE + wireMap.outbound_PBtoKBWireId*WIRE_DISTANCE
            if (variant=="variantC" && !last) {
                lightX += 0.5*(PLUGBOARD_WIDTH + ENIGMA_DISTANCE)
            }
            // outbound: plugboard out (at the bottom) to the key/light
            let lightY = ["variantA", "variantB"].includes(variant) ? keyboardY + (wireMap.outbound_PBtoKBWireId%2) * KEY_SHIFT : keyboardY
            path += `L ${xOffset + lightX} ${lightY} `
        } else  {
            let i = wireMap.outbound_PBtoKBWireId
            let startX = xOffset + 0.5*WIRE_DISTANCE + i*WIRE_DISTANCE
            let startY = plugboardY + PLUGBOARD_HEIGHT
            path += `M ${startX} ${startY} v ${0.5*i*WIRE_DISTANCE + WIRE_DISTANCE} h ${0.5 * (PLUGBOARD_WIDTH + ENIGMA_DISTANCE)} `
        }
        
        addPathNode (parent, path, `${parent.id}_enigma${enigmaIndex}_path_post`, "electricalPath_pre_post") 

    }

    drawFromConnectorPath(parent, variant, enigmaIndex, last, wireMap, plugboardY, xOffset) {
        xOffset += COMPONENT_SIZE + COMPONENT_DISTANCE
        let i = wireMap.outbound_R3toPBWireId
        if (variant=="variantE" && !last) {
            let startX = xOffset + 0.5*WIRE_DISTANCE + i*WIRE_DISTANCE
            let startY = plugboardY
            let path = `M ${startX} ${startY} v ${0.5*i*WIRE_DISTANCE + WIRE_DISTANCE} h ${0.5 * (PLUGBOARD_WIDTH + ENIGMA_DISTANCE)} `
            addPathNode (parent, path, `${parent.id}_enigma${enigmaIndex}_path_tonext`, "electricalPath") 
        }
    }
    

}
