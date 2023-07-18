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

//    var rotor1 = Rotor(RotorType.II, 'V', 'A')
//    var rotor2 = Rotor(RotorType.V, 'M', 'A')
//    var rotor3 = Rotor(RotorType.III, 'B', 'A')
//
//    println("${rotor1.startRingPosition} ${rotor2.startRingPosition} ${rotor3.startRingPosition}")
//
//    val reflector = Reflector(ReflectorType.B)
//    val plugboard1 = Plugboard("")
//    val plugboard2 = Plugboard("UF-ET-GQ-AD-VN-HM-ZP-LJ-IK-XO")
//    val enigma = Enigma(reflector, listOf(rotor1, rotor2, rotor3), plugboard2)
//
//    testEnigmaSimple("WETTERVORHERSAGE", enigma)
}

//fun testEnigmaSimple(input:String, enigma:Enigma) {
//    println("***********")
//    var output = enigma.encrypt(input)
//    println("$input --> $output" )
//
//    val input2 = output
//    val output2 = enigma.encrypt(input2)
//    println("$input2 --> $output2" )
//}