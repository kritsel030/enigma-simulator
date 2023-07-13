import enigma.Enigma
import enigma.components.*
import enigma.components.recorder.StepRecorder

/**
 * example from https://www.lysator.liu.se/~koma/turingbombe/TuringBombeTutorial.pdf
 *     var rotor1 = Rotor(RotorType.II, 'Y', 4)
var rotor2 = Rotor(RotorType.V, 'W', 11)
var rotor3 = Rotor(RotorType.III, 'Y', 24)
val reflector = Reflector(ReflectorType.B)
val plugboard2 = Plugboard("UF-ET-GQ-AD-VN-HM-ZP-LJ-IK-XO")

 input:  WETTERVORHERSAGE
 output: SNMKGGSTZZUGARLV
 */

fun main(args: Array<String>) {
//    var rotor1 = Rotor(RotorType.II, 'Y', 4)   // D = 4
//    var rotor2 = Rotor(RotorType.V, 'W', 11)   // K = 11
//    var rotor3 = Rotor(RotorType.III, 'Y', 24) // X = 24

//    var rotor1 = Rotor(RotorType.II, Char('Y'.code - 3), 4-3)
//    var rotor2 = Rotor(RotorType.V, Char('W'.code - 10), 11-10)
//    var rotor3 = Rotor(RotorType.III, Char('Y'.code - 23), 24-23)

    var rotor1 = Rotor(RotorType.II, 'V', 1)
    var rotor2 = Rotor(RotorType.V, 'M', 1)
    var rotor3 = Rotor(RotorType.III, 'B', 1)

    println("${rotor1.startPosition} ${rotor2.startPosition} ${rotor3.startPosition}")

    val reflector = Reflector(ReflectorType.B)
    val plugboard1 = Plugboard("")
    val plugboard2 = Plugboard("UF-ET-GQ-AD-VN-HM-ZP-LJ-IK-XO")
    val enigma = Enigma(reflector, listOf(rotor1, rotor2, rotor3), plugboard2)

    testEnigmaSimple("WETTERVORHERSAGE", enigma)
//    testEnigmaSimple('B', enigma)
//    testEnigmaSimple('H', enigma)
//    testSingleRotorSimple('B', rotor3)
//    testSingleRotorSimple('H', rotor3)

}

fun testSingleRotorSimple(input:Char, rotor:Rotor) {
    println("***********")

    var recorders = mutableListOf<StepRecorder>()
    val output = rotor.encryptRightToLeft(input, recorders)
    println("$input --> $output" )
    println(recorders.get(0).toStringSimple(true))
    println()

    val input2 = output
    recorders = mutableListOf<StepRecorder>()
    val output2 = rotor.encryptLeftToRight(input2, recorders)
    println("$input2 --> $output2" )
    println(recorders.get(0).toStringSimple(true))
    println()
}

fun testSingleRotorVerbose(input:Char, rotor:Rotor) {
    println("***********")
    var recorders = mutableListOf<StepRecorder>()
    val output = rotor.encryptRightToLeft(input, recorders)
    println("$input --> $output" )
    println(recorders.get(0).toStringVerbose(true))
    println()

    recorders = mutableListOf<StepRecorder>()
    val input2 = output
    val output2 = rotor.encryptLeftToRight(input2, recorders)
    println("$input2 --> $output2" )
    println(recorders.get(0).toStringVerbose(true))
}

fun testEnigmaSimple(input:Char, enigma:Enigma) {
    println("***********")
    var recorders = mutableListOf<StepRecorder>()
    var output = enigma.encrypt(input, true, recorders)
    println("$input --> $output" )
    recorders.forEach{r -> println(r.toStringSimple(true))}

    val input2 = output
    recorders = mutableListOf<StepRecorder>()
    val output2 = enigma.encrypt(input2, true, recorders)
    println("$input2 --> $output2" )
    recorders.forEach{r -> println(r.toStringSimple(true))}
}

fun testEnigmaSimple(input:String, enigma:Enigma) {
    println("***********")
    var output = enigma.encrypt(input)
    println("$input --> $output" )

    val input2 = output
    val output2 = enigma.encrypt(input2)
    println("$input2 --> $output2" )
}