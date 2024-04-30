//////////////////////////////////////////////////////////////////////////
// constants

// generic stuff
let LEFT_MARGIN = 350
let TOP_MARGIN = 0
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

const DRUM_ENIGMA_HALF_WIDTH_WITH_HORIZONTAL_CONNECTOR = COMPONENT_SIZE + 0.5*COMPONENT_DISTANCE
const DRUM_ENIGMA_HALF_WIDTH_WITH_VERTICAL_CONNECTOR = CONNECTOR_HEIGHT + COMPONENT_DISTANCE + DRUM_RADIUS

// gap between two enigma's which have a horizontal connector on their joining sides
const HORIZONTAL_CONNECTOR_GAP = 6 * COMPONENT_DISTANCE
// gap between two enigma's which have a vertical connector on their joining sides
const VERTICAL_CONNECTOR_GAP = 9 * COMPONENT_DISTANCE

// scrambler stuff
const SCHEMA_ENIGMA_WIDTH = 4 * COMPONENT_DISTANCE

// diagonal board stuff
const DIAGONAL_BOARD_HEIGHT = 7*WIRE_DISTANCE

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

function renderHorConnectorInputOutput(variant, first, last, inbound) {
    return renderHorizontalConnector(variant, first, last, inbound)
}

function renderVertConnectorInputOutput(variant, first, last, inbound) {
    return  renderVerticalConnector(variant, first, last, inbound) && 
    (!bombeEntryOrExit(first, last, inbound) || (last && !inbound && !["scrambler_multi_line_scanning", "scrambler_diagonal_board"].includes(variant)))
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
        else if ( ["variantA", "variantB"].includes(variant)) props.variant="clickableKeyboard"
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

function renderOutputLetterCableWires (variant, lastInCycle) {
    return (!lastInCycle && ["variantD", "variantE", "variantF"].includes(variant)) ||
        ( ["variantG", "variantH"].includes(variant)) ||
        variant.startsWith("scrambler")
}

function renderOutputToInputWires(variant) {
    return ["scrambler_multi_line_scanning", "scrambler_full_menu", "scrambler_diagonal_board"].includes(variant)
}

function renderDiagonalBoard (variant) {
    return ["scrambler_diagonal_board"].includes(variant)
}

function bombeEntryOrExit(first, last, inbound) {
    return (first && inbound) || (last && !inbound)
}

//////////////////////////////////////////////////////////////////////////
// helper methods to indicate where components should be rendered in which scenario

function preFirstScramblerWidth (variant, alphabetSize) {
    let whitespace = 10
    if (renderHorizontalConnector(variant, true, false, true)) {
        return whitespace
    } else {
        let preScramblerWidth = whitespace + alphabetSize*WIRE_DISTANCE + 0.5*(alphabetSize-1)*WIRE_DISTANCE + WIRE_DISTANCE
        if (variant == "scrambler_diagonal_board") {
            preScramblerWidth + (alphabetSize-1)*WIRE_DISTANCE
        }
        return preScramblerWidth
    }
}

function scramblerAbsoluteXOffset(variant, scramblerId, menuLetters) {
    let absoluteXOffset = LEFT_MARGIN
    for (let i=0; i < scramblerId; i++) {
        absoluteXOffset += scramblerWidth(variant, i, menuLetters)
        absoluteXOffset += scramblerGap(variant, i, menuLetters)
    }
    return absoluteXOffset
}

function scramblerWidth(variant, scramblerId, menuLetters) {
    let first = scramblerId == 0
    let last = scramblerId == numberOfScramblersToDisplay(variant, menuLetters)-1
    return enigmaWidth(variant, first, last) 
}

function scramblerGap(variant, scramblerId, menuLetters) {
    let first = scramblerId == 0
    let last = scramblerId == numberOfScramblersToDisplay(variant, menuLetters)-1
    return enigmaGap(variant, first, last)
}

function postScramblerWidth(variant, scramblerId, menuLetters) {
    let first = scramblerId == 0
    let last = scramblerId == numberOfScramblersToDisplay(variant, menuLetters)-1
    if (renderHorizontalConnector(variant, first, last, false)) {
        if (!last) {
            return HORIZONTAL_CONNECTOR_GAP
        } else {
            return 0
        }
    } else if (renderVerticalConnector(variant, first, last, false)) {
        
    }
}

function numberOfScramblersToDisplay(variant, menuLetters) {
    let result = 0
    if (["scrambler_diagonal_board", "scrambler_full_menu"].includes(variant)) {
        result = menuLetters.length - 1
    } else {
        result = menuLetters.indexOf(menuLetters[0], 1)
    }
    return result
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
        if (renderHorizontalConnector(variant, first, last, inbound) )
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
        result = CONNECTOR_HEIGHT + SCHEMA_ENIGMA_WIDTH
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
        return 20+ 2*COMPONENT_SIZE + COMPONENT_DISTANCE + 0.5*ENIGMA_DISTANCE - 0.5*COMPONENT_SIZE
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
        return DRUM_ENIGMA_HALF_WIDTH_WITH_HORIZONTAL_CONNECTOR
    } else if (renderDrums(variant)){
        // vertical connector + space + 1/2 DRUM + 1/2 distance between components
        return DRUM_ENIGMA_HALF_WIDTH_WITH_VERTICAL_CONNECTOR
    } else {
        // schematic scramblers instead of drums (2 vertical connectors + 1 scrambler make a full scrambler)
        return 0.5 * (SCHEMA_ENIGMA_WIDTH + 2*CONNECTOR_HEIGHT)
    }
}

