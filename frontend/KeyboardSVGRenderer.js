class KeyboardSVGRenderer {

    constructor(alphabetSize = 26) {
        this.alphabetSize = alphabetSize
    }

    draw(parent, x, y, pressedKeyId=null, lightedKeyId=null) {
        // console.log(`KeyboardSVGRenderer.draw(x = ${x}, y= ${y}, pressedKeyId = ${pressedKeyId}, lightedKeyId = ${lightedKeyId})`)
        // remove if the keyboard already exists
        let previousGroup = document.getElementById("keyboard")
        if (previousGroup) {
//            console.log("remove existing keyboard")
            previousGroup.remove();
        }

//        console.log("draw new keyboard")
        let group = addGroupNode(parent, "keyboard", 0, 0)

        // p = display position index (each rotor has 26 vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor
        for (let p=0; p<alphabetSize; p++) {
            let xButton = x + KEY_RADIUS + (p%2) * KEY_SHIFT
            let yButton = y + LEADING_STRAIGHT - 2*UNIT + p * SINGLE + KEY_RADIUS + CONNECTOR_RADIUS

            let letter = idToCharToken(displayIndexToId(p,alphabetSize))

            //console.log("add keygroup for " + letter)
            // the 'keyGroup' class is used in the onClick event handler to determine which element
            // with this class was clicked
            let keyGroup = addGroupNode (group, `key_${letter}`, 0, 0)
            keyGroup.setAttribute("class", "keyGroup")

            // key is pressed
            if ( pressedKeyId != null && (p === idToDisplayIndex(pressedKeyId, alphabetSize)) ) {
                // draw a wider white background circle + even wider colored background circle
                addCircleNode (keyGroup, "pressedKeyBG2", "pressedKeyBG2", KEY_RADIUS+4, xButton, yButton)
                addCircleNode (keyGroup, "pressedKeyBG1", "pressedKeyBG1", KEY_RADIUS+2, xButton, yButton)
                addCircleNode (keyGroup, "pressedKey", "key", KEY_RADIUS, xButton, yButton)
                addTextNode (keyGroup, letter, `letter_${letter}`, "key", xButton, yButton)

            // key lights up
            } else if ( lightedKeyId != null && (p === idToDisplayIndex(lightedKeyId, alphabetSize)) ) {
                // this lightbulb lights up
                addCircleNode (keyGroup, "lightedKey", "lightedKey", KEY_RADIUS+4, xButton, yButton)
                addTextNode (keyGroup, letter, "lightedKey", "lightedKeyLetter", xButton, yButton)

            // normal key
            } else {
                addCircleNode (keyGroup, `key_${letter}`, "key", KEY_RADIUS, xButton, yButton)
                addTextNode (keyGroup, letter, `letter_${letter}`, "key", xButton, yButton)
            }
        }
    }
}