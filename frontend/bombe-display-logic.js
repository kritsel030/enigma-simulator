//////////////////////////////////////////////////////////////////////////
// constants

// generic stuff
let LEFT_MARGIN = 150
let TOP_MARGIN = 20
let ALPHABET_SIZE = 6
const UNIT = 4
const WIRE_DISTANCE = 3*UNIT
const COMPONENT_SIZE = ALPHABET_SIZE * WIRE_DISTANCE
const COMPONENT_DISTANCE = 3*UNIT
// connector to plugboard
const CONN_TO_PB_DISTANCE = 6*UNIT
// plugboard to keyboard
const PB_TO_KB_DISTANCE = 6*UNIT

// drum stuff
const DRUM_RADIUS = 0.5*COMPONENT_SIZE
const DRUM_HEIGHT = COMPONENT_SIZE
const DRUM_WIDTH = DRUM_HEIGHT
const DRUM_DISTANCE = 2*UNIT

// reflector stuff
const REFLECTOR_WIDTH = COMPONENT_SIZE
const REFLECTOR_BROAD_WIDTH = 2*COMPONENT_SIZE + COMPONENT_DISTANCE
const REFLECTOR_HEIGHT = 5*UNIT

// connector stuff
const CONNECTOR_WIDTH = COMPONENT_SIZE
const CONNECTOR_HEIGHT = 2*UNIT

// plugboard stuff
const PLUGBOARD_WIDTH = COMPONENT_SIZE
const PLUGBOARD_HEIGHT = 5*UNIT

// keyboard stuff
let KEY_RADIUS = 2 * UNIT
let KEY_SHIFT = 5 * UNIT
let KEY_DISTANCE = 0.5*WIRE_DISTANCE

// enigma stuff
const ENIGMA_WIDTH = 2*COMPONENT_SIZE + COMPONENT_DISTANCE
// variantA: two drums/connectors/plugboards/keyboards next to each other, with some distance between them
const ENIGMA_WIDTH_BROAD = 2*COMPONENT_SIZE + COMPONENT_DISTANCE
// a drum, flanked on both sides by a vertical connector
const ENIGMA_WIDTH_NARROW = DRUM_WIDTH + 2*CONNECTOR_HEIGHT + 2*COMPONENT_DISTANCE
// first enigma in variants B to ...: take into account the horizontal connector (with similar dimensions as a single rotor) on left-hand side
// the left hand side is as broad as the left-hand side of a broad enigma, the right hand side is as broad as the right-hand side of a narrow enigma
const ENIGMA_WIDTH_MEDIUM = 0.5 * (ENIGMA_WIDTH_BROAD + ENIGMA_WIDTH_NARROW)
const ENIGMA_DISTANCE = 3 * COMPONENT_DISTANCE

const ENIGMA_HALF_WIDTH_WITH_HORIZONTAL_CONNECTOR = COMPONENT_SIZE + 0.5*COMPONENT_DISTANCE
const ENIGMA_HALF_WIDTH_WITH_VERTICAL_CONNECTOR = CONNECTOR_HEIGHT + COMPONENT_DISTANCE + DRUM_RADIUS 

// gap between two enigma's which have a horizontal connector on their joining sides
const HORIZONTAL_CONNECTOR_GAP = 3 * COMPONENT_DISTANCE
// gap between two enigma's which have a vertical connector on their joining sides
const VERTICAL_CONNECTOR_GAP = 6 * COMPONENT_DISTANCE

// scrambler stuff
const SCRAMBLER_WIDTH = 4 * COMPONENT_DISTANCE

const A_POSITION = 0

//////////////////////////////////////////////////////////////////////////
// helper methods to indicate which components should be rendered (and how) in which scenario

function renderReflector(variant) {
    return !variant.startsWith("scrambler")
}

function renderReflectorNarrow(variant) {
    return variant!="variantA"
}


function renderDrums(variant) {
    return !variant.startsWith("scrambler")
}

function renderDrumsIntegrated(variant) {
    return renderDrums(variant) && !renderDrumsSeparate(variant)
}

function renderDrumsSeparate(variant) {
    return renderDrums(variant) && variant=="variantA"
}

