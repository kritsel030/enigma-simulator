import enigma.Enigma
import enigma.components.*
import enigma.components.recorder.StepRecorder
import enigma.components.recorder.RotorStepRecorder

fun main(args: Array<String>) {
    var rotor1 = Rotor(RotorType.III, 'A', 1)
    var rotor2 = Rotor(RotorType.II, 'A', 1)
    var rotor3 = Rotor(RotorType.I, 'A', 1)
    val reflector = Reflector(ReflectorType.B)
    val enigma = Enigma(reflector, rotor1, rotor2, rotor3)

    testEnigmaSimple("ABCDEFGHIJKLMNOPQRSTUVWXYZ", enigma)
//    testEnigmaSimple('B', enigma)
//    testEnigmaSimple('H', enigma)
//    testSingleRotorSimple('B', rotor3)
//    testSingleRotorSimple('H', rotor3)

}

fun testSingleRotorSimple(input:Char, rotor:Rotor) {
    println("***********")

    var recorders = mutableListOf<StepRecorder>()
    val output = rotor.encryptContactRightToLeft(input, recorders)
    println("$input --> $output" )
    println(recorders.get(0).toStringSimple(true))
    println()

    val input2 = output
    recorders = mutableListOf<StepRecorder>()
    val output2 = rotor.encryptContactLeftToRight(input2, recorders)
    println("$input2 --> $output2" )
    println(recorders.get(0).toStringSimple(true))
    println()
}

fun testSingleRotorVerbose(input:Char, rotor:Rotor) {
    println("***********")
    var recorders = mutableListOf<StepRecorder>()
    val output = rotor.encryptContactRightToLeft(input, recorders)
    println("$input --> $output" )
    println(recorders.get(0).toStringVerbose(true))
    println()

    recorders = mutableListOf<StepRecorder>()
    val input2 = output
    val output2 = rotor.encryptContactLeftToRight(input2, recorders)
    println("$input2 --> $output2" )
    println(recorders.get(0).toStringVerbose(true))
}

fun testEnigmaSimple(input:Char, enigma:Enigma) {
    println("***********")
    var recorders = mutableListOf<StepRecorder>()
    var output = enigma.encrypt(input, recorders)
    println("$input --> $output" )
    recorders.forEach{r -> println(r.toStringSimple(true))}

    val input2 = output
    recorders = mutableListOf<StepRecorder>()
    val output2 = enigma.encrypt(input2, recorders)
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