class LWKeyAndLightboardSVGRenderer {

    constructor(alphabetSize = 26) {
        this.alphabetSize = alphabetSize
    }

    //  keyboarType LoV:
    //  - clickableKeyboard
    //  - keyboard
    //  - integrated
    //  - lightboard
    draw(parent, groupId, variant, keyboardType, x, y, pressedKeyId=null, lightedKeyId=null) {
        // console.log(`LWKeyAndLightboardSVGRenderer.draw(variant=${variant}, keyboardType=${keyboardType}, x=${x}, y=${y}, pressedKeyId=${pressedKeyId}, lightedKeyId=${lightedKeyId})`)
        // remove if the keyboard already exists
        let previousGroup = document.getElementById(groupId)
        if (previousGroup) {
//            console.log("remove existing keyboard")
            previousGroup.remove();
        }
//        console.log("draw new keyboard")
        let group = addGroupNode(parent, groupId, x+0.5*WIRE_DISTANCE, y+KEY_RADIUS)

        // p = display position index (each rotor has 26 vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor
        for (let p=0; p<alphabetSize; p++) {
            // let xButton = p * (KEY_DISTANCE + KEY_RADIUS)
            let xButton = p * WIRE_DISTANCE
            let yButton = (p%2) * KEY_SHIFT
            
            let letter = idToCharToken(p)
            let showAsPressed = ( (["clickableKeyboard", "keyboard"].includes(keyboardType) && pressedKeyId != null && (p === idToDisplayIndex(pressedKeyId, alphabetSize))) ||
                    ("integrated" == keyboardType && lightedKeyId != null && (p === idToDisplayIndex(lightedKeyId, alphabetSize))) )
            let showAsLighted = ( ("lightboard" == keyboardType && lightedKeyId != null && (p === idToDisplayIndex(lightedKeyId, alphabetSize))) ||
                    ("integrated" == keyboardType && lightedKeyId != null && (p === idToDisplayIndex(lightedKeyId, alphabetSize))) )


            //console.log("add keygroup for " + letter)
            // the 'keyGroup' class is used in the onClick event handler to determine which element
            // with this class was clicked
            let keyGroup = addGroupNode (group, `${group.id}_key_${letter}`, 0, 0)
            keyGroup.setAttribute("class", "keyGroup")

            // key is pressed
            if ( showAsPressed && showAsLighted) {
                //console.log("draw pressed + lighted key " + letter)
                // this lightbulb lights up
                addCircleNode (keyGroup, `${group.id}_${letter}_pressedKeyBG2`, "pressedKeyBG2", KEY_RADIUS+4, xButton, yButton)
                addCircleNode (keyGroup, `${group.id}_lightedKey`, "lightedKey", KEY_RADIUS+2, xButton, yButton)
                addTextNode (keyGroup, letter,`${group.id}_lightedKeyLetter`, "lightedKeyLetter", xButton, yButton)
            } else if ( showAsPressed ) {
                // console.log("draw pressed key " + letter)
                // draw a wider white background circle + even wider colored background circle
                addCircleNode (keyGroup, `${group.id}_${letter}_pressedKeyBG2`, "pressedKeyBG2", KEY_RADIUS+4, xButton, yButton)
                addCircleNode (keyGroup, `${group.id}_${letter}_pressedKeyBG1`, "pressedKeyBG1", KEY_RADIUS+2, xButton, yButton)
                addCircleNode (keyGroup, `${group.id}_${letter}_pressedKey`, "key", KEY_RADIUS, xButton, yButton)
                addTextNode (keyGroup, letter, `${group.id}_letter_${letter}`, "key", xButton, yButton)

            // key lights up
            } else if ( showAsLighted ) {
                // console.log("draw lighted key " + letter)
                // this lightbulb lights up
                addCircleNode (keyGroup, `${group.id}_lightedKey`, "lightedKey", KEY_RADIUS+4, xButton, yButton)
                addTextNode (keyGroup, letter,`${group.id}_lightedKeyLetter`, "lightedKeyLetter", xButton, yButton)

            // normal key
            } else {
                let clazz = keyboardType == "lightboard" ? "lightbulb" : "key"
                addCircleNode (keyGroup, `${group.id}_key_${letter}`, clazz, KEY_RADIUS, xButton, yButton)
                addTextNode (keyGroup, letter, `${group.id}_letter_${letter}`, clazz, xButton, yButton)
            }
        }
    }
}