function renderHorizontalConnector(variant, first, last, inbound) {
    return ["variantA", "variantB", "variantC", "variantD", "variantE"].includes(variant) || 
        (["variantF", "variantG"].includes(variant) && bombeEntryOrExit(first, last, inbound) )
}

function renderVerticalConnector(variant, first, last, inbound) {
    return ! renderHorizontalConnector(variant, first, last, inbound)
}

function renderPlugboard(variant, first, last, inbound) {
    return ["variantA", "variantB", "variantC", "variantD"].includes(variant)  || 
        (["variantE", "variantF"].includes(variant) && bombeEntryOrExit(first, last, inbound)) 
}

function renderKeyOrLightboard(variant, first, last, inbound) {
    return ["variantA", "variantB", "variantC", "variantD"].includes(variant)  || 
        (["variantE", "variantF"].includes(variant) && bombeEntryOrExit(first, last, inbound)) 
}

function keyOrLightboardProperties(variant, first, last, inbound) {
    let props = {}
    props.virtual=false
    props.hidden = false
    props.positionShifted = false
    props.variant = "<undefined>"
    
    if (inbound) {
        if (first) props.variant="clickableKeyboard"
        else if ( ["variantA", "variantB"].includes(variant)) props.variant="keyboard"
        else if (["variantC"].includes(variant)) {
            props.hidden=true
            props.positionShifted = true
        }
        else if (["variantD"].includes(variant)) props.virtual = true
    } else {
        if (last || ["variantA", "variantB"].includes(variant)) props.variant="lightboard"
        else if (["variantC"].includes(variant)) {
            props.variant="integrated"
            props.positionShifted = true
        }
        else if (["variantD"].includes(variant)) props.virtual = true
    }         
    return props
}

function renderScramblerWires(variant) {
    return variant.startsWith("scrambler")
}

function renderInputControlWires(variant, first) {
    return first && (["variantG", "variantH"].includes(variant) || renderScramblerWires(variant))
}


function renderOutputToInputWires(variant) {
    return ["scrambler_multi_line_scanning"].includes(variant)
}

function renderProceedWithPathButtons(variant) {
    return ["scrambler_multi_line_scanning"].includes(variant)
}


function bombeEntryOrExit(first, last, inbound) {
    return (first && inbound) || (last && !inbound)
}

//////////////////////////////////////////////////////////////////////////
// helper methods to indicate where components should be rendered in which scenario

function reflectorXOffset(variant, first, last) {
    return drumXOffset(variant, first, last, true)
}

function drumXOffset(variant, first, last, inbound, enigmaId) {
    let result = -1
    if (renderDrumsSeparate(variant)) {
        result = inbound ? 0 : DRUM_WIDTH + DRUM_DISTANCE
    } else if (inbound) {
        if (renderHorizontalConnector(variant, first, last, true) )
            result = DRUM_RADIUS + 0.5*COMPONENT_DISTANCE
        else
            result = CONNECTOR_HEIGHT + COMPONENT_DISTANCE
    }
    //console.log(`drumXOffset(variant=${variant}, first=${first}, last=${last}, inbound=${inbound}, enigmaId=${enigmaId}) : ${result}`)
    return result
}

function horConnectorXOffset(variant, first, last, inbound) {
    if (inbound) 
        return 0
    else 
        return enigmaCenterXOffset(variant, first, last) + 0.5*COMPONENT_DISTANCE
}

function vertConnectorXOffset(variant, first, last, inbound, enigmaId) {
    let result = -1
    if (inbound) 
        result = 0
    else if ( !variant.startsWith("scrambler"))
        result = enigmaCenterXOffset(variant, first, last) + DRUM_RADIUS + COMPONENT_DISTANCE
    else 
        result = CONNECTOR_HEIGHT + SCRAMBLER_WIDTH
    //console.log(`vertConnectorXOffset(variant=${variant}, first=${first}, last=${last}, inbound=${inbound}, enigmaId=${enigmaId}) : ${result}`)
    return result
}