// coordinate x-value for a diagonal board wire
function dbX(cableChar, wireId, menuLetters) {
    if (cableChar == menuLetters[0]) {
        // input of first enigma
        return LEFT_MARGIN - WIRE_DISTANCE - (ALPHABET_SIZE - wireId)*WIRE_DISTANCE - 4*WIRE_DISTANCE
    } else if (menuLetters.includes(cableChar)) {
        let scramblerIndex = menuLetters.indexOf(cableChar) - 1
        return LEFT_MARGIN + enigmaWidth("scrambler_diagonal_board") + scramblerIndex * (enigmaWidth("scrambler_diagonal_board")+VERTICAL_CONNECTOR_GAP) + 2*WIRE_DISTANCE + wireId*WIRE_DISTANCE
    }
    return "error"
}

// coordinate y-value for the top end point of a diagonal board wire
function dbY(wireId) {
    let ys = yValues("scrambler_diagonal_board")
    return ys.diagonalBoard + WIRE_DISTANCE + wireId*COMPONENT_DISTANCE
}

function wireY(wireId) {
    let ys = yValues("scrambler_diagonal_board")
    return TOP_MARGIN + ys.vertConnectorY + 0.5*WIRE_DISTANCE + wireId*WIRE_DISTANCE
}

function yValues(variant) {
    let yValues = {}
    yValues.reflectorY = 0
//    yValues.drum1Y = (renderReflector(variant) ? yValues.reflectorY + REFLECTOR_HEIGHT + COMPONENT_DISTANCE : 0)
    yValues.drum1Y = yValues.reflectorY + REFLECTOR_HEIGHT + COMPONENT_DISTANCE
    yValues.drum2Y = yValues.drum1Y + DRUM_HEIGHT + DRUM_DISTANCE
    yValues.drum3Y = yValues.drum2Y + DRUM_HEIGHT + DRUM_DISTANCE
    yValues.vertConnectorY = yValues.drum3Y 
    yValues.horConnectorY = yValues.drum3Y + DRUM_HEIGHT + COMPONENT_DISTANCE
    yValues.plugboardY = yValues.horConnectorY + CONNECTOR_HEIGHT + CONN_TO_PB_DISTANCE
    yValues.keyboardY = yValues.plugboardY + PLUGBOARD_HEIGHT + PB_TO_KB_DISTANCE
    yValues.interEnigmaConnectionsY = (variant=="variantD" ? yValues.plugboardY + PLUGBOARD_HEIGHT : yValues.plugboardY)
    yValues.diagonalBoard = 60
    return yValues
}

function enigmaWidth(variant, first=false, last=false) {
    if (!variant.startsWith("scrambler")) {
        let leftSide = renderHorizontalConnector(variant, first, last, true) ? DRUM_ENIGMA_HALF_WIDTH_WITH_HORIZONTAL_CONNECTOR : DRUM_ENIGMA_HALF_WIDTH_WITH_VERTICAL_CONNECTOR
        let rightSide = renderHorizontalConnector(variant, first, last, false) ? DRUM_ENIGMA_HALF_WIDTH_WITH_HORIZONTAL_CONNECTOR : DRUM_ENIGMA_HALF_WIDTH_WITH_VERTICAL_CONNECTOR
        return leftSide + rightSide
    } else {
        return SCHEMA_ENIGMA_WIDTH + 2*CONNECTOR_HEIGHT
    }
}

// the gap between this enigma and the next
// this is determined by this enigma's connector type on its right (outbound) side
function enigmaGap(variant, first, last) {
    return renderHorizontalConnector(variant, first, last, false) ? HORIZONTAL_CONNECTOR_GAP : VERTICAL_CONNECTOR_GAP
}

