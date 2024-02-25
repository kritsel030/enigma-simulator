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
let bombe = new Bombe(['SCII', 'SCIII', 'SCI'], "A-3-C-2-E-5-A", 'AE', 6) 
bombe.setIndicatorDrumPosition(1, "E")
bombe.setIndicatorDrumPosition(2, "B")
bombe.setIndicatorDrumPosition(3, "B")


let bombeRenderer = new BombeSVGRenderer(bombe)


//////////////////////////////////////////////////////////////////////////
// init

// parentId: id of the html element which will contain the SVG
function initSVG(parentId) {
    bombeRenderer.init(parentId)
}

function initFormFields() {
    document.getElementById(bombeRenderer.variant).checked = true
}

//////////////////////////////////////////////////////////////////////////
// UI input handlers

function handleVariant(event) {
    //console.log("handleVariant: " + event.target.value)
    bombeRenderer.variant = event.target.value
    bombeRenderer.redrawBombe("handleVariant")
}

function handleReset(event) {
    bombeRenderer.reset()
    bombeRenderer.redrawBombe("handleVariant")
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