function plugboardXOffset(variant, first, last, inbound) {
    if (inbound)
        return 0
    else 
        return enigmaCenterXOffset(variant, first, last) + 0.5*COMPONENT_DISTANCE

}

function keyboardXOffset(variant, first, last, inbound) {
    let keyboardProps = keyOrLightboardProperties(variant, first, last, inbound)
    if (inbound) 
        return 0
    else if (!keyboardProps.positionShifted) 
        return enigmaCenterXOffset(variant, first, last) + 0.5*COMPONENT_DISTANCE
    else 
        return 2*COMPONENT_SIZE + COMPONENT_DISTANCE + 0.5*ENIGMA_DISTANCE - 0.5*COMPONENT_SIZE
}

function verticalCableXOffset(variant, first, last, inbound) {
    let result = -1
    if (renderDrumsSeparate(variant)) {
        result = drumXOffset(variant, first, last, inbound) + DRUM_RADIUS
    } else {
        if (inbound) 
            result = enigmaCenterXOffset(variant, first, last) - 2*UNIT
        else 
            result = enigmaCenterXOffset(variant, first, last) + 2*UNIT
    }
    //console.log(`verticalCableXOffset(variant=${variant}, first=${first}, last=${last}, inbound=${inbound}, enigmaId=${enigmaId}) : ${result}`)
    return result
}

function enigmaCenterXOffset(variant, first, last) {
    if (renderHorizontalConnector(variant, first, last, true) ){
        return ENIGMA_HALF_WIDTH_WITH_HORIZONTAL_CONNECTOR
    } else if (renderDrums(variant)){
        // vertical connector + space + 1/2 DRUM + 1/2 distance between components
        return ENIGMA_HALF_WIDTH_WITH_VERTICAL_CONNECTOR   
    } else {
        // schematic scramblers instead of drums (2 vertical connectors + 1 scrambler make a full scrambler)
        return 0.5 * (SCRAMBLER_WIDTH + 2*CONNECTOR_HEIGHT)
    }
}

function yValues(variant) {
    let yValues = {}
    yValues.reflectorY = 0
    yValues.drum1Y = (renderReflector(variant) ? yValues.reflectorY + REFLECTOR_HEIGHT + COMPONENT_DISTANCE : 0) 
    yValues.drum2Y = yValues.drum1Y + DRUM_HEIGHT + DRUM_DISTANCE
    yValues.drum3Y = yValues.drum2Y + DRUM_HEIGHT + DRUM_DISTANCE
    yValues.vertConnectorY = yValues.drum3Y 
    yValues.horConnectorY = yValues.drum3Y + DRUM_HEIGHT + COMPONENT_DISTANCE
    yValues.plugboardY = yValues.horConnectorY + CONNECTOR_HEIGHT + CONN_TO_PB_DISTANCE
    yValues.keyboardY = yValues.plugboardY + PLUGBOARD_HEIGHT + PB_TO_KB_DISTANCE
    yValues.interEnigmaConnectionsY = (variant=="variantD" ? yValues.plugboardY + PLUGBOARD_HEIGHT : yValues.plugboardY)
    return yValues
}

function enigmaWidth(variant, first, last) {
    if (!variant.startsWith("scrambler")) {
        let leftSide = renderHorizontalConnector(variant, first, last, true) ? ENIGMA_HALF_WIDTH_WITH_HORIZONTAL_CONNECTOR : ENIGMA_HALF_WIDTH_WITH_VERTICAL_CONNECTOR
        let rightSide = renderHorizontalConnector(variant, first, last, false) ? ENIGMA_HALF_WIDTH_WITH_HORIZONTAL_CONNECTOR : ENIGMA_HALF_WIDTH_WITH_VERTICAL_CONNECTOR
        return leftSide + rightSide
    } else {
        return SCRAMBLER_WIDTH + 2*CONNECTOR_HEIGHT
    }
}

// the gap between this enigma and the next
// this is determined by this enigma's connector type on its right (outbound) side
function enigmaGap(variant, first, last) {
    return renderHorizontalConnector(variant, first, last, false) ? HORIZONTAL_CONNECTOR_GAP : VERTICAL_CONNECTOR_GAP
}

