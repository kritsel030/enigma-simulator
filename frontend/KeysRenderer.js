class KeysRenderer {

    constructor(alphabetSize = 26) {
        this.alphabetSize = alphabetSize
    }

    buttonCenters = [];

    draw(ctx, xOffset, yOffset, pressedKeyId=null, lightedKeyId=null) {
        //console.log("keysrenderer.draw; pressedKeyId=" + pressedKeyId + "; lightedKeyIde: " + lightedKeyId)
        this.buttonCenters = []
        // p = display position index (each rotor has 26 vertical positions)
        // 0 = position at the top of the rotor
        // 25 = position at the bottom of the rotor
        for (let p=0; p<alphabetSize; p++) {

            let layers = 6
            if ( (pressedKeyId && (p === idToDisplayIndex(pressedKeyId, alphabetSize))) || (lightedKeyId && (p === idToDisplayIndex(lightedKeyId, alphabetSize))) ) {
                layers = 1
            }
            for (let l = 0; l < layers; l++) {
                if (l < layers-2) {
                    // black layers to represent button height
                    ctx.fillStyle = 'rgb(0, 0, 0)';
                } else if (l < layers-1) {
                    // light gray layer to represent button border
                    ctx.fillStyle = 'rgb(232,232,232)';
                } else {
                    // dark grey layer to represent button top
                    ctx.fillStyle = 'rgb(80,80,80)';
                    if ( pressedKeyId && (p === idToDisplayIndex(pressedKeyId, alphabetSize)) ) {
                        // or black for a pressed key
                        ctx.fillStyle = 'rgb(0,0,0)'
                    } else if ( lightedKeyId && (p === idToDisplayIndex(lightedKeyId, alphabetSize)) ) {
                        // or gold/orange/yellow for a lighted key
                        ctx.fillStyle = 'rgb(255,215,0)'
                    }
                }
                let xButton = xOffset + KEY_RADIUS + (p%2) * KEY_SHIFT + l
                let yButton = yOffset + LEADING_HEIGHT - 8 + p * SINGLE_HEIGHT + KEY_RADIUS - l
                if (l === layers-1) {
                    // top layer, let's store these coordinates
                    this.buttonCenters[p] = [xButton, yButton]
                }
                ctx.beginPath();
                ctx.arc(xButton, yButton, KEY_RADIUS, 0, Math.PI * 2, true);
                ctx.fill()
            }

            // write letter
            ctx.fillStyle = 'rgb(255, 255, 255)';
            ctx.font = 'bold 12px arial'
            ctx.fillText(
                idToCharToken(displayIndexToId(p,alphabetSize)),
                xOffset + KEY_RADIUS - 4 + (p%2)*KEY_SHIFT + (layers-1),
                yOffset + LEADING_HEIGHT + 6 + p*SINGLE_HEIGHT - (layers-1))
        }
    }
}