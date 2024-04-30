//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
// variables

// alphabet size
//var alphabetSize = 26
var alphabetSize = 6


// 26 letter enigma
//let _reflector = new Reflector(reflectorParam ?? 'B', alphabetSize)
//let _rotor1 = new Rotor(rotor1TypeParam ?? 'I', rotor1PositionParam ?? 'A', rotor1RingSettingParam ?? 6, alphabetSize)
//let _rotor2 = new Rotor(rotor2TypeParam ?? 'II', rotor2PositionParam ?? 'D', rotor2RingSettingParam ?? 5, alphabetSize)
//let _rotor3 = new Rotor(rotor3TypeParam ?? 'III', rotor3PositionParam ?? 'U', rotor3RingSettingParam ?? 3, alphabetSize)
//let _plugboard = new Plugboard(plugboardParam ?? 'AC-DK-GI-JB-OE-XZ', alphabetSize)

// 6 letter enigma

let bombe = new Bombe(['D-II', 'D-III', 'D-I'], "A-2-C-1-E-4-A-3-F", 'AE', 6)
bombe.setIndicatorDrumPosition(1, "E")
bombe.setIndicatorDrumPosition(2, "B")
bombe.setIndicatorDrumPosition(3, "B")


let bombeRenderer = new BombeSVGRenderer(bombe)

let variants = [
    "variantA",
    "variantB",
    "variantC",
    "variantD",
    "variantE",
    "variantF",
    "variantG",
    "variantH",
    "scramblerBasic",
    "scrambler_multi_line_scanning",
    "scrambler_full_menu",
    "scrambler_diagonal_board"]


//////////////////////////////////////////////////////////////////////////
// init

// parentId: id of the html element which will contain the SVG
function initSVG(parentId) {
    bombeRenderer.init(parentId)
}

function initFormFields() {
    document.getElementById('variant').value = bombeRenderer.variant
}

//////////////////////////////////////////////////////////////////////////
// UI input handlers

function handleFirstVariant(event) {
    console.log("handleFirstVariant")
    let newVariant = variants[0]
    bombeRenderer.variant = newVariant
    document.getElementById("variant").value = newVariant
    bombeRenderer.redrawBombe("handleFirstVariant")
}

function handlePreviousVariant(event) {
    console.log("handlePreviousVariant")
    let currentVariantIndex = variants.indexOf(bombeRenderer.variant)
    if (currentVariantIndex != 0) {
        let newVariant = variants[currentVariantIndex-1]
        bombeRenderer.variant = newVariant
        document.getElementById("variant").value = newVariant
    }
    bombeRenderer.redrawBombe("handlePreviousVariant")
}

function handleVariant(event) {
    //console.log("handleVariant: " + event.target.value)
    console.log(event.target.value)
    bombeRenderer.variant = event.target.value
    bombeRenderer.redrawBombe("handleVariant")
}

function handleNextVariant(event) {
    console.log("handleNextVariant")
    let currentVariantIndex = variants.indexOf(bombeRenderer.variant)
    if (currentVariantIndex < variants.length-1) {
        let newVariant = variants[currentVariantIndex+1]
        bombeRenderer.variant = newVariant
        document.getElementById("variant").value = newVariant
    }
    bombeRenderer.redrawBombe("handleNextVariant")
}

function handleLastVariant(event) {
    console.log("handleLastVariant")
    let newVariant = variants[variants.length-1]
    bombeRenderer.variant = newVariant
    document.getElementById("variant").value = newVariant
    bombeRenderer.redrawBombe("handleLastVariant")
}

// ***************************************************************************************
// electrical path controls
function handleClearPath(event) {
    bombeRenderer.reset()
    bombeRenderer.redrawBombe("handleClearPath")
}

function handleNextPathSegment(event) {
    bombeRenderer.bombe.pathFinder.processNextPathRequest(bombeRenderer.variant)
    bombeRenderer.redrawBombe("next path segment")
}

function handleCompletePath(event) {
//    let pathAdded = true
//    while (pathAdded) {
//        pathAdded = bombeRenderer.bombe.pathFinder.processNextPathRequest(bombeRenderer.variant)
//    }
    bombeRenderer.bombe.pathFinder.completePath(bombeRenderer.variant)
    bombeRenderer.redrawBombe("complete the path")
}

// auto-complete the electrical path when the user selects an input wire
function handleAutoCompletePath(event) {
    bombeRenderer.autoCompletePathOnInputActivation = event.target.checked
}

// auto-activate the d-wire when the user changes the start position of a drum
function handleAutoActivateInput(event) {
    bombeRenderer.autoActivateInputOnRotorPositionChange = event.target.checked
}

//////////////////////////////////////////////////////////////////////////
// helper functions


function displayIndexToId (displayIndex, alphabetSize=26) {
    return (alphabetSize - displayIndex + A_POSITION) % alphabetSize
}

function displayIndexToId2 (displayIndex, rotor) {
    return (alphabetSize - displayIndex + A_POSITION + rotor.getNormalizedPosition() - rotor.getNormalizedRingSetting() + rotor.alphabetSize) % rotor.alphabetSize
}

function idToDisplayIndex (connectionId, alphabetSize=26) {
    return (connectionId + alphabetSize) % alphabetSize
}

function idToDisplayIndex2 (connectionId, rotor) {
    return (A_POSITION - connectionId + rotor.alphabetSize + rotor.getNormalizedPosition() - rotor.getNormalizedRingSetting() + rotor.alphabetSize) % rotor.alphabetSize
}

function charToId(char) {
    return char.charCodeAt(0) - 'A'.charCodeAt(0)
}

function idToCharToken(connectionId) {
    return String.fromCharCode(connectionId + 'A'.charCodeAt(0))
}

function idToNumberToken(connectionId) {
    return (connectionId+1).toLocaleString('nl-NL', {minimumIntegerDigits: 2})
}

function idToDisplayIndex (connectionId, alphabetSize=26) {
    return (connectionId + alphabetSize) % alphabetSize
}

function normalize (id, alphabetSize=26) {
    return (id + alphabetSize) % alphabetSize
}



